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
import android.view.ViewGroup;
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
    private DesignViewport viewport;
    private LiveDashboard liveDashboard;
    private final java.util.Set<String> knownCells = new java.util.LinkedHashSet<>();
    private ThemeConfig theme;
    private AppearanceStore appearance;
    private AlertDialog appearanceDialog;
    private ThemeAppearanceView appearanceView;
    private boolean settingsScreen, locked, hideTitle, persistent;
    private String currentPreset = "LIVE";
    private TextView status, historyNote, appTitle;
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
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        appearance = new AppearanceStore(settings.prefs); theme = appearance.active();
        locked = settings.prefs.getBoolean("locked", false); hideTitle = settings.prefs.getBoolean("hideTitle", false); persistent = settings.prefs.getBoolean("persistent", false);
        currentPreset = settings.prefs.getString("currentPreset", "LIVE");
        fullscreen = settings.prefs.getBoolean("fullscreen", false); keepScreen = settings.prefs.getBoolean("keepScreen", false);
        defined = settings.prefs.getBoolean("defined", false); localClock = settings.prefs.getBoolean("localClock", false);
        range = Math.max(0, Math.min(4, settings.prefs.getInt("range", 0))); selectedMetric = settings.prefs.getString("metric", "");
        if (state != null) { settingsScreen = state.getBoolean("settingsPage"); historyScreen = state.getBoolean("history"); quantity = state.getString("quantity", "watts"); offset = state.getInt("offset"); }
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::navigateBack);
        build();
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putBoolean("settingsPage", settingsScreen); out.putBoolean("history", historyScreen); out.putString("quantity", quantity); out.putInt("offset", offset); }
    @Override protected void onStart() { super.onStart(); active = true; screen.start(keepScreen); windowMode(); persistence(true); restartConnection(); handler.post(expire); }
    @Override protected void onResume() { super.onResume(); screen.apply(); windowMode(); }
    @Override public void onWindowFocusChanged(boolean focus) { super.onWindowFocusChanged(focus); if (focus && screen != null) { screen.apply(); windowMode(); } }
    @Override protected void onStop() {
        active = false; epoch++; historyEpoch++; handler.removeCallbacks(poll); handler.removeCallbacks(expire);
        if (liveClient != null) liveClient.cancel(); if (historyClient != null) historyClient.cancel(); if (summaryClient != null) summaryClient.cancel();
        live = null; history = null; historyLoading = false; screen.stop(); persistence(false); super.onStop();
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
    private TextView uiText(String value, float size, int color) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size); v.setTextColor(color);
        v.setTypeface(Typeface.create(theme.fontFamily, Typeface.NORMAL)); v.setIncludeFontPadding(false); return v;
    }
    private GradientDrawable uiBackground(int color, int border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(5); d.setStroke(1, border); return d;
    }
    private Button uiButton(String label, boolean selected, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 11.5f);
        b.setTypeface(Typeface.create(theme.fontFamily, Typeface.NORMAL)); b.setTextColor(selected ? theme.buttonTextOn : theme.buttonTextOff);
        b.setMinWidth(0); b.setMinimumWidth(0); b.setMinHeight(0); b.setMinimumHeight(0); b.setPadding(6,0,6,0);
        b.setBackground(uiBackground(theme.buttonBackground, selected ? theme.controls : theme.offButtonBorder)); b.setOnClickListener(v -> action.run()); return b;
    }
    private void build() {
        // Existing charts/summary popups keep their original preview palette and implementation.
        palette = new Palette(settings.prefs.getBoolean("light", false)); root = column(); root.setBackgroundColor(theme.background);
        appTitle = uiText("Ella Monitoring",15,theme.text);appTitle.setGravity(Gravity.CENTER);appTitle.setVisibility(hideTitle?View.GONE:View.VISIBLE);root.addView(appTitle,new LinearLayout.LayoutParams(-1,24));
        status = uiText(connectionState, 10.5f, theme.text); status.setSingleLine(); status.setEllipsize(TextUtils.TruncateAt.END); status.setPadding(8,0,8,0);
        status.setContentDescription("Connection and freshness status"); root.addView(status,new LinearLayout.LayoutParams(-1,20));
        if(historyScreen && !settingsScreen){LinearLayout ranges=row();ranges.setPadding(8,0,8,6);for(int i=0;i<HistoryData.RANGES.length;i++){final int n=i;Button b=uiButton(HistoryData.RANGES[i],range==i,()->{range=n;offset=0;settings.prefs.edit().putInt("range",range).apply();build();loadHistory();});LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,32,1);rp.setMargins(0,0,6,0);ranges.addView(b,rp);}root.addView(ranges);}
        content = column(); root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        BottomBar bottom = new BottomBar(this, theme, fullscreen); root.addView(bottom,new LinearLayout.LayoutParams(-1,fullscreen ? 54 : 44));
        Button presetButton=uiButton(currentPreset + (presetModified()?" *":"") + "  ▾",true,this::showPresetMenu);presetButton.setTextColor(presetModified()?theme.changedIndicator:theme.buttonTextOn);bottom.addControl(presetButton,0);
        bottom.addControl(uiButton("Live data",!historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=false; historyEpoch++; build(); }),1);
        bottom.addControl(uiButton("Charts",historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=true; build(); loadHistory(); }),2);
        bottom.addControl(new EqIconButton(this,theme,"keep","Keep screen on",keepScreen,fullscreen,() -> { keepScreen=!keepScreen; settings.prefs.edit().putBoolean("keepScreen",keepScreen).apply(); screen.setKeep(keepScreen); build(); }),3);
        bottom.addControl(new EqIconButton(this,theme,"screen","Full screen",fullscreen,fullscreen,() -> { fullscreen=!fullscreen; settings.prefs.edit().putBoolean("fullscreen",fullscreen).apply(); windowMode(); build(); }),4);
        bottom.addControl(new EqIconButton(this,theme,"lock","Lock gauge popups",locked,fullscreen,() -> { locked=!locked; settings.prefs.edit().putBoolean("locked",locked).apply(); build(); }),5);
        bottom.addControl(new EqIconButton(this,theme,"settings","Settings",settingsScreen,fullscreen,this::showSettings),6);
        viewport = new DesignViewport(this,root,fullscreen); setContentView(viewport);
        viewport.setOnApplyWindowInsetsListener((view,insets) -> {
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());view.setPadding(safe.left,safe.top,safe.right,safe.bottom);}
            else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        }); viewport.requestApplyInsets();
        getWindow().setFlags(settingsScreen ? android.view.WindowManager.LayoutParams.FLAG_SECURE : 0,android.view.WindowManager.LayoutParams.FLAG_SECURE);
        if(settingsScreen) buildSettingsPage(); else if(historyScreen) buildHistory(); else buildDashboard();
        screen.apply(); windowMode(); updateLive();
    }
    @android.annotation.SuppressLint("GestureBackNavigation") // API 33+ is registered above; retain legacy API 27–32 hardware Back.
    @Override public void onBackPressed() { navigateBack(); }
    private void navigateBack(){if(settingsScreen || historyScreen){settingsScreen=false;historyScreen=false;historyEpoch++;build();}else finish();}
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
        headings.put("loads","Current Draw"); headings.put("temps","Temps:");
        liveDashboard=new LiveDashboard(this,theme,readings,knownCells,new LiveDashboard.Host(){
            public String label(String id,String original){return settings.label(id,original);}
            public void detail(MonitorData.Datum d,String name){showDetail(d,name);}
            public void rename(String id,String original){editLabel(id,original);}
            public boolean locked(){return locked;}
        }); content.addView(liveDashboard,new LinearLayout.LayoutParams(-1,-1));
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
        if (!historyScreen && !settingsScreen) { if (changed || liveDashboard == null) buildDashboard(); else liveDashboard.update(readings); }
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
                    updateLive(); if (historyScreen && !settingsScreen && !historyLoading && (polls == 1 || polls % 30 == 0)) { buildHistory(); loadHistory(); }
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
        })); dialog.show(); styleDialog(dialog);
    }
    private void labelSettings() {
        Map<String, String> labels = new LinkedHashMap<>(); for (Map.Entry<String, String> h : headings.entrySet()) labels.put("group:" + h.getKey(), h.getValue());
        for (MonitorData.Datum d : readings.values()) labels.put(d.id, d.name);
        String[] ids = labels.keySet().toArray(new String[0]), names = new String[ids.length];
        for (int i = 0; i < ids.length; i++) names[i] = settings.label(ids[i], labels.get(ids[i]));
        themed(new AlertDialog.Builder(this).setTitle("Display labels").setItems(names, (d, i) -> editLabel(ids[i], labels.get(ids[i]))).setNegativeButton("Close", null).create());
    }
    private void showSettings() { settingsScreen=true; historyEpoch++; build(); }
    private LinearLayout settingsGroup(LinearLayout parent,String title) {
        LinearLayout g=column();g.setPadding(12,6,12,6);g.setBackground(uiBackground(theme.panel,theme.border));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,6);parent.addView(g,lp);
        TextView h=uiText(title,15,theme.controls);g.addView(h,new LinearLayout.LayoutParams(-1,28));return g;
    }
    private CheckBox setting(LinearLayout group,String text,boolean checked,java.util.function.Consumer<Boolean> action) {
        CheckBox c=new CheckBox(this);c.setText(text);c.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,13);c.setTextColor(theme.text);c.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));
        c.setButtonTintList(android.content.res.ColorStateList.valueOf(theme.controls));c.setChecked(checked);group.addView(c,new LinearLayout.LayoutParams(-1,38));c.setOnCheckedChangeListener((v,on)->action.accept(on));return c;
    }
    private void settingButton(LinearLayout group,String text,Runnable action){Button b=uiButton(text,true,action);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,36);lp.setMargins(0,0,0,6);group.addView(b,lp);}
    private void buildSettingsPage() {
        content.removeAllViews();ScrollView scroll=new ScrollView(this);LinearLayout columns=row();columns.setPadding(8,6,8,0);scroll.addView(columns);content.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout left=column(),right=column();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,6,0);columns.addView(left,lp);columns.addView(right,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout connection=settingsGroup(left,"CONNECTION");
        TextView help=uiText("Pi API address and private token from api.json. HTTP is for a trusted LAN; HTTPS verifies its certificate.",12,theme.text);help.setPadding(0,0,0,6);connection.addView(help);
        EditText address=new EditText(this);address.setHint("http://PI_HOST:8080");address.setSingleLine();address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);address.setText(settings.prefs.getString("address",""));styleEditor(address);connection.addView(address,new LinearLayout.LayoutParams(-1,42));
        EditText token=new EditText(this);token.setHint(settings.prefs.contains("token")?"Blank keeps saved token":"Private API token");token.setSingleLine();token.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);token.setSaveEnabled(false);styleEditor(token);connection.addView(token,new LinearLayout.LayoutParams(-1,42));
        settingButton(connection,"Save & connect",()->{
            String enteredToken=token.getText().toString().trim(),enteredAddress=address.getText().toString();
            liveWorker.execute(()->{try{settings.saveConnection(enteredAddress,enteredToken.isEmpty()?settings.token():enteredToken);handler.post(()->{if(isDestroyed())return;token.setText("");if(active){if(!allowed())requestPermissions(new String[]{LAN_PERMISSION},37);else restartConnection();}});}catch(Exception e){handler.post(()->toast(e instanceof IllegalArgumentException?e.getMessage():"Could not save credentials. Re-enter the token."));}});
        });
        LinearLayout display=settingsGroup(right,"DISPLAY & APP");
        setting(display,"Hide app title",hideTitle,on->{hideTitle=on;settings.prefs.edit().putBoolean("hideTitle",on).apply();appTitle.setVisibility(on?View.GONE:View.VISIBLE);viewport.requestLayout();});
        setting(display,"Persistent app notification",persistent,on->{persistent=on;settings.prefs.edit().putBoolean("persistent",on).apply();if(on&&Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},33);persistence(true);});
        setting(display,"Display local clock",localClock,on->{localClock=on;settings.prefs.edit().putBoolean("localClock",on).apply();updateLive();});
        LinearLayout appearanceGroup=settingsGroup(right,"THEME & APPEARANCE");settingButton(appearanceGroup,theme.name+"  ▾",this::showThemeMenu);settingButton(appearanceGroup,"Edit theme",this::showThemeEditor);
        LinearLayout labels=settingsGroup(left,"LABELS & EXPORT");settingButton(labels,"Edit display labels",this::labelSettings);settingButton(labels,"Export current chart range",this::exportHistory);
        TextView version=uiText("Ella Monitoring "+BuildConfig.VERSION_NAME+"\nLogging continues on the Pi. Persistence provides a return-to-app notification; live polling remains foreground only.",12,theme.text);version.setPadding(6,6,6,6);right.addView(version);
    }
    private void persistence(boolean visible) {
        Intent service=new Intent(this,PersistentService.class).putExtra("visible",visible);
        if(!persistent){stopService(service);return;}
        try { if(visible)startService(service);else startForegroundService(service); }catch(RuntimeException e){toast("Android could not keep the app notification active.");}
    }
    private void styleEditor(EditText v){v.setTextColor(theme.text);v.setHintTextColor(theme.buttonTextOff);v.setTextSize(14);v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.controls));v.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));}
    private AlertDialog themed(AlertDialog dialog) {
        dialog.setOnShowListener(v->styleDialog(dialog));dialog.show();styleDialog(dialog);return dialog;
    }
    private void styleDialog(AlertDialog d){d.getWindow().setBackgroundDrawable(uiBackground(theme.panel,theme.border));styleDialogViews(d.getWindow().getDecorView());for(int i:new int[]{-1,-2,-3}){Button b=d.getButton(i);if(b!=null){b.setTextColor(theme.buttonTextOn);b.setBackground(uiBackground(theme.buttonBackground,theme.controls));b.setPadding(dp(6),0,dp(6),0);b.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));}}}
    private void styleDialogViews(View view){if(view instanceof TextView){TextView t=(TextView)view;t.setTextColor(theme.text);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));}if(view instanceof EditText)styleEditor((EditText)view);if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)styleDialogViews(((ViewGroup)view).getChildAt(i));}
    private void menu(String title,List<String> names,List<Runnable> actions){themed(new AlertDialog.Builder(this).setTitle(title).setItems(names.toArray(new String[0]),(d,i)->actions.get(i).run()).setNegativeButton("CLOSE",null).create());}
    private void showThemeMenu(){List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE THEME AS…");actions.add(()->saveTheme(null));if(appearance.names().contains(theme.name)&&!theme.sameColours(appearance.load(theme.name))){names.add("UPDATE \""+theme.name+"\"");actions.add(()->saveTheme(theme.name));}names.add("MANAGE THEMES…");actions.add(()->manage(true));names.add("EDIT THEME");actions.add(this::showThemeEditor);names.add("Default");actions.add(()->applyTheme(ThemeConfig.defaults()));for(String name:appearance.names()){names.add(name);actions.add(()->applyTheme(appearance.load(name)));}menu("THEMES",names,actions);}
    private void applyTheme(ThemeConfig value){if(value==null)return;theme=value;appearance.active(theme);build();refreshAppearance();}
    private void confirmOverwrite(String name,Runnable action){themed(new AlertDialog.Builder(this).setTitle("Save changes to \""+name+"\"?").setMessage("Overwrite the saved settings?").setNegativeButton("CANCEL",null).setPositiveButton("SAVE CHANGES",(d,w)->action.run()).create());}
    private void writeTheme(String name){try{if(name.equals("Default"))throw new IllegalArgumentException("Choose a name other than Default.");appearance.save(name,theme);theme.name=name;appearance.active(theme);build();refreshAppearance();}catch(Exception e){toast(e.getMessage());}}
    private void saveTheme(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writeTheme(overwrite));return;}promptName("SAVE THEME AS","",name->{if(appearance.names().contains(name))confirmOverwrite(name,()->writeTheme(name));else writeTheme(name);});}
    private void promptName(String title,String initial,java.util.function.Consumer<String> action){EditText input=new EditText(this);input.setSingleLine();input.setText(initial);styleEditor(input);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("CANCEL",null).setPositiveButton("SAVE",null).create();themed(dialog);dialog.getButton(-1).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()||name.length()>80){input.setError("Use 1–80 characters.");return;}dialog.dismiss();action.accept(name);});}
    private void showThemeEditor(){
        appearanceView=new ThemeAppearanceView(this,this);
        appearanceDialog=new AlertDialog.Builder(this).setView(appearanceView).create();themed(appearanceDialog);
        appearanceDialog.setOnDismissListener(d->{appearanceDialog=null;appearanceView=null;});
    }
    ThemeConfig getThemeConfig(){return theme;}
    boolean isFullscreenMode(){return fullscreen;}
    Typeface getUiTypeface(){return Typeface.create(theme.fontFamily,Typeface.NORMAL);}
    int dialogDp(float n){return dp(n);}
    String getFontDisplayName(){String[] f={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"},labels={"SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE"};for(int i=0;i<f.length;i++)if(f[i].equals(theme.fontFamily))return labels[i];return "SANS";}
    void dismissThemeAppearanceDialog(){if(appearanceDialog!=null)appearanceDialog.dismiss();}
    void editThemeAppearanceColour(String field,String label){editColour(field,label);}
    void showFontMenu(View anchor,android.graphics.RectF position){String[] families={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"};themed(new AlertDialog.Builder(this).setTitle("FONT").setItems(new String[]{"SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE"},(d,i)->{theme.fontFamily=families[i];appearance.active(theme);build();refreshAppearance();}).setNegativeButton("CANCEL",null).create());}
    private void refreshAppearance(){if(appearanceView!=null)appearanceView.invalidate();if(appearanceDialog!=null)styleDialog(appearanceDialog);}
    private GradientDrawable swatch(int color){GradientDrawable d=uiBackground(color,theme.border);d.setSize(dp(24),dp(24));return d;}
    private void editColour(String field,String label){LinearLayout panel=column();panel.setPadding(dp(6),dp(6),dp(6),dp(6));ColorWheelView wheel=new ColorWheelView(this,theme.get(field));wheel.setColor(theme.get(field));panel.addView(wheel,new LinearLayout.LayoutParams(-1,dp(235)));EditText hex=new EditText(this);hex.setSingleLine();hex.setText(String.format(Locale.UK,"#%06X",theme.get(field)&0xffffff));styleEditor(hex);panel.addView(hex);wheel.setListener(c->hex.setText(String.format(Locale.UK,"#%06X",c&0xffffff)));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(label).setView(panel).setNegativeButton("CANCEL",null).setPositiveButton("APPLY",null).create();themed(dialog);dialog.getButton(-1).setOnClickListener(v->{try{int color=android.graphics.Color.parseColor(hex.getText().toString().trim());theme.set(field,color);appearance.active(theme);dialog.dismiss();build();refreshAppearance();}catch(IllegalArgumentException e){hex.setError("Enter #RRGGBB.");}});
    }
    private JSONObject presetState()throws Exception{JSONObject state=new JSONObject();for(Map.Entry<String,?> e:settings.prefs.getAll().entrySet())if(e.getKey().startsWith("label:"))state.put(e.getKey(),e.getValue());state.put("theme",theme.toJson()).put("localClock",localClock).put("hideTitle",hideTitle).put("range",range).put("defined",defined).put("metric",selectedMetric).put("quantity",quantity);return state;}
    private List<String> presetNames(){List<String> names=new ArrayList<>();for(String key:settings.prefs.getAll().keySet())if(key.startsWith("viewer.preset:"))names.add(key.substring(14));java.util.Collections.sort(names,String.CASE_INSENSITIVE_ORDER);return names;}
    private String canonical(Object value)throws Exception{if(value instanceof JSONObject){JSONObject object=(JSONObject)value;List<String> keys=new ArrayList<>();object.keys().forEachRemaining(keys::add);java.util.Collections.sort(keys);StringBuilder result=new StringBuilder("{");for(String key:keys)result.append(JSONObject.quote(key)).append(':').append(canonical(object.get(key))).append(',');return result.append('}').toString();}return String.valueOf(value);}
    private boolean presetModified(){try{return presetNames().contains(currentPreset)&&!canonical(presetState()).equals(canonical(new JSONObject(settings.prefs.getString("viewer.preset:"+currentPreset,"{}"))));}catch(Exception e){return false;}}
    private void showPresetMenu(){List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE SETTINGS AS…");actions.add(()->savePreset(null));if(presetNames().contains(currentPreset)&&presetModified()){names.add("UPDATE \""+currentPreset+"\"");actions.add(()->savePreset(currentPreset));}names.add("MANAGE PRESETS…");actions.add(()->manage(false));for(String n:presetNames()){names.add(n);actions.add(()->loadPreset(n));}menu("PRESETS",names,actions);}
    private void writePreset(String name){try{settings.prefs.edit().putString("viewer.preset:"+name,presetState().toString()).putString("currentPreset",name).apply();currentPreset=name;build();}catch(Exception e){toast("Could not save preset.");}}
    private void savePreset(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writePreset(overwrite));return;}promptName("SAVE SETTINGS AS","",name->{if(presetNames().contains(name))confirmOverwrite(name,()->writePreset(name));else writePreset(name);});}
    private void showPresetValues(String name){try{JSONObject state=new JSONObject(settings.prefs.getString("viewer.preset:"+name,"{}"));StringBuilder values=new StringBuilder("Theme: ").append(MonitorData.object(state,"theme").optString("name","Default")).append("\nApp title: ").append(state.optBoolean("hideTitle")?"Hidden":"Shown").append("\nClock: ").append(state.optBoolean("localClock")?"Local":"UTC").append("\nChart range: ").append(HistoryData.RANGES[Math.max(0,Math.min(4,state.optInt("range")))]).append(state.optBoolean("defined")?" / Defined UTC":" / Rolling");List<String> labels=new ArrayList<>();state.keys().forEachRemaining(key->{if(key.startsWith("label:"))labels.add(state.optString(key));});java.util.Collections.sort(labels);if(!labels.isEmpty()){values.append("\n\nDisplay labels:");for(String label:labels)values.append("\n").append(label);}themed(new AlertDialog.Builder(this).setTitle(name+" • VALUES").setMessage(values).setNegativeButton("CLOSE",null).create());}catch(Exception e){toast("Could not read preset.");}}
    private void loadPreset(String name){try{JSONObject p=new JSONObject(settings.prefs.getString("viewer.preset:"+name,"{}"));android.content.SharedPreferences.Editor edit=settings.prefs.edit();for(String key:settings.prefs.getAll().keySet())if(key.startsWith("label:"))edit.remove(key);java.util.Iterator<String> keys=p.keys();while(keys.hasNext()){String key=keys.next();if(key.startsWith("label:"))edit.putString(key,p.getString(key));}localClock=p.optBoolean("localClock",false);hideTitle=p.optBoolean("hideTitle",false);range=Math.max(0,Math.min(4,p.optInt("range",0)));defined=p.optBoolean("defined",false);selectedMetric=p.optString("metric","");quantity=p.optString("quantity","watts");edit.putBoolean("localClock",localClock).putBoolean("hideTitle",hideTitle).putInt("range",range).putBoolean("defined",defined).putString("metric",selectedMetric).putString("currentPreset",name).apply();currentPreset=name;if(p.optJSONObject("theme")!=null){theme=ThemeConfig.fromJson(p.getJSONObject("theme"));appearance.active(theme);}offset=0;build();if(historyScreen&&!settingsScreen)loadHistory();}catch(Exception e){toast("Could not load preset.");}}
    private void manage(boolean themes){List<String> names=themes?appearance.names():presetNames();List<Runnable> actions=new ArrayList<>();for(String name:names)actions.add(()->{List<String> commands=new ArrayList<>();List<Runnable> work=new ArrayList<>();if(!themes){commands.add("VIEW VALUES");work.add(()->showPresetValues(name));}commands.add("RENAME");work.add(()->promptName("RENAME",name,replacement->{try{if(themes)appearance.rename(name,replacement);else{if(presetNames().contains(replacement))throw new IllegalArgumentException("That name already exists.");String saved=settings.prefs.getString("viewer.preset:"+name,"");settings.prefs.edit().putString("viewer.preset:"+replacement,saved).remove("viewer.preset:"+name).apply();if(currentPreset.equals(name)){currentPreset=replacement;settings.prefs.edit().putString("currentPreset",replacement).apply();}}if(themes&&theme.name.equals(name)){theme.name=replacement;appearance.active(theme);}build();}catch(Exception e){toast(e.getMessage());}}));commands.add("DELETE");work.add(()->themed(new AlertDialog.Builder(this).setTitle("Delete \""+name+"\"?").setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{if(themes)appearance.delete(name);else{settings.prefs.edit().remove("viewer.preset:"+name).apply();if(currentPreset.equals(name)){currentPreset="LIVE";settings.prefs.edit().putString("currentPreset",currentPreset).apply();}}build();}).create()));menu(name,commands,work);});menu(themes?"MANAGE THEMES":"MANAGE PRESETS",names,actions);}
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
