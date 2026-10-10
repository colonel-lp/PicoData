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
    private android.app.Dialog appearanceDialog;
    private Button presetAnchor, themeAnchor;
    private UpdateController updates;
    private Runnable storageAction;
    private ThemeAppearanceView appearanceView;
    private boolean settingsScreen, locked, hideStatus, persistent;
    private String currentPreset = "System default";
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
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        appearance = new AppearanceStore(this,settings.prefs); theme = appearance.active();
        locked = settings.prefs.getBoolean("locked", false); hideStatus = settings.prefs.getBoolean("hideStatus", false); persistent = settings.prefs.getBoolean("persistent", false);
        currentPreset = settings.prefs.getString("currentPreset", "System default");
        if(currentPreset.equals("LIVE")&&!settings.prefs.contains("viewer.preset:LIVE")){currentPreset="System default";settings.prefs.edit().putString("currentPreset",currentPreset).apply();}
        fullscreen = settings.prefs.getBoolean("fullscreen", false); keepScreen = settings.prefs.getBoolean("keepScreen", false);
        defined = settings.prefs.getBoolean("defined", false); localClock = settings.prefs.getBoolean("localClock", false);
        range = Math.max(0, Math.min(4, settings.prefs.getInt("range", 0))); selectedMetric = settings.prefs.getString("metric", "");
        if (state != null) { settingsScreen = state.getBoolean("settingsPage"); historyScreen = state.getBoolean("history"); quantity = state.getString("quantity", "watts"); offset = state.getInt("offset"); }
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::navigateBack);
        updates=new UpdateController(this);
        build();updates.onCreated();
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putBoolean("settingsPage", settingsScreen); out.putBoolean("history", historyScreen); out.putString("quantity", quantity); out.putInt("offset", offset); }
    @Override protected void onStart() { super.onStart(); active = true; screen.start(keepScreen); windowMode(); persistence(true); restartConnection(); handler.post(expire); }
    @Override protected void onResume() { super.onResume(); screen.apply(); windowMode(); if(updates!=null)updates.onResumed(); }
    @Override protected void onPause(){if(updates!=null)updates.onPaused();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focus) { super.onWindowFocusChanged(focus); if (focus && screen != null) { screen.apply(); windowMode(); } }
    @Override protected void onStop() {
        active = false; epoch++; historyEpoch++; handler.removeCallbacks(poll); handler.removeCallbacks(expire);
        if (liveClient != null) liveClient.cancel(); if (historyClient != null) historyClient.cancel(); if (summaryClient != null) summaryClient.cancel();
        live = null; history = null; historyLoading = false; screen.stop(); persistence(false); super.onStop();
    }
    @Override protected void onDestroy() { if(updates!=null)updates.destroy();liveWorker.shutdownNow(); historyWorker.shutdownNow(); super.onDestroy(); }

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
        // Preserve chart calculations/content; dialogs follow the shared companion theme.
        palette = new Palette(settings.prefs.getBoolean("light", false)); root = column(); root.setBackgroundColor(theme.background);
        status = uiText(connectionState, 10.5f, theme.text); status.setSingleLine(); status.setEllipsize(TextUtils.TruncateAt.END); status.setPadding(8,0,8,0);status.setVisibility(settingsScreen||hideStatus?View.GONE:View.VISIBLE);
        status.setContentDescription("Connection and freshness status"); root.addView(status,new LinearLayout.LayoutParams(-1,20));
        if(historyScreen && !settingsScreen){LinearLayout ranges=row();ranges.setPadding(8,0,8,6);for(int i=0;i<HistoryData.RANGES.length;i++){final int n=i;Button b=uiButton(HistoryData.RANGES[i],range==i,()->{range=n;offset=0;settings.prefs.edit().putInt("range",range).apply();build();loadHistory();});LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,32,1);rp.setMargins(0,0,6,0);ranges.addView(b,rp);}root.addView(ranges);}
        content = column();content.setPadding(0,6,0,0); root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        BottomBar bottom = new BottomBar(this, theme, fullscreen); root.addView(bottom,new LinearLayout.LayoutParams(-1,54));
        Button presetButton=new ChangedButton(this,theme,"Preset",presetModified(),this::showPresetMenu);presetAnchor=presetButton;bottom.addControl(presetButton,0);
        boolean themeModified=!theme.sameColours(appearance.names().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults());
        themeAnchor=null;if(!settingsScreen){themeAnchor=new ChangedButton(this,theme,"Theme",themeModified,()->withThemeStorage(this::showThemeMenu));bottom.addControl(themeAnchor,7);}
        bottom.addControl(uiButton("Live data",!historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=false; historyEpoch++; build(); }),1);
        bottom.addControl(uiButton("Charts",historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=true; build(); loadHistory(); }),2);
        bottom.addControl(new EqIconButton(this,theme,"keep","Keep screen on",keepScreen,fullscreen,() -> { keepScreen=!keepScreen; settings.prefs.edit().putBoolean("keepScreen",keepScreen).apply(); screen.setKeep(keepScreen); build(); }),3);
        bottom.addControl(new EqIconButton(this,theme,"screen","Full screen",fullscreen,fullscreen,() -> { fullscreen=!fullscreen; settings.prefs.edit().putBoolean("fullscreen",fullscreen).apply(); windowMode(); build(); }),4);
        bottom.addControl(new EqIconButton(this,theme,"lock","Lock gauge popups",locked,fullscreen,() -> { locked=!locked; settings.prefs.edit().putBoolean("locked",locked).apply(); build(); }),5);
        bottom.addControl(new EqIconButton(this,theme,"settings","Settings",settingsScreen,fullscreen,this::showSettings),6);
        viewport = new DesignViewport(this,root,fullscreen); setContentView(viewport);
        viewport.setOnApplyWindowInsetsListener((view,insets) -> {
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets((fullscreen?0:WindowInsets.Type.systemBars())|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());view.setPadding(safe.left,safe.top,safe.right,safe.bottom);}
            else view.setPadding(fullscreen?0:insets.getSystemWindowInsetLeft(),fullscreen?0:insets.getSystemWindowInsetTop(),fullscreen?0:insets.getSystemWindowInsetRight(),fullscreen?Math.max(0,insets.getSystemWindowInsetBottom()-insets.getStableInsetBottom()):insets.getSystemWindowInsetBottom());return insets;
        }); viewport.requestApplyInsets();
        getWindow().setFlags(settingsScreen ? android.view.WindowManager.LayoutParams.FLAG_SECURE : 0,android.view.WindowManager.LayoutParams.FLAG_SECURE);
        if(settingsScreen) buildSettingsPage(); else if(historyScreen) buildHistory(); else buildDashboard();
        screen.apply(); windowMode(); updateLive();
    }
    @android.annotation.SuppressLint("GestureBackNavigation") // API 33+ is registered above; retain legacy API 27–32 hardware Back.
    @Override public void onBackPressed() { navigateBack(); }
    private void navigateBack(){if(settingsScreen || historyScreen){settingsScreen=false;historyScreen=false;historyEpoch++;build();}else finish();}
    @SuppressWarnings("deprecation") private void windowMode() {
        getWindow().setFlags(fullscreen?android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN:0,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
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
        headings.put("loads","Currents:"); headings.put("flags","Ella Monitoring"); headings.put("temps","Temps:");
        liveDashboard=new LiveDashboard(this,theme,readings,knownCells,new LiveDashboard.Host(){
            public String label(String id,String original){return settings.label(id,original);}
            public void detail(MonitorData.Datum d,String name,boolean gauge){showDetail(d,name,gauge);}
            public void rename(String id,String original){editLabel(id,original);}
            public boolean locked(){return locked;}
            public boolean highlighted(String id){return settings.prefs.getBoolean("gaugeHighlight:"+id,false);}
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
            String value = d == null ? "—" : formatted(d);
            if (gauge != null) gauge.value(d == null ? null : d.value);
            if (value.equals(lastText)) return; lastText = value;
            if (number != null) { number.setText(value); boolean fault = id.equals("flag:OVLK") || id.equals("flag:UVLK") || id.equals("flag:IOT") || id.equals("flag:LVC") || id.equals("flag:CELF"); number.setTextColor(d != null && d.flag != null ? d.flag ? fault ? palette.negative : palette.positive : palette.muted : d != null && d.value != null ? palette.text : palette.muted); }
            view.setContentDescription(settings.label(id, original) + ", " + value);
        }
    }
    private String formatted(MonitorData.Datum d) {
        if (d.group.equals("flags")) return d.flag == null ? "—" : d.flag ? "● On" : "○ Off";
        if (d.value == null) return "—";
        if (d.unit.equals("h"))return LiveDashboard.runtimeText(d.value);
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
            status.setText("Pi connected   ·   Pico " + (MonitorData.fresh(MonitorData.object(live, "pico"), elapsed, 2) ? "live" : "—") + "   ·   SBMS " + (MonitorData.fresh(MonitorData.object(live, "sbms"), elapsed, 3) ? "live" : "—") + "   ·   logging " + logging + "   ·   " + clock(now()));
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
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) { super.onRequestPermissionsResult(requestCode, permissions, results); if (requestCode == 37) restartConnection(); if(requestCode==28){Runnable action=storageAction;storageAction=null;if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED&&action!=null)action.run();else toast("Storage access is needed to save theme files in Downloads.");} }

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
        themedAlertBuilder().setTitle("Measurement").setItems(names, (dialog, n) -> {
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
        String message = clock(r.start) + " → " + clock(r.end) + "\n" + (value == null ? "—" : String.format(Locale.UK, "%.3f %s", value, unit(history.metric)))
                + "\nCoverage: " + (coverage == null ? "last reading / no duration coverage" : String.format(Locale.UK, "%.2f / %.0f seconds", coverage, (r.end.toEpochMilli() - r.start.toEpochMilli()) / 1000d))
                + "\n" + (r.partial ? "Partial checkpoint" : "Closed bucket; coverage may contain gaps") + "\nSource: " + history.metric.source + " · " + history.metric.id;
        if (bars) message += "\nRecorded forward: " + r.raw.opt("forward" + quantity) + "\nRecorded reverse: " + r.raw.opt("reverse" + quantity);
        themedAlertBuilder().setTitle(metricLabel(history.metric)).setMessage(message).setPositiveButton("Close", null).show();
    }
    private void showDetail(MonitorData.Datum d,String fallback){showDetail(d,fallback,false);}
    private void showDetail(MonitorData.Datum d, String fallback,boolean gauge) {
        if (d == null) { LinearLayout empty=column();empty.addView(uiText("—",14,theme.text));if(gauge)addGaugeHighlight(empty,"pending:"+fallback);themedAlertBuilder().setTitle(fallback).setView(empty).setPositiveButton("Close",null).show(); return; }
        LinearLayout view = column(); if(gauge)addGaugeHighlight(view,d.id);view.addView(text("Snapshot at tap: " + formatted(d) + "\nReceived: " + (d.receivedAt.isEmpty() ? "—" : d.receivedAt) + "\nOriginal label: " + d.name, 15, palette.text));
        if (d.metric == null) view.addView(text("Live only. No history is recorded for this element." + (d.id.contains("timeRemaining") ? " Time estimate follows Node-RED using Pico nominal/remaining capacity and raw battery current." : ""), 14, palette.muted));
        else {
            view.addView(text("Source: " + d.metric.source + " · " + d.metric.id + "\nChoose retained history:", 13, palette.muted));
            TextView summary = text("Choose a summary period.", 14, palette.text); view.addView(summary);
            LinearLayout choices = row(); view.addView(choices);
            final AlertDialog[] popup = new AlertDialog[1];
            for (int i = 0; i < 4; i++) { final int n = i; choices.addView(button(new String[]{"Hour", "Day", "Week", "Month"}[i], false, () -> loadSummary(d.metric, n, summary, popup[0]))); }
            popup[0] = themedAlertBuilder().setTitle(settings.label(d.id, d.name)).setView(view).setPositiveButton("Close", null)
                    .setNeutralButton("Chart", (a, b) -> { selectedMetric = d.metric.id; quantity = d.quantity; offset = 0; historyScreen = true; build(); loadHistory(); }).create();
            popup[0].setOnDismissListener(a -> { summaryEpoch++; if (summaryClient != null) summaryClient.cancel(); }); popup[0].show(); return;
        }
        themedAlertBuilder().setTitle(settings.label(d.id, d.name)).setView(view).setPositiveButton("Close", null).show();
    }
    private void addGaugeHighlight(LinearLayout view,String id){
        setting(view,"Highlight background",settings.prefs.getBoolean("gaugeHighlight:"+id,false),on->{android.content.SharedPreferences.Editor e=settings.prefs.edit();if(on)e.putBoolean("gaugeHighlight:"+id,true);else e.remove("gaugeHighlight:"+id);e.apply();if(liveDashboard!=null)liveDashboard.update(readings);});
    }
    private void clearDialogDim(android.app.Dialog dialog){if(dialog.getWindow()!=null){dialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);android.view.WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();lp.dimAmount=0;dialog.getWindow().setAttributes(lp);}}
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
            return "Last recorded value: " + (v == null ? "—" : String.format(Locale.UK, "%.3f", v)) + "\nUTC interval: " + last.start + " → " + last.end + "\n" + result.rows.size() + " recorded periods; no missing periods invented.";
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
        message += "Mean power: " + (powerCoverage > 0 ? String.format(Locale.UK, "%.2f W", wattSeconds / powerCoverage) : "—");
        message += "\nMean current: " + (ampCoverage > 0 ? String.format(Locale.UK, "%.2f A", ampSeconds / ampCoverage) : "—");
        message += "\nMean voltage: " + (voltsCoverage > 0 ? String.format(Locale.UK, "%.3f V", voltsSeconds / voltsCoverage) : "—");
        message += "\nSigned net energy: " + (powerCoverage > 0 ? String.format(Locale.UK, "%.3f Wh", wattSeconds / 3600) : "—");
        message += "\nSigned net charge: " + (ampCoverage > 0 ? String.format(Locale.UK, "%.3f Ah", ampSeconds / 3600) : "—");
        Double soc = result.rows.get(result.rows.size() - 1).value("stateOfCharge", "electrical");
        if (result.metric.role.equals("battery")) message += "\nLast recorded SOC: " + (soc == null ? "—" : String.format(Locale.UK, "%.1f %%", soc));
        message += String.format(Locale.UK, "\nPower/current coverage: %.0f / %.0f s\n%d recorded periods. Gaps/retention may limit this summary.", powerCoverage, ampCoverage, result.rows.size());
        return message;
    }
    private void editLabel(String id, String original) {
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(9),dialogDp(9),dialogDp(9),dialogDp(9));
        EditText input=new EditText(this);input.setText(settings.label(id,original));input.setSingleLine();input.setSelectAllOnFocus(true);input.setTextColor(theme.text);input.setTypeface(getUiTypeface());input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(13));input.setPadding(dialogDp(8),0,dialogDp(8),0);input.setBackground(roundedBackground(theme.buttonBackground,theme.border,1,5));panel.addView(input,new LinearLayout.LayoutParams(-1,dialogDp(36)));
        LinearLayout buttons=row();String[] labels={"CANCEL","RESTORE","SAVE"};for(int i=0;i<labels.length;i++){final int action=i;TextView b=dialogButton(labels[i],i==0?theme.border:theme.controls);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dialogDp(32),1);if(i>0)lp.setMargins(dialogDp(6),0,0,0);buttons.addView(b,lp);b.setOnClickListener(v->{if(action==0){dialog.dismiss();return;}if(action==1)settings.resetLabel(id);else{String label=input.getText().toString().trim();if(label.isEmpty()||label.length()>80){input.setError("Use 1–80 characters.");return;}settings.setLabel(id,label);}dialog.dismiss();build();});}LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dialogDp(32));bp.setMargins(0,dialogDp(6),0,0);panel.addView(buttons,bp);showStyledDialog(dialog,panel,350);
    }
    private void labelSettings() {
        Map<String, String> labels = new LinkedHashMap<>(); for (Map.Entry<String, String> h : headings.entrySet()) labels.put("group:" + h.getKey(), h.getValue());
        for (MonitorData.Datum d : readings.values()) labels.put(d.id, d.name);
        String[] ids = labels.keySet().toArray(new String[0]), names = new String[ids.length];
        for (int i = 0; i < ids.length; i++) names[i] = settings.label(ids[i], labels.get(ids[i]));
        themed(themedAlertBuilder().setTitle("Display labels").setItems(names, (d, i) -> editLabel(ids[i], labels.get(ids[i]))).setNegativeButton("Close", null).create());
    }
    private void showSettings() { settingsScreen=true; historyEpoch++; build(); }
    private LinearLayout settingsGroup(LinearLayout parent,String title) {
        LinearLayout g=column();g.setPadding(12,6,12,6);g.setBackground(uiBackground(theme.panel,theme.border));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,6);parent.addView(g,lp);
        TextView h=uiText(title,15,theme.controls);g.addView(h,new LinearLayout.LayoutParams(-1,28));return g;
    }
    private CheckBox setting(LinearLayout group,String text,boolean checked,java.util.function.Consumer<Boolean> action) {
        CheckBox c=new CheckBox(this);c.setText(text);c.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,13);c.setTextColor(theme.text);c.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));
        c.setButtonDrawable(new CheckboxDrawable(theme));c.setMinWidth(0);c.setMinimumWidth(0);c.setMinHeight(0);c.setMinimumHeight(0);c.setPadding(0,0,6,0);c.setCompoundDrawablePadding(6);c.setGravity(Gravity.CENTER_VERTICAL);c.setIncludeFontPadding(false);c.setSingleLine();c.setEllipsize(TextUtils.TruncateAt.END);c.setChecked(checked);group.addView(c,new LinearLayout.LayoutParams(-1,38));c.setOnCheckedChangeListener((v,on)->{action.accept(on);refreshChangedIndicators();});return c;
    }
    private void settingButton(LinearLayout group,String text,Runnable action){Button b=uiButton(text,true,action);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,36);lp.setMargins(0,0,0,6);group.addView(b,lp);}
    private void buildSettingsPage() {
        content.removeAllViews();ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout columns=row();columns.setGravity(Gravity.TOP);columns.setPadding(8,0,8,0);scroll.addView(columns);content.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout left=column(),right=column();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,6,0);columns.addView(left,lp);columns.addView(right,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout connection=settingsGroup(left,"CONNECTION");
        TextView help=uiText("Pi API address and private token from api.json. HTTP is for a trusted LAN; HTTPS verifies its certificate.",12,theme.text);help.setPadding(0,0,0,6);connection.addView(help);
        EditText address=new EditText(this);address.setHint("http://PI_HOST:8080");address.setSingleLine();address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);address.setText(settings.prefs.getString("address",""));styleEditor(address);connection.addView(address,new LinearLayout.LayoutParams(-1,42));
        EditText token=new EditText(this);token.setHint(settings.prefs.contains("token")?"Blank keeps saved token":"Private API token");token.setSingleLine();token.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);token.setSaveEnabled(false);styleEditor(token);connection.addView(token,new LinearLayout.LayoutParams(-1,42));
        settingButton(connection,"Save & connect",()->{
            String enteredToken=token.getText().toString().trim(),enteredAddress=address.getText().toString();
            liveWorker.execute(()->{try{settings.saveConnection(enteredAddress,enteredToken.isEmpty()?settings.token():enteredToken);handler.post(()->{if(isDestroyed())return;token.setText("");if(active){if(!allowed())requestPermissions(new String[]{LAN_PERMISSION},37);else restartConnection();}});}catch(Exception e){handler.post(()->toast(e instanceof IllegalArgumentException?e.getMessage():"Could not save credentials. Re-enter the token."));}});
        });
        LinearLayout display=settingsGroup(right,"DISPLAY & APP");
        setting(display,"Hide connection status",hideStatus,on->{hideStatus=on;settings.prefs.edit().putBoolean("hideStatus",on).apply();status.setVisibility(View.GONE);});
        setting(display,"Persistent app notification",persistent,on->{persistent=on;settings.prefs.edit().putBoolean("persistent",on).apply();if(on&&Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},33);persistence(true);});
        setting(display,"Display local clock",localClock,on->{localClock=on;settings.prefs.edit().putBoolean("localClock",on).apply();updateLive();});
        LinearLayout labels=settingsGroup(left,"EXPORT");settingButton(labels,"Export current chart range",this::exportHistory);
        View spacer=new View(this);right.addView(spacer,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=column();footer.setPadding(6,6,6,6);footer.setBackground(uiBackground(theme.panel,theme.border));right.addView(footer,new LinearLayout.LayoutParams(-1,-2));
        final int[] intervals={0,1,3,6,12,24};final String[] labelsInterval={"Off","1hr","3hr","6hr","12hr","24hr"};
        int current=settings.prefs.getInt("update.intervalHours",24),index=5;for(int i=0;i<intervals.length;i++)if(intervals[i]==current)index=i;
        LinearLayout upper=row();Button frequencyButton=uiButton("Updates: "+labelsInterval[index]+"  ▾",true,()->{});upper.addView(frequencyButton,new LinearLayout.LayoutParams(0,41,1));View future=new View(this);future.setBackground(uiBackground(theme.buttonBackground,theme.offButtonBorder));LinearLayout.LayoutParams reserved=new LinearLayout.LayoutParams(0,41,1);reserved.setMargins(6,0,0,0);upper.addView(future,reserved);footer.addView(upper);
        frequencyButton.setOnClickListener(v->{List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();for(int i=0;i<intervals.length;i++){final int choice=i;names.add(labelsInterval[i]);actions.add(()->{settings.prefs.edit().putInt("update.intervalHours",intervals[choice]).apply();frequencyButton.setText("Updates: "+labelsInterval[choice]+"  ▾");updates.schedule();refreshChangedIndicators();});}anchoredMenu(frequencyButton,false,names,actions,-1);});
        LinearLayout lower=row();lower.addView(uiButton("Changelog / Update",true,()->updates.showChangelog()),new LinearLayout.LayoutParams(0,41,1));Button back=uiButton("Back",true,()->{settingsScreen=false;historyScreen=false;build();});LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(0,41,1);backLp.setMargins(6,0,0,0);lower.addView(back,backLp);LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,41);rowLp.setMargins(0,6,0,0);footer.addView(lower,rowLp);
        TextView version=uiText("Version "+BuildConfig.VERSION_NAME+" / build "+BuildConfig.VERSION_CODE,12.5f,theme.text);version.setGravity(Gravity.CENTER);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,25);vp.setMargins(0,6,0,0);footer.addView(version,vp);

    }
    private void persistence(boolean visible) {
        Intent service=new Intent(this,PersistentService.class).putExtra("visible",visible);
        if(!persistent){stopService(service);return;}
        try { if(visible)startService(service);else startForegroundService(service); }catch(RuntimeException e){toast("Android could not keep the app notification active.");}
    }
    private void styleEditor(EditText v){v.setTextColor(theme.text);v.setHintTextColor(theme.buttonTextOff);v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,14);v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.controls));v.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));}
    private AlertDialog themed(AlertDialog dialog) {
        dialog.setOnShowListener(v->styleDialog(dialog));dialog.show();styleDialog(dialog);return dialog;
    }
    private AlertDialog.Builder themedAlertBuilder(){return new AlertDialog.Builder(this){
        @Override public AlertDialog create(){AlertDialog d=super.create();d.setOnShowListener(v->styleDialog(d));return d;}
        @Override public AlertDialog show(){AlertDialog d=super.show();styleDialog(d);return d;}
    };}
    private void styleDialog(AlertDialog d){
        android.view.Window window=d.getWindow();if(window==null)return;window.setBackgroundDrawableResource(android.R.color.transparent);
        View decor=window.getDecorView();int id=getResources().getIdentifier("parentPanel","id","android");View parent=id==0?null:decor.findViewById(id);if(parent!=null)parent.setBackground(roundedBackground(theme.panel,theme.border,1.4f,9));
        for(String name:new String[]{"topPanel","contentPanel","buttonPanel","customPanel"}){int panelId=getResources().getIdentifier(name,"id","android");View panel=panelId==0?null:decor.findViewById(panelId);if(panel!=null)panel.setBackgroundColor(android.graphics.Color.TRANSPARENT);}
        styleDialogViews(decor);for(int i:new int[]{-1,-2,-3}){Button b=d.getButton(i);if(b!=null){b.setTextColor(theme.text);b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(11.5f));b.setBackground(roundedBackground(theme.buttonBackground,i==-2?theme.border:theme.controls,1.25f,6));b.setPadding(dialogDp(6),0,dialogDp(6),0);b.setTypeface(getUiTypeface());b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);ViewGroup.LayoutParams bp=b.getLayoutParams();if(bp!=null){bp.height=dialogDp(35);b.setLayoutParams(bp);}}}
        if(d.getListView()!=null){d.getListView().setBackgroundColor(theme.panel);d.getListView().setDivider(new android.graphics.drawable.ColorDrawable(theme.border));d.getListView().setDividerHeight(1);}
        window.getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());window.setLayout(Math.min(dialogDp(460),Math.max(1,viewport.getWidth()-dialogDp(30))),-2);
    }
    private void styleDialogViews(View view){if(view instanceof TextView){TextView t=(TextView)view;t.setTextColor(theme.text);t.setTypeface(getUiTypeface());t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(view instanceof Button?11.5f:13.5f));if(view instanceof Button)t.setBackground(roundedBackground(theme.buttonBackground,theme.controls,1.25f,6));}if(view instanceof EditText)styleEditor((EditText)view);if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)styleDialogViews(((ViewGroup)view).getChildAt(i));}
    private void menu(String title,List<String> names,List<Runnable> actions){
        View anchor=title.equals("THEMES")?themeAnchor:title.equals("PRESETS")?presetAnchor:null;
        if(anchor!=null){String selected=title.equals("THEMES")?theme.name:currentPreset;anchoredMenu(anchor,true,names,actions,names.indexOf(selected));return;}
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.addView(styledTitle(title));
        for(int i=0;i<names.size();i++){final Runnable action=actions.get(i);Button button=uiButton(names.get(i),false,()->{dialog.dismiss();action.run();});panel.addView(button,new LinearLayout.LayoutParams(-1,dialogDp(34)));}
        showStyledDialog(dialog,panel,460);
    }
    private void showThemeMenu(){List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE THEME AS…");actions.add(()->saveTheme(null));if(appearance.names().contains(theme.name)&&!theme.sameColours(appearance.load(theme.name))){names.add("UPDATE \""+theme.name+"\"");actions.add(()->saveTheme(theme.name));}names.add("MANAGE THEMES…");actions.add(()->manage(true));names.add("EDIT THEME");actions.add(this::showThemeEditor);names.add("EDIT DISPLAY LABELS");actions.add(this::labelSettings);names.add("System default");actions.add(()->applyTheme(ThemeConfig.defaults()));for(String name:appearance.names()){names.add(name);actions.add(()->applyTheme(appearance.load(name)));}menu("THEMES",names,actions);}
    private void applyTheme(ThemeConfig value){if(value==null)return;theme=value;appearance.active(theme);build();refreshAppearance();}
    private void confirmOverwrite(String name,Runnable action){themed(themedAlertBuilder().setTitle("Save changes to \""+name+"\"?").setMessage("Overwrite the saved settings?").setNegativeButton("CANCEL",null).setPositiveButton("SAVE CHANGES",(d,w)->action.run()).create());}
    private void writeTheme(String name){try{if(name.equals("Default")||name.equals("System default"))throw new IllegalArgumentException("Choose another theme name.");appearance.save(name,theme);theme.name=name;appearance.active(theme);build();refreshAppearance();}catch(Exception e){toast(e.getMessage());}}
    private void saveTheme(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writeTheme(overwrite));return;}promptName("SAVE THEME AS","",name->{if(appearance.names().contains(name))confirmOverwrite(name,()->writeTheme(name));else writeTheme(name);});}
    private void promptName(String title,String initial,java.util.function.Consumer<String> action){
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(10),dialogDp(7),dialogDp(10),dialogDp(9));panel.addView(styledTitle(title));
        EditText input=new EditText(this);input.setSingleLine();input.setText(initial);input.setSelectAllOnFocus(true);input.setTextColor(theme.buttonTextOn);input.setTypeface(getUiTypeface());input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(13));input.setPadding(dialogDp(9),0,dialogDp(9),0);input.setBackground(roundedBackground(theme.buttonBackground,theme.border,1.1f,5));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dialogDp(38));lp.setMargins(dialogDp(4),dialogDp(3),dialogDp(4),dialogDp(7));panel.addView(input,lp);
        LinearLayout buttons=row();TextView cancel=dialogButton("CANCEL",theme.border),save=dialogButton(title.startsWith("RENAME")?"RENAME":"SAVE",theme.controls);buttons.addView(cancel,new LinearLayout.LayoutParams(0,dialogDp(35),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(35),1);bp.setMargins(dialogDp(6),0,0,0);buttons.addView(save,bp);panel.addView(buttons);cancel.setOnClickListener(v->dialog.dismiss());save.setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()||name.length()>80){input.setError("Use 1–80 characters.");return;}dialog.dismiss();action.accept(name);});showStyledDialog(dialog,panel,350);
    }
    private void showThemeEditor(){
        appearanceView=new ThemeAppearanceView(this,this);
        appearanceDialog=new android.app.Dialog(this);showStyledDialog(appearanceDialog,appearanceView,560);
        final android.app.Dialog opened=appearanceDialog;appearanceDialog.setOnDismissListener(d->{if(appearanceDialog==opened){appearanceDialog=null;appearanceView=null;}build();});
        clearDialogDim(appearanceDialog);
    }
    ThemeConfig getThemeConfig(){return theme;}
    boolean isFullscreenMode(){return fullscreen;}
    Typeface getUiTypeface(){return Typeface.create(theme.fontFamily,Typeface.NORMAL);}
    int dialogDp(float n){return Math.max(1,Math.round(n*(viewport==null?getResources().getDisplayMetrics().density:viewport.scale)));}
    float dialogTextSize(float n){return n*(viewport==null?getResources().getDisplayMetrics().density:viewport.scale);}
    android.content.SharedPreferences viewerPreferences(){return settings.prefs;}
    String getFontDisplayName(){String[] f={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"},labels={"SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE"};for(int i=0;i<f.length;i++)if(f[i].equals(theme.fontFamily))return labels[i];return "SANS";}
    void dismissThemeAppearanceDialog(){if(appearanceDialog!=null)appearanceDialog.dismiss();}
    void editThemeAppearanceColour(String field,String label){editColour(field,label);}
    void showFontMenu(View anchor,android.graphics.RectF position){String[] families={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"};List<String> labels=java.util.Arrays.asList("SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE");List<Runnable> actions=new ArrayList<>();for(String family:families)actions.add(()->{theme.fontFamily=family;appearance.active(theme);refreshAppearance();refreshChangedIndicators();});anchoredMenu(anchor,position,false,labels,actions,java.util.Arrays.asList(families).indexOf(theme.fontFamily));}
    private void refreshAppearance(){if(appearanceView!=null)appearanceView.invalidate();if(appearanceDialog!=null&&appearanceDialog.getWindow()!=null)appearanceDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);}
    private GradientDrawable swatch(int color){GradientDrawable d=uiBackground(color,theme.border);d.setSize(dp(24),dp(24));return d;}
    private void editColour(String field,String label){
        final int original=theme.get(field);dismissThemeAppearanceDialog();android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.addView(styledTitle(label+" COLOUR"));final boolean[] syncing={false},accepted={false};
        EditText hex=new EditText(this);hex.setSingleLine();hex.setText(String.format(Locale.UK,"#%06X",original&0xffffff));hex.setSelectAllOnFocus(true);hex.setTextColor(theme.text);hex.setTypeface(getUiTypeface());hex.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(14));hex.setGravity(Gravity.CENTER);hex.setPadding(dialogDp(6),0,dialogDp(6),0);hex.setBackground(roundedBackground(theme.buttonBackground,theme.border,1,6));
        ColorWheelView picker=new ColorWheelView(this,original);picker.setListener(color->{theme.set(field,color);if(!syncing[0]){syncing[0]=true;hex.setText(String.format(Locale.UK,"#%06X",color&0xffffff));hex.setSelection(hex.length());syncing[0]=false;}refreshAppearance();});panel.addView(picker,new LinearLayout.LayoutParams(-1,dialogDp(300)));
        hex.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void afterTextChanged(android.text.Editable s){}public void onTextChanged(CharSequence s,int start,int before,int count){if(syncing[0])return;try{if(!s.toString().matches("#[0-9a-fA-F]{6}"))return;syncing[0]=true;picker.setColor(android.graphics.Color.parseColor(s.toString()));}catch(IllegalArgumentException ignored){}finally{syncing[0]=false;}}});LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dialogDp(116),dialogDp(42));hp.gravity=Gravity.CENTER_HORIZONTAL;hp.setMargins(0,dialogDp(4),0,dialogDp(7));panel.addView(hex,hp);
        LinearLayout buttons=row();TextView cancel=dialogButton("CANCEL",theme.border),ok=dialogButton("OK",theme.controls);buttons.addView(cancel,new LinearLayout.LayoutParams(0,dialogDp(36),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(36),1);bp.setMargins(dialogDp(6),0,0,0);buttons.addView(ok,bp);panel.addView(buttons);
        Runnable rollback=()->{theme.set(field,original);appearance.active(theme);build();};cancel.setOnClickListener(v->{accepted[0]=true;dialog.dismiss();rollback.run();showThemeEditor();});ok.setOnClickListener(v->{try{String code=hex.getText().toString();if(!code.matches("#[0-9a-fA-F]{6}"))throw new IllegalArgumentException();picker.setColor(android.graphics.Color.parseColor(code));appearance.active(theme);accepted[0]=true;dialog.dismiss();showThemeEditor();}catch(IllegalArgumentException e){hex.setError("Enter #RRGGBB.");}});dialog.setOnDismissListener(d->{if(!accepted[0]){rollback.run();showThemeEditor();}});showStyledDialog(dialog,panel,360);android.view.WindowManager.LayoutParams wp=dialog.getWindow().getAttributes();wp.gravity=Gravity.BOTTOM|Gravity.LEFT;wp.dimAmount=0;dialog.getWindow().setAttributes(wp);clearDialogDim(dialog);
    }
    private JSONObject presetState()throws Exception{JSONObject state=new JSONObject();for(Map.Entry<String,?> e:settings.prefs.getAll().entrySet())if(e.getKey().startsWith("label:"))state.put(e.getKey(),e.getValue());for(Map.Entry<String,?> e:settings.prefs.getAll().entrySet())if(e.getKey().startsWith("gaugeHighlight:"))state.put(e.getKey(),e.getValue());state.put("persistent",persistent).put("keepScreen",keepScreen).put("fullscreen",fullscreen).put("locked",locked).put("updateIntervalHours",settings.prefs.getInt("update.intervalHours",24));state.put("theme",theme.toJson()).put("localClock",localClock).put("hideStatus",hideStatus).put("range",range).put("defined",defined).put("metric",selectedMetric).put("quantity",quantity);return state;}
    private List<String> presetNames(){List<String> names=new ArrayList<>();for(String key:settings.prefs.getAll().keySet())if(key.startsWith("viewer.preset:"))names.add(key.substring(14));java.util.Collections.sort(names,String.CASE_INSENSITIVE_ORDER);return names;}
    private String canonical(Object value)throws Exception{if(value instanceof JSONObject){JSONObject object=(JSONObject)value;List<String> keys=new ArrayList<>();object.keys().forEachRemaining(keys::add);java.util.Collections.sort(keys);StringBuilder result=new StringBuilder("{");for(String key:keys)result.append(JSONObject.quote(key)).append(':').append(canonical(object.get(key))).append(',');return result.append('}').toString();}return String.valueOf(value);}
    private JSONObject defaultPreset()throws Exception{return new JSONObject().put("theme",ThemeConfig.defaults().toJson()).put("localClock",false).put("hideStatus",false).put("range",0).put("defined",false).put("metric","").put("quantity","watts").put("persistent",false).put("keepScreen",false).put("fullscreen",false).put("locked",false).put("updateIntervalHours",24);}
    private boolean presetModified(){try{JSONObject saved=presetNames().contains(currentPreset)?new JSONObject(settings.prefs.getString("viewer.preset:"+currentPreset,"{}")):defaultPreset();if(saved.optJSONObject("theme")!=null)saved.put("theme",ThemeConfig.fromJson(saved.getJSONObject("theme")).toJson());JSONObject defaults=defaultPreset();for(java.util.Iterator<String> keys=defaults.keys();keys.hasNext();){String key=keys.next();if(!saved.has(key))saved.put(key,defaults.get(key));}return !canonical(presetState()).equals(canonical(saved));}catch(Exception e){return false;}}
    private void refreshChangedIndicators(){if(presetAnchor instanceof ChangedButton)((ChangedButton)presetAnchor).changed(presetModified());if(themeAnchor instanceof ChangedButton)((ChangedButton)themeAnchor).changed(!theme.sameColours(appearance.names().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults()));}
    private void showPresetMenu(){List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE SETTINGS AS…");actions.add(()->savePreset(null));if(presetNames().contains(currentPreset)&&presetModified()){names.add("UPDATE \""+currentPreset+"\"");actions.add(()->savePreset(currentPreset));}names.add("MANAGE PRESETS…");actions.add(()->manage(false));names.add("System default");actions.add(()->loadPreset("System default"));for(String n:presetNames()){names.add(n);actions.add(()->loadPreset(n));}menu("PRESETS",names,actions);}
    private void writePreset(String name){try{settings.prefs.edit().putString("viewer.preset:"+name,presetState().toString()).putString("currentPreset",name).apply();currentPreset=name;build();}catch(Exception e){toast("Could not save preset.");}}
    private void savePreset(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writePreset(overwrite));return;}promptName("SAVE SETTINGS AS","",name->{if(presetNames().contains(name))confirmOverwrite(name,()->writePreset(name));else writePreset(name);});}
    private void showPresetValues(String name){try{JSONObject state=new JSONObject(settings.prefs.getString("viewer.preset:"+name,"{}"));StringBuilder values=new StringBuilder("Theme: ").append(MonitorData.object(state,"theme").optString("name","Default")).append("\nConnection status: ").append(state.optBoolean("hideStatus")?"Hidden":"Shown").append("\nClock: ").append(state.optBoolean("localClock")?"Local":"UTC").append("\nChart range: ").append(HistoryData.RANGES[Math.max(0,Math.min(4,state.optInt("range")))]).append(state.optBoolean("defined")?" / Defined UTC":" / Rolling");List<String> labels=new ArrayList<>();state.keys().forEachRemaining(key->{if(key.startsWith("label:"))labels.add(state.optString(key));});java.util.Collections.sort(labels);if(!labels.isEmpty()){values.append("\n\nDisplay labels:");for(String label:labels)values.append("\n").append(label);}themed(themedAlertBuilder().setTitle(name+" • VALUES").setMessage(values).setNegativeButton("CLOSE",null).setPositiveButton("COPY",(d,w)->{android.content.ClipboardManager clipboard=getSystemService(android.content.ClipboardManager.class);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Preset values",values));}).create());}catch(Exception e){toast("Could not read preset.");}}
    private void loadPreset(String name){try{JSONObject p=name.equals("System default")?defaultPreset():new JSONObject(settings.prefs.getString("viewer.preset:"+name,"{}"));android.content.SharedPreferences.Editor edit=settings.prefs.edit();for(String key:settings.prefs.getAll().keySet())if(key.startsWith("label:")||key.startsWith("gaugeHighlight:"))edit.remove(key);java.util.Iterator<String> keys=p.keys();while(keys.hasNext()){String key=keys.next();if(key.startsWith("label:"))edit.putString(key,p.getString(key));else if(key.startsWith("gaugeHighlight:"))edit.putBoolean(key,p.getBoolean(key));}persistent=p.optBoolean("persistent",false);keepScreen=p.optBoolean("keepScreen",false);fullscreen=p.optBoolean("fullscreen",false);locked=p.optBoolean("locked",false);edit.putBoolean("persistent",persistent).putBoolean("keepScreen",keepScreen).putBoolean("fullscreen",fullscreen).putBoolean("locked",locked).putInt("update.intervalHours",p.optInt("updateIntervalHours",24));localClock=p.optBoolean("localClock",false);hideStatus=p.optBoolean("hideStatus",false);range=Math.max(0,Math.min(4,p.optInt("range",0)));defined=p.optBoolean("defined",false);selectedMetric=p.optString("metric","");quantity=p.optString("quantity","watts");edit.putBoolean("localClock",localClock).putBoolean("hideStatus",hideStatus).putInt("range",range).putBoolean("defined",defined).putString("metric",selectedMetric).putString("currentPreset",name).apply();currentPreset=name;if(p.optJSONObject("theme")!=null){theme=ThemeConfig.fromJson(p.getJSONObject("theme"));appearance.active(theme);}offset=0;screen.setKeep(keepScreen);windowMode();persistence(true);updates.schedule();build();if(historyScreen&&!settingsScreen)loadHistory();}catch(Exception e){toast("Could not load preset.");}}
    private void manage(boolean themes){
        List<String> names=new ArrayList<>(themes?appearance.names():presetNames());if(names.isEmpty()){toast(themes?"No saved themes":"No saved presets");return;}
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(9),dialogDp(7),dialogDp(9),dialogDp(9));panel.addView(styledTitle(themes?"MANAGE THEMES":"MANAGE PRESETS"));ScrollView scroll=new ScrollView(this);LinearLayout list=column();scroll.addView(list);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,Math.min(dialogDp(150),Math.max(dialogDp(36),names.size()*dialogDp(34))));sp.setMargins(0,dialogDp(2),0,dialogDp(6));panel.addView(scroll,sp);
        int[] selected={-1};List<TextView> rows=new ArrayList<>();LinearLayout buttons=row();TextView rename=dialogButton("RENAME",theme.controls),delete=dialogButton("DELETE",0xffd76969),values=themes?null:dialogButton("VIEW VALUES",theme.controls);for(TextView b:new TextView[]{rename,delete,values})if(b!=null){b.setEnabled(false);b.setAlpha(.42f);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(36),1);bp.setMargins(dialogDp(3),0,dialogDp(3),0);buttons.addView(b,bp);}panel.addView(buttons);
        Runnable refresh=()->{names.clear();names.addAll(themes?appearance.names():presetNames());selected[0]=-1;list.removeAllViews();rows.clear();rename.setEnabled(false);delete.setEnabled(false);rename.setAlpha(.42f);delete.setAlpha(.42f);if(values!=null){values.setEnabled(false);values.setAlpha(.42f);}for(int i=0;i<names.size();i++){int index=i;TextView row=dialogButton(names.get(i),theme.border);row.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);row.setPadding(dialogDp(10),0,dialogDp(6),0);rows.add(row);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dialogDp(32));rp.setMargins(dialogDp(2),dialogDp(1),dialogDp(2),dialogDp(1));list.addView(row,rp);row.setOnClickListener(v->{selected[0]=index;for(int j=0;j<rows.size();j++){boolean active=j==index;rows.get(j).setTextColor(active?theme.controls:theme.text);rows.get(j).setBackground(roundedBackground(active?ThemeConfig.blend(theme.buttonBackground,theme.controls,.14f):theme.buttonBackground,active?theme.controls:theme.border,1.1f,5));}rename.setEnabled(true);delete.setEnabled(true);rename.setAlpha(1);delete.setAlpha(1);if(values!=null){values.setEnabled(true);values.setAlpha(1);}});}};
        refresh.run();rename.setOnClickListener(v->{if(selected[0]<0)return;String old=names.get(selected[0]);promptName(themes?"RENAME THEME":"RENAME PRESET",old,name->{try{if(name.equals(old))return;if(themes){appearance.rename(old,name);if(theme.name.equals(old)){theme.name=name;appearance.active(theme);}}else{if(presetNames().contains(name))throw new IllegalArgumentException("That name already exists.");settings.prefs.edit().putString("viewer.preset:"+name,settings.prefs.getString("viewer.preset:"+old,"{}")).remove("viewer.preset:"+old).apply();if(currentPreset.equals(old)){currentPreset=name;settings.prefs.edit().putString("currentPreset",name).apply();}}build();refresh.run();}catch(Exception e){toast(e.getMessage());}});});
        delete.setOnClickListener(v->{if(selected[0]<0)return;String name=names.get(selected[0]);themedAlertBuilder().setTitle(themes?"Delete theme?":"Delete preset?").setMessage(name).setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{try{if(themes){appearance.delete(name);if(theme.name.equals(name))applyTheme(ThemeConfig.defaults());}else{settings.prefs.edit().remove("viewer.preset:"+name).apply();if(currentPreset.equals(name)){currentPreset="System default";settings.prefs.edit().putString("currentPreset",currentPreset).apply();}}build();refresh.run();}catch(Exception e){toast(e.getMessage());}}).show();});if(values!=null)values.setOnClickListener(v->{if(selected[0]>=0)showPresetValues(names.get(selected[0]));});showStyledDialog(dialog,panel,370);
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
    private void withThemeStorage(Runnable action){
        if(Build.VERSION.SDK_INT<=28&&checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")!=PackageManager.PERMISSION_GRANTED){storageAction=action;requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},28);}else action.run();
    }
    private GradientDrawable roundedBackground(int fill,int border,float stroke,float radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dialogDp(radius));if(stroke>0)d.setStroke(dialogDp(stroke),border);return d;}
    private LinearLayout styledDialogRoot(){LinearLayout panel=column();panel.setGravity(Gravity.CENTER_HORIZONTAL);panel.setPadding(dialogDp(14),dialogDp(12),dialogDp(14),dialogDp(12));panel.setBackground(roundedBackground(theme.panel,theme.border,1.4f,9));return panel;}
    private TextView styledTitle(String title){TextView t=uiText(title,dialogTextSize(14),theme.text);t.setGravity(Gravity.CENTER);t.setPadding(dialogDp(8),dialogDp(5),dialogDp(8),dialogDp(8));return t;}
    private TextView dialogButton(String label,int accent){TextView t=styledTitle(label);t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(11.5f));t.setBackground(roundedBackground(theme.buttonBackground,accent,1.25f,6));t.setPadding(dialogDp(6),0,dialogDp(6),0);return t;}
    private void showStyledDialog(android.app.Dialog dialog,View panel,int width){
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);int maxW=Math.max(1,Math.min(dialogDp(width),viewport.getWidth()-dialogDp(30)));int maxH=Math.max(1,viewport.getHeight()-dialogDp(24));
        panel.measure(View.MeasureSpec.makeMeasureSpec(maxW,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        if(panel.getMeasuredHeight()>maxH&&!(panel instanceof ScrollView)){ScrollView scroll=new ScrollView(this);scroll.addView(panel);panel=scroll;}
        dialog.setContentView(panel);dialog.setCanceledOnTouchOutside(true);dialog.show();dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(maxW,Math.min(maxH,panel.getMeasuredHeight()>0?panel.getMeasuredHeight():maxH));dialog.getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
    }
    private void anchoredMenu(View anchor,boolean above,List<String> items,List<Runnable> actions,int selected){anchoredMenu(anchor,null,above,items,actions,selected);}
    private void anchoredMenu(View anchor,android.graphics.RectF position,boolean above,List<String> items,List<Runnable> actions,int selected){
        if(items.isEmpty()||items.size()!=actions.size())return;
        android.graphics.Rect bounds=new android.graphics.Rect();anchor.getGlobalVisibleRect(bounds);float scale=viewport.scale;
        if(position!=null&&anchor instanceof ThemeAppearanceView){ThemeAppearanceView view=(ThemeAppearanceView)anchor;int[] loc=new int[2];anchor.getLocationOnScreen(loc);bounds.set(loc[0]+Math.round(position.left*view.getUiScaleX()),loc[1]+Math.round(position.top*view.getUiScaleY()),loc[0]+Math.round(position.right*view.getUiScaleX()),loc[1]+Math.round(position.bottom*view.getUiScaleY()));}
        int height=dialogDp(34),chrome=dialogDp(6),available=above?bounds.top:viewport.getHeight()-bounds.bottom;
        int menuHeight=Math.min(items.size()*height+chrome*2,Math.max(height+chrome*2,available-chrome));
        LinearLayout list=column();list.setPadding(0,chrome,0,chrome);list.setBackground(roundedBackground(theme.panel,theme.border,1.2f,6));
        final android.widget.PopupWindow[] holder=new android.widget.PopupWindow[1];
        for(int i=0;i<items.size();i++){final Runnable action=actions.get(i);TextView item=uiText(items.get(i),dialogTextSize(11.5f),i==selected?theme.controls:theme.text);item.setSingleLine();item.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);item.setPadding(dialogDp(10),0,dialogDp(6),0);if(i==selected)item.setBackgroundColor(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f));list.addView(item,new LinearLayout.LayoutParams(-1,height));item.setOnClickListener(v->{holder[0].dismiss();action.run();});}
        ScrollView scroll=new ScrollView(this);scroll.addView(list);holder[0]=new android.widget.PopupWindow(scroll,Math.max(dialogDp(150),bounds.width()),menuHeight,true);holder[0].setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));holder[0].setOutsideTouchable(true);holder[0].setElevation(dialogDp(6));holder[0].showAtLocation(viewport,Gravity.TOP|Gravity.LEFT,bounds.left,above?Math.max(0,bounds.top-menuHeight-chrome):bounds.bottom+chrome);
    }

    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }
}

