package com.colonellp.ellamonitor;

import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Instant;

public class ApiClientTest {
    // Portable loopback fixture: Android's test boot classpath excludes JDK HttpServer.
    private static final class HttpServer {
        interface Handler { void handle(Exchange exchange) throws java.io.IOException; }
        private final java.net.ServerSocket socket;
        private final java.util.Map<String, Handler> handlers = new java.util.HashMap<>();
        private volatile boolean running;
        private final java.util.List<java.net.Socket> clients = new java.util.concurrent.CopyOnWriteArrayList<>();
        private HttpServer(InetSocketAddress address) throws java.io.IOException { socket = new java.net.ServerSocket(); socket.bind(address); }
        static HttpServer create(InetSocketAddress address, int backlog) throws java.io.IOException { return new HttpServer(address); }
        InetSocketAddress getAddress() { return (InetSocketAddress)socket.getLocalSocketAddress(); }
        void createContext(String path, Handler handler) { handlers.put(path, handler); }
        void start() {
            running = true; Thread thread = new Thread(() -> {
                while (running) try {
                    java.net.Socket client = socket.accept(); clients.add(client); client.setSoTimeout(5000);
                    try { Exchange exchange = new Exchange(client); handlers.get(exchange.getRequestURI().getPath()).handle(exchange); }
                    finally { client.close(); clients.remove(client); }
                } catch (java.io.IOException ignored) { }
            }); thread.setDaemon(true); thread.start();
        }
        void stop(int delay) { running = false; try { socket.close(); } catch (Exception ignored) { } for (java.net.Socket c : clients) try { c.close(); } catch (Exception ignored) { } }
    }
    private static final class Headers extends java.util.HashMap<String, String> {
        void set(String key, String value) { put(key.toLowerCase(java.util.Locale.ROOT), value); }
        String getFirst(String key) { return get(key.toLowerCase(java.util.Locale.ROOT)); }
    }
    private static final class Exchange {
        private final java.net.Socket socket;
        private final String method;
        private final java.net.URI uri;
        private final Headers request = new Headers(), response = new Headers();
        Exchange(java.net.Socket socket) throws java.io.IOException {
            this.socket = socket; java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String[] line = reader.readLine().split(" "); method = line[0]; uri = java.net.URI.create(line[1]);
            String header; while ((header = reader.readLine()) != null && !header.isEmpty()) { int colon = header.indexOf(':'); request.set(header.substring(0, colon), header.substring(colon + 1).trim()); }
        }
        String getRequestMethod() { return method; }
        java.net.URI getRequestURI() { return uri; }
        Headers getRequestHeaders() { return request; }
        Headers getResponseHeaders() { return response; }
        void sendResponseHeaders(int status, long length) throws java.io.IOException {
            StringBuilder head = new StringBuilder("HTTP/1.1 " + status + " Fixture\r\nContent-Length: " + Math.max(0, length) + "\r\nConnection: close\r\n");
            response.forEach((k, v) -> head.append(k).append(": ").append(v).append("\r\n")); head.append("\r\n"); getResponseBody().write(head.toString().getBytes(StandardCharsets.UTF_8));
        }
        java.io.OutputStream getResponseBody() throws java.io.IOException { return socket.getOutputStream(); }
        void close() throws java.io.IOException { socket.close(); }
    }
    private static final String TOKEN = new String(new char[64]).replace('\0', 'a');
    private ApiClient client(HttpServer server) { return new ApiClient("http://127.0.0.1:" + server.getAddress().getPort(), TOKEN, url -> (HttpURLConnection)url.openConnection()); }
    @Test public void authenticationIsHeaderOnlyAndRedirectsAreRejected() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0); AtomicInteger redirected = new AtomicInteger();
        server.createContext("/api/v1/live", exchange -> {
            assertEquals("GET", exchange.getRequestMethod()); assertEquals("Bearer " + TOKEN, exchange.getRequestHeaders().getFirst("Authorization")); assertFalse(exchange.getRequestURI().toString().contains(TOKEN));
            exchange.getResponseHeaders().set("Location", "/redirected"); exchange.sendResponseHeaders(302, -1); exchange.close();
        });
        server.createContext("/redirected", exchange -> { redirected.incrementAndGet(); exchange.sendResponseHeaders(200, -1); exchange.close(); }); server.start();
        try { try { client(server).get("/api/v1/live"); fail(); } catch (Exception e) { assertFalse(e.toString().contains(TOKEN)); } assertEquals(0, redirected.get()); } finally { server.stop(0); }
    }
    @Test public void oversizedResponseAndUnsupportedVersionAreRejected() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/big", exchange -> { exchange.sendResponseHeaders(200, 4 * 1024 * 1024 + 1); exchange.close(); });
        server.createContext("/api/v1/version", exchange -> { byte[] b = "{\"apiVersion\":2}".getBytes(StandardCharsets.UTF_8); exchange.sendResponseHeaders(200, b.length); exchange.getResponseBody().write(b); exchange.close(); }); server.start();
        try { for (String path : new String[]{"big", "version"}) { try { client(server).get("/api/v1/" + path); fail(); } catch (Exception expected) { assertFalse(expected.toString().contains(TOKEN)); } } } finally { server.stop(0); }
    }
    @Test public void historyFetchesAllPagesFor24HoursWithoutInventingOrDuplicatingRows() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0); AtomicInteger requests = new AtomicInteger();
        Instant start = Instant.parse("2026-01-01T00:00:00Z"), end = start.plusSeconds(86400);
        server.createContext("/api/v1/history/metrics/bms-test", exchange -> {
            try {
                int page = requests.getAndIncrement(); int from = page == 0 ? 0 : 1000, to = page == 0 ? 1000 : 1440;
                JSONArray rows = new JSONArray(); for (int i = from; i < to; i++) rows.put(new JSONObject().put("start", start.plusSeconds(i * 60L).toString()).put("end", start.plusSeconds((i + 1) * 60L).toString()).put("values", new JSONArray().put(0).put(0).put(12.4).put(50)));
                JSONObject body = new JSONObject().put("apiVersion", 1).put("schemaVersion", 2).put("metric", new JSONObject().put("id", "bms-test")).put("rows", rows).put("next", page == 0 ? new JSONObject().put("from", start.plusSeconds(60000).toString()).put("to", end.toString()).put("resolution", "minute") : JSONObject.NULL);
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8); exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes);
            } catch (Exception e) { throw new java.io.IOException(e); } finally { exchange.close(); }
        }); server.start();
        try {
            MonitorData.Metric metric = new MonitorData.Metric(ContractTest.metric("bms-test", "sbms", "electrical", "battery", "sbmsBattery", 0));
            HistoryData.Result r = HistoryData.fetch(client(server), metric, new HistoryData.Window(start, end, "minute"));
            assertEquals(1440, r.rows.size()); assertEquals(2, requests.get()); assertEquals(0, r.rows.get(1439).value("watts", "electrical"), 0);
            String csv = HistoryData.csv(r, "Battery label"); assertEquals(1441, csv.split("\r\n").length); assertTrue(csv.contains("\"bms-test\"")); assertFalse(csv.contains(TOKEN));
        } finally { server.stop(0); }
    }
    @Test public void malformedPaginationCannotLoopOrChangeTheRange() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/history/metrics/bms-test", exchange -> {
            String body = "{\"apiVersion\":1,\"schemaVersion\":2,\"metric\":{\"id\":\"bms-test\"},\"rows\":[],\"next\":{\"from\":\"2026-01-01T00:00:00Z\",\"to\":\"2026-01-02T00:00:00Z\",\"resolution\":\"minute\"}}";
            byte[] b = body.getBytes(StandardCharsets.UTF_8); exchange.sendResponseHeaders(200, b.length); exchange.getResponseBody().write(b); exchange.close();
        }); server.start();
        try { try { HistoryData.fetch(client(server), new MonitorData.Metric(ContractTest.metric("bms-test", "sbms", "electrical", "battery", "sbmsBattery", 0)), new HistoryData.Window(Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"), "minute")); fail(); } catch (Exception expected) { assertTrue(expected.getMessage().contains("continuation")); } } finally { server.stop(0); }
    }
}
