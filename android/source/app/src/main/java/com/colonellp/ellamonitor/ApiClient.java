package com.colonellp.ellamonitor;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Bounded client; the only write is the allowlisted Pi Wi-Fi mode action. */
final class ApiClient {
    interface Connections { HttpURLConnection open(URL url) throws IOException; default long requestId(HttpURLConnection c){return 0;} default long headersNanos(HttpURLConnection c){return System.nanoTime();} default void release(HttpURLConnection c){c.disconnect();} default void close(){} }
    static final class Failure extends IOException {final int status;Failure(int s,String m){super(m);status=s;}}
    private final String base, token;
    private final Connections connections;
    private final ConnectionDiagnostics diagnostics;
    private volatile HttpURLConnection active;
    private volatile boolean cancelled;
    private volatile long responseHeadersNanos;
    long responseAgeMillis(){return responseHeadersNanos==0?Long.MAX_VALUE:Math.max(0,(System.nanoTime()-responseHeadersNanos)/1_000_000);}
    ApiClient(String base, String token, Connections connections) {
        this(base,token,connections,null);
    }
    ApiClient(String base,String token,Connections connections,ConnectionDiagnostics diagnostics){
        this.base = validateBase(base); this.token = token; this.connections = connections;
        this.diagnostics=diagnostics;
        if (!token.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Enter the 64-character API token.");
    }
    static String validateBase(String input) {
        try {
            URI uri = new URI(input.trim());
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))
                    || uri.getPort() == 0 || uri.getPort() > 65535) throw new Exception();
            return uri.toString().replaceAll("/$", "");
        } catch (Exception e) { throw new IllegalArgumentException("Use http://PI_HOST:8080 or an HTTPS origin, without a path or credentials."); }
    }
    JSONObject get(String path) throws Exception {
        return request(path,null);
    }
    JSONObject mode(String mode)throws Exception{
        if(!mode.equals("auto")&&!mode.equals("home")&&!mode.equals("m1"))throw new IllegalArgumentException("Invalid Pi mode.");
        return request("/api/v1/pi/wifi",new JSONObject().put("mode",mode).toString());
    }
    private JSONObject request(String path,String payload)throws Exception{
        if (cancelled) throw new IOException("Request cancelled.");
        if (!path.startsWith("/api/v1/") || path.contains("#") || path.contains("\r") || path.contains("\n")) throw new IOException("Invalid API route.");
        long started=System.nanoTime(),callId=0;String stage="open";int status=0;HttpURLConnection c=null;
        try {
            c=connections.open(new URL(base+path));callId=connections.requestId(c);active=c;
            if (cancelled) throw new IOException("Request cancelled.");
            c.setInstanceFollowRedirects(false); c.setConnectTimeout(4000); c.setReadTimeout(4000);
            c.setRequestMethod(payload==null?"GET":"POST"); c.setUseCaches(false);
            c.setRequestProperty("Authorization", "Bearer " + token);
            c.setRequestProperty("Accept", "application/json");
            if(payload!=null){byte[] data=payload.getBytes(StandardCharsets.UTF_8);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Content-Length",Integer.toString(data.length));try(java.io.OutputStream out=c.getOutputStream()){out.write(data);}}
            stage="headers";status = c.getResponseCode();
            long headersAt=connections.headersNanos(c);
            if (status != 200 && !(payload!=null&&status==202)) throw new Failure(status,status == 401 ? "API token rejected." : status == 403 ? "Pi Wi-Fi control needs the one-time API control setup." : status == 404 ? "Update the Pi collector to enable Pi status/API controls." : status == 429 ? "API busy; retry later." : "API request failed (" + status + ").");
            if (c.getContentLengthLong() > 4 * 1024 * 1024) throw new IOException("API response too large.");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            stage="body";
            long deadline = System.nanoTime() + 8_000_000_000L;
            try (InputStream in = c.getInputStream()) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) {
                    if (bytes.size() + count > 4 * 1024 * 1024 || System.nanoTime() > deadline) throw new IOException("API response limit exceeded.");
                    bytes.write(buffer, 0, count);
                }
            }
            JSONObject body;
            stage="json";
            try { body = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8)); }
            catch (Exception e) { throw new IOException("Invalid API response."); }
            if (!(body.opt("apiVersion") instanceof Number) || body.optInt("apiVersion") != 1) throw new IOException("Unsupported API version.");
            responseHeadersNanos=headersAt;
            if(diagnostics!=null)diagnostics.request(ConnectionDiagnostics.operation(path),"complete",(System.nanoTime()-started)/1_000_000,status,null,callId);
            return body;
        } catch(Exception e){if(diagnostics!=null)diagnostics.request(ConnectionDiagnostics.operation(path),cancelled?"cancelled":stage,(System.nanoTime()-started)/1_000_000,status,e,callId);throw e;
        } finally { active = null; if(c!=null)connections.release(c); }
    }
    void cancel() { cancelled = true; HttpURLConnection c = active; if (c != null) c.disconnect(); connections.close(); }
}

