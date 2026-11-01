package com.colonellp.ellamonitor;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.function.LongSupplier;

/** Bounded RAM-only evidence. Never accepts URLs, headers, tokens or exception messages. */
final class ConnectionDiagnostics {
    private final ArrayDeque<String> entries=new ArrayDeque<>();
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
    private synchronized void add(String text){
        entries.addLast(Instant.ofEpochMilli(wall.getAsLong())+" t+"+Math.max(0,monotonic.getAsLong()-started)+"ms "+text);
        while(entries.size()>256)entries.removeFirst();
    }
    void request(String operation,String stage,long duration,int status,Throwable error){
        add("request "+word(operation)+" stage="+word(stage)+" elapsed="+Math.max(0,duration)+"ms status="+status+" error="+(error==null?"none":word(error.getClass().getSimpleName())));
    }
    void transport(String operation,String stage,Throwable error){request(operation,stage,0,0,error);}
    void network(String reason,int generation,long handle,boolean ssidKnown){
        add("network "+word(reason)+" generation="+generation+" handle="+handle+" ssid-known="+ssidKnown);
    }
    synchronized void snapshot(long ageMillis,boolean present){
        if(!present){expired=null;return;}
        boolean next=ageMillis>3000;
        if(expired==null||next!=expired){add("snapshot "+(next?"expired":"fresh")+" age="+Math.max(0,ageMillis)+"ms");expired=next;}
    }
    synchronized String text(String version){
        StringBuilder s=new StringBuilder("Ella Monitoring ").append(version.matches("[0-9]+\\.[0-9]{2}")?version:"unknown").append(" connection diagnostics\nRAM only; latest 256 events. No addresses, SSIDs, headers or credentials.\n\n");
        for(String e:entries)s.append(e).append('\n');
        if(entries.isEmpty())s.append("No connection events captured yet.\n");return s.toString();
    }
}
