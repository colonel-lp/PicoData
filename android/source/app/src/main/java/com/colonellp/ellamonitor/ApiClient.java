package com.colonellp.ellamonitor;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Bounded GET-only client. No token in a URL, redirect, log or exception. */
final class ApiClient {
    interface Connections { HttpURLConnection open(URL url) throws IOException; }
    private final String base, token;
    private final Connections connections;
    private volatile HttpURLConnection active;
    ApiClient(String base, String token, Connections connections) {
        this.base = validateBase(base); this.token = token; this.connections = connections;
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
        if (!path.startsWith("/api/v1/") || path.contains("#") || path.contains("\r") || path.contains("\n")) throw new IOException("Invalid API route.");
        HttpURLConnection c = connections.open(new URL(base + path)); active = c;
        try {
            c.setInstanceFollowRedirects(false); c.setConnectTimeout(4000); c.setReadTimeout(4000);
            c.setRequestMethod("GET"); c.setUseCaches(false);
            c.setRequestProperty("Authorization", "Bearer " + token);
            c.setRequestProperty("Accept", "application/json");
            int status = c.getResponseCode();
            if (status != 200) throw new IOException(status == 401 ? "API token rejected." : status == 429 ? "API busy; retrying." : "API request failed (" + status + ").");
            if (c.getContentLengthLong() > 4 * 1024 * 1024) throw new IOException("API response too large.");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            long deadline = System.nanoTime() + 8_000_000_000L;
            try (InputStream in = c.getInputStream()) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) {
                    if (bytes.size() + count > 4 * 1024 * 1024 || System.nanoTime() > deadline) throw new IOException("API response limit exceeded.");
                    bytes.write(buffer, 0, count);
                }
            }
            JSONObject body;
            try { body = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8)); }
            catch (Exception e) { throw new IOException("Invalid API response."); }
            if (!(body.opt("apiVersion") instanceof Number) || body.optInt("apiVersion") != 1) throw new IOException("Unsupported API version.");
            return body;
        } finally { active = null; c.disconnect(); }
    }
    void cancel() { HttpURLConnection c = active; if (c != null) c.disconnect(); }
}
