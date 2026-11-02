package com.colonellp.ellamonitor;

import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Pure JVM cases; main executes the same assertions without an Android runtime. */
public class ConnectionFaultStoreTest {
    private static int checks;
    private static void check(boolean value){checks++;if(!value)throw new AssertionError("Check "+checks);}
    private static void clean(File file)throws IOException{if(file.isDirectory()){File[] children=file.listFiles();if(children!=null)for(File c:children)clean(c);}Files.deleteIfExists(file.toPath());}
    @Test public void durablePrefixAndCompleteTail()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();long[] clock={72000};ConnectionFaultStore store=new ConnectionFaultStore(root,"0.32",()->clock[0]);
        try{
            store.foreground(true);ArrayList<ConnectionFaultStore.Entry> history=new ArrayList<>();
            for(long i=0;i<=72000;i+=1000)history.add(new ConnectionFaultStore.Entry(i,"fixture="+i));
            store.capture("fixture_outage",70000,history);store.awaitWrites();File[] files=store.files();check(files.length==1);check(files[0].getName().endsWith(".pending"));
            String prefix=store.read(files[0]);check(prefix.contains("state=collecting"));check(prefix.contains("pre_window_available=true"));check(prefix.contains("fixture=10000\n"));check(!prefix.contains("fixture=9000\n"));
            for(long i=73000;i<=130000;i+=1000)store.event(new ConnectionFaultStore.Entry(i,"fixture="+i));
            clock[0]=130000;store.checkpoint();store.awaitWrites();files=store.files();check(files.length==1);check(!files[0].getName().endsWith(".pending"));String full=store.read(files[0]);check(full.contains("state=complete"));check(full.contains("fixture=130000"));check(full.contains("fixture=10000\n"));check(store.error().isEmpty());
        }finally{store.close();clean(root);}
    }
    @Test public void foregroundExitIsAnHonestPartialCapture()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();long[] clock={100000};ConnectionFaultStore store=new ConnectionFaultStore(root,"0.32",()->clock[0]);
        try{store.foreground(true);store.capture("fixture",100000,Collections.singletonList(new ConnectionFaultStore.Entry(100000,"fixture")));store.foreground(false);store.awaitWrites();String text=store.read(store.files()[0]);check(text.contains("state=interrupted"));check(text.contains("pre_window_available=false"));check(!text.contains("state=complete"));}finally{store.close();clean(root);}
    }
    @Test public void restartRetainsAnInterruptedPendingRecord()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();File pending=new File(root,"fault-1-1.log.pending");Files.write(pending.toPath(),"state=collecting\nfixture-before-death\n".getBytes(StandardCharsets.UTF_8));
        ConnectionFaultStore store=new ConnectionFaultStore(root,"0.32",()->1000);
        try{store.awaitWrites();check(!pending.exists());check(store.files().length==1);String data=store.read(store.files()[0]);check(data.contains("state=interrupted"));check(data.contains("fixture-before-death"));}finally{store.close();clean(root);}
    }
    @Test public void recoveredAttemptErrorsDoNotCreateOutageFiles()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();long[] now={100000};ConnectionDiagnostics d=new ConnectionDiagnostics(()->now[0],()->0);
        try{d.attach(root,"0.32");d.foreground(true);long call=d.nextCallId();d.attempt("live","connect",100,new java.net.ConnectException("secret address"),call,"IPv6");d.request("live","complete",200,200,null,call);d.snapshot(200,true);d.awaitFaultWrites();check(d.savedFaults().length==0);String text=d.text("0.32");check(text.contains("attempt live call="+call+" family=IPv6"));check(text.contains("request live call="+call+" stage=complete"));check(!text.contains("secret address"));}finally{d.close();clean(root);}
    }
    @Test public void finalErrorsAndExpiryPersistOneCoalescedWindow()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();long[] now={0};ConnectionDiagnostics d=new ConnectionDiagnostics(()->now[0],()->0);
        try{d.attach(root,"0.32");d.foreground(true);for(int i=0;i<=70;i++){now[0]=i*1000;d.request("live","complete",10,200,null);}now[0]=71000;d.request("live","headers",20,0,new java.net.ConnectException("secret token"));now[0]=72000;d.request("live","headers",20,0,new java.net.ConnectException("secret token"));d.awaitFaultWrites();d.snapshot(3200,true);d.awaitFaultWrites();check(d.savedFaults().length==1);String data=d.savedFault(d.savedFaults()[0]);check(data.contains("pre_window_available=true"));check(data.contains("ConnectException"));check(data.contains("snapshot expired"));check(!data.contains("secret token"));d.foreground(false);d.awaitFaultWrites();}finally{d.close();clean(root);}
    }
    @Test public void storageFailureIsReportedWithoutBreakingDiagnostics()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();File blocked=new File(root,"blocked");Files.write(blocked.toPath(),new byte[]{1});ConnectionFaultStore store=new ConnectionFaultStore(blocked,"0.32",()->1000);
        try{store.foreground(true);store.capture("fixture",1000,Collections.emptyList());store.awaitWrites();check(!store.error().isEmpty());check(store.files().length==0);store.foreground(false);store.awaitWrites();}finally{store.close();clean(root);}
    }
    @Test public void retentionAndReaderBounds()throws Exception{
        File root=Files.createTempDirectory("fault-fixture").toFile();for(int i=0;i<8;i++)Files.write(new File(root,"fault-"+i+"-"+i+".log").toPath(),new byte[]{1});ConnectionFaultStore store=new ConnectionFaultStore(root,"0.32",()->1000);
        try{store.awaitWrites();check(store.files().length==5);File elsewhere=File.createTempFile("elsewhere", ".txt");try{store.read(elsewhere);throw new AssertionError();}catch(IOException expected){check(true);}finally{elsewhere.delete();}}finally{store.close();clean(root);}
    }
    public static void main(String[] args)throws Exception{ConnectionFaultStoreTest t=new ConnectionFaultStoreTest();t.durablePrefixAndCompleteTail();t.foregroundExitIsAnHonestPartialCapture();t.restartRetainsAnInterruptedPendingRecord();t.recoveredAttemptErrorsDoNotCreateOutageFiles();t.finalErrorsAndExpiryPersistOneCoalescedWindow();t.storageFailureIsReportedWithoutBreakingDiagnostics();t.retentionAndReaderBounds();System.out.println("Connection fault store: "+checks+" checks passed in 7 cases");}
}
