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
    @Test public void cancelDuringChunkedReadOnlyClosesOnReaderWorker()throws Exception{
        for(int repeat=0;repeat<12;repeat++){
            try(ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"))){
                CountDownLatch bodyRead=new CountDownLatch(1),finish=new CountDownLatch(1);
                AtomicInteger closes=new AtomicInteger();AtomicReference<String> closer=new AtomicReference<>();AtomicBoolean reading=new AtomicBoolean(),overlap=new AtomicBoolean();
                Thread peer=new Thread(()->{try(Socket socket=server.accept()){
                    request(socket);socket.getOutputStream().write("HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\nf\r\n{\"apiVersion\":1\r\n".getBytes(StandardCharsets.UTF_8));socket.getOutputStream().flush();finish.await(3,TimeUnit.SECONDS);
                }catch(Exception ignored){}});peer.setDaemon(true);peer.start();
                OkHttpClient http=new OkHttpClient.Builder().callTimeout(3,TimeUnit.SECONDS).addInterceptor(chain->{
                    Response response=chain.proceed(chain.request());ResponseBody body=response.body();
                    okio.BufferedSource source=okio.Okio.buffer(new okio.ForwardingSource(body.source()){
                        @Override public long read(okio.Buffer sink,long count)throws IOException{reading.set(true);try{long n=super.read(sink,count);if(n>0)bodyRead.countDown();return n;}finally{reading.set(false);}}
                        @Override public void close()throws IOException{if(reading.get())overlap.set(true);closes.incrementAndGet();closer.set(Thread.currentThread().getName());super.close();}
                    });
                    return response.newBuilder().body(new ResponseBody(){public MediaType contentType(){return body.contentType();}public long contentLength(){return -1;}public okio.BufferedSource source(){return source;}}).build();
                }).build();
                ApiClient client=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),new NetworkTransport(http,null));
                ExecutorService reader=Executors.newSingleThreadExecutor(r->new Thread(r,"request-reader"));
                Future<Throwable> result=reader.submit(()->{try{client.get("/api/v1/live");return new AssertionError("Cancelled body succeeded");}catch(Throwable e){return e;}});
                try{assertTrue(bodyRead.await(2,TimeUnit.SECONDS));long start=System.nanoTime();client.cancel();assertTrue("UI cancellation blocked",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)<500);
                    assertTrue(result.get(2,TimeUnit.SECONDS) instanceof IOException);assertFalse("Concurrent read/close",overlap.get());assertEquals(1,closes.get());assertEquals("request-reader",closer.get());
                }finally{finish.countDown();client.cancel();reader.shutdownNow();peer.join(1000);}
            }
        }
    }
    @Test public void cancelBeforeExecuteAndDuringHeadersTerminatesWithoutBodyCleanupRace()throws Exception{
        ApiClient cancelled=new ApiClient("http://127.0.0.1:1",ConnectionStabilityTest.repeat('a'),new NetworkTransport(new OkHttpClient(),null));cancelled.cancel();
        try{cancelled.get("/api/v1/live");fail();}catch(IOException expected){}
        try(ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"))){
            CountDownLatch accepted=new CountDownLatch(1),finish=new CountDownLatch(1);
            Thread peer=new Thread(()->{try(Socket socket=server.accept()){request(socket);accepted.countDown();finish.await(3,TimeUnit.SECONDS);}catch(Exception ignored){}});peer.setDaemon(true);peer.start();
            ApiClient client=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),new NetworkTransport(new OkHttpClient.Builder().callTimeout(3,TimeUnit.SECONDS).build(),null));
            ExecutorService reader=Executors.newSingleThreadExecutor();Future<Throwable> result=reader.submit(()->{try{client.get("/api/v1/live");return null;}catch(Throwable e){return e;}});
            try{assertTrue(accepted.await(2,TimeUnit.SECONDS));client.cancel();assertTrue(result.get(2,TimeUnit.SECONDS) instanceof IOException);}finally{finish.countDown();client.cancel();reader.shutdownNow();peer.join(1000);}
        }
    }
    @Test public void freshReplyAgeExcludesHeaderWaitButIncludesBodyTransitAndLaterDelivery()throws Exception{
        try(ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"))){
            AtomicReference<Throwable> error=new AtomicReference<>();
            Thread peer=new Thread(()->{try(Socket socket=server.accept()){
                request(socket);Thread.sleep(450);byte[] body="{\"apiVersion\":1}".getBytes(StandardCharsets.UTF_8);
                socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Length: "+body.length+"\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));socket.getOutputStream().flush();Thread.sleep(3200);socket.getOutputStream().write(body);socket.getOutputStream().flush();
            }catch(Throwable e){error.set(e);}});peer.setDaemon(true);peer.start();
            ApiClient client=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),new NetworkTransport(new OkHttpClient(),null));
            try{long started=System.nanoTime();client.get("/api/v1/live");long age=client.responseAgeMillis(),total=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started);assertTrue(age>=3000);assertTrue(total-age>=350);org.json.JSONObject source=new org.json.JSONObject().put("fresh",true).put("state","connected").put("ageSeconds",0);assertFalse(MonitorData.fresh(source,age/1000d));assertTrue(MonitorData.fresh(source,.1));source.put("fresh",false);assertFalse(MonitorData.fresh(source,0));Thread.sleep(70);assertTrue(client.responseAgeMillis()>=age+50);peer.join(1000);if(error.get()!=null)throw new AssertionError(error.get());}finally{client.cancel();}
        }
    }
    @Test public void workerReleasesBodyForRejectedStatusAndMalformedJson()throws Exception{
        for(int status:new int[]{401,500,200}){
            AtomicInteger closes=new AtomicInteger();AtomicReference<String> closer=new AtomicReference<>();
            OkHttpClient http=new OkHttpClient.Builder().addInterceptor(chain->{
                okio.BufferedSource source=okio.Okio.buffer(new okio.ForwardingSource(new okio.Buffer().writeUtf8("invalid-json")){
                    @Override public void close()throws IOException{closes.incrementAndGet();closer.set(Thread.currentThread().getName());super.close();}
                });
                return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("fixture").body(new ResponseBody(){public MediaType contentType(){return null;}public long contentLength(){return 12;}public okio.BufferedSource source(){return source;}}).build();
            }).build();
            ApiClient c=new ApiClient("http://127.0.0.1:1",ConnectionStabilityTest.repeat('a'),new NetworkTransport(http,null));
            ExecutorService reader=Executors.newSingleThreadExecutor(r->new Thread(r,"response-reader"));
            try{Throwable error=reader.submit(()->{try{c.get("/api/v1/live");return null;}catch(Throwable e){return e;}}).get(2,TimeUnit.SECONDS);assertTrue(error instanceof IOException);assertEquals(1,closes.get());assertEquals("response-reader",closer.get());}finally{c.cancel();reader.shutdownNow();}
        }
    }
    @Test public void connectionWaitDoesNotExpireANewSuccessfulReply()throws Exception{
        try(ServerSocket server=new ServerSocket(0,4,InetAddress.getByName("127.0.0.1"))){
            AtomicReference<Throwable> error=new AtomicReference<>();Thread peer=new Thread(()->{try(Socket socket=server.accept()){request(socket);Thread.sleep(3200);response(socket,false);}catch(Throwable e){error.set(e);}});peer.setDaemon(true);peer.start();
            ApiClient c=new ApiClient("http://127.0.0.1:"+server.getLocalPort(),ConnectionStabilityTest.repeat('a'),new NetworkTransport(new OkHttpClient.Builder().callTimeout(6,TimeUnit.SECONDS).build(),null));
            try{long started=System.nanoTime();c.get("/api/v1/live");assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started)>=3000);assertTrue(c.responseAgeMillis()<1000);peer.join(1000);if(error.get()!=null)throw new AssertionError(error.get());}finally{c.cancel();}
        }
    }
    @Test public void expiredRouteCandidatesRecoverOnNextPollWithoutWaitingForCacheExpiry() throws Exception {
        InetAddress ipv4 = InetAddress.getByName("127.0.0.1");
        InetAddress ipv6 = InetAddress.getByName("::1");
        try (ServerSocket server = new ServerSocket(0, 4, ipv4)) {
            server.setSoTimeout(4000);
            AtomicReference<Throwable> error = new AtomicReference<>();
            Thread peer = new Thread(() -> {
                try (Socket established = server.accept()) {
                    established.setSoTimeout(4000);
                    for (int i = 0; i < 2; i++) {
                        assertEquals("GET", request(established));
                        response(established, i == 0);
                    }
                    try (Socket recovered = server.accept()) {
                        recovered.setSoTimeout(4000);
                        assertEquals("GET", request(recovered));
                        response(recovered, false);
                    }
                } catch (Throwable e) { error.set(e); }
            });
            peer.setDaemon(true);
            peer.start();

            AtomicInteger queries = new AtomicInteger();
            AtomicLong time = new AtomicLong();
            AddressCache cache = new AddressCache(time::get);
            NetworkTransport.RouteResolver resolver = host -> cache.lookup(0, host,
                () -> new AddressCache.Candidates(queries.incrementAndGet() == 2
                    ? java.util.Collections.singletonList(ipv6)
                    : java.util.Arrays.asList(ipv6, ipv4), "fixture"));
            ConnectionDiagnostics diagnostics = new ConnectionDiagnostics();
            OkHttpClient http = new OkHttpClient.Builder().callTimeout(3, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(1, 5, TimeUnit.SECONDS)).build();
            String base = "http://pi.test:" + server.getLocalPort();
            String token = ConnectionStabilityTest.repeat('a');
            ApiClient live = new ApiClient(base, token, new NetworkTransport(http, diagnostics, resolver), diagnostics);
            // An independent client cannot use the live connection already in the pool.
            ApiClient status = new ApiClient(base, token,
                new NetworkTransport(new OkHttpClient(), diagnostics, resolver), diagnostics);
            try {
                assertEquals(1, live.get("/api/v1/live").getInt("apiVersion"));
                time.set(15_000);
                // Seed the exact failure: a shared IPv6-only answer, while an IPv4 pool still works.
                resolver.lookup("pi.test");
                assertEquals(1, live.get("/api/v1/live").getInt("apiVersion"));
                try { status.get("/api/v1/status"); fail("IPv6-only candidates reached an IPv4 listener"); }
                catch (ConnectException expected) {}
                assertEquals(1, status.get("/api/v1/status").getInt("apiVersion"));
                assertEquals(3, queries.get());
                peer.join(4500);
                assertFalse(peer.isAlive());
                if (error.get() != null) throw new AssertionError(error.get());
                String log = diagnostics.text("0.34");
                assertTrue(log.contains("cache=cached cache-age=0ms"));
                assertTrue(log.contains("resolver retry status"));
                assertTrue(log.contains("family=IPv6 stage=connect"));
                assertTrue(log.contains("family=IPv4 connected=127.0.0.1:"));
                assertTrue(log.contains("endpoint=pi.test:"));
                assertFalse(log.contains(token));
            } finally {
                live.cancel();
                status.cancel();
            }
        }
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
