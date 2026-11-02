package com.colonellp.ellamonitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;

/** Private incident files; no network operations and no per-request disk writes. */
final class ConnectionFaultStore {
    static final int LIMIT=256*1024;
    static final class Entry {final long at;final String text;Entry(long a,String t){at=a;text=t;}}
    private static final class Window {
        final String name,reason,utc;final long onset,confirmed,end;final boolean preComplete;
        final ArrayList<String> lines=new ArrayList<>();final LinkedHashSet<String> reasons=new LinkedHashSet<>();boolean truncated;
        Window(String n,String r,long o,long c,boolean p){name=n;reason=r;onset=o;confirmed=c;end=o+60000;preComplete=p;utc=Instant.now().toString();reasons.add(r);}
    }
    private final File folder;private final String version;private final LongSupplier clock;
    private final ScheduledExecutorService worker;
    private Window active;private boolean foreground,closed,latched;private long firstGood=-1;
    private volatile String lastError="";
    ConnectionFaultStore(File folder,String version,LongSupplier clock){
        this.folder=folder;this.version=version;this.clock=clock;
        worker=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"connection-faults");t.setDaemon(true);return t;});
        worker.execute(()->{try{prepare();for(File f:files(true))if(f.getName().endsWith(".pending")){String text=read(f).replace("state=collecting","state=interrupted (process/activity ended)");write(new File(folder,f.getName().replace(".pending","")),text);Files.deleteIfExists(f.toPath());}prune();}catch(Exception e){failed(e);}});
        worker.scheduleAtFixedRate(this::checkpoint,10,10,TimeUnit.SECONDS);
    }
    synchronized void foreground(boolean value){foreground=value;if(!value){firstGood=-1;finish("interrupted (app left foreground)");}}
    synchronized void event(Entry entry){
        if(active!=null&&entry.at>=active.confirmed&&entry.at<=active.end){
            if(active.lines.size()<2048)active.lines.add(entry.text);else active.truncated=true;
        }
    }
    synchronized void recovered(){if(firstGood<0)firstGood=clock.getAsLong();if(active==null&&clock.getAsLong()-firstGood>=60000)latched=false;}
    synchronized void capture(String reason,long onset,List<Entry> history){
        if(!foreground||closed)return;
        long now=clock.getAsLong();firstGood=-1;
        if(active!=null){if(active.reasons.add(reason))worker.execute(this::checkpoint);return;}
        if(latched)return;
        boolean pre=!history.isEmpty()&&history.get(0).at<=onset-60000;
        active=new Window("fault-"+System.currentTimeMillis()+"-"+now+".log",reason,onset,now,pre);
        for(Entry e:history)if(e.at>=onset-60000&&e.at<=active.end)active.lines.add(e.text);
        latched=true;worker.execute(this::checkpoint);
    }
    private synchronized String render(Window w,String state){
        StringBuilder out=new StringBuilder("Ella Monitoring ").append(version).append(" connection fault\n")
            .append("state=").append(state).append("\nconfirmed_utc=").append(w.utc)
            .append("\nreason=").append(w.reason).append("\nrelated_reasons=").append(w.reasons).append("\nonset_monotonic_ms=").append(w.onset)
            .append("\nconfirmed_monotonic_ms=").append(w.confirmed).append("\nwindow=60 seconds before and after observed onset")
            .append("\npre_window_available=").append(w.preComplete).append("\ncontext_truncated=").append(w.truncated)
            .append("\nNo addresses, Wi-Fi names, headers, credentials or exception messages.\n\n");
        int bytes=out.toString().getBytes(StandardCharsets.UTF_8).length;boolean limited=w.truncated;
        for(String line:w.lines){int n=line.getBytes(StandardCharsets.UTF_8).length+1;if(bytes+n>LIMIT-100){out.append("[remaining context omitted at file-size limit]\n");limited=true;break;}out.append(line).append('\n');bytes+=n;}
        return limited?out.toString().replace("context_truncated=false","context_truncated=true"):out.toString();
    }
    void checkpoint(){
        File destination;String text;
        synchronized(this){
            if(active==null)return;
            if(clock.getAsLong()>=active.end){finish("complete");return;}
            destination=new File(folder,active.name+".pending");text=render(active,"collecting");
        }
        // Never hold the UI/event lock while storage waits for a durable write.
        try{write(destination,text);}catch(Exception e){failed(e);}
    }
    private synchronized void finish(String state){
        if(active==null)return;Window w=active;active=null;String data=render(w,state);
        worker.execute(()->{try{write(new File(folder,w.name),data);Files.deleteIfExists(new File(folder,w.name+".pending").toPath());prune();}catch(Exception e){failed(e);}});
    }
    private void prepare()throws IOException{
        Files.createDirectories(folder.toPath());
        folder.setReadable(false,false);folder.setWritable(false,false);folder.setExecutable(false,false);
        if(!folder.setReadable(true,true)||!folder.setWritable(true,true)||!folder.setExecutable(true,true))throw new IOException("Private fault folder unavailable");
    }
    private void write(File file,String text)throws IOException{
        prepare();byte[] bytes=text.getBytes(StandardCharsets.UTF_8);if(bytes.length>LIMIT)throw new IOException("Fault size limit");
        Path temporary=Files.createTempFile(folder.toPath(),".fault-",".tmp");
        try{
            File f=temporary.toFile();f.setReadable(false,false);f.setWritable(false,false);if(!f.setReadable(true,true)||!f.setWritable(true,true))throw new IOException("Private file unavailable");
            try(FileOutputStream out=new FileOutputStream(f)){out.write(bytes);out.getFD().sync();}
            try{Files.move(temporary,file.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e){Files.move(temporary,file.toPath(),StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temporary);}
    }
    private File[] files(boolean includePending){
        File[] files=folder.listFiles(f->f.isFile()&&!Files.isSymbolicLink(f.toPath())&&f.getName().matches("fault-[0-9]+-[0-9]+\\.log"+(includePending?"(?:\\.pending)?":"")));
        if(files==null)return new File[0];Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));return files;
    }
    File[] files(){return files(true);}
    private void prune()throws IOException{File[] f=files(false);for(int i=5;i<f.length;i++)Files.deleteIfExists(f[i].toPath());}
    String read(File file)throws IOException{
        if(!file.getCanonicalFile().getParentFile().equals(folder.getCanonicalFile())||Files.isSymbolicLink(file.toPath())||file.length()>LIMIT)throw new IOException("Invalid fault file");
        try(InputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(out.size()+n>LIMIT)throw new IOException("Fault size limit");out.write(b,0,n);}return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    private void failed(Exception error){lastError=error.getClass().getSimpleName();}
    String error(){return lastError;}
    void close(){synchronized(this){foreground=false;finish("interrupted (activity destroyed)");closed=true;}worker.shutdown();}
    void awaitWrites()throws InterruptedException{for(int i=0;i<2;i++){CountDownLatch done=new CountDownLatch(1);worker.execute(done::countDown);if(!done.await(5,TimeUnit.SECONDS))throw new IllegalStateException("Fault writer timed out");}}
}
