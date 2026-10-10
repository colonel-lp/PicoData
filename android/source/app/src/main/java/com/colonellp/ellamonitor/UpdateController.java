package com.colonellp.ellamonitor;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Themed changelog and update UI, isolated from the DSP activity code. */
final class UpdateController {
    private final MainActivity host;
    private final AppUpdateManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Dialog changelogDialog;
    private Dialog updateDialog;
    private boolean created, resumed;
    private final Runnable startupCheck = () -> automaticCheck(false);
    private final Runnable dailyCheck = () -> automaticCheck(false);

    UpdateController(MainActivity host) {
        this.host = host;
        this.manager = new AppUpdateManager(host, host.viewerPreferences());
    }

    void onCreated() {
        if (created) return;
        created = true;
        manager.cleanupInstalledUpdate();
        
    }

    void onResumed() {
        resumed = true;
        schedule();
        manager.resumePendingInstall();
        if (manager.cleanupError() != null)
            Toast.makeText(host, manager.cleanupError(), Toast.LENGTH_LONG).show();
    }

    void onPaused() { resumed = false; handler.removeCallbacks(dailyCheck); handler.removeCallbacks(startupCheck); manager.onPaused(); }

    void schedule() {
        handler.removeCallbacks(dailyCheck); handler.removeCallbacks(startupCheck);
        if (resumed && manager.updateIntervalMillis() > 0) handler.postDelayed(startupCheck, 2500L);
    }

    void destroy() {
        handler.removeCallbacksAndMessages(null);
        manager.destroy();
        if (changelogDialog != null) changelogDialog.dismiss();
        if (updateDialog != null) updateDialog.dismiss();
    }

    private void automaticCheck(boolean force) {
        if (!resumed || host.isFinishing() || manager.updateIntervalMillis() == 0) return;
        manager.check(force, result -> {
            handler.removeCallbacks(dailyCheck);
            if (resumed && manager.updateIntervalMillis() > 0) handler.postDelayed(dailyCheck, result!=null&&result.error!=null?manager.updateIntervalMillis():manager.nextCheckDelayMillis());
            if (resumed && manager.updateIntervalMillis() > 0 && result != null && result.update != null && !host.isFinishing()
                    && host.hasWindowFocus()
                    && (updateDialog == null || !updateDialog.isShowing())
                    && (changelogDialog == null || !changelogDialog.isShowing())) {
                showAvailableUpdate(result.update);
            }
        });
    }

    void showChangelog() {
        if (changelogDialog != null && changelogDialog.isShowing()) changelogDialog.dismiss();
        ThemeConfig theme = host.getThemeConfig();
        final Dialog dialog = new Dialog(host);
        changelogDialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout root = root(theme);
        TextView title = text("CHANGELOG", 16f, theme.text);
        root.addView(title, match(dp(42)));
        TextView status = text("CHECKING FOR UPDATES…", 12f, theme.controls);
        root.addView(status, match(dp(34)));

        final String[] changelog = {manager.currentChangelog()};
        TextView notes = text(ChangelogText.display(changelog[0], null), 13f, theme.text);
        notes.setGravity(Gravity.LEFT);
        notes.setPadding(dp(12), dp(8), dp(12), dp(12));
        notes.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(host);
        scroll.setFillViewport(true);
        scroll.addView(notes, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, match(dp(330)));

        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        TextView close = button("CLOSE", theme.border, theme);
        TextView install = button("DOWNLOAD & INSTALL", theme.controls, theme);
        install.setVisibility(View.GONE);
        row.addView(close, weighted());
        row.addView(install, weighted());
        root.addView(row, match(dp(46)));

        final AppUpdateManager.ReleaseInfo[] available = {null};
        close.setOnClickListener(v -> dialog.dismiss());
        install.setOnClickListener(v -> {
            if (available[0] == null) return;
            install.setEnabled(false);
            startInstall(available[0], status, install, close, dialog);
        });
        dialog.setOnDismissListener(d -> {
            if (changelogDialog == dialog) changelogDialog = null;
        });
        show(dialog, root, 720);

        manager.loadChangelog((file, fresh) -> {
            if (!dialog.isShowing()) return;
            changelog[0] = file;
            notes.setText(ChangelogText.display(file, available[0] == null ? null : available[0].versionName));
            title.setText(fresh ? "CHANGELOG" : "CHANGELOG • OFFLINE COPY");
            scroll.post(() -> scroll.fullScroll(View.FOCUS_UP));
        });
        manager.check(true, result -> {
            if (!dialog.isShowing() || result == null) return;
            notes.setText(ChangelogText.display(changelog[0],
                    result.update == null ? null : result.update.versionName));
            scroll.post(() -> scroll.fullScroll(View.FOCUS_UP));
            if (result.update != null) {
                available[0] = result.update;
                status.setText("UPDATE AVAILABLE • " + result.update.versionName);
                install.setVisibility(View.VISIBLE);
            } else if (result.error != null) {
                status.setText("UPDATE CHECK FAILED • " + result.error);
            } else {
                status.setText(result.status == null ? "UP TO DATE" : result.status);
            }
        });
    }

    private void showAvailableUpdate(AppUpdateManager.ReleaseInfo release) {
        if (updateDialog != null && updateDialog.isShowing()) updateDialog.dismiss();
        ThemeConfig theme = host.getThemeConfig();
        final Dialog dialog = new Dialog(host);
        updateDialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        LinearLayout root = root(theme);
        root.addView(text("UPDATE AVAILABLE • " + release.versionName, 15f, theme.text), match(dp(46)));
        TextView notes = text(updateNotes(manager.currentChangelog(), release), 13f, theme.text);
        notes.setGravity(Gravity.LEFT);
        notes.setPadding(dp(10), dp(8), dp(10), dp(8));
        ScrollView scroll = new ScrollView(host);
        scroll.addView(notes);
        root.addView(scroll, match(dp(250)));
        TextView status = text("", 12f, theme.controls);
        status.setVisibility(View.GONE);
        root.addView(status, match(dp(32)));
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView later = button("LATER", theme.border, theme);
        TextView install = button("DOWNLOAD & INSTALL", theme.controls, theme);
        row.addView(later, weighted());
        row.addView(install, weighted());
        root.addView(row, match(dp(46)));
        later.setOnClickListener(v -> dialog.dismiss());
        install.setOnClickListener(v -> {
            install.setEnabled(false);
            later.setEnabled(false);
            status.setVisibility(View.VISIBLE);
            startInstall(release, status, install, later, dialog);
        });
        dialog.setOnDismissListener(d -> { if (updateDialog == dialog) updateDialog = null; });
        show(dialog, root, 650);
        manager.loadChangelog((file, fresh) -> {
            if (dialog.isShowing()) notes.setText(updateNotes(file, release));
        });
    }

    private String updateNotes(String file, AppUpdateManager.ReleaseInfo release) {
        String notes = ChangelogText.notesForVersion(file, release.versionName);
        return notes.isEmpty() ? release.changelog : notes;
    }

    private void startInstall(AppUpdateManager.ReleaseInfo release,
                              TextView status, TextView install, TextView cancel, Dialog dialog) {
        manager.downloadAndInstall(release, new AppUpdateManager.InstallCallback() {
            @Override public void onStatus(String message) { status.setText(message); }
            @Override public void onInstallerOpened() { dialog.dismiss(); }
            @Override public void onFailure(String message) {
                status.setText("UPDATE FAILED • " + message);
                install.setEnabled(true);
                cancel.setEnabled(true);
                Toast.makeText(host, "Update failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private LinearLayout root(ThemeConfig theme) {
        LinearLayout root = new LinearLayout(host);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.setBackground(background(theme.panel, theme.border, 1.4f, 9f));
        return root;
    }

    private TextView text(String value, float size, int color) {
        TextView view = new TextView(host);
        view.setText(value);
        view.setTypeface(host.getUiTypeface());
        view.setTextColor(color);
        view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, host.dialogTextSize(size));
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private TextView button(String label, int accent, ThemeConfig theme) {
        TextView view = text(label, 11.5f, theme.text);
        view.setBackground(background(theme.buttonBackground, accent, 1.25f, 6f));
        view.setPadding(dp(6), 0, dp(6), 0);
        return view;
    }

    private LinearLayout.LayoutParams weighted() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        lp.setMargins(dp(5), 0, dp(5), 0);
        return lp;
    }

    private LinearLayout.LayoutParams match(int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(dp(8), dp(3), dp(8), dp(3));
        return lp;
    }

    private GradientDrawable background(int fill, int stroke, float strokeDp, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(Math.max(1, dp(strokeDp)), stroke);
        return drawable;
    }

    private void show(Dialog dialog, View content, int widthDp) {
        int maxHeight = host.getResources().getDisplayMetrics().heightPixels - dp(24);
        int width = Math.min(dp(widthDp), host.getResources().getDisplayMetrics().widthPixels - dp(30));
        content.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        if (content.getMeasuredHeight() > maxHeight && content instanceof LinearLayout) {
            LinearLayout root = (LinearLayout)content;
            int excess = content.getMeasuredHeight() - maxHeight;
            for (int i=0; i<root.getChildCount(); i++) {
                View child = root.getChildAt(i);
                if (!(child instanceof ScrollView)) continue;
                android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
                lp.height = Math.max(dp(60), lp.height - excess);
                child.setLayoutParams(lp); break;
            }
        }
        dialog.setContentView(content);
        dialog.show();
        Window window = dialog.getWindow();
        if (window == null) return;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(Math.min(dp(widthDp), host.getResources().getDisplayMetrics().widthPixels - dp(30)),
                WindowManager.LayoutParams.WRAP_CONTENT);
        window.getDecorView().setSystemUiVisibility(
                host.getWindow().getDecorView().getSystemUiVisibility());
    }

    private int dp(float value) {
        return Math.max(1, host.dialogDp(value));
    }
}

