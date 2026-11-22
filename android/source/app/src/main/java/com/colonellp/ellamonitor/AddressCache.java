package com.colonellp.ellamonitor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongSupplier;

/** Share one lookup per network and host. An old failure can only evict its own answer. */
final class AddressCache {
    private static final long CACHE_MS = 15_000;
    private final LongSupplier clock;
    private final Map<String, Answer> answers = new HashMap<>();
    private final Map<String, CompletableFuture<Answer>> pending = new HashMap<>();
    private int generation;

    interface Resolver { Candidates resolve() throws UnknownHostException; }

    static final class Candidates {
        final List<InetAddress> addresses;
        final String source;

        Candidates(List<InetAddress> addresses, String source) {
            List<InetAddress> sorted = new ArrayList<>(new LinkedHashSet<>(addresses));
            sorted.sort(Comparator.comparingInt(a -> a instanceof Inet4Address ? 0 : 1));
            this.addresses = Collections.unmodifiableList(sorted);
            this.source = source;
        }
    }

    private static final class Answer {
        final Candidates candidates;
        final long savedAt;

        Answer(Candidates candidates, long savedAt) {
            this.candidates = candidates;
            this.savedAt = savedAt;
        }
    }

    final class Lookup {
        private final String key;
        private final Answer answer;
        final List<InetAddress> addresses;
        final String source, use;
        final long ageMillis;

        private Lookup(String key, Answer answer, String use) {
            this.key = key;
            this.answer = answer;
            addresses = answer.candidates.addresses;
            source = answer.candidates.source;
            this.use = use;
            ageMillis = Math.max(0, clock.getAsLong() - answer.savedAt);
        }

        boolean discard() {
            synchronized (AddressCache.this) { return answers.remove(key, answer); }
        }
    }

    AddressCache(LongSupplier clock) { this.clock = clock; }

    synchronized int generation() { return generation; }

    synchronized void reset() {
        generation++;
        answers.clear();
        for (CompletableFuture<Answer> future : pending.values()) {
            future.completeExceptionally(new UnknownHostException("Network changed."));
        }
        pending.clear();
    }

    Lookup lookup(int epoch, String key, Resolver resolver) throws UnknownHostException {
        CompletableFuture<Answer> future;
        boolean owner;
        synchronized (this) {
            requireGeneration(epoch);
            Answer cached = answers.get(key);
            if (cached != null && clock.getAsLong() - cached.savedAt < CACHE_MS) {
                return new Lookup(key, cached, "cached");
            }
            future = pending.get(key);
            owner = future == null;
            if (owner) {
                future = new CompletableFuture<>();
                pending.put(key, future);
            }
        }
        if (owner) resolve(epoch, key, resolver, future);
        try {
            Answer answer = future.get(4, TimeUnit.SECONDS);
            synchronized (this) {
                requireGeneration(epoch);
                return new Lookup(key, answer, owner ? "fresh" : "shared");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnknownHostException("Address lookup interrupted.");
        } catch (TimeoutException e) {
            throw new UnknownHostException("Shared address lookup timed out.");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownHostException) throw (UnknownHostException) e.getCause();
            throw (UnknownHostException) new UnknownHostException("Address lookup failed.").initCause(e.getCause());
        }
    }

    private void resolve(int epoch, String key, Resolver resolver, CompletableFuture<Answer> future) {
        try {
            Candidates candidates = resolver.resolve();
            if (candidates.addresses.isEmpty()) throw new UnknownHostException("No Pi address found.");
            synchronized (this) {
                requireGeneration(epoch);
                Answer answer = new Answer(candidates, clock.getAsLong());
                answers.put(key, answer);
                future.complete(answer);
            }
        } catch (UnknownHostException | RuntimeException e) {
            future.completeExceptionally(e);
        } finally {
            synchronized (this) { pending.remove(key, future); }
        }
    }

    private void requireGeneration(int epoch) throws UnknownHostException {
        if (epoch != generation) throw new UnknownHostException("Network changed.");
    }
}
