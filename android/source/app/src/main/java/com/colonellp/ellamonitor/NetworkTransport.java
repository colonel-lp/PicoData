package com.colonellp.ellamonitor;

import android.net.Network;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

/** Network-scoped DNS with standard HTTP parsing and TLS host verification. */
final class NetworkTransport implements ApiClient.Connections {
    private final OkHttpClient http,readHttp;
    private final ConnectionDiagnostics diagnostics;
    private static final class Label {final long id;Label(long value){id=value;}}
    NetworkTransport(Network route,LocalNameResolver resolver){
        this(route,resolver,null);
    }
    NetworkTransport(Network route,LocalNameResolver resolver,ConnectionDiagnostics diagnostics){
        this(new OkHttpClient.Builder().socketFactory(route.getSocketFactory()).dns(host->resolver.lookup(route,host))
            .proxy(Proxy.NO_PROXY).followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .connectTimeout(4,TimeUnit.SECONDS).readTimeout(4,TimeUnit.SECONDS).callTimeout(9,TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(1,2,TimeUnit.SECONDS)).build(),diagnostics);
    }
    NetworkTransport(OkHttpClient client,ConnectionDiagnostics diagnostics){
        this.diagnostics=diagnostics;
        OkHttpClient.Builder builder=client.newBuilder().proxy(Proxy.NO_PROXY).followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false);
        if(diagnostics!=null)builder.eventListenerFactory(call->new okhttp3.EventListener(){
            private final long started=System.nanoTime();
            private void failed(Call c,String stage,IOException e,String family){Label label=c.request().tag(Label.class);diagnostics.attempt(ConnectionDiagnostics.operation(c.request().url().encodedPath()),stage,(System.nanoTime()-started)/1_000_000,e,label==null?0:label.id,family);}
            @Override public void connectFailed(Call c,InetSocketAddress address,Proxy proxy,Protocol protocol,IOException e){failed(c,"connect",e,address.getAddress() instanceof Inet6Address?"IPv6":address.getAddress() instanceof Inet4Address?"IPv4":"unresolved");}
            @Override public void requestFailed(Call c,IOException e){failed(c,"send",e,"unknown");}
            @Override public void responseFailed(Call c,IOException e){failed(c,"receive",e,"unknown");}
        });
        http=builder.build();
        // Shared pool/dispatcher/network/TLS policy; GET recovery only, within one call deadline.
        readHttp=http.newBuilder().retryOnConnectionFailure(true).build();
    }
    public HttpURLConnection open(URL url){return new Connection(url,http,readHttp,diagnostics==null?0:diagnostics.nextCallId());}
    public long requestId(HttpURLConnection c){return c instanceof Connection?((Connection)c).id:0;}
    public void close(){http.dispatcher().cancelAll();http.connectionPool().evictAll();http.dispatcher().executorService().shutdown();}
    private static RequestBody commandBody(byte[] bytes){return new RequestBody(){
        @Override public MediaType contentType(){return MediaType.get("application/json");}
        @Override public long contentLength(){return bytes.length;}
        @Override public boolean isOneShot(){return true;}
        @Override public void writeTo(okio.BufferedSink sink)throws IOException{sink.write(bytes);}
    };}
    private static final class Connection extends HttpURLConnection {
        final OkHttpClient http,readHttp;final Map<String,String> headers=new LinkedHashMap<>();final ByteArrayOutputStream output=new ByteArrayOutputStream();
        volatile Call call;volatile boolean cancelled;Response response;final long id;
        Connection(URL url,OkHttpClient client,OkHttpClient reader,long id){super(url);http=client;readHttp=reader;this.id=id;}
        @Override public void setRequestProperty(String key,String value){if(connected)throw new IllegalStateException("Already connected");headers.put(key,value);}
        @Override public OutputStream getOutputStream(){return output;}
        @Override public void connect()throws IOException{
            if(cancelled)throw new IOException("Cancelled.");if(response!=null)return;
            Request.Builder request=new Request.Builder().url(url).tag(Label.class,new Label(id));for(Map.Entry<String,String> h:headers.entrySet())request.header(h.getKey(),h.getValue());
            if(method.equals("POST")){if(output.size()>64)throw new IOException("Request too large.");request.post(commandBody(output.toByteArray()));}
            else if(!method.equals("GET"))throw new IOException("Unsupported operation.");
            Call prepared=(method.equals("GET")?readHttp:http).newCall(request.build());call=prepared;if(cancelled){prepared.cancel();throw new IOException("Cancelled.");}
            response=prepared.execute();connected=true;if(cancelled){response.close();throw new IOException("Cancelled.");}
        }
        @Override public int getResponseCode()throws IOException{connect();return response.code();}
        @Override public long getContentLengthLong(){return response==null||response.body()==null?-1:response.body().contentLength();}
        @Override public InputStream getInputStream()throws IOException{connect();if(response.body()==null)throw new IOException("Missing response.");return response.body().byteStream();}
        @Override public InputStream getErrorStream(){try{return getInputStream();}catch(IOException e){return null;}}
        @Override public boolean usingProxy(){return false;}
        @Override public void disconnect(){cancelled=true;Call c=call;if(c!=null)c.cancel();if(response!=null)response.close();}
    }
}

