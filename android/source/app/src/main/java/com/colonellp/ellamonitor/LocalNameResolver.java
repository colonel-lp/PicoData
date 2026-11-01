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
    private final Context context;private int generation;
    private final Map<String,Entry> cache=new HashMap<>();private final Set<DatagramSocket> sockets=new HashSet<>();
    LocalNameResolver(Context c){context=c.getApplicationContext();}
    synchronized void reset(){generation++;cache.clear();for(DatagramSocket s:sockets)s.close();sockets.clear();}
    List<InetAddress> lookup(Network network,String host)throws UnknownHostException{
        final int epoch;String key=network.getNetworkHandle()+"/"+MdnsPacket.name(host);long now=SystemClock.elapsedRealtime();
        synchronized(this){epoch=generation;Entry e=cache.get(key);if(e!=null&&now-e.at<15000)return new ArrayList<>(e.addresses);}
        List<InetAddress> result=null;
        if(!MdnsPacket.name(host).endsWith(".local")||Build.VERSION.SDK_INT>=31){
            try{result=Arrays.asList(network.getAllByName(host));}catch(UnknownHostException ignored){}
        }
        if((result==null||result.isEmpty())&&MdnsPacket.name(host).endsWith(".local")){
            try{result=multicast(network,host,epoch);}catch(IOException|SecurityException e){throw new UnknownHostException("Local Pi hostname unavailable on this Wi-Fi network.");}
        }
        if(result==null||result.isEmpty())throw new UnknownHostException("Pi hostname unavailable.");
        result=new ArrayList<>(result);result.sort(Comparator.comparingInt(a->a instanceof Inet4Address?0:1));
        synchronized(this){if(epoch!=generation)throw new UnknownHostException("Network changed.");cache.put(key,new Entry(result,now));}
        return result;
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
            synchronized(this){if(epoch!=generation)throw new IOException("Network changed.");sockets.add(socket);}
            network.bindSocket(socket);socket.bind(new InetSocketAddress(0));socket.setNetworkInterface(iface);socket.setTimeToLive(255);
            if(lock!=null){lock.setReferenceCounted(false);lock.acquire();}
            byte[] query=MdnsPacket.query(host);boolean sent=false;
            for(String group:new String[]{"224.0.0.251","ff02::fb"})try{
                InetAddress address=InetAddress.getByName(group);if(address instanceof Inet6Address)address=Inet6Address.getByAddress(null,address.getAddress(),iface);
                socket.send(new DatagramPacket(query,query.length,address,5353));sent=true;
            }catch(IOException ignored){}
            if(!sent)throw new IOException("Cannot send mDNS query.");
            long end=SystemClock.elapsedRealtime()+2000;List<InetAddress> found=new ArrayList<>();int packets=0;
            while(SystemClock.elapsedRealtime()<end&&packets++<64){
                socket.setSoTimeout((int)Math.max(1,end-SystemClock.elapsedRealtime()));byte[] buffer=new byte[9000];DatagramPacket reply=new DatagramPacket(buffer,buffer.length);
                try{socket.receive(reply);}catch(SocketTimeoutException e){break;}
                if(reply.getPort()!=5353||!onLink(reply.getAddress(),link))continue;
                try{for(InetAddress a:MdnsPacket.addresses(buffer,reply.getLength(),host)){
                    if(!onLink(a,link))continue;
                    if(a instanceof Inet6Address&&a.isLinkLocalAddress())a=Inet6Address.getByAddress(host,a.getAddress(),iface);
                    if(!found.contains(a))found.add(a);
                }}catch(IOException ignored){}
                if(!found.isEmpty())return found;
            }
            throw new UnknownHostException("No matching mDNS answer.");
        }finally{socket.close();synchronized(this){sockets.remove(socket);}if(lock!=null&&lock.isHeld())lock.release();}
    }
    private static boolean onLink(InetAddress a,LinkProperties link){
        if(a.isLinkLocalAddress())return true;
        for(LinkAddress local:link.getLinkAddresses())if(MdnsPacket.prefix(a.getAddress(),local.getAddress().getAddress(),local.getPrefixLength()))return true;
        return false;
    }
    private static final class Entry{final List<InetAddress> addresses;final long at;Entry(List<InetAddress> a,long t){addresses=new ArrayList<>(a);at=t;}}
}
