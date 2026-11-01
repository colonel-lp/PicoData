package com.colonellp.ellamonitor;

import android.net.Network;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

/** Network-scoped DNS with standard HTTP parsing and TLS host verification. */
final class NetworkTransport implements ApiClient.Connections {
    private final OkHttpClient http;
    NetworkTransport(Network route,LocalNameResolver resolver){
        http=new OkHttpClient.Builder().socketFactory(route.getSocketFactory()).dns(host->resolver.lookup(route,host))
            .proxy(Proxy.NO_PROXY).followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .connectTimeout(4,TimeUnit.SECONDS).readTimeout(4,TimeUnit.SECONDS).callTimeout(9,TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(1,2,TimeUnit.SECONDS)).build();
    }
    public HttpURLConnection open(URL url){return new Connection(url,http);}
    public void close(){http.dispatcher().cancelAll();http.connectionPool().evictAll();http.dispatcher().executorService().shutdown();}
    private static final class Connection extends HttpURLConnection {
        final OkHttpClient http;final Map<String,String> headers=new LinkedHashMap<>();final ByteArrayOutputStream output=new ByteArrayOutputStream();
        volatile Call call;volatile boolean cancelled;Response response;
        Connection(URL url,OkHttpClient client){super(url);http=client;}
        @Override public void setRequestProperty(String key,String value){if(connected)throw new IllegalStateException("Already connected");headers.put(key,value);}
        @Override public OutputStream getOutputStream(){return output;}
        @Override public void connect()throws IOException{
            if(cancelled)throw new IOException("Cancelled.");if(response!=null)return;
            Request.Builder request=new Request.Builder().url(url);for(Map.Entry<String,String> h:headers.entrySet())request.header(h.getKey(),h.getValue());
            if(method.equals("POST")){if(output.size()>64)throw new IOException("Request too large.");request.post(RequestBody.create(output.toByteArray(),MediaType.get("application/json")));}
            else if(!method.equals("GET"))throw new IOException("Unsupported operation.");
            Call prepared=http.newCall(request.build());call=prepared;if(cancelled){prepared.cancel();throw new IOException("Cancelled.");}
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
