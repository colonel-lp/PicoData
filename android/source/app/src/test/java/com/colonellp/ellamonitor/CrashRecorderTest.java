package com.colonellp.ellamonitor;

import java.nio.file.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class CrashRecorderTest {
    @Test public void crashSavesRedactedContextAndPreservesPlatformHandlerAcrossRecreation()throws Exception{
        Path folder=Files.createTempDirectory("ella-crash-test");Thread.UncaughtExceptionHandler original=Thread.getDefaultUncaughtExceptionHandler();AtomicInteger delivered=new AtomicInteger();
        ConnectionDiagnostics first=new ConnectionDiagnostics(),current=new ConnectionDiagnostics();
        try{
            Thread.setDefaultUncaughtExceptionHandler((thread,error)->delivered.incrementAndGet());
            CrashRecorder old=CrashRecorder.install(first);current.attach(folder.toFile(),"0.33");current.foreground(true);CrashRecorder active=CrashRecorder.install(current);old.restore();assertSame(active,Thread.getDefaultUncaughtExceptionHandler());
            current.transport("history","popup",new IOException("secret address token"));
            RuntimeException error=new RuntimeException("private credential");error.setStackTrace(new StackTraceElement[]{new StackTraceElement("com.colonellp.ellamonitor.PopupChartView","onDraw","PopupChartView.java",40),new StackTraceElement("unrelated.Private","secret","private-path",9)});
            active.uncaughtException(Thread.currentThread(),error);current.awaitFaultWrites();assertEquals(1,delivered.get());assertEquals(1,current.savedFaults().length);
            String saved=current.savedFault(current.savedFaults()[0]);assertTrue(saved.contains("application_crash"));assertTrue(saved.contains("PopupChartView.onDraw:40"));assertTrue(saved.contains("stage=popup"));assertTrue(saved.contains("post-crash tail unavailable"));assertFalse(saved.contains("private credential"));assertFalse(saved.contains("secret address"));assertFalse(saved.contains("unrelated.Private"));assertFalse(saved.contains("private-path"));active.restore();assertNotSame(old,Thread.getDefaultUncaughtExceptionHandler());
        }finally{Thread.setDefaultUncaughtExceptionHandler(original);first.close();current.close();try(java.util.stream.Stream<Path> paths=Files.walk(folder)){paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(IOException ignored){}});}}
    }
    @Test public void intentionalCancellationDoesNotTriggerRepeatedLiveFailureFault()throws Exception{
        Path folder=Files.createTempDirectory("ella-cancel-test");ConnectionDiagnostics d=new ConnectionDiagnostics();try{d.attach(folder.toFile(),"0.33");d.foreground(true);for(int i=0;i<3;i++)d.request("live","cancelled",20,0,new IOException());d.awaitFaultWrites();assertEquals(0,d.savedFaults().length);assertTrue(d.text("0.33").contains("stage=cancelled"));}finally{d.close();try(java.util.stream.Stream<Path> paths=Files.walk(folder)){paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(IOException ignored){}});}}
    }
}
