package com.colonellp.ellamonitor;

import org.junit.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

public class AddressCacheTest {
    @Test public void collectsSeparateRepliesInEitherOrderWithBoundedWaits() throws Exception {
        InetAddress ipv4 = InetAddress.getByName("192.0.2.5");
        InetAddress ipv6 = InetAddress.getByName("2001:db8::5");
        MdnsPacket.Replies ipv6First = new MdnsPacket.Replies(0);
        ipv6First.add(ipv6, 5);
        assertTrue(ipv6First.waiting(1_800));
        ipv6First.add(ipv4, 1_800);
        assertFalse(ipv6First.waiting(1_800));
        assertEquals(Arrays.asList(ipv4, ipv6),
            new AddressCache.Candidates(ipv6First.addresses(), "mdns").addresses);

        MdnsPacket.Replies ipv4First = new MdnsPacket.Replies(0);
        ipv4First.add(ipv4, 20);
        assertTrue(ipv4First.waiting(190));
        ipv4First.add(ipv6, 190);
        assertFalse(ipv4First.waiting(190));

        MdnsPacket.Replies ipv6Only = new MdnsPacket.Replies(20);
        ipv6Only.add(ipv6, 30);
        assertTrue(ipv6Only.waiting(2_019));
        assertFalse(ipv6Only.waiting(2_020));
        assertEquals(Collections.singletonList(ipv6), ipv6Only.addresses());

        MdnsPacket.Replies ipv4Only = new MdnsPacket.Replies(20);
        ipv4Only.add(ipv4, 30);
        assertFalse(ipv4Only.waiting(230));
        assertEquals(Collections.singletonList(ipv4), ipv4Only.addresses());
        assertFalse(new MdnsPacket.Replies(0).waiting(2_000));
    }

    @Test public void expiresFromCompletionAndOldFailuresCannotEvictANewAnswer() throws Exception {
        AtomicLong clock = new AtomicLong();
        InetAddress address = InetAddress.getByName("192.0.2.5");
        AddressCache cache = new AddressCache(clock::get);
        AddressCache.Resolver resolver = () -> {
            clock.addAndGet(2_000);
            return new AddressCache.Candidates(Collections.singletonList(address), "mdns");
        };
        AddressCache.Lookup first = cache.lookup(0, "pi.local", resolver);
        clock.set(16_999);
        assertEquals(14_999, cache.lookup(0, "pi.local", resolver).ageMillis);
        clock.set(17_000);
        assertEquals("fresh", cache.lookup(0, "pi.local", resolver).use);
        assertFalse(first.discard());
        assertEquals("cached", cache.lookup(0, "pi.local", resolver).use);
    }

    @Test public void concurrentClientsShareAnAnswerAndNetworkResetCancelsTheOldGeneration() throws Exception {
        AddressCache cache = new AddressCache(() -> 1_000);
        InetAddress address = InetAddress.getByName("192.0.2.5");
        AddressCache.Candidates result = new AddressCache.Candidates(Collections.singletonList(address), "mdns");
        ExecutorService workers = Executors.newFixedThreadPool(4);
        CountDownLatch lookupStarted = new CountDownLatch(1), waitForReplies = new CountDownLatch(1);
        AtomicInteger queries = new AtomicInteger();
        try {
            Future<AddressCache.Lookup> live = workers.submit(() -> cache.lookup(0, "pi.local", () -> {
                queries.incrementAndGet();
                lookupStarted.countDown();
                try { if (!waitForReplies.await(3, TimeUnit.SECONDS)) throw new UnknownHostException("fixture timed out"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new UnknownHostException(); }
                return result;
            }));
            assertTrue(lookupStarted.await(2, TimeUnit.SECONDS));
            java.util.List<Future<AddressCache.Lookup>> others = new java.util.ArrayList<>();
            for (String operation : Arrays.asList("catalogue", "status", "history")) {
                others.add(workers.submit(() -> cache.lookup(0, "pi.local", () -> {
                    fail("Unexpected duplicate " + operation + " lookup");
                    return result;
                })));
            }
            waitForReplies.countDown();
            assertEquals(result.addresses, live.get(2, TimeUnit.SECONDS).addresses);
            for (Future<AddressCache.Lookup> client : others) {
                assertEquals(result.addresses, client.get(2, TimeUnit.SECONDS).addresses);
            }
            assertEquals(1, queries.get());
            cache.reset();
            try { cache.lookup(0, "pi.local", () -> result); fail("Old network reused"); }
            catch (UnknownHostException expected) {}
            assertEquals("fresh", cache.lookup(cache.generation(), "pi.local", () -> result).use);
        } finally {
            waitForReplies.countDown();
            workers.shutdownNow();
        }
    }

    @Test public void resetDuringLookupCannotOverwriteTheReplacementNetwork() throws Exception {
        AddressCache cache = new AddressCache(() -> 1_000);
        InetAddress oldIp = InetAddress.getByName("192.0.2.5");
        InetAddress newIp = InetAddress.getByName("192.0.2.6");
        AddressCache.Candidates current = new AddressCache.Candidates(Collections.singletonList(newIp), "mdns");
        ExecutorService worker = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        try {
            Future<AddressCache.Lookup> old = worker.submit(() -> cache.lookup(0, "pi.local", () -> {
                started.countDown();
                try {
                    if (!release.await(3, TimeUnit.SECONDS)) throw new UnknownHostException("Fixture timed out");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new UnknownHostException();
                }
                return new AddressCache.Candidates(Collections.singletonList(oldIp), "mdns");
            }));
            assertTrue(started.await(2, TimeUnit.SECONDS));
            cache.reset();
            int generation = cache.generation();
            assertEquals(current.addresses, cache.lookup(generation, "pi.local", () -> current).addresses);
            release.countDown();
            try { old.get(2, TimeUnit.SECONDS); fail("Old lookup succeeded after the network reset"); }
            catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof UnknownHostException); }
            assertEquals(current.addresses, cache.lookup(generation, "pi.local", () -> current).addresses);
        } finally {
            release.countDown();
            worker.shutdownNow();
        }
    }
}
