package com.colonellp.ellamonitor;

import java.time.Instant;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.StringJoiner;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.File;
import java.util.function.LongSupplier;

/** Bounded redacted request context, with automatic private outage windows. */
final class ConnectionDiagnostics {
    private final ArrayDeque<String> entries=new ArrayDeque<>();
    private final ArrayDeque<ConnectionFaultStore.Entry> history=new ArrayDeque<>();
    private ConnectionFaultStore faults;
    private long callId,firstFailure;private int liveFailures;
    private final LongSupplier monotonic,wall;
    private final long started;
    private Boolean expired;
    ConnectionDiagnostics(){this(()->System.nanoTime()/1_000_000,System::currentTimeMillis);}
    ConnectionDiagnostics(LongSupplier monotonic,LongSupplier wall){this.monotonic=monotonic;this.wall=wall;started=monotonic.getAsLong();}
    static String operation(String path){
        if(path.equals("/api/v1/live"))return "live";
        if(path.equals("/api/v1/pi/wifi"))return "pi-action";
        if(path.equals("/api/v1/pi"))return "pi-status";
        if(path.equals("/api/v1/status"))return "status";
        if(path.equals("/api/v1/metrics"))return "catalogue";
        if(path.startsWith("/api/v1/history/"))return "history";
        return "other";
    }
    private static String word(String s){return s!=null&&s.matches("[A-Za-z0-9_-]{1,64}")?s:"other";}
    synchronized long nextCallId(){return ++callId;}
    synchronized void attach(File folder,String version){faults=new ConnectionFaultStore(folder,version,monotonic);}
    synchronized void foreground(boolean value){if(faults!=null)faults.foreground(value);if(!value){liveFailures=0;expired=null;}}
    synchronized void close(){if(faults!=null)faults.close();}
    synchronized File[] savedFaults(){return faults==null?new File[0]:faults.files();}
    synchronized String savedFault(File f)throws java.io.IOException{return faults==null?"":faults.read(f);}
    void awaitFaultWrites()throws InterruptedException{ConnectionFaultStore f; synchronized(this){f=faults;}if(f!=null)f.awaitWrites();}
    private void fault(String reason,long onset){if(faults!=null)faults.capture(reason,onset,new ArrayList<>(history));}
    private synchronized void add(String text){
        long now=monotonic.getAsLong();String line=Instant.ofEpochMilli(wall.getAsLong())+" t+"+Math.max(0,now-started)+"ms "+text;
        entries.addLast(line);
        while(entries.size()>256)entries.removeFirst();
        ConnectionFaultStore.Entry event=new ConnectionFaultStore.Entry(now,line);history.addLast(event);
        while(history.size()>2048||!history.isEmpty()&&history.peekFirst().at<now-180000)history.removeFirst();
        if(faults!=null)faults.event(event);
    }
    void request(String operation,String stage,long duration,int status,Throwable error){
        request(operation,stage,duration,status,error,0);
    }
    synchronized void request(String operation,String stage,long duration,int status,Throwable error,long id){
        add("request "+word(operation)+" call="+id+" stage="+word(stage)+" elapsed="+Math.max(0,duration)+"ms status="+status+" error="+(error==null?"none":word(error.getClass().getSimpleName()))+" errno="+errno(error));
        if(operation.equals("live")&&!stage.equals("cancelled")){
            if(error==null&&stage.equals("complete"))liveFailures=0;
            else if(error!=null){if(liveFailures++==0)firstFailure=monotonic.getAsLong();if(liveFailures>=2)fault("repeated_live_request_failure",firstFailure);}
        }
    }
    void destination(String operation, long id, String host, int port) {
        add("destination "+word(operation)+" call="+id+" endpoint="+endpoint(host, port));
    }

    void resolved(String operation, long id, List<InetAddress> addresses, AddressCache.Lookup lookup) {
        StringJoiner candidates = new StringJoiner(",");
        for (int i = 0; i < Math.min(8, addresses.size()); i++) {
            InetAddress address = addresses.get(i);
            candidates.add(family(address) + "/" + host(address.getHostAddress()));
        }
        if (addresses.size() > 8) candidates.add("more");
        String provenance = lookup == null ? " source=client" :
            " source="+lookup.source+" cache="+lookup.use+" cache-age="+lookup.ageMillis+"ms";
        add("resolved "+word(operation)+" call="+id+provenance+" candidates="+candidates);
    }

    void discarded(String operation, long id) {
        add("resolver retry "+word(operation)+" call="+id+" reason=connect_failed");
    }

    void peer(String operation, long id, InetSocketAddress address) {
        add("peer "+word(operation)+" call="+id+" family="+family(address.getAddress())+
            " connected="+socketEndpoint(address));
    }

    void attempt(String operation, String stage, long duration, Throwable error, long id, InetSocketAddress address) {
        String target = address == null ? "" : " destination="+socketEndpoint(address);
        add("attempt "+word(operation)+" call="+id+" family="+family(address==null?null:address.getAddress())+
            " stage="+word(stage)+" elapsed="+Math.max(0,duration)+"ms error="+word(error.getClass().getSimpleName())+
            " errno="+errno(error)+target);
    }

    private static String socketEndpoint(InetSocketAddress address) {
        InetAddress peer = address.getAddress();
        return endpoint(peer == null ? address.getHostString() : peer.getHostAddress(), address.getPort());
    }

    private static String family(InetAddress address) {
        return address instanceof Inet6Address ? "IPv6" : address instanceof Inet4Address ? "IPv4" : "unknown";
    }

    private static String host(String value) {
        return value != null && value.matches("[A-Za-z0-9._:%-]{1,253}") ? value : "unavailable";
    }

    private static String endpoint(String value, int port) {
        String safe = host(value);
        return (safe.contains(":") ? "["+safe+"]" : safe)+":"+port;
    }

    private static String endpoint(String origin) {
        try {
            java.net.URI uri = new java.net.URI(origin);
            String host = uri.getHost();
            if (host == null) return "unavailable";
            if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length()-1);
            int port = uri.getPort();
            return endpoint(host, port == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : port);
        } catch (java.net.URISyntaxException e) { return "unavailable"; }
    }
    private static String errno(Throwable error){
        for(int i=0;error!=null&&i<4;i++,error=error.getCause())try{
            Object value=error.getClass().getField("errno").get(error);if(value instanceof Integer)return Integer.toString((Integer)value);
        }catch(ReflectiveOperationException|SecurityException ignored){}
        return "unknown";
    }
    void crash(Throwable error){
        synchronized(this){
            add("crash error="+word(error.getClass().getSimpleName()));
            int count=0;for(StackTraceElement frame:error.getStackTrace())if(frame.getClassName().startsWith("com.colonellp.ellamonitor.")&&count++<12)
                add("frame "+frame.getClassName()+"."+frame.getMethodName()+":"+frame.getLineNumber());
            if(faults!=null)faults.captureCrash(monotonic.getAsLong(),new ArrayList<>(history));
        }
        ConnectionFaultStore store;synchronized(this){store=faults;}if(store!=null)store.awaitCrashWrite();
    }
    void transport(String operation,String stage,Throwable error){request(operation,stage,0,0,error);}
    void network(String reason,int generation,long handle,boolean ssidKnown,String previous,String next){
        add("network "+word(reason)+" generation="+generation+" handle="+handle+" ssid-known="+ssidKnown+" previous="+endpoint(previous)+" next="+endpoint(next));
    }
    synchronized void snapshot(long ageMillis,boolean present){
        if(!present){expired=null;return;}
        boolean next=ageMillis>3000;
        if(expired==null||next!=expired){add("snapshot "+(next?"expired":"fresh")+" age="+Math.max(0,ageMillis)+"ms");expired=next;if(next)fault("display_snapshot_expired",liveFailures>0?firstFailure:monotonic.getAsLong());}
        if(!next&&faults!=null)faults.recovered();
    }
    synchronized String text(String version){
        StringBuilder s=new StringBuilder("Ella Monitoring ").append(version.matches("[0-9]+\\.[0-9]{2}")?version:"unknown").append(" connection diagnostics\nLatest 256 events; private outage files are available under FAULT LOGS.\nAttempt failures may recover: match call IDs to final request results.\nIncludes LAN destinations and peer addresses. Wi-Fi names, headers and credentials are omitted.\n");
        if(faults!=null&&!faults.error().isEmpty())s.append("Fault storage error: ").append(faults.error()).append('\n');s.append('\n');
        for(String e:entries)s.append(e).append('\n');
        if(entries.isEmpty())s.append("No connection events captured yet.\n");return s.toString();
    }
}

