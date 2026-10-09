package com.colonellp.ellamonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final String LAN_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService liveWorker = Executors.newSingleThreadExecutor(), historyWorker = Executors.newSingleThreadExecutor();
    private PrivateSettings settings;
    private ScreenControl screen;
    private Palette palette;
    private LinearLayout root, content, dashboard;
    private TextView status, historyNote;
    private final Map<String, Tile> tiles = new LinkedHashMap<>();
    private final Map<String, String> headings = new LinkedHashMap<>();
    private Map<String, MonitorData.Datum> readings = new LinkedHashMap<>();
    private List<MonitorData.Metric> metrics = new ArrayList<>();
    private JSONObject live, collectorStatus;
    private long receivedMono, piNowAt, piNowMono;
    private volatile ApiClient liveClient, historyClient, summaryClient;
    private volatile boolean active;
    private boolean historyScreen, defined, bars, fullscreen, keepScreen, localClock, historyLoading;
    private volatile int epoch, historyEpoch;
    private int polls, range, offset;
    private String connectionState = "Set the Pi address and API token in Settings.", quantity = "watts", selectedMetric = "";
    private HistoryData.Result history;
    private ChartView chart;
    private byte[] exportBytes;
    private final Runnable poll = this::poll;
    private final Runnable expire = new Runnable() {
        @Override public void run() { if (!active) return; updateLive(); handler.postDelayed(this, 250); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); settings = new PrivateSettings(this); screen = new ScreenControl(this, settings.prefs);
        fullscreen = settings.prefs.getBoolean("fullscreen", false); keepScreen = settings.prefs.getBoolean("keepScreen", false);
        defined = settings.prefs.getBoolean("defined", false); localClock = settings.prefs.getBoolean("localClock", false);
        range = Math.max(0, Math.min(4, settings.prefs.getInt("range", 0))); selectedMetric = settings.prefs.getString("metric", "");
        if (state != null) { historyScreen = state.getBoolean("history"); quantity = state.getString("quantity", "watts"); offset = state.getInt("offset"); }
        build();
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putBoolean("history", historyScreen); out.putString("quantity", quantity); out.putInt("offset", offset); }
    @Override protected void onStart() { super.onStart(); active = true; screen.start(keepScreen); windowMode(); restartConnection(); handler.post(expire); }
    @Override protected void onResume() { super.onResume(); screen.apply(); windowMode(); }
    @Override public void onWindowFocusChanged(boolean focus) { super.onWindowFocusChanged(focus); if (focus && screen != null) { screen.apply(); windowMode(); } }
    @Override protected void onStop() {
        active = false; epoch++; historyEpoch++; handler.removeCallbacks(poll); handler.removeCallbacks(expire);
        if (liveClient != null) liveClient.cancel(); if (historyClient != null) historyClient.cancel(); if (summaryClient != null) summaryClient.cancel();
        live = null; history = null; historyLoading = false; screen.stop(); super.onStop();
    }
    @Override protected void onDestroy() { liveWorker.shutdownNow(); historyWorker.shutdownNow(); super.onDestroy(); }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private TextView text(String value, float size, int color) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setPadding(dp(8), dp(5), dp(8), dp(5)); return view;
    }
    private GradientDrawable background(boolean selected) {
        GradientDrawable d = new GradientDrawable(); d.setColor(palette.panel); d.setCornerRadius(dp(8)); d.setStroke(dp(selected ? 2 : 1), selected ? palette.accent : palette.border); return d;
    }
    private Button button(String name, boolean selected, Runnable action) {
        Button b = new Button(this); b.setText(name); b.setAllCaps(false); b.setTextSize(13); b.setTextColor(selected ? palette.accent : palette.text);
        b.setMinWidth(dp(48)); b.setMinimumHeight(dp(48)); b.setPadding(dp(12), dp(4), dp(12), dp(4)); b.setBackground(background(selected));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(48)); p.setMargins(dp(3), dp(3), dp(3), dp(3)); b.setLayoutParams(p);
        b.setOnClickListener(v -> action.run()); return b;
    }
    private void build() {
        palette = new Palette(settings.prefs.getBoolean("light", false)); root = column(); root.setBackgroundColor(palette.background);
        status = text(connectionState, 12, palette.muted); status.setMaxLines(2); status.setMinHeight(dp(38)); status.setContentDescription("Connection and freshness status"); root.addView(status);
        content = column(); root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        HorizontalScrollView bottom = new HorizontalScrollView(this); bottom.setFillViewport(true); bottom.setHorizontalScrollBarEnabled(false);
        LinearLayout controls = row(); bottom.addView(controls); root.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        controls.addView(button("Main", !historyScreen, () -> { historyScreen = false; historyEpoch++; build(); }));
        for (int i = 0; i < HistoryData.RANGES.length; i++) { final int n = i; controls.addView(button(HistoryData.RANGES[i], historyScreen && range == i, () -> { historyScreen = true; range = n; offset = 0; settings.prefs.edit().putInt("range", range).apply(); build(); loadHistory(); })); }
        controls.addView(button("Settings", false, this::showSettings));
        controls.addView(button(fullscreen ? "Window" : "Full screen", fullscreen, () -> { fullscreen = !fullscreen; settings.prefs.edit().putBoolean("fullscreen", fullscreen).apply(); windowMode(); build(); }));
        controls.addView(button("Keep screen", keepScreen, () -> { keepScreen = !keepScreen; settings.prefs.edit().putBoolean("keepScreen", keepScreen).apply(); screen.setKeep(keepScreen); build(); }));
        controls.addView(button("Theme", false, () -> { settings.prefs.edit().putBoolean("light", !settings.prefs.getBoolean("light", false)).apply(); build(); }));
        setContentView(root);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            } else view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
        if (historyScreen) buildHistory(); else buildDashboard();
        screen.apply(); windowMode(); updateLive();
    }
    @SuppressWarnings("deprecation") private void windowMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) { c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE); if (fullscreen) c.hide(WindowInsets.Type.systemBars()); else c.show(WindowInsets.Type.systemBars()); }
        } else getWindow().getDecorView().setSystemUiVisibility(fullscreen ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE : View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    private LinearLayout group(String key, String label) {
        headings.put(key, label); LinearLayout g = column(); g.setBackground(background(false)); g.setPadding(dp(3), dp(3), dp(3), dp(3));
        TextView title = text(settings.label("group:" + key, label), 14, palette.accent); title.setTypeface(null, Typeface.BOLD); title.setSingleLine(); title.setEllipsize(TextUtils.TruncateAt.END);
        title.setOnLongClickListener(v -> { editLabel("group:" + key, label); return true; }); g.addView(title); return g;
    }
    private void weighted(LinearLayout parent, View child, float weight) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, weight); p.setMargins(dp(3), dp(3), dp(3), dp(3)); parent.addView(child, p);
    }
    private MonitorData.Datum find(String channel, String q) { for (MonitorData.Datum d : readings.values()) if (d.metric != null && d.metric.channel.equals(channel) && d.quantity.equals(q)) return d; return null; }
    private void tile(LinearLayout parent, MonitorData.Datum datum, String fallbackId, String fallback, String unit, boolean gauge, boolean weight) {
        String id = datum == null ? fallbackId : datum.id, name = datum == null ? fallback : datum.name;
        Tile tile = new Tile(id, name, datum == null ? unit : datum.unit, gauge); tiles.put(id, tile);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(weight ? 0 : -1, gauge ? dp(145) : dp(49), weight ? 1 : 0);
        p.setMargins(dp(2), dp(2), dp(2), dp(2)); parent.addView(tile.view, p); tile.update(datum);
    }
    private void buildDashboard() {
        content.removeAllViews(); tiles.clear(); headings.clear();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); dashboard = column(); scroll.addView(dashboard); content.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        LinearLayout loads = group("loads", "Current draw"), centre = group("monitor", "Battery monitoring"), environment = group("environment", "Environment & tanks"), temps = group("temps", "Temperatures");
        for (MonitorData.Datum d : readings.values()) if (d.group.equals("loads") && d.quantity.equals("current")) tile(loads, d, d.id, d.name, d.unit, false, false);
        if (loads.getChildCount() == 1) loads.addView(text("Selected load shunts appear after connection.", 13, palette.muted));
        for (int r = 0; r < 2; r++) { LinearLayout flags = row(); centre.addView(flags); for (String f : r == 0 ? new String[]{"CFET", "OVLK", "EOC", "LVC"} : new String[]{"DFET", "UVLK", "IOT", "CELF"}) tile(flags, readings.get("flag:" + f), "flag:" + f, f, "", false, true); }
        LinearLayout top = row(), lower = row(); centre.addView(top); centre.addView(lower);
        tile(top, find("externalLoad", "current"), "pending:load-sbms", "Load Σ · SBMS", "A", true, true);
        tile(top, find("pv1", "current"), "pending:pv1", "PV1 · SBMS", "A", true, true);
        tile(top, find("sbmsBattery", "current"), "pending:battery-sbms", "Battery · SBMS", "A", true, true);
        tile(top, find("sbmsBattery", "stateOfCharge"), "pending:soc-sbms", "SOC · SBMS", "%", true, true);
        tile(lower, readings.get("pico:load-sum"), "pico:load-sum", "Load Σ · Pico", "A", true, true);
        tile(lower, find("pv2", "current"), "pending:pv2", "PV2 · SBMS", "A", true, true);
        tile(lower, find("picoBattery", "current"), "pending:battery-pico", "Battery · Pico", "A", true, true);
        tile(lower, find("picoBattery", "stateOfCharge"), "pending:soc-pico", "SOC · Pico", "%", true, true);
        List<MonitorData.Datum> env = new ArrayList<>();
        for (MonitorData.Datum d : readings.values()) { if (d.group.equals("environment")) env.add(d); if (d.group.equals("temps")) tile(temps, d, d.id, d.name, d.unit, false, false); }
        for (int i = 0; i < env.size(); i += 2) { LinearLayout line = row(); environment.addView(line); for (int j = i; j < Math.min(i + 2, env.size()); j++) { MonitorData.Datum d = env.get(j); tile(line, d, d.id, d.name, d.unit, !d.unit.equals("L"), true); } }
        if (env.isEmpty()) environment.addView(text("Pressure, tanks and inclination appear when Pico data is available.", 13, palette.muted));
        if (temps.getChildCount() == 1) temps.addView(text("Waiting for temperatures", 13, palette.muted));
        if (getResources().getConfiguration().screenWidthDp >= 800) {
            LinearLayout main = row(); main.setGravity(Gravity.TOP); dashboard.addView(main);
            weighted(main, loads, 17); weighted(main, centre, 45); weighted(main, environment, 23); weighted(main, temps, 15);
        } else { dashboard.addView(centre); dashboard.addView(loads); dashboard.addView(environment); dashboard.addView(temps); }
        LinearLayout summary = group("summary", "Battery details"); dashboard.addView(summary);
        List<MonitorData.Datum> details = new ArrayList<>();
        MonitorData.Datum voltage = find("sbmsBattery", "voltage"), secondary = find("secondaryVoltage", "voltage"); if (voltage != null) details.add(voltage); if (secondary != null) details.add(secondary);
        for (MonitorData.Datum d : readings.values()) if (d.group.equals("summary") || d.group.equals("cells")) details.add(d);
        int columns = getResources().getConfiguration().screenWidthDp >= 800 ? 6 : 2;
        for (int i = 0; i < details.size(); i += columns) { LinearLayout line = row(); summary.addView(line); for (int j = i; j < Math.min(i + columns, details.size()); j++) { MonitorData.Datum d = details.get(j); tile(line, d, d.id, d.name, d.unit, false, true); } }
    }
    private final class Tile {
        final String id, original, unit;
        final LinearLayout view;
        final TextView label, number;
        final GaugeView gauge;
        String lastText;
        Tile(String id, String original, String unit, boolean isGauge) {
            this.id = id; this.original = original; this.unit = unit; view = column(); view.setGravity(Gravity.CENTER); view.setBackground(background(false));
            label = text(settings.label(id, original), 11, palette.muted); label.setSingleLine(); label.setEllipsize(TextUtils.TruncateAt.END); label.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); view.addView(label, new LinearLayout.LayoutParams(-1, -2));
            if (isGauge) { gauge = new GaugeView(MainActivity.this, palette, unit); number = null; view.addView(gauge, new LinearLayout.LayoutParams(-1, 0, 1)); }
            else { gauge = null; number = text("—", 14, palette.text); number.setTypeface(null, Typeface.BOLD); number.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); number.setSingleLine(); number.setAutoSizeTextTypeUniformWithConfiguration(9, 14, 1, android.util.TypedValue.COMPLEX_UNIT_SP); view.addView(number, new LinearLayout.LayoutParams(-1, 0, 1)); }
            view.setFocusable(true); view.setOnClickListener(v -> showDetail(readings.get(id), original)); view.setOnLongClickListener(v -> { editLabel(id, original); return true; });
        }
        void update(MonitorData.Datum d) {
            String value = d == null ? "Unavailable" : formatted(d);
            if (gauge != null) gauge.value(d == null ? null : d.value);
            if (value.equals(lastText)) return; lastText = value;
            if (number != null) { number.setText(value); boolean fault = id.equals("flag:OVLK") || id.equals("flag:UVLK") || id.equals("flag:IOT") || id.equals("flag:LVC") || id.equals("flag:CELF"); number.setTextColor(d != null && d.flag != null ? d.flag ? fault ? palette.negative : palette.positive : palette.muted : d != null && d.value != null ? palette.text : palette.muted); }
            view.setContentDescription(settings.label(id, original) + ", " + value);
        }
    }
    private String formatted(MonitorData.Datum d) {
        if (d.group.equals("flags")) return d.flag == null ? "Unavailable" : d.flag ? "● On" : "○ Off";
        if (d.value == null) return "—";
        if (d.unit.equals("s")) { long seconds = Math.max(0, d.value.longValue()); return String.format(Locale.UK, "%dd %dh", seconds / 86400, seconds / 3600 % 24); }
        return String.format(Locale.UK, d.unit.equals("hPa") || d.unit.equals("mV") || d.unit.equals("%") ? "%.0f %s" : d.unit.equals("V") ? "%.3f %s" : d.unit.equals("°C") ? "%.1f %s" : "%.2f %s", d.value, d.unit);
    }
    private void updateLive() {
        if (settings == null || status == null) return;
        Map<String, MonitorData.Datum> next = MonitorData.display(metrics, live, live == null ? 0 : (SystemClock.elapsedRealtime() - receivedMono) / 1000d);
        // Retain metadata/layout when source snapshots disappear, but never retain their current values.
        boolean havePicoMetadata = MonitorData.object(live, "pico").optJSONObject("readings") != null;
        for (MonitorData.Datum old : readings.values()) if (!havePicoMetadata && !next.containsKey(old.id) && old.metric == null && old.id.startsWith("raw:")) next.put(old.id, new MonitorData.Datum(old.id, old.name, old.unit, old.group, old.quantity, null, null, null, old.receivedAt));
        boolean changed = !next.keySet().equals(readings.keySet()); readings = next;
        if (!historyScreen) { if (changed) buildDashboard(); else for (Tile t : tiles.values()) t.update(readings.get(t.id)); }
        if (live == null) status.setText(connectionState);
        else {
            double elapsed = (SystemClock.elapsedRealtime() - receivedMono) / 1000d;
            String logging = MonitorData.object(collectorStatus, "logging").optString("state", "unknown");
            status.setText("Pi connected   ·   Pico " + (MonitorData.fresh(MonitorData.object(live, "pico"), elapsed, 2) ? "live" : "unavailable") + "   ·   SBMS " + (MonitorData.fresh(MonitorData.object(live, "sbms"), elapsed, 3) ? "live" : "unavailable") + "   ·   logging " + logging + "   ·   " + clock(now()));
        }
    }
    private Instant now() { return piNowAt > 0 ? Instant.ofEpochMilli(piNowAt + SystemClock.elapsedRealtime() - piNowMono) : Instant.now(); }
    private String clock(Instant at) { return DateTimeFormatter.ofPattern("dd MMM HH:mm:ss").withZone(localClock ? ZoneId.systemDefault() : ZoneOffset.UTC).format(at) + (localClock ? " local" : " UTC"); }

    private HttpURLConnection open(URL url) throws java.io.IOException {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if (manager != null) for (Network network : manager.getAllNetworks()) {
            NetworkCapabilities cap = manager.getNetworkCapabilities(network);
            if (cap != null && (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) return (HttpURLConnection)network.openConnection(url);
        }
        throw new java.io.IOException("Join the Pi's Wi-Fi network.");
    }
    private ApiClient client() throws Exception { return new ApiClient(settings.prefs.getString("address", ""), settings.token(), this::open); }
    private boolean allowed() { return Build.VERSION.SDK_INT < 37 || checkSelfPermission(LAN_PERMISSION) == PackageManager.PERMISSION_GRANTED; }
    private void restartConnection() {
        epoch++; historyEpoch++; polls = 0; live = null; collectorStatus = null; history = null; historyLoading = false; piNowAt = 0; metrics = new ArrayList<>(); readings = new LinkedHashMap<>();
        handler.removeCallbacks(poll); if (liveClient != null) liveClient.cancel(); if (historyClient != null) historyClient.cancel();
        if (!active) return;
        if (!allowed()) { connectionState = "Local network permission is needed. Open Settings to connect."; updateLive(); return; }
        connectionState = "Connecting to Pi…"; liveClient = null; build();
        final int generation = epoch;
        liveWorker.execute(() -> {
            try {
                ApiClient prepared = client();
                handler.post(() -> { if (active && generation == epoch) { liveClient = prepared; handler.post(poll); } });
            } catch (Exception e) { handler.post(() -> { if (active && generation == epoch) { connectionState = "Set the Pi address and API token in Settings."; updateLive(); } }); }
        });
    }
    private void poll() {
        if (!active || liveClient == null) return;
        final int generation = epoch; final ApiClient client = liveClient;
        liveWorker.execute(() -> {
            try {
                List<MonitorData.Metric> catalogue = polls % 30 == 0 ? MonitorData.catalogue(client.get("/api/v1/metrics")) : null;
                JSONObject health = polls % 30 == 0 ? client.get("/api/v1/status") : null;
                long requestAt = SystemClock.elapsedRealtime(); JSONObject body = client.get("/api/v1/live");
                long clockAt = Instant.parse(body.getString("now")).toEpochMilli();
                handler.post(() -> {
                    if (!active || generation != epoch) return;
                    if (catalogue != null) metrics = catalogue; if (health != null) collectorStatus = health; live = body; receivedMono = requestAt;
                    piNowAt = clockAt; piNowMono = SystemClock.elapsedRealtime(); connectionState = "Pi connected"; polls++;
                    updateLive(); if (historyScreen && !historyLoading && (polls == 1 || polls % 30 == 0)) { buildHistory(); loadHistory(); }
                    handler.postDelayed(poll, 1000);
                });
            } catch (Exception e) {
                handler.post(() -> { if (!active || generation != epoch) return; live = null; connectionState = "Pi unavailable. Check Wi-Fi, address, token and TLS trust. Retrying…"; updateLive(); handler.postDelayed(poll, 3000); });
            }
        });
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) { super.onRequestPermissionsResult(requestCode, permissions, results); if (requestCode == 37) restartConnection(); }

    private MonitorData.Metric selected() {
        for (MonitorData.Metric m : metrics) if (m.id.equals(selectedMetric)) return m;
        for (MonitorData.Metric m : metrics) if (m.channel.equals("sbmsBattery")) { selectedMetric = m.id; return m; }
        if (!metrics.isEmpty()) { selectedMetric = metrics.get(0).id; return metrics.get(0); } return null;
    }
    private String metricLabel(MonitorData.Metric m) { return settings.label(m.alias + ":" + quantityFor(m), m.name + " · " + MonitorData.fieldName(quantityFor(m))); }
    private String quantityFor(MonitorData.Metric m) { return m.kind.equals("electrical") ? quantity.equals("Wh") ? "watts" : quantity.equals("Ah") ? "current" : quantity : m.kind.equals("barometer") ? "pressure" : m.kind; }
    private String unit(MonitorData.Metric m) { return m.kind.equals("electrical") ? quantity.equals("watts") ? "W" : quantity.equals("current") ? "A" : quantity.equals("voltage") ? "V" : quantity.equals("stateOfCharge") ? "%" : quantity : m.kind.equals("barometer") ? "hPa" : m.kind.equals("temperature") ? "°C" : "V"; }
    private void buildHistory() {
        content.removeAllViews(); MonitorData.Metric m = selected();
        LinearLayout controls = row(); HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.addView(controls); content.addView(scroll);
        controls.addView(button(m == null ? "Select measurement" : metricLabel(m), true, this::selectMetric));
        controls.addView(button(defined ? "Defined UTC" : "Rolling", defined, () -> { defined = !defined; offset = 0; settings.prefs.edit().putBoolean("defined", defined).apply(); history = null; buildHistory(); loadHistory(); }));
        controls.addView(button("‹ Previous", false, () -> { offset--; loadHistory(); }));
        controls.addView(button("Now", offset == 0, () -> { offset = 0; loadHistory(); }));
        controls.addView(button("Next ›", false, () -> { if (offset < 0) offset++; loadHistory(); }));
        controls.addView(button("Refresh", false, this::loadHistory));
        if (m != null && m.kind.equals("electrical")) {
            LinearLayout quantities = row(); HorizontalScrollView qscroll = new HorizontalScrollView(this); qscroll.addView(quantities); content.addView(qscroll);
            for (String q : m.role.equals("battery") ? new String[]{"watts", "current", "voltage", "stateOfCharge", "Wh", "Ah"} : new String[]{"watts", "current", "voltage", "Wh", "Ah"}) quantities.addView(button(q.equals("stateOfCharge") ? "SOC" : q.equals("watts") ? "W" : q.equals("current") ? "A" : q.equals("voltage") ? "V" : q, quantity.equals(q), () -> { quantity = q; if (!(q.equals("Wh") || q.equals("Ah"))) bars = false; buildHistory(); }));
            if (!quantity.equals("voltage") && !quantity.equals("stateOfCharge")) quantities.addView(button(bars ? "Bars" : "Lines", bars, () -> { bars = !bars; if (bars && !(quantity.equals("Wh") || quantity.equals("Ah"))) quantity = quantity.equals("current") ? "Ah" : "Wh"; buildHistory(); }));
        } else if (m != null && m.kind.equals("temperature")) {
            LinearLayout quantities = row(); content.addView(quantities);
            for (String q : new String[]{"value", "min", "max"}) quantities.addView(button(q.equals("value") ? "Last reading" : q.equals("min") ? "Low" : "High", quantity.equals(q), () -> { quantity = q; bars = false; buildHistory(); }));
        }
        historyNote = text(m == null ? "Connect to load the measurement catalogue." : "Select a period to load retained history.", 12, palette.muted); content.addView(historyNote);
        chart = new ChartView(this, palette); content.addView(chart, new LinearLayout.LayoutParams(-1, 0, 1));
        Button export = button("Export this range as CSV", false, this::exportHistory); content.addView(export);
        showHistory();
    }
    private void selectMetric() {
        if (metrics.isEmpty()) { toast("Connect to the Pi first."); return; }
        String[] names = new String[metrics.size()]; for (int i = 0; i < names.length; i++) names[i] = metricLabel(metrics.get(i));
        new AlertDialog.Builder(this).setTitle("Measurement").setItems(names, (dialog, n) -> {
            selectedMetric = metrics.get(n).id; settings.prefs.edit().putString("metric", selectedMetric).apply(); quantity = metrics.get(n).kind.equals("electrical") ? "watts" : "value"; bars = false; history = null; buildHistory(); loadHistory();
        }).show();
    }
    private void loadHistory() {
        MonitorData.Metric metric = selected(); if (!active || metric == null || !allowed()) return;
        final int generation = ++historyEpoch; final int connectionGeneration = epoch;
        historyLoading = true;
        if (historyClient != null) historyClient.cancel(); history = null; showHistory();
        if (historyNote != null) historyNote.setText("Loading retained history…");
        final HistoryData.Window window = HistoryData.window(now(), range, defined, offset, metric.kind);
        historyWorker.execute(() -> {
            try {
                if (generation != historyEpoch || connectionGeneration != epoch) return;
                ApiClient client = client(); historyClient = client; HistoryData.Result result = HistoryData.fetch(client, metric, window);
                handler.post(() -> { if (!active || generation != historyEpoch || connectionGeneration != epoch) return; historyLoading = false; history = result; showHistory(); });
            } catch (Exception e) { handler.post(() -> { if (active && generation == historyEpoch && connectionGeneration == epoch) { historyLoading = false; if (historyNote != null) historyNote.setText("History unavailable. Check connection and retained range; tap Refresh."); } }); }
        });
    }
    private void showHistory() {
        if (chart == null || historyNote == null) return;
        chart.show(history, quantity, bars, localClock, this::inspect);
        if (history == null) return;
        boolean verified = MonitorData.number(history.metric.definition, "polarity") != null;
        String direction = bars ? verified ? "  ·  green in / amber out" + (history.metric.role.equals("battery") ? " (net battery flow)" : "") : "  ·  unclassified signed net bars" : "";
        historyNote.setText(metricLabel(history.metric) + " · " + unit(history.metric) + " · " + history.window.resolution + " · " + history.rows.size() + " recorded periods" + direction + "\n" + clock(history.window.from) + " → " + clock(history.window.to) + "\nCheckpoint: " + history.savedAt + "\nWhole UTC buckets; edge/partial buckets are not prorated. Tap a point for coverage.");
    }
    private void inspect(HistoryData.Record r) {
        if (history == null) return;
        Double value = r.value(quantity, history.metric.kind), coverage = r.coverage(quantity, history.metric.kind);
        String message = clock(r.start) + " → " + clock(r.end) + "\n" + (value == null ? "Unavailable" : String.format(Locale.UK, "%.3f %s", value, unit(history.metric)))
                + "\nCoverage: " + (coverage == null ? "last reading / no duration coverage" : String.format(Locale.UK, "%.2f / %.0f seconds", coverage, (r.end.toEpochMilli() - r.start.toEpochMilli()) / 1000d))
                + "\n" + (r.partial ? "Partial checkpoint" : "Closed bucket; coverage may contain gaps") + "\nSource: " + history.metric.source + " · " + history.metric.id;
        if (bars) message += "\nRecorded forward: " + r.raw.opt("forward" + quantity) + "\nRecorded reverse: " + r.raw.opt("reverse" + quantity);
        new AlertDialog.Builder(this).setTitle(metricLabel(history.metric)).setMessage(message).setPositiveButton("Close", null).show();
    }
    private void showDetail(MonitorData.Datum d, String fallback) {
        if (d == null) { new AlertDialog.Builder(this).setTitle(fallback).setMessage("Unavailable. Connect to the Pi and wait for fresh readings.").setPositiveButton("Close", null).show(); return; }
        LinearLayout view = column(); view.addView(text("Snapshot at tap: " + formatted(d) + "\nReceived: " + (d.receivedAt.isEmpty() ? "unavailable" : d.receivedAt) + "\nOriginal label: " + d.name, 15, palette.text));
        if (d.metric == null) view.addView(text("Live only. No history is recorded for this element." + (d.id.contains("timeRemaining") ? " Runtime uses the existing Pico estimate." : ""), 14, palette.muted));
        else {
            view.addView(text("Source: " + d.metric.source + " · " + d.metric.id + "\nChoose retained history:", 13, palette.muted));
            TextView summary = text("Choose a summary period.", 14, palette.text); view.addView(summary);
            LinearLayout choices = row(); view.addView(choices);
            final AlertDialog[] popup = new AlertDialog[1];
            for (int i = 0; i < 4; i++) { final int n = i; choices.addView(button(new String[]{"Hour", "Day", "Week", "Month"}[i], false, () -> loadSummary(d.metric, n, summary, popup[0]))); }
            popup[0] = new AlertDialog.Builder(this).setTitle(settings.label(d.id, d.name)).setView(view).setPositiveButton("Close", null)
                    .setNeutralButton("Chart", (a, b) -> { selectedMetric = d.metric.id; quantity = d.quantity; offset = 0; historyScreen = true; build(); loadHistory(); }).create();
            popup[0].setOnDismissListener(a -> { summaryEpoch++; if (summaryClient != null) summaryClient.cancel(); }); popup[0].show(); return;
        }
        new AlertDialog.Builder(this).setTitle(settings.label(d.id, d.name)).setView(view).setPositiveButton("Close", null).show();
    }
    private volatile int summaryEpoch;
    private void loadSummary(MonitorData.Metric metric, int period, TextView view, AlertDialog popup) {
        final int generation = ++summaryEpoch, connectionGeneration = epoch;
        if (summaryClient != null) summaryClient.cancel(); view.setText("Loading recorded summary…");
        Instant end = now(); HistoryData.Window window = period == 0 ? new HistoryData.Window(end.minusSeconds(3600), end, metric.kind.equals("temperature") || metric.kind.equals("barometer") ? "hour" : "minute") : HistoryData.window(end, period == 1 ? 2 : period == 2 ? 3 : 4, false, 0, metric.kind);
        historyWorker.execute(() -> {
            try {
                if (!active || connectionGeneration != epoch || generation != summaryEpoch) return;
                ApiClient client = client(); summaryClient = client; HistoryData.Result result = HistoryData.fetch(client, metric, window);
                String message = summary(result);
                handler.post(() -> { if (active && popup.isShowing() && connectionGeneration == epoch && generation == summaryEpoch) view.setText(message); });
            } catch (Exception e) { handler.post(() -> { if (popup.isShowing() && generation == summaryEpoch) view.setText("Summary unavailable. Check connection and retained range."); }); }
        });
    }
    private String summary(HistoryData.Result result) {
        if (result.rows.isEmpty()) return "No recorded periods in this range.";
        if (!result.metric.kind.equals("electrical")) {
            HistoryData.Record last = result.rows.get(result.rows.size() - 1); Double v = last.value("value", result.metric.kind);
            return "Last recorded value: " + (v == null ? "Unavailable" : String.format(Locale.UK, "%.3f", v)) + "\nUTC interval: " + last.start + " → " + last.end + "\n" + result.rows.size() + " recorded periods; no missing periods invented.";
        }
        double wattSeconds = 0, ampSeconds = 0, voltsSeconds = 0, powerCoverage = 0, ampCoverage = 0, voltsCoverage = 0;
        for (HistoryData.Record r : result.rows) {
            Double p = r.value("watts", "electrical"), a = r.value("current", "electrical"), v = r.value("voltage", "electrical");
            Double pc = r.coverage("watts", "electrical"), ac = r.coverage("current", "electrical"), vc = r.coverage("voltage", "electrical");
            if (p != null && pc != null && pc > 0) { wattSeconds += p * pc; powerCoverage += pc; }
            if (a != null && ac != null && ac > 0) { ampSeconds += a * ac; ampCoverage += ac; }
            if (v != null && vc != null && vc > 0) { voltsSeconds += v * vc; voltsCoverage += vc; }
        }
        String message = "Recorded whole buckets (includes intersecting edges):\n";
        message += "Mean power: " + (powerCoverage > 0 ? String.format(Locale.UK, "%.2f W", wattSeconds / powerCoverage) : "Unavailable");
        message += "\nMean current: " + (ampCoverage > 0 ? String.format(Locale.UK, "%.2f A", ampSeconds / ampCoverage) : "Unavailable");
        message += "\nMean voltage: " + (voltsCoverage > 0 ? String.format(Locale.UK, "%.3f V", voltsSeconds / voltsCoverage) : "Unavailable");
        message += "\nSigned net energy: " + (powerCoverage > 0 ? String.format(Locale.UK, "%.3f Wh", wattSeconds / 3600) : "Unavailable");
        message += "\nSigned net charge: " + (ampCoverage > 0 ? String.format(Locale.UK, "%.3f Ah", ampSeconds / 3600) : "Unavailable");
        Double soc = result.rows.get(result.rows.size() - 1).value("stateOfCharge", "electrical");
        if (result.metric.role.equals("battery")) message += "\nLast recorded SOC: " + (soc == null ? "Unavailable" : String.format(Locale.UK, "%.1f %%", soc));
        message += String.format(Locale.UK, "\nPower/current coverage: %.0f / %.0f s\n%d recorded periods. Gaps/retention may limit this summary.", powerCoverage, ampCoverage, result.rows.size());
        return message;
    }
    private void editLabel(String id, String original) {
        EditText input = new EditText(this); input.setText(settings.label(id, original)); input.setSingleLine(); input.setSelectAllOnFocus(true); input.setMaxLines(1);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Rename display label").setMessage("Original: " + original).setView(input)
                .setPositiveButton("Save", null).setNeutralButton("Restore original", (a, b) -> { settings.resetLabel(id); build(); }).setNegativeButton("Cancel", null).create();
        dialog.setOnShowListener(a -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String label = input.getText().toString().trim(); if (label.isEmpty() || label.length() > 80) { input.setError("Use 1–80 characters."); return; }
            settings.setLabel(id, label); dialog.dismiss(); build();
        })); dialog.show();
    }
    private void labelSettings() {
        Map<String, String> labels = new LinkedHashMap<>(); for (Map.Entry<String, String> h : headings.entrySet()) labels.put("group:" + h.getKey(), h.getValue());
        for (MonitorData.Datum d : readings.values()) labels.put(d.id, d.name);
        String[] ids = labels.keySet().toArray(new String[0]), names = new String[ids.length];
        for (int i = 0; i < ids.length; i++) names[i] = settings.label(ids[i], labels.get(ids[i]));
        new AlertDialog.Builder(this).setTitle("Display labels").setItems(names, (d, i) -> editLabel(ids[i], labels.get(ids[i]))).setNegativeButton("Close", null).show();
    }
    private void showSettings() {
        ScrollView scroll = new ScrollView(this); LinearLayout view = column(); scroll.addView(view); view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.addView(text("Ella Monitoring " + BuildConfig.VERSION_NAME, 18, palette.text));
        view.addView(text("Connect on the same Wi-Fi as the Pi. Enter the origin and private token from api.json. HTTP is for a trusted LAN; HTTPS checks the certificate and hostname.", 14, palette.muted));
        EditText address = new EditText(this); address.setHint("http://PI_HOST:8080"); address.setSingleLine(); address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI); address.setText(settings.prefs.getString("address", "")); view.addView(address);
        EditText token = new EditText(this); token.setHint(settings.prefs.contains("token") ? "Leave blank to keep the saved token" : "Private API token"); token.setSingleLine(); token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); token.setSaveEnabled(false); view.addView(token);
        CheckBox local = new CheckBox(this); local.setText("Display local clock (UTC bucket boundaries stay unchanged)"); local.setChecked(localClock); view.addView(local);
        view.addView(button("Edit labels", false, this::labelSettings));
        view.addView(button("Export current chart range", false, this::exportHistory));
        view.addView(text("Preview: foreground live monitoring and retained history. Alerts, internet relay and APK updates will be added in later stages. Logging continues on the Pi when this app closes.", 13, palette.muted));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Settings").setView(scroll).setPositiveButton("Save & connect", null).setNegativeButton("Close", null).create();
        dialog.setOnShowListener(a -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String enteredToken = token.getText().toString().trim(), enteredAddress = address.getText().toString(); final boolean showLocal = local.isChecked();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            liveWorker.execute(() -> {
                try {
                    String value = enteredToken.isEmpty() ? settings.token() : enteredToken; settings.saveConnection(enteredAddress, value);
                    settings.prefs.edit().putBoolean("localClock", showLocal).apply();
                    handler.post(() -> { if (isDestroyed()) return; localClock = showLocal; token.setText(""); dialog.dismiss(); if (active) { if (!allowed()) requestPermissions(new String[]{LAN_PERMISSION}, 37); else restartConnection(); } });
                } catch (Exception e) {
                    String message = e instanceof IllegalArgumentException ? e.getMessage() : "Could not save credentials. Re-enter the token.";
                    handler.post(() -> { if (isDestroyed()) return; toast(message); dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); });
                }
            });
        })); dialog.show();
        dialog.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE);
    }
    private void exportHistory() {
        if (history == null) { toast("Load a chart range before exporting."); return; }
        exportBytes = HistoryData.csv(history, metricLabel(history.metric)).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/csv").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE, "ella-history-" + history.window.resolution + ".csv");
        try { startActivityForResult(intent, 100); } catch (Exception e) { exportBytes = null; toast("No file picker is available."); }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); if (requestCode != 100) return;
        byte[] bytes = exportBytes; exportBytes = null; Uri uri = data == null ? null : data.getData();
        if (resultCode != RESULT_OK || uri == null) return;
        if (bytes == null) { toast("Export was interrupted. Reload the chart and try again."); return; }
        historyWorker.execute(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new Exception(); out.write(bytes); handler.post(() -> toast("CSV exported."));
            } catch (Exception e) { handler.post(() -> toast("Could not write the export.")); }
        });
    }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }
}
