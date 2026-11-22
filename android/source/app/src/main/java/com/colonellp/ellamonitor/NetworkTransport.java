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
    private final java.util.concurrent.atomic.AtomicBoolean closed=new java.util.concurrent.atomic.AtomicBoolean();
    interface RouteResolver { AddressCache.Lookup lookup(String host) throws UnknownHostException; }

    private static final class Label {
        final long id;
        volatile AddressCache.Lookup lookup;
        volatile boolean connectFailed, acquired;

        Label(long id) { this.id = id; }
    }

    private final ThreadLocal<Label> resolvingCall = new ThreadLocal<>();

    NetworkTransport(Network route, LocalNameResolver resolver, ConnectionDiagnostics diagnostics) {
        this(new OkHttpClient.Builder().socketFactory(route.getSocketFactory())
            .connectTimeout(4,TimeUnit.SECONDS).readTimeout(4,TimeUnit.SECONDS).callTimeout(9,TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(1,2,TimeUnit.SECONDS)).build(), diagnostics,
            scopedResolver(route, resolver));
    }

    private static RouteResolver scopedResolver(Network route, LocalNameResolver resolver) {
        int generation = resolver.generation();
        return host -> resolver.lookup(route, host, generation);
    }

    NetworkTransport(OkHttpClient client, ConnectionDiagnostics diagnostics) {
        this(client, diagnostics, null);
    }

    NetworkTransport(OkHttpClient client, ConnectionDiagnostics diagnostics, RouteResolver resolver) {
        this.diagnostics = diagnostics;
        OkHttpClient.Builder builder = client.newBuilder().proxy(Proxy.NO_PROXY)
            .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false);
        if (resolver != null) builder.dns(host -> {
            AddressCache.Lookup lookup = resolver.lookup(host);
            // Calls execute synchronously. Associate shared lookup results with the calling request.
            Label label = resolvingCall.get();
            if (label != null) label.lookup = lookup;
            return lookup.addresses;
        });
        if (diagnostics != null || resolver != null) builder.eventListenerFactory(call -> new okhttp3.EventListener() {
            private final Label label = call.request().tag(Label.class);
            private final long started = System.nanoTime();
            private final String operation = ConnectionDiagnostics.operation(call.request().url().encodedPath());
            private boolean resolving;

            private void failed(String stage, IOException error, InetSocketAddress address) {
                if (diagnostics != null) diagnostics.attempt(operation, stage,
                    (System.nanoTime() - started) / 1_000_000, error, label.id, address);
            }

            @Override public void dnsStart(Call call, String host) { resolving = true; }

            @Override public void dnsEnd(Call call, String host, List<InetAddress> addresses) {
                resolving = false;
                if (diagnostics != null) diagnostics.resolved(operation, label.id, addresses, label.lookup);
            }

            @Override public void connectFailed(Call call, InetSocketAddress address, Proxy proxy,
                    Protocol protocol, IOException error) {
                if (error instanceof ConnectException || error instanceof NoRouteToHostException
                        || error instanceof SocketTimeoutException) label.connectFailed = true;
                failed("connect", error, address);
            }

            @Override public void connectionAcquired(Call call, okhttp3.Connection connection) {
                label.acquired = true;
                if (diagnostics != null) diagnostics.peer(operation, label.id, connection.route().socketAddress());
            }

            @Override public void callFailed(Call call, IOException error) {
                if (resolving) failed("resolve", error, null);
                if (label.lookup != null && label.connectFailed && !label.acquired && !call.isCanceled()) {
                    if (label.lookup.discard() && diagnostics != null) diagnostics.discarded(operation,label.id);
                }
            }

            @Override public void requestFailed(Call call, IOException error) { failed("send", error, null); }
            @Override public void responseFailed(Call call, IOException error) { failed("receive", error, null); }
        });
        http=builder.build();
        // Shared pool/dispatcher/network/TLS policy; GET recovery only, within one call deadline.
        readHttp=http.newBuilder().retryOnConnectionFailure(true).build();
    }

    public HttpURLConnection open(URL url)throws IOException{
        if(closed.get())throw new IOException("Transport closed.");
        return new Connection(url,http,readHttp,diagnostics==null?0:diagnostics.nextCallId());
    }
    public long requestId(HttpURLConnection c){return c instanceof Connection?((Connection)c).id:0;}
    public long headersNanos(HttpURLConnection c){return c instanceof Connection?((Connection)c).headersAt:System.nanoTime();}
    public void release(HttpURLConnection c){if(c instanceof Connection)((Connection)c).release();else c.disconnect();}
    public void close(){if(!closed.compareAndSet(false,true))return;http.dispatcher().cancelAll();http.connectionPool().evictAll();http.dispatcher().executorService().shutdown();}
    private static RequestBody commandBody(byte[] bytes){return new RequestBody(){
        @Override public MediaType contentType(){return MediaType.get("application/json");}
        @Override public long contentLength(){return bytes.length;}
        @Override public boolean isOneShot(){return true;}
        @Override public void writeTo(okio.BufferedSink sink)throws IOException{sink.write(bytes);}
    };}
    private final class Connection extends HttpURLConnection {
        final OkHttpClient http,readHttp;final Map<String,String> headers=new LinkedHashMap<>();final ByteArrayOutputStream output=new ByteArrayOutputStream();
        volatile Call call;volatile boolean cancelled;Response response;long headersAt;final long id;
        Connection(URL url,OkHttpClient client,OkHttpClient reader,long id){super(url);http=client;readHttp=reader;this.id=id;}
        @Override public void setRequestProperty(String key,String value){if(connected)throw new IllegalStateException("Already connected");headers.put(key,value);}
        @Override public OutputStream getOutputStream(){return output;}
        @Override public void connect()throws IOException{
            if(cancelled)throw new IOException("Cancelled.");if(response!=null)return;
            Label label = new Label(id);
            Request.Builder request=new Request.Builder().url(url).tag(Label.class,label);for(Map.Entry<String,String> h:headers.entrySet())request.header(h.getKey(),h.getValue());
            if(method.equals("POST")){if(output.size()>64)throw new IOException("Request too large.");request.post(commandBody(output.toByteArray()));}
            else if(!method.equals("GET"))throw new IOException("Unsupported operation.");
            Call prepared=(method.equals("GET")?readHttp:http).newCall(request.build());call=prepared;if(cancelled){prepared.cancel();throw new IOException("Cancelled.");}
            if (diagnostics != null) diagnostics.destination(ConnectionDiagnostics.operation(prepared.request().url().encodedPath()),
                id, prepared.request().url().host(), prepared.request().url().port());
            resolvingCall.set(label);
            try {
                response=prepared.execute();headersAt=System.nanoTime();connected=true;
            } finally {
                resolvingCall.remove();
            }
            if(cancelled)throw new IOException("Cancelled.");
        }
        @Override public int getResponseCode()throws IOException{connect();return response.code();}
        @Override public long getContentLengthLong(){return response==null||response.body()==null?-1:response.body().contentLength();}
        @Override public InputStream getInputStream()throws IOException{connect();if(response.body()==null)throw new IOException("Missing response.");return response.body().byteStream();}
        @Override public InputStream getErrorStream(){try{return getInputStream();}catch(IOException e){return null;}}
        @Override public boolean usingProxy(){return false;}
        // Cancellation may run on the UI thread. Only the reading worker owns the body.
        void release(){Response owned=response;response=null;if(owned!=null)owned.close();}
        @Override public void disconnect(){cancelled=true;Call c=call;if(c!=null)c.cancel();}
    }
}

