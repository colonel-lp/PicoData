package com.colonellp.ellamonitor;

import okhttp3.*;
import org.junit.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Actual OkHttp loopback checks for Android Studio; no Pi, credentials or radio. */
public class NetworkTransportTest {
    private static String request(Socket socket)throws IOException{
        InputStream in=socket.getInputStream();ByteArrayOutputStream head=new ByteArrayOutputStream();int b;
        while((b=in.read())!=-1){head.write(b);if(head.size()>8192)throw new IOException("fixture headers too large");String h=head.toString("UTF-8");if(h.endsWith("\r\n\r\n"))break;}
        if(head.size()==0)throw new EOFException();String h=head.toString("UTF-8");int body=0;
        for(String line:h.split("\r\n"))if(line.toLowerCase(java.util.Locale.ROOT).startsWith("content-length:"))body=Integer.parseInt(line.substring(15).trim());
        if(body>64)throw new IOException("fixture body too large");for(int i=0;i<body;i++)if(in.read()==-1)throw new EOFException();return h.split(" ")[0];
    }
    private static void response(Socket socket,boolean keep)throws IOException{
        byte[] body="{\"apiVersion\":1}".getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "+body.length+"\r\nConnection: "+(keep?"keep-alive":"close")+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));socket.getOutputStream().write(body);socket.getOutputStream().flush();
    }
    @Test public void safeGetRecoversWhenPrimedPooledConnectionFails()throws Exception{check(false);}
    @Test public void modePostIsNeverAutomaticallyReplayed()throws Exception{check(true);}
    @Test public void modePostIsNotReplayedWhenServerRequestsImmediate503Retry()throws Exception{
        ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"));server.setSoTimeout(5000);
        AtomicInteger count=new AtomicInteger();AtomicReference<Throwable> error=new AtomicReference<>();
        Thread worker=new Thread(()->{try{
            try(Socket socket=server.accept()){socket.setSoTimeout(1500);assertEquals("POST",request(socket));count.incrementAndGet();
                socket.getOutputStream().write("HTTP/1.1 503 Unavailable\r\nRetry-After: 0\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));socket.getOutputStream().flush();}
            server.setSoTimeout(500);
            try(Socket replay=server.accept()){count.incrementAndGet();throw new AssertionError("POST replayed after 503");}catch(SocketTimeoutException expected){}
        }catch(Throwable e){error.set(e);}});worker.setDaemon(true);worker.start();
        ApiClient c=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),new NetworkTransport(new OkHttpClient.Builder().callTimeout(3,TimeUnit.SECONDS).build(),new ConnectionDiagnostics()));
        try{try{c.mode("home");fail();}catch(ApiClient.Failure expected){assertEquals(503,expected.status);}
            worker.join(2500);assertFalse(worker.isAlive());if(error.get()!=null)throw new AssertionError(error.get());assertEquals(1,count.get());
        }finally{c.cancel();server.close();}
    }
    private void check(boolean post)throws Exception{
        ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"));server.setSoTimeout(5000);
        AtomicInteger count=new AtomicInteger();AtomicReference<Throwable> error=new AtomicReference<>();
        Thread worker=new Thread(()->{try{
            try(Socket pooled=server.accept()){
                pooled.setSoTimeout(1500);assertEquals("GET",request(pooled));count.incrementAndGet();response(pooled,true);
                assertEquals(post?"POST":"GET",request(pooled));count.incrementAndGet();
                // The server closes after receiving the second request, before its response.
            }
            server.setSoTimeout(500);
            try(Socket retry=server.accept()){count.incrementAndGet();if(post)throw new AssertionError("POST replayed");retry.setSoTimeout(1500);assertEquals("GET",request(retry));response(retry,false);}
            catch(SocketTimeoutException expected){if(!post)throw expected;}
        }catch(Throwable e){error.set(e);}});worker.setDaemon(true);worker.start();
        OkHttpClient http=new OkHttpClient.Builder().callTimeout(3,TimeUnit.SECONDS).readTimeout(2,TimeUnit.SECONDS).connectionPool(new ConnectionPool(1,5,TimeUnit.SECONDS)).build();
        NetworkTransport transport=new NetworkTransport(http,new ConnectionDiagnostics());
        ApiClient c=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),transport);
        try{
            c.get("/api/v1/live");
            if(post){try{c.mode("home");fail("Expected dropped POST response");}catch(IOException expected){}}
            else assertEquals(1,c.get("/api/v1/live").getInt("apiVersion"));
            worker.join(3500);assertFalse("fixture did not finish",worker.isAlive());if(error.get()!=null)throw new AssertionError(error.get());assertEquals(post?2:3,count.get());
        }finally{c.cancel();server.close();}
    }
}
