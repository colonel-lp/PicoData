package com.colonellp.ellamonitor;

import android.content.Context;
import android.net.*;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import java.io.IOException;
import java.net.*;
import java.util.*;

/** Lookup/cache/packets belong to one foreground network generation. */
final class LocalNameResolver {
    private final Context context;
    private final AddressCache cache = new AddressCache(SystemClock::elapsedRealtime);
    private final Set<DatagramSocket> sockets = new HashSet<>();

    LocalNameResolver(Context context) { this.context = context.getApplicationContext(); }

    int generation() { return cache.generation(); }

    synchronized void reset() {
        cache.reset();
        for (DatagramSocket socket : sockets) socket.close();
        sockets.clear();
    }

    List<InetAddress> lookup(Network network, String host) throws UnknownHostException {
        return lookup(network, host, generation()).addresses;
    }

    AddressCache.Lookup lookup(Network network, String host, int epoch) throws UnknownHostException {
        String name = MdnsPacket.name(host);
        return cache.lookup(epoch, network.getNetworkHandle() + "/" + name,
            () -> resolve(network, host, name.endsWith(".local"), epoch));
    }

    private AddressCache.Candidates resolve(Network network, String host, boolean local, int epoch)
            throws UnknownHostException {
        List<InetAddress> nativeAddresses = Collections.emptyList();
        if (!local || Build.VERSION.SDK_INT >= 31) {
            try { nativeAddresses = Arrays.asList(network.getAllByName(host)); }
            catch (UnknownHostException ignored) {}
        }
        if (!local || nativeAddresses.stream().anyMatch(Inet4Address.class::isInstance)) {
            return new AddressCache.Candidates(nativeAddresses, "system");
        }

        // A native AAAA-only result can also be incomplete. Look for a separate A answer.
        try {
            List<InetAddress> replies = multicast(network, host, epoch);
            if (!replies.isEmpty()) {
                replies.addAll(nativeAddresses);
                return new AddressCache.Candidates(replies, nativeAddresses.isEmpty() ? "mdns" : "system-mdns");
            }
        } catch (IOException | SecurityException ignored) {}
        return new AddressCache.Candidates(nativeAddresses, nativeAddresses.isEmpty() ? "mdns" : "system");
    }

    private List<InetAddress> multicast(Network network,String host,int epoch)throws IOException{
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        LinkProperties link=cm==null?null:cm.getLinkProperties(network);
        if(link==null||link.getInterfaceName()==null)throw new IOException("Missing Wi-Fi route.");
        NetworkInterface iface=NetworkInterface.getByName(link.getInterfaceName());if(iface==null)throw new IOException("Missing interface.");
        WifiManager wm=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock=wm==null?null:wm.createMulticastLock("Ella Pi lookup");
        MulticastSocket socket=new MulticastSocket(null);
        try{
            synchronized(this){if(epoch!=generation())throw new IOException("Network changed.");sockets.add(socket);}
            network.bindSocket(socket);socket.bind(new InetSocketAddress(0));socket.setNetworkInterface(iface);socket.setTimeToLive(255);
            if(lock!=null){lock.setReferenceCounted(false);lock.acquire();}
            byte[] query=MdnsPacket.query(host);boolean sent=false;
            for(String group:new String[]{"224.0.0.251","ff02::fb"})try{
                InetAddress address=InetAddress.getByName(group);if(address instanceof Inet6Address)address=Inet6Address.getByAddress(null,address.getAddress(),iface);
                socket.send(new DatagramPacket(query,query.length,address,5353));sent=true;
            }catch(IOException ignored){}
            if(!sent)throw new IOException("Cannot send mDNS query.");
            MdnsPacket.Replies replies = new MdnsPacket.Replies(SystemClock.elapsedRealtime());
            byte[] buffer = new byte[9000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            for (int packets = 0; packets < 64 && replies.waiting(SystemClock.elapsedRealtime()); packets++) {
                socket.setSoTimeout(replies.remaining(SystemClock.elapsedRealtime()));
                reply.setLength(buffer.length);
                try { socket.receive(reply); }
                catch (SocketTimeoutException e) { break; }
                if (reply.getPort() != 5353 || !onLink(reply.getAddress(), link)) continue;
                try {
                    for (InetAddress address : MdnsPacket.addresses(buffer, reply.getLength(), host)) {
                        if (!onLink(address, link)) continue;
                        if (address instanceof Inet6Address && address.isLinkLocalAddress()) {
                            address = Inet6Address.getByAddress(host, address.getAddress(), iface);
                        }
                        replies.add(address, SystemClock.elapsedRealtime());
                    }
                } catch (IOException ignored) {}
            }
            return replies.addresses();
        }finally{socket.close();synchronized(this){sockets.remove(socket);}if(lock!=null&&lock.isHeld())lock.release();}
    }
    private static boolean onLink(InetAddress a,LinkProperties link){
        if(a.isLinkLocalAddress())return true;
        for(LinkAddress local:link.getLinkAddresses())if(MdnsPacket.prefix(a.getAddress(),local.getAddress().getAddress(),local.getPrefixLength()))return true;
        return false;
    }
}
