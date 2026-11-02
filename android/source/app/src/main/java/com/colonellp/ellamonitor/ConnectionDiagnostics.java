package com.colonellp.ellamonitor;

import java.time.Instant;
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
        if(operation.equals("live")){
            if(error==null&&stage.equals("complete"))liveFailures=0;
            else if(error!=null){if(liveFailures++==0)firstFailure=monotonic.getAsLong();if(liveFailures>=2)fault("repeated_live_request_failure",firstFailure);}
        }
    }
    void attempt(String operation,String stage,long duration,Throwable error,long id,String family){
        add("attempt "+word(operation)+" call="+id+" family="+word(family)+" stage="+word(stage)+" elapsed="+Math.max(0,duration)+"ms error="+word(error.getClass().getSimpleName())+" errno="+errno(error));
    }
    private static String errno(Throwable error){
        for(int i=0;error!=null&&i<4;i++,error=error.getCause())try{
            Object value=error.getClass().getField("errno").get(error);if(value instanceof Integer)return Integer.toString((Integer)value);
        }catch(ReflectiveOperationException|SecurityException ignored){}
        return "unknown";
    }
    void transport(String operation,String stage,Throwable error){request(operation,stage,0,0,error);}
    void network(String reason,int generation,long handle,boolean ssidKnown){
        add("network "+word(reason)+" generation="+generation+" handle="+handle+" ssid-known="+ssidKnown);
    }
    synchronized void snapshot(long ageMillis,boolean present){
        if(!present){expired=null;return;}
        boolean next=ageMillis>3000;
        if(expired==null||next!=expired){add("snapshot "+(next?"expired":"fresh")+" age="+Math.max(0,ageMillis)+"ms");expired=next;if(next)fault("display_snapshot_expired",liveFailures>0?firstFailure:monotonic.getAsLong());}
        if(!next&&faults!=null)faults.recovered();
    }
    synchronized String text(String version){
        StringBuilder s=new StringBuilder("Ella Monitoring ").append(version.matches("[0-9]+\\.[0-9]{2}")?version:"unknown").append(" connection diagnostics\nLatest 256 events; private outage files are available under FAULT LOGS.\nAttempt failures may recover: match call IDs to final request results.\nNo addresses, SSIDs, headers or credentials.\n");
        if(faults!=null&&!faults.error().isEmpty())s.append("Fault storage error: ").append(faults.error()).append('\n');s.append('\n');
        for(String e:entries)s.append(e).append('\n');
        if(entries.isEmpty())s.append("No connection events captured yet.\n");return s.toString();
    }
}

