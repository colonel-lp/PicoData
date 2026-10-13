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
    private final ExecutorService popupWorker=Executors.newFixedThreadPool(2),viewSettingsWorker=Executors.newSingleThreadExecutor();
    private PopupSettingsStore popupSettings;
    private volatile ApiClient popupMonthClient;
    private final Runnable savePopupSettings=()->withDownloadsStorage(()->viewSettingsWorker.execute(()->{try{popupSettings.save();}catch(Exception e){handler.post(()->{if(MainActivity.this.active)toast(e.getMessage());});}}));
    private final ExecutorService liveWorker = Executors.newSingleThreadExecutor(), historyWorker = Executors.newSingleThreadExecutor(), metadataWorker=Executors.newSingleThreadExecutor();
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
    private android.app.Dialog colourDialog;
    private android.widget.PopupWindow anchoredPopup;
    private final Map<View,String> changedViews=new java.util.IdentityHashMap<>();
    private final Map<TextView,Integer> normalColours=new java.util.IdentityHashMap<>();
    private java.util.Set<String> presetChanges=new java.util.LinkedHashSet<>();
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
    private volatile ApiClient liveClient, historyClient, summaryClient, metadataClient;
    private boolean metadataLoading;
    private ThemeConfig previewBaseline;
    private volatile boolean active;
    private boolean historyScreen, defined, bars, fullscreen, keepScreen, localClock, historyLoading;
    private volatile int epoch, historyEpoch;
    private int polls, range, offset;
    private String connectionState = "Set the Pi address and API token in Settings.", quantity = "watts", selectedMetric = "";
    private HistoryData.Result history;
    private ChartView chart;
    private byte[] exportBytes;
    private PresetStore presets;
    private JSONObject cachedPreset;
    private String cachedPresetName;
    private final Runnable poll = this::poll;
    private final Runnable expire = new Runnable() {
        @Override public void run() { if (!active) return; updateLive(); handler.postDelayed(this, 250); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); settings = new PrivateSettings(this); screen = new ScreenControl(this, settings.prefs);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        appearance = new AppearanceStore(this,settings.prefs); theme = appearance.active();migrateViewerSettings();
        presets=new PresetStore(this,settings.prefs);popupSettings=new PopupSettingsStore(this);
        theme.fontFamily=settings.prefs.getString("fontFamily",theme.fontFamily);
        locked = settings.prefs.getBoolean("locked", false); hideStatus = settings.prefs.getBoolean("hideStatus", false); persistent = settings.prefs.getBoolean("persistent", false);
        currentPreset = settings.prefs.getString("currentPreset", "System default");
        if(currentPreset.equals("LIVE")&&!settings.prefs.contains("viewer.preset:LIVE")){currentPreset="System default";settings.prefs.edit().putString("currentPreset",currentPreset).apply();}
        fullscreen = settings.prefs.getBoolean("fullscreen", false); keepScreen = settings.prefs.getBoolean("keepScreen", false);
        defined = settings.prefs.getBoolean("defined", false); localClock = settings.prefs.getBoolean("localClock", false);
        range = Math.max(0, Math.min(4, settings.prefs.getInt("range", 0))); selectedMetric = settings.prefs.getString("metric", "");
        quantity=settings.prefs.getString("quantity","watts");bars=settings.prefs.getBoolean("bars",false);
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
        dismissAnchoredMenu();
        if(Build.VERSION.SDK_INT>=29||checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED)flushPopupSettings();
        active = false;if(elementPopup!=null)elementPopup.pause(); epoch++; historyEpoch++; handler.removeCallbacks(poll); handler.removeCallbacks(expire);
        if (liveClient != null) liveClient.cancel(); if (metadataClient!=null)metadataClient.cancel();metadataLoading=false; if (historyClient != null) historyClient.cancel(); if (summaryClient != null) summaryClient.cancel();if(popupMonthClient!=null)popupMonthClient.cancel();
        live = null; history = null; historyLoading = false; screen.stop(); persistence(false); super.onStop();
    }
    @Override protected void onDestroy() { if(elementPopup!=null)elementPopup.dialog.dismiss();if(colourDialog!=null)colourDialog.dismiss();if(appearanceDialog!=null){appearanceDialog.setOnDismissListener(null);appearanceDialog.dismiss();} if(updates!=null)updates.destroy();handler.removeCallbacks(savePopupSettings);popupWorker.shutdownNow();viewSettingsWorker.shutdown();liveWorker.shutdownNow(); historyWorker.shutdownNow();metadataWorker.shutdownNow(); super.onDestroy(); }

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
    private Button changedChartButton(String name,boolean selected,Runnable action,String field){return trackChange(button(name,selected,action),field);}
    private TextView uiText(String value, float size, int color) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size); v.setTextColor(color);
        v.setTypeface(Typeface.create(theme.fontFamily, Typeface.NORMAL)); v.setIncludeFontPadding(false); return v;
    }
    private GradientDrawable uiBackground(int color, int border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(5); d.setStroke(1, border); return d;
    }
    private Button uiButton(String label, boolean selected, Runnable action) {
        Button b = new Button(this); b.setActivated(selected);b.setText(label); b.setAllCaps(false); b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 11.5f);
        b.setTypeface(Typeface.create(theme.fontFamily, Typeface.NORMAL)); b.setTextColor(selected ? theme.buttonTextOn : theme.buttonTextOn);
        b.setMinWidth(0); b.setMinimumWidth(0); b.setMinHeight(0); b.setMinimumHeight(0); b.setPadding(6,0,6,0);
        b.setBackground(uiBackground(theme.buttonBackground, selected ? theme.controls : theme.offButtonBorder)); b.setOnClickListener(v -> action.run()); return b;
    }
    private void build() {
        changedViews.clear();normalColours.clear();presetChanges=presetDifferences();
        // Preserve chart calculations/content; dialogs follow the shared companion theme.
        palette = new Palette(settings.prefs.getBoolean("light", false)); root = column(); root.setBackgroundColor(theme.background);
        status = uiText(connectionState, 10.5f, theme.text); status.setSingleLine(); status.setEllipsize(TextUtils.TruncateAt.END); status.setPadding(8,0,8,0);status.setVisibility(settingsScreen||hideStatus?View.GONE:View.VISIBLE);
        status.setContentDescription("Connection and freshness status"); root.addView(status,new LinearLayout.LayoutParams(-1,20));
        if(historyScreen && !settingsScreen){LinearLayout ranges=row();ranges.setPadding(8,0,8,6);for(int i=0;i<HistoryData.RANGES.length;i++){final int n=i;Button b=uiButton(HistoryData.RANGES[i],range==i,()->{range=n;offset=0;settings.prefs.edit().putInt("range",range).apply();build();loadHistory();});trackChange(b,"range");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,32,1);rp.setMargins(0,0,6,0);ranges.addView(b,rp);}root.addView(ranges);}
        content = column();content.setPadding(0,6,0,0); root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        BottomBar bottom = new BottomBar(this, theme, fullscreen); root.addView(bottom,new LinearLayout.LayoutParams(-1,54));
        Button presetButton=new ChangedButton(this,theme,"Preset",currentPreset,presetModified(),()->withDownloadsStorage(this::showPresetMenu));presetAnchor=presetButton;bottom.addControl(presetButton,0);
        boolean themeModified=!theme.sameColours(themeNames().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults());
        themeAnchor=new ChangedButton(this,theme,"Theme",theme.name,themeModified,()->withDownloadsStorage(this::showThemeMenu));bottom.addControl(themeAnchor,7);
        bottom.addControl(uiButton("Live data",!historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=false; historyEpoch++; build(); }),1);
        bottom.addControl(uiButton("Charts",historyScreen && !settingsScreen,() -> { settingsScreen=false; historyScreen=true; build(); loadHistory(); }),2);
        bottom.addControl(trackChange(new EqIconButton(this,theme,"keep","Keep screen on",keepScreen,fullscreen,() -> { keepScreen=!keepScreen; settings.prefs.edit().putBoolean("keepScreen",keepScreen).apply(); screen.setKeep(keepScreen); build(); }),"keepScreen"),3);
        bottom.addControl(trackChange(new EqIconButton(this,theme,"screen","Full screen",fullscreen,fullscreen,() -> { fullscreen=!fullscreen; settings.prefs.edit().putBoolean("fullscreen",fullscreen).apply(); windowMode(); build(); }),"fullscreen"),4);
        bottom.addControl(trackChange(new EqIconButton(this,theme,"lock","Lock gauge popups",locked,fullscreen,() -> { locked=!locked; settings.prefs.edit().putBoolean("locked",locked).apply(); build(); }),"locked"),5);
        bottom.addControl(new EqIconButton(this,theme,"settings","Settings",settingsScreen,fullscreen,this::showSettings),6);
        viewport = new DesignViewport(this,root,fullscreen); setContentView(viewport);
        viewport.setOnApplyWindowInsetsListener((view,insets) -> {
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets((fullscreen?0:WindowInsets.Type.systemBars())|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());view.setPadding(safe.left,safe.top,safe.right,safe.bottom);}
            else view.setPadding(fullscreen?0:insets.getSystemWindowInsetLeft(),fullscreen?0:insets.getSystemWindowInsetTop(),fullscreen?0:insets.getSystemWindowInsetRight(),fullscreen?Math.max(0,insets.getSystemWindowInsetBottom()-insets.getStableInsetBottom()):insets.getSystemWindowInsetBottom());return insets;
        }); viewport.requestApplyInsets();
        getWindow().setFlags(settingsScreen ? android.view.WindowManager.LayoutParams.FLAG_SECURE : 0,android.view.WindowManager.LayoutParams.FLAG_SECURE);
        if(settingsScreen) buildSettingsPage(); else if(historyScreen) buildHistory(); else buildDashboard();
        screen.apply(); windowMode(); updateLive();refreshChangedIndicators();
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
            public void detail(String id,MonitorData.Datum d,String name,boolean gauge){showElementDetail(id,d,name,gauge);}
            public void rename(String id,String original){editLabel(id,original);}
            public boolean locked(){return locked;}
            public int highlight(String id){return settings.highlight(id);}
            public boolean hidden(String id){return settings.hidden(id);}
            public boolean changed(String id,String original){return presetChanges.contains("label:"+id)||presetChanges.contains("elementHighlight:"+id)||presetChanges.contains("elementHidden:"+id);}
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
        MonitorData.retainRawBindings(readings, next);
        Map<String, MonitorData.Datum> bound = new LinkedHashMap<>();
        for (MonitorData.Datum datum : next.values()) {
            String id = settings.instrumentBinding(datum);
            bound.put(id, id.equals(datum.id) ? datum : new MonitorData.Datum(id,datum.name,datum.unit,datum.group,datum.quantity,datum.value,datum.flag,datum.metric,datum.receivedAt));
        }
        next = bound;
        boolean changed = liveDashboard!=null&&!liveDashboard.accepts(next); readings = next;
        if (!historyScreen && !settingsScreen) { if (changed || liveDashboard == null) buildDashboard(); else liveDashboard.update(readings); }
        if (live == null) status.setText(connectionState);
        else {
            double elapsed = (SystemClock.elapsedRealtime() - receivedMono) / 1000d;
            String logging = MonitorData.object(collectorStatus, "logging").optString("state", "unknown");
            status.setText(connectionState+"   ·   Pico " + (MonitorData.fresh(MonitorData.object(live, "pico"), elapsed) ? "live" : "—") + "   ·   SBMS " + (MonitorData.fresh(MonitorData.object(live, "sbms"), elapsed) ? "live" : "—") + "   ·   logging " + logging + "   ·   " + clock(now()));
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
        if(elementPopup!=null)elementPopup.pause();epoch++; historyEpoch++; summaryEpoch++;if(summaryClient!=null)summaryClient.cancel();if(popupMonthClient!=null)popupMonthClient.cancel();polls = 0; live = null; collectorStatus = null; history = null; historyLoading = false; piNowAt = 0; metrics = new ArrayList<>(); readings = new LinkedHashMap<>();
        handler.removeCallbacks(poll); if (liveClient != null) liveClient.cancel(); if (metadataClient!=null)metadataClient.cancel();metadataLoading=false; if (historyClient != null) historyClient.cancel();
        if (!active) return;
        if (!allowed()) { connectionState = "Local network permission is needed. Open Settings to connect."; updateLive(); return; }
        connectionState = "Connecting to Pi…"; liveClient = null; build();
        final int generation = epoch;
        liveWorker.execute(() -> {
            try {
                ApiClient prepared = client();
                handler.post(() -> { if (active && generation == epoch) { liveClient = prepared; handler.post(poll);if(elementPopup!=null)elementPopup.resume(); } });
            } catch (Exception e) { handler.post(() -> { if (active && generation == epoch) { connectionState = "Set the Pi address and API token in Settings."; updateLive(); } }); }
        });
    }
    private void poll() {
        if (!active || liveClient == null) return;
        final int generation = epoch; final ApiClient client = liveClient;
        liveWorker.execute(() -> {
            try {
                long requestAt=SystemClock.elapsedRealtime();JSONObject body = client.get("/api/v1/live");
                long responseAt=SystemClock.elapsedRealtime(),clockAt=Instant.parse(body.getString("now")).toEpochMilli();
                handler.post(() -> {
                    if (!active || generation != epoch) return;
                    live=body;receivedMono=requestAt;piNowAt=clockAt;piNowMono=responseAt;connectionState="Pi connected";
                    if(polls++ % 30 == 0||metrics.isEmpty())refreshMetadata(generation);
                    updateLive();handler.postDelayed(poll,1000);
                });
            } catch (Exception e) {
                handler.post(() -> { if (!active || generation != epoch)return;
                    connectionState="Pi unavailable. Check Wi-Fi, address, token and TLS trust. Retrying…";
                    // Let the bounded last successful HTTP snapshot expire normally.
                    // A failed catalogue/status refresh never clears current readings.
                    updateLive();handler.postDelayed(poll,1000);
                });
            }
        });
    }
    private void refreshMetadata(int generation){refreshMetadata(generation,null);}
    private void refreshMetadata(int generation,ApiClient prepared){
        if(metadataLoading||!active)return;metadataLoading=true;
        metadataWorker.execute(()->{
            if(!active||generation!=epoch)return;
            List<MonitorData.Metric> catalogue=null;JSONObject health=null;
            try{ApiClient client=prepared==null?client():prepared;metadataClient=client;
                try{catalogue=MonitorData.catalogue(client.get("/api/v1/metrics"));}catch(Exception ignored){}
                if(active&&generation==epoch)try{health=client.get("/api/v1/status");}catch(Exception ignored){}
            }catch(Exception ignored){}
            final List<MonitorData.Metric> result=catalogue;final JSONObject statusResult=health;
            handler.post(()->{if(!active||generation!=epoch)return;metadataLoading=false;metadataClient=null;
                if(result!=null)metrics=result;if(statusResult!=null)collectorStatus=statusResult;updateLive();
                if(historyScreen&&!settingsScreen&&!historyLoading&&result!=null){buildHistory();loadHistory();}
            });
        });
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) { super.onRequestPermissionsResult(requestCode, permissions, results); if (requestCode == 37) restartConnection(); if(requestCode==28){Runnable action=storageAction;storageAction=null;if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED&&action!=null)action.run();else toast("Storage access is needed to save presets, themes and exports in Downloads.");} }

    private List<MonitorData.Metric> historyMetrics(){List<MonitorData.Metric> result=new ArrayList<>(metrics);for(boolean inverter:new boolean[]{false,true}){MonitorData.Metric derived=DerivedHistory.metric(metrics,inverter);if(derived!=null)result.add(derived);}MonitorData.Metric bms=DerivedHistory.bmsLoad(metrics);if(bms!=null)result.add(bms);return result;}
    private MonitorData.Metric selected() {
        for (MonitorData.Metric m : historyMetrics()) if (m.id.equals(selectedMetric)) return m;
        for (MonitorData.Metric m : metrics) if (m.channel.equals("sbmsBattery")) return m;
        if (!metrics.isEmpty()) return metrics.get(0); return null;
    }
    private String derivedDisplayId(MonitorData.Metric m){for(MonitorData.Datum d:readings.values())if(d.metric!=null&&d.metric.id.equals(m.id)&&d.quantity.equals("current"))return d.id;return m.id.equals("derived:pico-load")?"pico:load-sum":m.id.equals("derived:sbms-load")?"sbms:load-sum":"pico:inverter";}
    private String metricLabel(MonitorData.Metric m) { if(DerivedHistory.isDerived(m))return settings.label(derivedDisplayId(m),m.name)+" · "+MonitorData.fieldName(quantityFor(m));if(m.channel.equals("externalLoad"))return m.name+" · raw externalLoad · "+MonitorData.fieldName(quantityFor(m));return settings.label(m.alias + ":" + quantityFor(m), m.name + " · " + MonitorData.fieldName(quantityFor(m))); }
    private String quantityFor(MonitorData.Metric m) { return m.kind.equals("electrical") ? quantity.equals("Wh") ? "watts" : quantity.equals("Ah") ? "current" : quantity : m.kind.equals("barometer") ? "pressure" : m.kind; }
    private String unit(MonitorData.Metric m) { return m.kind.equals("electrical") ? quantity.equals("watts") ? "W" : quantity.equals("current") ? "A" : quantity.equals("voltage") ? "V" : quantity.equals("stateOfCharge") ? "%" : quantity : m.kind.equals("barometer") ? "hPa" : m.kind.equals("temperature") ? "°C" : "V"; }
    private void buildHistory() {
        removeTrackedChildren(content);content.removeAllViews(); MonitorData.Metric m = selected();
        LinearLayout controls = row(); HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.addView(controls); content.addView(scroll);
        controls.addView(changedChartButton(m == null ? "Select measurement" : metricLabel(m), true, this::selectMetric,"metric"));
        controls.addView(changedChartButton(defined ? "Defined UTC" : "Rolling", defined, () -> { defined = !defined; offset = 0; settings.prefs.edit().putBoolean("defined", defined).apply(); history = null; buildHistory(); loadHistory(); },"defined"));
        controls.addView(button("‹ Previous", false, () -> { offset--; loadHistory(); }));
        controls.addView(button("Now", offset == 0, () -> { offset = 0; loadHistory(); }));
        controls.addView(button("Next ›", false, () -> { if (offset < 0) offset++; loadHistory(); }));
        controls.addView(button("Refresh", false, this::loadHistory));
        if (m != null && m.kind.equals("electrical")) {
            LinearLayout quantities = row(); HorizontalScrollView qscroll = new HorizontalScrollView(this); qscroll.addView(quantities); content.addView(qscroll);
            for (String q : m.role.equals("battery") ? new String[]{"watts", "current", "voltage", "stateOfCharge", "Wh", "Ah"} : DerivedHistory.isDerived(m)?new String[]{"watts","current","Wh","Ah"}:new String[]{"watts", "current", "voltage", "Wh", "Ah"}) quantities.addView(changedChartButton(q.equals("stateOfCharge") ? "SOC" : q.equals("watts") ? "W" : q.equals("current") ? "A" : q.equals("voltage") ? "V" : q, quantity.equals(q), () -> { quantity = q; if (!(q.equals("Wh") || q.equals("Ah"))) bars = false; buildHistory(); },"quantity"));
            if (!quantity.equals("voltage") && !quantity.equals("stateOfCharge")) quantities.addView(changedChartButton(bars ? "Bars" : "Lines", bars, () -> { bars = !bars; if (bars && !(quantity.equals("Wh") || quantity.equals("Ah"))) quantity = quantity.equals("current") ? "Ah" : "Wh"; buildHistory(); },"bars"));
        } else if (m != null && m.kind.equals("temperature")) {
            LinearLayout quantities = row(); content.addView(quantities);
            for (String q : new String[]{"value", "min", "max"}) quantities.addView(changedChartButton(q.equals("value") ? "Last reading" : q.equals("min") ? "Low" : "High", quantity.equals(q), () -> { quantity = q; bars = false; buildHistory(); },"quantity"));
        }
        historyNote = text(m == null ? "Connect to load the measurement catalogue." : "Select a period to load retained history.", 12, palette.muted); content.addView(historyNote);
        chart = new ChartView(this, palette); content.addView(chart, new LinearLayout.LayoutParams(-1, 0, 1));
        Button export = button("Export this range as CSV", false, this::exportHistory); content.addView(export);
        showHistory();settings.prefs.edit().putString("quantity",quantity).putBoolean("bars",bars).apply();refreshChangedIndicators();
    }
    private void selectMetric() {
        if (metrics.isEmpty()) { toast("Connect to the Pi first."); return; }
        List<MonitorData.Metric> choices=historyMetrics();String[] names = new String[choices.size()]; for (int i = 0; i < names.length; i++) names[i] = metricLabel(choices.get(i));
        themedAlertBuilder().setTitle("Measurement").setItems(names, (dialog, n) -> {
            selectedMetric = choices.get(n).id; settings.prefs.edit().putString("metric", selectedMetric).apply(); quantity = choices.get(n).kind.equals("electrical") ? "watts" : "value"; bars = false; history = null; buildHistory(); loadHistory();
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
        String direction = bars ? PopupHistory.positiveCurrent(history.metric,quantity)?history.metric.role.equals("load")?"  ·  usage magnitude":"  ·  supply magnitude":verified ? "  ·  green in / amber out" + (history.metric.role.equals("battery") ? " (net battery flow)" : "") : "  ·  unclassified signed net bars" : "";
        historyNote.setText(metricLabel(history.metric) + " · " + unit(history.metric) + " · " + history.window.resolution + " · " + history.rows.size() + " recorded periods" + direction + "\n" + clock(history.window.from) + " → " + clock(history.window.to) + "\nCheckpoint: " + history.savedAt + "\nWhole UTC buckets; edge/partial buckets are not prorated. Tap a point for coverage.");
    }
    private void inspect(HistoryData.Record r) {
        if (history == null) return;
        Double value = PopupHistory.displayValue(history.metric,quantity,r.value(quantity, history.metric.kind)), coverage = r.coverage(quantity, history.metric.kind);
        String message = clock(r.start) + " → " + clock(r.end) + "\n" + (value == null ? "—" : String.format(Locale.UK, "%.3f %s", value, unit(history.metric)))
                + "\nCoverage: " + (coverage == null ? "last reading / no duration coverage" : String.format(Locale.UK, "%.2f / %.0f seconds", coverage, (r.end.toEpochMilli() - r.start.toEpochMilli()) / 1000d))
                + "\n" + (r.partial ? "Partial checkpoint" : "Closed bucket; coverage may contain gaps") + "\nSource: " + history.metric.source + " · " + history.metric.id;
        if (bars) message += "\nRecorded forward: " + r.raw.opt("forward" + quantity) + "\nRecorded reverse: " + r.raw.opt("reverse" + quantity);
        themedAlertBuilder().setTitle(metricLabel(history.metric)).setMessage(message).setPositiveButton("Close", null).show();
    }
    private void showDetail(MonitorData.Datum d,String fallback){showDetail(d,fallback,false);}
    private void showDetail(MonitorData.Datum d, String fallback,boolean gauge) {showElementDetail(d==null?"pending:"+fallback:d.id,d,fallback,gauge);}
    private void showElementDetail(String id,MonitorData.Datum d,String fallback,boolean gauge){new ElementPopup(id,d,fallback,gauge).show();}
    private volatile ElementPopup elementPopup;
    private final class ElementPopup {
        final String id,field;final MonitorData.Metric metric;final boolean current,positive,temperature,historyAvailable;
        final android.app.Dialog dialog=new android.app.Dialog(MainActivity.this);
        final LinearLayout panel=styledDialogRoot();final PopupSelection selection;
        final TextView[][] periods=new TextView[2][4];TextView firstValue,secondValue,samples,caption,line,sum,quantityButton,valuesButton;
        PopupChartView graph;android.widget.FrameLayout graphFrame;HistoryData.Result data;PopupHistory.Plan plan;
        java.util.List<java.time.YearMonth> months;java.time.YearMonth monthsAt;boolean loadingMonths,monthsError,busy;int finalRetries;java.time.Instant finalWindow;
        volatile boolean open;volatile int requestVersion,monthVersion;volatile ApiClient dataClient,monthClient;
        final Runnable refreshMonths=()->{if(isCurrent()){loadMonths(true);scheduleMonths();}};
        final Runnable refresh=()->{if(isCurrent()){reload(true);if(!java.time.YearMonth.from(now().atZone(ZoneId.systemDefault())).equals(monthsAt))loadMonths(true);}};
        ElementPopup(String key,MonitorData.Datum datum,String fallback,boolean gauge){
            id=key;metric=PopupHistory.binding(id,datum,metrics);field=datum==null?"current":datum.quantity.equals("starterVoltage")?"voltage":datum.quantity;
            GaugeScale.Kind kind=GaugeScale.kind("gauges",fallback);current=datum!=null?datum.quantity.equals("current"):gauge&&(kind==GaugeScale.Kind.LOAD||kind==GaugeScale.Kind.PV||kind==GaugeScale.Kind.BATTERY);
            positive=current&&(metric==null||!metric.role.equals("battery"));temperature=metric!=null&&metric.kind.equals("temperature");selection=popupSettings.get(id);
            String saved=settings.label(id,fallback),title=saved.equals(fallback)?PopupHistory.title(id,datum,fallback):saved;
            if(datum!=null&&datum.unit.equals("°C")&&!title.toLowerCase(Locale.UK).endsWith("temperature"))title+=" temperature";
            panel.setPadding(dialogDp(6),dialogDp(6),dialogDp(6),dialogDp(6));TextView heading=uiText(title,dialogTextSize(22),theme.titleText);heading.setGravity(Gravity.CENTER);heading.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));panel.addView(heading,new LinearLayout.LayoutParams(-1,dialogDp(38)));
            historyAvailable=current||metric!=null&&(field.equals("stateOfCharge")||field.equals("voltage")||metric.kind.equals("voltage")||metric.kind.equals("barometer")||temperature);
            if(historyAvailable){
                plan=PopupHistory.plan(now(),selection,ZoneId.systemDefault());if(metric!=null&&!PopupHistory.supports(metric,plan.query.resolution)){selection.period=1;selection.fixed=false;}
                LinearLayout header=row();header.setBaselineAligned(false);header.setPadding(dialogDp(PopupChartView.LABEL_INSET),0,dialogDp(PopupChartView.LABEL_INSET),0);
                LinearLayout readings=column();String first=current?"Current:":temperature?"Min:":"Latest:",second=current?"Energy:":temperature?"Max:":"";
                firstValue=headerReading(readings,first);if(!second.isEmpty())secondValue=headerReading(readings,second);else readings.addView(new View(MainActivity.this),new LinearLayout.LayoutParams(1,dialogDp(20)));
                header.addView(readings,new LinearLayout.LayoutParams(-2,dialogDp(40)));
                LinearLayout middle=column();samples=uiText("Samples: —/—",dialogTextSize(11.5f),theme.text);caption=uiText("",dialogTextSize(11.5f),theme.text);
                for(TextView text:new TextView[]{samples,caption}){text.setGravity(Gravity.CENTER);text.setIncludeFontPadding(false);text.setSingleLine();text.setEllipsize(TextUtils.TruncateAt.END);middle.addView(text,new LinearLayout.LayoutParams(-1,dialogDp(20)));}
                LinearLayout.LayoutParams middleSize=new LinearLayout.LayoutParams(0,dialogDp(40),1);middleSize.setMargins(dialogDp(6),0,dialogDp(6),0);header.addView(middle,middleSize);
                LinearLayout controls=row();valuesButton=popupButton("Values",selection.values,()->{selection.values=!selection.values;remember();draw();});controls.addView(valuesButton,new LinearLayout.LayoutParams(dialogDp(48),dialogDp(40)));
                if(current){quantityButton=popupButton(selection.power?"Wh":"Ah",selection.power,()->{selection.power=!selection.power;remember();draw();});LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(dialogDp(42),dialogDp(40));q.leftMargin=dialogDp(6);controls.addView(quantityButton,q);}header.addView(controls,new LinearLayout.LayoutParams(-2,dialogDp(40)));popupRow(panel,header);
                graphFrame=new android.widget.FrameLayout(MainActivity.this);graph=new PopupChartView(MainActivity.this,theme,dialogTextSize(1));graphFrame.addView(graph,new android.widget.FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dialogDp(280));fp.topMargin=dialogDp(6);panel.addView(graphFrame,fp);
                LinearLayout periodArea=row();periodArea.setBaselineAligned(false);LinearLayout periodBox=popupBox();LinearLayout.LayoutParams timeSize=new LinearLayout.LayoutParams(0,-2,4);periodArea.addView(periodBox,timeSize);
                for(int r=0;r<2;r++){final boolean fixed=r==1;LinearLayout buttons=row();buttons.setBaselineAligned(false);for(int i=0;i<4;i++){final int period=i;TextView button=popupButton("",false,()->{if(fixed&&period==3){chooseMonth();return;}selection.fixed=fixed;selection.period=period;remember();reload(false);});periods[r][i]=button;popupControl(buttons,button,i);}if(r==0)periodBox.addView(buttons,new LinearLayout.LayoutParams(-1,-2));else popupRow(periodBox,buttons);}
                LinearLayout modeBox=popupBox();line=popupButton(selection.bars?"Bar":"Line",selection.bars,()->{selection.bars=!selection.bars;remember();draw();});modeBox.addView(line,new LinearLayout.LayoutParams(-1,dialogDp(28)));
                sum=popupButton("Sum",current&&selection.sum,()->{if(current){selection.sum=!selection.sum;remember();draw();}});sum.setEnabled(current);sum.setAlpha(current?1:.4f);LinearLayout.LayoutParams sumSize=new LinearLayout.LayoutParams(-1,dialogDp(28));sumSize.topMargin=dialogDp(6);modeBox.addView(sum,sumSize);LinearLayout.LayoutParams modeSize=new LinearLayout.LayoutParams(0,-2,1);modeSize.leftMargin=dialogDp(6);periodArea.addView(modeBox,modeSize);popupRow(panel,periodArea);
                dialog.setOnShowListener(v->{if(elementPopup!=this||!dialog.isShowing())return;open=true;reload(false);loadMonths(true);scheduleMonths();});
            }
            LinearLayout footerBox=popupBox(),footer=row();footer.setBaselineAligned(false);footerBox.addView(footer,new LinearLayout.LayoutParams(-1,-2));popupRow(panel,footerBox);addElementOptions(footer,id,()->dialog.dismiss(),metric,field);
            dialog.setOnDismissListener(v->{open=false;pause();if(elementPopup==this)elementPopup=null;dismissAnchoredMenu();removeTrackedChildren(panel);flushPopupSettings();});
        }
        LinearLayout popupBox(){LinearLayout box=column();box.setPadding(dialogDp(6),dialogDp(6),dialogDp(6),dialogDp(6));box.setBackground(roundedBackground(theme.panel,theme.border,1.1f,5));return box;}
        TextView headerReading(LinearLayout column,String label){LinearLayout row=row();TextView name=uiText(label,dialogTextSize(16),theme.text);name.setSingleLine();name.setIncludeFontPadding(false);name.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);int labelWidth=Math.round(name.getPaint().measureText(current?"Current:":temperature?"Max:":"Latest:"));row.addView(name,new LinearLayout.LayoutParams(labelWidth,dialogDp(20)));TextView value=uiText("—",dialogTextSize(16),theme.text);value.setSingleLine();value.setIncludeFontPadding(false);value.setPadding(dialogDp(6),0,0,0);value.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);value.setEllipsize(TextUtils.TruncateAt.END);row.addView(value,new LinearLayout.LayoutParams(dialogDp(116),dialogDp(20)));column.addView(row);return value;}
        boolean isCurrent(){return active&&open&&elementPopup==this&&dialog.isShowing();}
        boolean valid(int version,int connection){return isCurrent()&&version==requestVersion&&connection==epoch;}
        void show(){
            if(elementPopup!=null)elementPopup.dialog.dismiss();elementPopup=this;
            if(graphFrame!=null){int width=Math.max(1,Math.min(dialogDp(560),viewport.getWidth()-dialogDp(30))),height=Math.max(1,viewport.getHeight()-dialogDp(24));panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));graphFrame.getLayoutParams().height=Math.max(dialogDp(140),dialogDp(280)-Math.max(0,panel.getMeasuredHeight()-height));}
            showStyledDialog(dialog,panel,560);if(!historyAvailable)open=true;refreshChangedIndicators();
        }
        void remember(){try{popupSettings.put(id,selection);handler.removeCallbacks(savePopupSettings);handler.postDelayed(savePopupSettings,250);}catch(Exception e){toast(e.getMessage());}}
        void pause(){handler.removeCallbacks(refresh);handler.removeCallbacks(refreshMonths);requestVersion++;monthVersion++;busy=false;loadingMonths=false;if(dataClient!=null)dataClient.cancel();if(monthClient!=null)monthClient.cancel();}
        void resume(){if(isCurrent()&&historyAvailable){reload(true);loadMonths(true);scheduleMonths();}}
        void scheduleMonths(){handler.removeCallbacks(refreshMonths);if(!isCurrent()||metric==null||!PopupHistory.supports(metric,"day"))return;java.time.Instant at=now(),next=java.time.YearMonth.from(at.atZone(ZoneId.systemDefault())).plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();handler.postDelayed(refreshMonths,Math.max(1000,java.time.Duration.between(at,next).toMillis()));}
        void schedule(boolean retry){handler.removeCallbacks(refresh);if(!isCurrent()||!historyAvailable)return;java.time.Instant next=PopupHistory.nextRefresh(now(),selection,ZoneId.systemDefault());if(retry)handler.postDelayed(refresh,15000);else if(next!=null)handler.postDelayed(refresh,Math.max(1000,java.time.Duration.between(now(),next).toMillis()));}
        void updateButtons(){
            java.time.YearMonth selected=selection.month.isEmpty()?java.time.YearMonth.from(now().atZone(ZoneId.systemDefault())).minusMonths(1):java.time.YearMonth.parse(selection.month);
            for(int r=0;r<2;r++)for(int p=0;p<4;p++){TextView button=periods[r][p];String name=SummaryPeriods.label(now(),p,r==1,ZoneId.systemDefault());if(r==1&&p==3)name=monthsError?"Retry months":PopupHistory.monthLabel(selected,now(),ZoneId.systemDefault())+" ▾";
                button.setText(name);popupButtonState(button,selection.fixed==(r==1)&&selection.period==p);boolean available=PopupHistory.supports(metric,p==0?"minute":p<3?"hour":"day")&&(r==0||p!=3||monthsError||months==null||!months.isEmpty());button.setEnabled(available);button.setAlpha(available?1:.4f);if(r==1&&p==3)button.setContentDescription(monthsError?"Retry recorded months":months!=null&&months.isEmpty()?"No earlier daily readings":name);
            }
        }
        void reload(boolean automatic){
            if(!historyAvailable||!isCurrent())return;if(automatic&&busy){schedule(true);return;}handler.removeCallbacks(refresh);busy=true;
            final PopupHistory.Plan requested=PopupHistory.plan(now(),selection,ZoneId.systemDefault());final int version=++requestVersion,connection=epoch;if(dataClient!=null)dataClient.cancel();
            if(!automatic||data==null){plan=requested;data=null;updateButtons();caption.setText(PopupHistory.caption(plan,selection.fixed));draw();}
            if(!PopupHistory.supports(metric,requested.query.resolution)||!requested.query.to.isAfter(requested.query.from)){busy=false;schedule(false);return;}
            popupWorker.execute(()->{try{if(!valid(version,connection))return;ApiClient client=client();dataClient=client;summaryClient=client;if(!valid(version,connection)){client.cancel();return;}HistoryData.Result result=HistoryData.fetch(client,metric,requested.query);
                handler.post(()->{if(valid(version,connection)){busy=false;data=result;plan=requested;caption.setText(PopupHistory.caption(plan,selection.fixed));updateButtons();draw();if(!requested.query.to.equals(finalWindow)){finalWindow=requested.query.to;finalRetries=0;}boolean finishing=PopupHistory.awaitingFinalCheckpoint(result,selection);schedule(finishing&&finalRetries++<3);}});
            }catch(Exception ignored){handler.post(()->{if(valid(version,connection)){busy=false;if(!automatic)toast("Popup history could not be loaded. Retrying.");schedule(true);}});}});
        }
        void draw(){
            if(graph==null)return;String q=current?(selection.power?"watts":"current"):field;PopupHistory.Series series=data==null?null:PopupHistory.series(data,plan,q,current&&selection.sum,positive,selection.bars);graph.show(plan,series,selection.bars,selection.values,metric!=null&&metric.kind.equals("barometer"));
            samples.setText(data==null?"Samples: —/"+SummaryPeriods.expected(plan.query,plan.zone):"Samples: "+series.captured+"/"+series.expected);
            if(current){SummaryPeriods.Totals t=data==null?new SummaryPeriods.Totals():SummaryPeriods.totals(data,positive);firstValue.setText(amount(t.charge,"Ah"));secondValue.setText(amount(t.energy,"Wh"));quantityButton.setText(selection.power?"Wh":"Ah");popupButtonState(quantityButton,selection.power);}
            else if(temperature){firstValue.setText(reading(series==null?null:series.minimum,"°C"));secondValue.setText(reading(series==null?null:series.maximum,"°C"));}
            else firstValue.setText(reading(series==null?null:series.latest,series==null?"":series.unit));
            popupButtonState(valuesButton,selection.values);line.setText(selection.bars?"Bar":"Line");popupButtonState(line,selection.bars);popupButtonState(sum,current&&selection.sum);
        }
        String amount(Double value,String unit){return value==null?"—":String.format(Locale.UK,"%.3f %s",value,unit);}
        String reading(Double value,String unit){return value==null?"—":String.format(Locale.UK,unit.equals("V")?"%.3f %s":unit.equals("°C")?"%.1f %s":"%.0f %s",value,unit);}
        void loadMonths(boolean force){
            if(metric==null||loadingMonths||!PopupHistory.supports(metric,"day")||!isCurrent()||!force&&months!=null)return;loadingMonths=true;monthsError=false;updateButtons();final int connection=epoch,version=++monthVersion;
            java.time.Instant at=now();java.time.ZoneId zone=ZoneId.systemDefault();
            popupWorker.execute(()->{try{if(!isCurrent()||connection!=epoch||version!=monthVersion)return;ApiClient client=client();monthClient=client;popupMonthClient=client;if(!isCurrent()||connection!=epoch||version!=monthVersion){client.cancel();return;}java.util.List<java.time.YearMonth> recorded=PopupHistory.months(client,metric,at,zone,field);java.time.YearMonth current=java.time.YearMonth.from(at.atZone(zone));recorded.removeIf(m->!m.isBefore(current));
                handler.post(()->{if(isCurrent()&&connection==epoch&&version==monthVersion){months=recorded;monthsAt=current;loadingMonths=false;monthsError=false;updateButtons();}});
            }catch(Exception e){handler.post(()->{if(isCurrent()&&connection==epoch&&version==monthVersion){loadingMonths=false;monthsError=true;updateButtons();}});}});
        }
        void chooseMonth(){
            if(monthsError){toast("Could not load recorded months. Retrying.");loadMonths(true);return;}if(months==null||loadingMonths){toast("Recorded months are loading.");return;}if(months.isEmpty()){toast("No earlier daily readings are available.");return;}
            List<String> labels=new ArrayList<>();List<Runnable> actions=new ArrayList<>();for(java.time.YearMonth month:months){labels.add(PopupHistory.monthLabel(month,now(),ZoneId.systemDefault()));actions.add(()->{selection.month=month.toString();selection.fixed=true;selection.period=3;remember();reload(false);});}
            anchoredMenu(periods[1][3],true,labels,actions,months.indexOf(selection.month.isEmpty()?java.time.YearMonth.from(now().atZone(ZoneId.systemDefault())).minusMonths(1):java.time.YearMonth.parse(selection.month)));
        }
    }
    private void flushPopupSettings(){if(popupSettings!=null&&popupSettings.hasChanges()&&!viewSettingsWorker.isShutdown()){handler.removeCallbacks(savePopupSettings);savePopupSettings.run();}}
    private void popupRow(LinearLayout panel,View child){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dialogDp(6),0,0);panel.addView(child,lp);}
    private TextView popupButton(String label,boolean selected,Runnable action){TextView button=uiText(label,dialogTextSize(11.5f),theme.buttonTextOn);button.setGravity(Gravity.CENTER);button.setSingleLine();button.setEllipsize(TextUtils.TruncateAt.END);button.setPadding(dialogDp(6),0,dialogDp(6),0);button.setFocusable(true);button.setClickable(true);popupButtonState(button,selected);button.setOnClickListener(v->action.run());return button;}
    private void popupButtonState(TextView button,boolean selected){button.setActivated(selected);button.setTextColor(theme.buttonTextOn);android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},roundedBackground(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f),theme.controls,1.1f,5));states.addState(new int[]{android.R.attr.state_focused},roundedBackground(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f),theme.controls,1.1f,5));states.addState(new int[]{},roundedBackground(selected?ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f):theme.buttonBackground,selected?theme.controls:theme.offButtonBorder,1.1f,5));button.setBackground(states);}
    private void popupControl(LinearLayout row,TextView button,int index){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dialogDp(28),1);if(index>0)lp.setMargins(dialogDp(6),0,0,0);row.addView(button,lp);}
    private void addElementOptions(LinearLayout row,String id,Runnable close,MonitorData.Metric metric,String field){
        List<TextView> highlights=new ArrayList<>();
        for(int i=0;i<2;i++){final int choice=i+1;TextView button=popupButton("Highlight "+choice,settings.highlight(id)==choice,()->{
            settings.setHighlight(id,settings.highlight(id)==choice?0:choice);for(int j=0;j<highlights.size();j++)popupButtonState(highlights.get(j),settings.highlight(id)==j+1);if(liveDashboard!=null)liveDashboard.refreshChanges();refreshChangedIndicators();
        });android.graphics.drawable.GradientDrawable swatch=roundedBackground(choice==1?theme.gaugeHighlight:theme.highlight2,theme.border,1,3);swatch.setBounds(0,0,dialogDp(10),dialogDp(10));button.setCompoundDrawables(null,null,swatch,null);button.setCompoundDrawablePadding(dialogDp(3));highlights.add(button);popupControl(row,button,i);}
        final TextView[] hide=new TextView[1];hide[0]=popupButton("Hide",settings.hidden(id),()->{boolean hidden=!settings.hidden(id);android.content.SharedPreferences.Editor edit=settings.prefs.edit();if(hidden)edit.putBoolean("elementHidden:"+id,true);else edit.remove("elementHidden:"+id);edit.apply();popupButtonState(hide[0],hidden);if(liveDashboard!=null)liveDashboard.refreshChanges();refreshChangedIndicators();});hide[0].setContentDescription("Hide contents");popupControl(row,hide[0],2);
        TextView chart=popupButton("Chart",false,()->{if(metric==null)return;close.run();selectedMetric=metric.id;quantity=field;settings.prefs.edit().putString("metric",selectedMetric).putString("quantity",quantity).apply();offset=0;historyScreen=true;build();loadHistory();});chart.setEnabled(metric!=null);chart.setAlpha(metric==null?.4f:1);popupControl(row,chart,3);
        popupControl(row,popupButton("Close",false,close),4);
    }
    private void clearDialogDim(android.app.Dialog dialog){if(dialog.getWindow()!=null){dialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);android.view.WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();lp.dimAmount=0;dialog.getWindow().setAttributes(lp);}}
    private volatile int summaryEpoch;
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
    private void removeTrackedChildren(View view){
        changedViews.remove(view);if(view instanceof TextView)normalColours.remove(view);
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)removeTrackedChildren(((ViewGroup)view).getChildAt(i));
    }
    private <T extends View> T trackChange(T view,String field){
        changedViews.put(view,field);if(view instanceof TextView)normalColours.put((TextView)view,((TextView)view).getCurrentTextColor());
        colourChangedView(view,field);return view;
    }
    private void colourChangedView(View view,String field){
        boolean changed=presetChanges.contains(field);
        if(view instanceof TextView)((TextView)view).setTextColor(changed?theme.changedIndicator:normalColours.get((TextView)view));
        else if(view instanceof EqIconButton)((EqIconButton)view).changed(changed);
    }
    private void buildSettingsPage() {
        content.removeAllViews();ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout columns=row();columns.setGravity(Gravity.TOP);columns.setPadding(8,0,8,0);scroll.addView(columns);content.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout left=column(),right=column();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,6,0);columns.addView(left,lp);columns.addView(right,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout connection=settingsGroup(left,"CONNECTION");
        TextView help=uiText("Pi API address and private token from api.json. HTTP is for a trusted LAN; HTTPS verifies its certificate.",12,theme.text);help.setPadding(0,0,0,6);connection.addView(help);
        EditText address=new EditText(this);address.setHint("http://PI_HOST:8080");address.setSingleLine();address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);address.setText(settings.prefs.getString("address",""));styleEditor(address);trackChange(address,"connection.address");connection.addView(address,new LinearLayout.LayoutParams(-1,42));
        EditText token=new EditText(this);token.setHint(settings.prefs.contains("token")?"Blank keeps saved token":"Private API token");token.setSingleLine();token.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);token.setSaveEnabled(false);styleEditor(token);trackChange(token,"connection.token");connection.addView(token,new LinearLayout.LayoutParams(-1,42));
        settingButton(connection,"Save & connect",()->{
            String enteredToken=token.getText().toString().trim(),enteredAddress=address.getText().toString();
            liveWorker.execute(()->{try{settings.saveConnection(enteredAddress,enteredToken.isEmpty()?settings.token():enteredToken);handler.post(()->{if(isDestroyed())return;token.setText("");refreshChangedIndicators();if(active){if(!allowed())requestPermissions(new String[]{LAN_PERMISSION},37);else restartConnection();}});}catch(Exception e){handler.post(()->toast(e instanceof IllegalArgumentException?e.getMessage():"Could not save credentials. Re-enter the token."));}});
        });
        LinearLayout display=settingsGroup(right,"DISPLAY & APP");
        trackChange(setting(display,"Hide connection status",hideStatus,on->{hideStatus=on;settings.prefs.edit().putBoolean("hideStatus",on).apply();status.setVisibility(settingsScreen||on?View.GONE:View.VISIBLE);}),"hideStatus");
        trackChange(setting(display,"Persistent app notification",persistent,on->{persistent=on;settings.prefs.edit().putBoolean("persistent",on).apply();if(on&&Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},33);persistence(true);}),"persistent");
        trackChange(setting(display,"Display local clock",localClock,on->{localClock=on;settings.prefs.edit().putBoolean("localClock",on).apply();updateLive();}),"localClock");
        View spacer=new View(this);right.addView(spacer,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=column();footer.setPadding(6,6,6,6);footer.setBackground(uiBackground(theme.panel,theme.border));right.addView(footer,new LinearLayout.LayoutParams(-1,-2));
        final int[] intervals={0,1,3,6,12,24};final String[] labelsInterval={"Off","1hr","3hr","6hr","12hr","24hr"};
        int current=settings.prefs.getInt("update.intervalHours",24),index=5;for(int i=0;i<intervals.length;i++)if(intervals[i]==current)index=i;
        LinearLayout upper=row();Button frequencyButton=uiButton("Updates: "+labelsInterval[index]+"  ▾",true,()->{});trackChange(frequencyButton,"updateIntervalHours");upper.addView(frequencyButton,new LinearLayout.LayoutParams(0,41,1));View future=new View(this);future.setBackground(uiBackground(theme.buttonBackground,theme.offButtonBorder));LinearLayout.LayoutParams reserved=new LinearLayout.LayoutParams(0,41,1);reserved.setMargins(6,0,0,0);upper.addView(future,reserved);footer.addView(upper);
        frequencyButton.setOnClickListener(v->{List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();for(int i=0;i<intervals.length;i++){final int choice=i;names.add(labelsInterval[i]);actions.add(()->{settings.prefs.edit().putInt("update.intervalHours",intervals[choice]).apply();frequencyButton.setText("Updates: "+labelsInterval[choice]+"  ▾");updates.schedule();refreshChangedIndicators();});}anchoredMenu(frequencyButton,true,names,actions,-1);});
        LinearLayout lower=row();lower.addView(uiButton("Changelog / Update",true,()->updates.showChangelog()),new LinearLayout.LayoutParams(0,41,1));Button back=uiButton("Back",true,()->{settingsScreen=false;historyScreen=false;build();});LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(0,41,1);backLp.setMargins(6,0,0,0);lower.addView(back,backLp);LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,41);rowLp.setMargins(0,6,0,0);footer.addView(lower,rowLp);
        TextView version=uiText("Version "+BuildConfig.VERSION_NAME+" / build "+BuildConfig.VERSION_CODE,12.5f,theme.text);version.setGravity(Gravity.CENTER);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,25);vp.setMargins(0,6,0,0);footer.addView(version,vp);

    }
    private void persistence(boolean visible) {
        Intent service=new Intent(this,PersistentService.class).putExtra("visible",visible);
        if(!persistent){stopService(service);return;}
        try { if(visible)startService(service);else startForegroundService(service); }catch(RuntimeException e){toast("Android could not keep the app notification active.");}
    }
    private void styleEditor(EditText v){v.setTextColor(theme.text);v.setHintTextColor(theme.buttonTextOn);v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,14);v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.controls));v.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));}
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
    private void showThemeMenu(){List<String> available=themeNames();String issue=appearance.takeProblem();if(issue!=null)toast(issue);List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE THEME AS…");actions.add(()->saveTheme(null));if(themeNames().contains(theme.name)&&!theme.sameColours(appearance.load(theme.name))){names.add("UPDATE \""+theme.name+"\"");actions.add(()->saveTheme(theme.name));}names.add("MANAGE THEMES…");actions.add(()->manage(true));names.add("EDIT THEME");actions.add(this::showThemeEditor);names.add("EDIT DISPLAY LABELS");actions.add(this::labelSettings);names.add("System default");actions.add(()->applyTheme(ThemeConfig.defaults()));for(String name:available){names.add(name);actions.add(()->applyTheme(appearance.load(name)));}menu("THEMES",names,actions);}
    private void applyTheme(ThemeConfig value){if(value==null){toast("Theme file is unavailable or malformed; current colours were kept.");return;}value.fontFamily=theme.fontFamily;theme=value;appearance.active(theme);build();refreshAppearance();}
    private void confirmOverwrite(String name,Runnable action){
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(10),dialogDp(7),dialogDp(10),dialogDp(9));
        panel.addView(styledTitle("Save changes to \""+name+"\"?"));
        TextView message=uiText("Overwrite the saved values?",dialogTextSize(13),theme.text);message.setGravity(Gravity.CENTER);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);mp.setMargins(dialogDp(4),dialogDp(3),dialogDp(4),dialogDp(7));panel.addView(message,mp);
        LinearLayout buttons=row();TextView cancel=dialogButton("CANCEL",theme.border),save=dialogButton("SAVE CHANGES",theme.controls);buttons.addView(cancel,new LinearLayout.LayoutParams(0,dialogDp(35),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(35),1);bp.setMargins(dialogDp(6),0,0,0);buttons.addView(save,bp);panel.addView(buttons);
        cancel.setOnClickListener(v->dialog.dismiss());save.setOnClickListener(v->{dialog.dismiss();action.run();});showStyledDialog(dialog,panel,350);
    }
    private void writeTheme(String name){try{if(name.equals("Default")||name.equals("System default"))throw new IllegalArgumentException("Choose another theme name.");appearance.save(name,theme);theme.name=name;appearance.active(theme);build();refreshAppearance();}catch(Exception e){toast(e.getMessage());}}
    private void saveTheme(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writeTheme(overwrite));return;}promptName("SAVE THEME AS","",name->{try{if(appearance.exists(name))confirmOverwrite(name,()->writeTheme(name));else writeTheme(name);}catch(Exception e){toast("Could not access theme files.");}});}
    private void promptName(String title,String initial,java.util.function.Consumer<String> action){
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(10),dialogDp(7),dialogDp(10),dialogDp(9));panel.addView(styledTitle(title));
        EditText input=new EditText(this);input.setSingleLine();input.setText(initial);input.setSelectAllOnFocus(true);input.setTextColor(theme.buttonTextOn);input.setTypeface(getUiTypeface());input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(13));input.setPadding(dialogDp(9),0,dialogDp(9),0);input.setBackground(roundedBackground(theme.buttonBackground,theme.border,1.1f,5));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dialogDp(38));lp.setMargins(dialogDp(4),dialogDp(3),dialogDp(4),dialogDp(7));panel.addView(input,lp);
        LinearLayout buttons=row();TextView cancel=dialogButton("CANCEL",theme.border),save=dialogButton(title.startsWith("RENAME")?"RENAME":"SAVE",theme.controls);buttons.addView(cancel,new LinearLayout.LayoutParams(0,dialogDp(35),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(35),1);bp.setMargins(dialogDp(6),0,0,0);buttons.addView(save,bp);panel.addView(buttons);cancel.setOnClickListener(v->dialog.dismiss());save.setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()||name.length()>80){input.setError("Use 1–80 characters.");return;}dialog.dismiss();action.accept(name);});showStyledDialog(dialog,panel,350);
    }
    private void showThemeEditor(){
        if(isFinishing()||isDestroyed())return;
        if(appearanceDialog!=null&&appearanceDialog.isShowing()){appearanceDialog.show();refreshAppearance();clearDialogDim(appearanceDialog);return;}
        previewBaseline=themeNames().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults();
        appearanceView=new ThemeAppearanceView(this,this);
        final android.app.Dialog opened=new android.app.Dialog(this);appearanceDialog=opened;
        opened.setOnDismissListener(d->{dismissAnchoredMenu();if(appearanceDialog==opened){appearanceDialog=null;appearanceView=null;}if(!isFinishing()&&!isDestroyed())previewTheme();});
        showStyledDialog(opened,appearanceView,560);clearDialogDim(opened);
    }
    ThemeConfig getThemeConfig(){return theme;}
    boolean isFullscreenMode(){return fullscreen;}
    Typeface getUiTypeface(){return Typeface.create(theme.fontFamily,Typeface.NORMAL);}
    int dialogDp(float n){return Math.max(1,Math.round(n*(viewport==null?getResources().getDisplayMetrics().density:viewport.scale)));}
    float dialogTextSize(float n){return n*(viewport==null?getResources().getDisplayMetrics().density:viewport.scale);}
    android.content.SharedPreferences viewerPreferences(){return settings.prefs;}
    String getFontDisplayName(){String[] f={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"},labels={"SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE"};for(int i=0;i<f.length;i++)if(f[i].equals(theme.fontFamily))return labels[i];return "SANS";}
    boolean isFontChanged(){return presetChanges.contains("fontFamily");}
    void dismissThemeAppearanceDialog(){if(appearanceDialog!=null)appearanceDialog.dismiss();}
    void editThemeAppearanceColour(String field,String label){
        dismissAnchoredMenu();editColour(field,label);}
    void showFontMenu(View anchor,android.graphics.RectF position){String[] families={"sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace"};List<String> labels=java.util.Arrays.asList("SANS","CONDENSED","MEDIUM","LIGHT","MONOSPACE");List<Runnable> actions=new ArrayList<>();for(String family:families)actions.add(()->{theme.fontFamily=family;settings.prefs.edit().putString("fontFamily",family).apply();build();refreshAppearance();refreshChangedIndicators();});anchoredMenu(anchor,position,anchor instanceof ThemeAppearanceView,labels,actions,java.util.Arrays.asList(families).indexOf(theme.fontFamily));}
    private void refreshAppearance(){if(appearanceView!=null)appearanceView.invalidate();if(appearanceDialog!=null&&appearanceDialog.getWindow()!=null)appearanceDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);}
    private void previewTheme(){
        if(root!=null)root.setBackgroundColor(theme.background);
        if(status!=null)status.setTextColor(theme.text);
        if(liveDashboard!=null)liveDashboard.refreshTheme();
        if(presetAnchor instanceof ChangedButton)((ChangedButton)presetAnchor).refreshTheme();
        if(themeAnchor instanceof ChangedButton){((ChangedButton)themeAnchor).refreshTheme();if(previewBaseline!=null)((ChangedButton)themeAnchor).changed(!theme.sameColours(previewBaseline));}
        if(presetAnchor!=null&&presetAnchor.getParent() instanceof ViewGroup){ViewGroup bar=(ViewGroup)presetAnchor.getParent();bar.invalidate();for(int i=0;i<bar.getChildCount();i++){View v=bar.getChildAt(i);if(v instanceof Button&&!(v instanceof ChangedButton)){Button button=(Button)v;button.setBackground(uiBackground(theme.buttonBackground,button.isActivated()?theme.controls:theme.offButtonBorder));button.setTextColor(theme.buttonTextOn);}v.invalidate();}}
        refreshAppearance();
    }
    private GradientDrawable swatch(int color){GradientDrawable d=uiBackground(color,theme.border);d.setSize(dp(24),dp(24));return d;}
    static Integer parseHexColour(String raw){
        if(raw==null)return null;String code=raw.trim();if(code.startsWith("#"))code=code.substring(1);
        if(!code.matches("[0-9a-fA-F]{6}"))return null;
        try{return android.graphics.Color.parseColor("#"+code);}catch(IllegalArgumentException e){return null;}
    }
    private void editColour(String field,String label){
        final int original=theme.get(field);
        previewBaseline=themeNames().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults();
        if(settingsScreen||historyScreen){settingsScreen=false;historyScreen=false;historyEpoch++;build();}
        if(appearanceDialog!=null)appearanceDialog.hide();
        final android.app.Dialog dialog=new android.app.Dialog(this);colourDialog=dialog;
        LinearLayout panel=styledDialogRoot();panel.addView(styledTitle(label+" COLOUR"));final boolean[] syncing={false},accepted={false};
        EditText hex=new EditText(this);hex.setSingleLine();hex.setText(String.format(Locale.UK,"#%06X",original&0xffffff));hex.setSelectAllOnFocus(true);hex.setTextColor(theme.text);hex.setTypeface(getUiTypeface());hex.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dialogTextSize(14));hex.setGravity(Gravity.CENTER);hex.setPadding(dialogDp(6),0,dialogDp(6),0);hex.setBackground(roundedBackground(theme.buttonBackground,theme.border,1,6));
        ColorWheelView picker=new ColorWheelView(this,original);picker.setListener(color->{theme.set(field,color);if(!syncing[0]){syncing[0]=true;hex.setText(String.format(Locale.UK,"#%06X",color&0xffffff));hex.setSelection(hex.length());syncing[0]=false;}hex.setError(null);previewTheme();});panel.addView(picker,new LinearLayout.LayoutParams(-1,dialogDp(300)));
        hex.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence value,int start,int count,int after){}public void afterTextChanged(android.text.Editable value){}public void onTextChanged(CharSequence value,int start,int before,int count){if(syncing[0])return;Integer parsed=parseHexColour(value.toString());if(parsed!=null){syncing[0]=true;try{picker.setColor(parsed);}finally{syncing[0]=false;}}}});
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dialogDp(116),dialogDp(42));hp.gravity=Gravity.CENTER_HORIZONTAL;hp.setMargins(0,dialogDp(4),0,dialogDp(7));panel.addView(hex,hp);
        LinearLayout buttons=row();TextView cancel=dialogButton("CANCEL",theme.border),ok=dialogButton("OK",theme.controls);buttons.addView(cancel,new LinearLayout.LayoutParams(0,dialogDp(36),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(36),1);bp.setMargins(dialogDp(6),0,0,0);buttons.addView(ok,bp);panel.addView(buttons);
        cancel.setOnClickListener(v->dialog.dismiss());
        ok.setOnClickListener(v->{Integer parsed=parseHexColour(hex.getText().toString());if(parsed==null){hex.setError("Enter six hex digits, with optional #.");return;}picker.setColor(parsed);accepted[0]=true;dialog.dismiss();});
        dialog.setOnDismissListener(d->{if(colourDialog==dialog)colourDialog=null;if(!accepted[0])theme.set(field,original);appearance.active(theme);if(isFinishing()||isDestroyed())return;previewTheme();showThemeEditor();});
        showStyledDialog(dialog,panel,360);android.view.WindowManager.LayoutParams wp=dialog.getWindow().getAttributes();wp.gravity=Gravity.BOTTOM|Gravity.LEFT;wp.dimAmount=0;dialog.getWindow().setAttributes(wp);clearDialogDim(dialog);
    }
    private void migrateViewerSettings(){
        if(settings.prefs.getInt("viewer.settingsFormat",0)>=3)return;
        try{
            android.content.SharedPreferences.Editor edit=settings.prefs.edit();
            if(!settings.prefs.contains("fontFamily"))edit.putString("fontFamily",theme.fontFamily);
            JSONObject connection=settings.connectionSnapshot();
            if(!settings.prefs.contains("viewer.defaultConnection"))edit.putString("viewer.defaultConnection",connection.toString());
            for(String key:settings.prefs.getAll().keySet())if(key.startsWith("viewer.preset:")){
                JSONObject migrated=PresetState.normalize(new JSONObject(settings.prefs.getString(key,"{}")));
                if(!migrated.has("connection"))migrated.put("connection",connection);
                edit.putString(key,migrated.toString());
            }
            for(String key:settings.prefs.getAll().keySet())if(key.startsWith("gaugeHighlight:")){
                String target="elementHighlight:"+key.substring(15);if(settings.prefs.getBoolean(key,false)&&!settings.prefs.contains(target))edit.putInt(target,1);edit.remove(key);
            }
            edit.putInt("viewer.settingsFormat",3).commit();
        }catch(Exception e){toast("Saved settings migration could not finish.");}
    }
    private JSONObject presetState()throws Exception{
        JSONObject state=PresetState.defaults();
        for(Map.Entry<String,?> e:settings.prefs.getAll().entrySet())if(e.getKey().startsWith("label:")||e.getKey().startsWith("gaugeHighlight:")||e.getKey().startsWith("elementHighlight:")||e.getKey().startsWith("elementHidden:"))state.put(e.getKey(),e.getValue());
        state.put("persistent",persistent)
                .put("updateIntervalHours",settings.prefs.getInt("update.intervalHours",24)).put("fontFamily",theme.fontFamily)
                .put("localClock",localClock).put("hideStatus",hideStatus).put("range",range).put("defined",defined)
                .put("metric",selectedMetric).put("quantity",quantity).put("bars",bars).put("connection",settings.connectionSnapshot());
        return PresetState.normalize(state);
    }
    private List<String> presetNames(){List<String> names=presets.names();currentPreset=settings.prefs.getString("currentPreset",currentPreset);if(presetAnchor instanceof ChangedButton)((ChangedButton)presetAnchor).loadedName(currentPreset);return names;}
    private List<String> themeNames(){List<String> names=appearance.names();theme.name=appearance.migratedName(theme.name);if(themeAnchor instanceof ChangedButton)((ChangedButton)themeAnchor).loadedName(theme.name);return names;}
    private JSONObject defaultPreset()throws Exception{
        JSONObject state=PresetState.defaults();String baseline=settings.prefs.getString("viewer.defaultConnection",null);
        return state.put("connection",baseline==null?settings.connectionSnapshot():new JSONObject(baseline));
    }
    private JSONObject presetBaseline()throws Exception{
        if(cachedPreset!=null&&currentPreset.equals(cachedPresetName))return cachedPreset;
        presetNames();JSONObject saved=currentPreset.equals("System default")?defaultPreset():presets.load(currentPreset);
        if(!saved.has("connection"))saved.put("connection",settings.connectionSnapshot());
        cachedPreset=saved;cachedPresetName=currentPreset;return saved;
    }
    private java.util.Set<String> presetDifferences(){
        try{JSONObject saved=presetBaseline();java.util.Set<String> changed=PresetState.differences(presetState(),saved);if(liveDashboard!=null)changed.removeIf(key->key.startsWith("label:")&&liveDashboard.originalLabel(key.substring(6))!=null&&!labelModified(key.substring(6),liveDashboard.originalLabel(key.substring(6))));changed.addAll(settings.connectionDifferences(saved.getJSONObject("connection")));return changed;}
        catch(Exception e){return new java.util.LinkedHashSet<>(java.util.Arrays.asList("connection.token"));}
    }
    private boolean presetModified(){return !presetDifferences().isEmpty();}
    private boolean labelModified(String id,String original){try{return !settings.label(id,original).equals(presetBaseline().optString("label:"+id,original));}catch(Exception e){return false;}}
    private void refreshChangedIndicators(){
        presetChanges=presetDifferences();
        if(presetAnchor instanceof ChangedButton)((ChangedButton)presetAnchor).changed(!presetChanges.isEmpty());
        if(themeAnchor instanceof ChangedButton)((ChangedButton)themeAnchor).changed(!theme.sameColours(themeNames().contains(theme.name)?appearance.load(theme.name):ThemeConfig.defaults()));
        for(Map.Entry<View,String> entry:changedViews.entrySet())colourChangedView(entry.getKey(),entry.getValue());
        if(liveDashboard!=null&&!settingsScreen&&!historyScreen)liveDashboard.refreshChanges();
    }
    private void showPresetMenu(){List<String> available=presetNames();String issue=presets.takeProblem();if(issue!=null)toast(issue);List<String> names=new ArrayList<>();List<Runnable> actions=new ArrayList<>();names.add("SAVE SETTINGS AS…");actions.add(()->savePreset(null));if(presetNames().contains(currentPreset)&&presetModified()){names.add("UPDATE \""+currentPreset+"\"");actions.add(()->savePreset(currentPreset));}names.add("MANAGE PRESETS…");actions.add(()->manage(false));names.add("System default");actions.add(()->loadPreset("System default"));for(String n:available){names.add(n);actions.add(()->loadPreset(n));}menu("PRESETS",names,actions);}
    private void writePreset(String name){try{name=SavedJson.name(name);JSONObject saved=presetState();presets.save(name,saved);settings.prefs.edit().putString("currentPreset",name).apply();currentPreset=name;cachedPreset=saved;cachedPresetName=name;build();}catch(Exception e){toast(e instanceof IllegalArgumentException?e.getMessage():"Could not save preset to Downloads.");}}
    private void savePreset(String overwrite){if(overwrite!=null){confirmOverwrite(overwrite,()->writePreset(overwrite));return;}promptName("SAVE SETTINGS AS","",name->{try{if(presets.exists(name))confirmOverwrite(name,()->writePreset(name));else writePreset(name);}catch(Exception e){toast("Could not access preset files.");}});}
    private void showPresetValues(String name){
        try{
            JSONObject state=presets.load(name);
            StringBuilder values=new StringBuilder("Font: ").append(state.optString("fontFamily"))
                    .append("\nConnection: ").append(state.has("connection")?"Saved privately; token hidden":"Keep current")
                    .append("\nConnection status: ").append(state.optBoolean("hideStatus")?"Hidden":"Shown")
                    .append("\nClock: ").append(state.optBoolean("localClock")?"Local":"UTC")
                    .append("\nChart range: ").append(HistoryData.RANGES[Math.max(0,Math.min(4,state.optInt("range")))])
                    .append(state.optBoolean("defined")?" / Defined UTC":" / Rolling");
            java.util.List<String> labels=new ArrayList<>();state.keys().forEachRemaining(key->{if(key.startsWith("label:"))labels.add(state.optString(key));});java.util.Collections.sort(labels);
            if(!labels.isEmpty()){values.append("\n\nDisplay labels:");for(String label:labels)values.append("\n").append(label);}
            themed(themedAlertBuilder().setTitle(name+" • VALUES").setMessage(values).setNegativeButton("CLOSE",null).setPositiveButton("COPY",(d,w)->{android.content.ClipboardManager clipboard=getSystemService(android.content.ClipboardManager.class);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Preset values",values));}).create());
        }catch(Exception e){toast("Could not read preset.");}
    }
    private void loadPreset(String name){
        try{
            boolean defaults=name.equals("System default");
            JSONObject p=defaults?PresetState.defaults():presets.load(name);
            JSONObject connection=p.optJSONObject("connection");if(connection==null)connection=settings.connectionSnapshot();
            android.content.SharedPreferences.Editor edit=settings.prefs.edit();settings.stageConnection(edit,connection);
            for(String key:settings.prefs.getAll().keySet())if(key.startsWith("label:")||key.startsWith("gaugeHighlight:")||key.startsWith("elementHighlight:")||key.startsWith("elementHidden:"))edit.remove(key);
            for(java.util.Iterator<String> keys=p.keys();keys.hasNext();){String key=keys.next();if(key.startsWith("label:"))edit.putString(key,p.getString(key));else if(key.startsWith("elementHighlight:"))edit.putInt(key,p.getInt(key));else if(key.startsWith("elementHidden:"))edit.putBoolean(key,p.getBoolean(key));}
            edit.putBoolean("persistent",p.getBoolean("persistent"))
                    .putInt("update.intervalHours",p.getInt("updateIntervalHours")).putBoolean("localClock",p.getBoolean("localClock"))
                    .putBoolean("hideStatus",p.getBoolean("hideStatus")).putInt("range",p.getInt("range")).putBoolean("defined",p.getBoolean("defined"))
                    .putString("metric",p.getString("metric")).putString("quantity",p.getString("quantity")).putBoolean("bars",p.getBoolean("bars"))
                    .putString("fontFamily",p.getString("fontFamily")).putString("currentPreset",name);
            if(defaults)edit.putString("viewer.defaultConnection",connection.toString());
            if(!edit.commit())throw new Exception();
            persistent=p.getBoolean("persistent");
            localClock=p.getBoolean("localClock");hideStatus=p.getBoolean("hideStatus");range=p.getInt("range");defined=p.getBoolean("defined");
            selectedMetric=p.getString("metric");quantity=p.getString("quantity");bars=p.getBoolean("bars");theme.fontFamily=p.getString("fontFamily");currentPreset=name;offset=0;cachedPreset=null;cachedPresetName=null;
            screen.setKeep(keepScreen);windowMode();persistence(true);updates.schedule();build();restartConnection();if(historyScreen&&!settingsScreen)loadHistory();
        }catch(Exception e){toast("Could not load preset. File may be malformed or its encrypted connection belongs to another installation. Current settings were kept.");}
    }
    private void manage(boolean themes){
        List<String> names=new ArrayList<>(themes?themeNames():presetNames());if(names.isEmpty()){toast(themes?"No saved themes":"No saved presets");return;}
        android.app.Dialog dialog=new android.app.Dialog(this);LinearLayout panel=styledDialogRoot();panel.setPadding(dialogDp(9),dialogDp(7),dialogDp(9),dialogDp(9));panel.addView(styledTitle(themes?"MANAGE THEMES":"MANAGE PRESETS"));ScrollView scroll=new ScrollView(this);LinearLayout list=column();scroll.addView(list);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,Math.min(dialogDp(150),Math.max(dialogDp(36),names.size()*dialogDp(34))));sp.setMargins(0,dialogDp(2),0,dialogDp(6));panel.addView(scroll,sp);
        int[] selected={-1};List<TextView> rows=new ArrayList<>();LinearLayout buttons=row();TextView rename=dialogButton("RENAME",theme.controls),delete=dialogButton("DELETE",0xffd76969),values=themes?null:dialogButton("VIEW VALUES",theme.controls);for(TextView b:new TextView[]{rename,delete,values})if(b!=null){b.setEnabled(false);b.setAlpha(.42f);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dialogDp(36),1);bp.setMargins(dialogDp(3),0,dialogDp(3),0);buttons.addView(b,bp);}panel.addView(buttons);
        Runnable refresh=()->{names.clear();names.addAll(themes?themeNames():presetNames());selected[0]=-1;list.removeAllViews();rows.clear();rename.setEnabled(false);delete.setEnabled(false);rename.setAlpha(.42f);delete.setAlpha(.42f);if(values!=null){values.setEnabled(false);values.setAlpha(.42f);}for(int i=0;i<names.size();i++){int index=i;TextView row=dialogButton(names.get(i),theme.border);row.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);row.setPadding(dialogDp(10),0,dialogDp(6),0);rows.add(row);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dialogDp(32));rp.setMargins(dialogDp(2),dialogDp(1),dialogDp(2),dialogDp(1));list.addView(row,rp);row.setOnClickListener(v->{selected[0]=index;for(int j=0;j<rows.size();j++){boolean active=j==index;rows.get(j).setTextColor(active?theme.controls:theme.text);rows.get(j).setBackground(roundedBackground(active?ThemeConfig.blend(theme.buttonBackground,theme.controls,.14f):theme.buttonBackground,active?theme.controls:theme.border,1.1f,5));}rename.setEnabled(true);delete.setEnabled(true);rename.setAlpha(1);delete.setAlpha(1);if(values!=null){values.setEnabled(true);values.setAlpha(1);}});}};
        refresh.run();rename.setOnClickListener(v->{if(selected[0]<0)return;String old=names.get(selected[0]);promptName(themes?"RENAME THEME":"RENAME PRESET",old,name->{try{if(name.equals(old))return;if(themes){appearance.rename(old,name);if(theme.name.equals(old)){theme.name=name;appearance.active(theme);}}else{presets.rename(old,name);cachedPreset=null;if(currentPreset.equals(old)){currentPreset=name;settings.prefs.edit().putString("currentPreset",name).apply();}}build();refresh.run();}catch(Exception e){toast(e.getMessage());}});});
        delete.setOnClickListener(v->{if(selected[0]<0)return;String name=names.get(selected[0]);themedAlertBuilder().setTitle(themes?"Delete theme?":"Delete preset?").setMessage(name).setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{try{if(themes){appearance.delete(name);if(theme.name.equals(name))applyTheme(ThemeConfig.defaults());}else{presets.delete(name);cachedPreset=null;if(currentPreset.equals(name)){currentPreset="System default";settings.prefs.edit().putString("currentPreset",currentPreset).apply();}}build();refresh.run();}catch(Exception e){toast(e.getMessage());}}).show();});if(values!=null)values.setOnClickListener(v->{if(selected[0]>=0)showPresetValues(names.get(selected[0]));});showStyledDialog(dialog,panel,370);
    }
    private void exportHistory() {
        if (history == null) { toast("Load a chart range before exporting."); return; }
        exportBytes = HistoryData.csv(history, metricLabel(history.metric)).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/csv").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE, "ella-history-" + history.window.resolution + ".csv");
        try { startActivityForResult(intent, 100); } catch (RuntimeException e) {
            byte[] bytes=exportBytes;exportBytes=null;
            toast(e instanceof android.content.ActivityNotFoundException?"No save dialogue handles CSV files. Saving to Downloads.":"Save dialogue could not open ("+e.getClass().getSimpleName()+"). Saving to Downloads.");
            exportToDownloads(bytes);
        }
    }
    private void exportToDownloads(byte[] bytes){
        if(bytes==null)return;
        String stamp=new java.text.SimpleDateFormat("yyyyMMdd-HHmmss-SSS",Locale.UK).format(new java.util.Date());
        String filename="ella-history-"+stamp+".csv";
        withDownloadsStorage(()->historyWorker.execute(()->{try{new DownloadsFiles(this,"exports").write(filename,"text/csv",bytes);handler.post(()->toast("CSV saved to Downloads/ella-monitoring/exports/"+filename));}catch(Exception e){handler.post(()->toast("Could not save CSV to Downloads. Check storage access and space."));}}));
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
    private void withDownloadsStorage(Runnable action){
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
        if(items.isEmpty()||items.size()!=actions.size())return;dismissAnchoredMenu();
        android.graphics.Rect bounds=new android.graphics.Rect();anchor.getGlobalVisibleRect(bounds);float scale=viewport.scale;
        if(position!=null&&anchor instanceof ThemeAppearanceView){ThemeAppearanceView view=(ThemeAppearanceView)anchor;int[] loc=new int[2];anchor.getLocationOnScreen(loc);bounds.set(loc[0]+Math.round(position.left*view.getUiScaleX()),loc[1]+Math.round(position.top*view.getUiScaleY()),loc[0]+Math.round(position.right*view.getUiScaleX()),loc[1]+Math.round(position.bottom*view.getUiScaleY()));}
        int height=dialogDp(34),chrome=dialogDp(6),available=above?bounds.top:viewport.getHeight()-bounds.bottom;
        int menuHeight=Math.min(items.size()*height+chrome*2,Math.max(height+chrome*2,available-chrome));
        LinearLayout list=column();list.setPadding(0,chrome,0,chrome);list.setBackground(roundedBackground(theme.panel,theme.border,1.2f,6));
        final android.widget.PopupWindow[] holder=new android.widget.PopupWindow[1];
        for(int i=0;i<items.size();i++){final Runnable action=actions.get(i);TextView item=uiText(items.get(i),dialogTextSize(11.5f),i==selected?theme.controls:theme.text);item.setSingleLine();item.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);item.setPadding(dialogDp(10),0,dialogDp(6),0);if(i==selected)item.setBackgroundColor(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f));list.addView(item,new LinearLayout.LayoutParams(-1,height));item.setOnClickListener(v->{holder[0].dismiss();action.run();});}
        ScrollView scroll=new ScrollView(this);scroll.addView(list);holder[0]=new android.widget.PopupWindow(scroll,Math.max(dialogDp(150),bounds.width()),menuHeight,true);holder[0].setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));holder[0].setOutsideTouchable(true);holder[0].setElevation(dialogDp(6));anchoredPopup=holder[0];holder[0].setOnDismissListener(()->{if(anchoredPopup==holder[0])anchoredPopup=null;});holder[0].showAtLocation(anchor,Gravity.TOP|Gravity.LEFT,bounds.left,above?Math.max(0,bounds.top-menuHeight-chrome):bounds.bottom+chrome);
    }

    private void dismissAnchoredMenu(){if(anchoredPopup!=null){android.widget.PopupWindow popup=anchoredPopup;anchoredPopup=null;popup.dismiss();}}
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }
}

