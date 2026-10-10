package com.colonellp.ellamonitor;

import android.content.Intent;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Public GitHub-release checker and verified APK installer. */
final class AppUpdateManager {
    private static final String RELEASES_API =
            "https://api.github.com/repos/colonel-lp/PicoData/releases?per_page=20";
    private static final String CHANGELOG_URL =
            "https://raw.githubusercontent.com/colonel-lp/PicoData/main/android/CHANGELOG.md";
    private static final String KEY_CHANGELOG = "update_changelog_file_v1";
    private static final String RELEASE_DOWNLOAD_PREFIX =
            "https://github.com/colonel-lp/PicoData/releases/download/";
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;
    private static final long MAX_APK_BYTES = 128L * 1024L * 1024L;
    private static final String KEY_LAST_CHECK = "update_last_check_ms_v1";
    private static final String KEY_PENDING_VERSION = "update_pending_version_code_v1";
    private static final String KEY_WAITING_PERMISSION = "update_waiting_install_permission_v1";

    private static final String KEY_PENDING_LONG = "update_pending_version_code_v2";
    private static final String KEY_INSTALL_READY = "update_install_ready_v2";
    private static final String KEY_CLEANUP_FAILED = "update_cleanup_failed_v2";

    interface ChangelogCallback { void onComplete(String file, boolean fresh); }
    interface CheckCallback { void onComplete(CheckResult result); }
    interface InstallCallback {
        void onStatus(String status);
        void onFailure(String message);
        default void onInstallerOpened() { }
    }

    static final class ReleaseInfo {
        final String versionName;
        final int advertisedVersionCode;
        final String changelog;
        final String downloadUrl;
        final String sha256;
        final long size;
        final boolean prerelease;

        ReleaseInfo(String versionName, int versionCode, String changelog,
                    String downloadUrl, String sha256, long size, boolean prerelease) {
            this.versionName = versionName;
            this.advertisedVersionCode = versionCode;
            this.changelog = changelog;
            this.downloadUrl = downloadUrl;
            this.sha256 = sha256;
            this.size = size;
            this.prerelease = prerelease;
        }
    }

    static final class CheckResult {
        final ReleaseInfo update;
        final String releaseHistory;
        final String error;
        final boolean skipped;
        final String status;

        CheckResult(ReleaseInfo update, String history, String error, boolean skipped) {
            this(update, history, error, skipped, null);
        }

        CheckResult(ReleaseInfo update, String history, String error, boolean skipped, String status) {
            this.update = update;
            this.releaseHistory = history;
            this.error = error;
            this.skipped = skipped;
            this.status = status;
        }
    }

    private final MainActivity activity;
    private final SharedPreferences prefs;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean destroyed;
    private volatile boolean resumed;
    private static final AtomicBoolean downloading = new AtomicBoolean();
    private static volatile AppUpdateManager foreground;
    private volatile InstallCallback pendingCallback;
    private String cleanupError;
    private String bundledChangelog;

    void onPaused() { resumed = false; if (foreground == this) foreground = null; }
    String cleanupError() { return cleanupError; }

    private long pendingVersion() {
        return prefs.getLong(KEY_PENDING_LONG, prefs.getInt(KEY_PENDING_VERSION, 0));
    }

    AppUpdateManager(MainActivity activity, SharedPreferences prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    void destroy() { destroyed = true; onPaused(); }

    void cleanupInstalledUpdate() {
        cleanupError = null;
        if (downloading.get()) return;
        long pending = pendingVersion();
        boolean installed = pending > 0 && currentVersionCode() >= pending;
        if (!installed && !prefs.getBoolean(KEY_CLEANUP_FAILED, false)) return;
        try {
            File apk = UpdateFileProvider.updateFile(activity);
            File part = new File(apk.getAbsolutePath() + ".part");
            boolean apkDeleted = UpdateFiles.delete(apk);
            boolean partDeleted = UpdateFiles.delete(part);
            if (!apkDeleted || !partDeleted) {
                cleanupError = "Cannot delete update download; cleanup will retry when the app resumes";
                return;
            }
            prefs.edit().remove(KEY_PENDING_VERSION).remove(KEY_PENDING_LONG)
                    .remove(KEY_WAITING_PERMISSION).remove(KEY_INSTALL_READY)
                    .remove(KEY_CLEANUP_FAILED).apply();
        } catch (Exception error) { cleanupError = shortMessage(error); }
    }

    long updateIntervalMillis() {
        int hours = prefs.getInt("update.intervalHours", 24);
        if (hours != 0 && hours != 1 && hours != 3 && hours != 6 && hours != 12 && hours != 24) hours = 24;
        return hours * 60L * 60L * 1000L;
    }

    long nextCheckDelayMillis(){
        long interval=updateIntervalMillis(),last=prefs.getLong(KEY_LAST_CHECK,0),age=System.currentTimeMillis()-last;
        return age<0||last==0?2500L:Math.max(2500L,interval-age);
    }

    void check(boolean force, CheckCallback callback) {
        long now = System.currentTimeMillis();
        long last = prefs.getLong(KEY_LAST_CHECK, 0L);
        if (!force && updateIntervalMillis() == 0) { post(callback, new CheckResult(null, "", null, true)); return; }
        if (!force && now - last >= 0L && now - last < updateIntervalMillis()) {
            post(callback, new CheckResult(null, "", null, true));
            return;
        }
        new Thread(() -> {
            try {
                String json = readUtf8(RELEASES_API, MAX_RESPONSE_BYTES);
                CheckResult result = parseReleases(new JSONArray(json));
                prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
                post(callback, result);
            } catch (Exception e) {
                post(callback, new CheckResult(null, "", shortMessage(e), false));
            }
        }, "ella-update-check").start();
    }

    void downloadAndInstall(ReleaseInfo release, InstallCallback callback) {
        if (release == null || release.downloadUrl == null
                || !release.downloadUrl.startsWith(RELEASE_DOWNLOAD_PREFIX)) {
            if (callback != null) callback.onFailure("Release does not contain a valid APK");
            return;
        }
        if (!downloading.compareAndSet(false, true)) {
            if (callback != null) callback.onFailure("An update download is already running");
            return;
        }
        pendingCallback = callback;
        new Thread(() -> {
            File apk = null, part = null;
            Runnable handoff = null;
            boolean ownsApk = false, ownsPart = false;
            try {
                apk = UpdateFileProvider.updateFile(activity);
                part = new File(apk.getAbsolutePath() + ".part");
                postStatus(callback, "Downloading " + release.versionName + "…");
                if (!UpdateFiles.delete(part)) throw new Exception("Cannot replace partial download");
                if (!UpdateFiles.delete(apk)) throw new Exception("Cannot replace previous download");
                prefs.edit().remove(KEY_INSTALL_READY).remove(KEY_WAITING_PERMISSION)
                        .remove(KEY_PENDING_LONG).remove(KEY_PENDING_VERSION)
                        .putBoolean(KEY_CLEANUP_FAILED, true).apply();
                ownsPart = true;
                download(release, part);
                if (!part.renameTo(apk)) throw new Exception("Cannot stage downloaded APK");
                ownsApk = true;
                postStatus(callback, "Verifying downloaded APK…");
                long version = verifyApk(apk, release.sha256);
                prefs.edit().putLong(KEY_PENDING_LONG, version)
                        .putBoolean(KEY_INSTALL_READY, true).remove(KEY_CLEANUP_FAILED).apply();
                final File staged = apk;
                handoff = () -> {
                    if (destroyed) {
                        AppUpdateManager current = foreground;
                        if (current != null) current.resumePendingInstall();
                        return;
                    }
                    if (!prefs.getBoolean(KEY_INSTALL_READY, false)) return;
                    if (!resumed) {
                        if (callback != null) callback.onStatus("Update ready; return to the app to install.");
                        return;
                    }
                    requestInstall(staged, version, callback);
                };
            } catch (Exception error) {
                boolean cleaned = true;
                if (ownsPart && !UpdateFiles.delete(part)) cleaned = false;
                if (ownsApk && !UpdateFiles.delete(apk)) cleaned = false;
                if (ownsPart || ownsApk) {
                    prefs.edit().putBoolean(KEY_CLEANUP_FAILED, !cleaned)
                            .remove(KEY_INSTALL_READY).remove(KEY_PENDING_LONG).apply();
                }
                postFailure(callback, shortMessage(error)
                        + (cleaned ? "" : "; download cleanup will retry"));
            } finally {
                downloading.set(false);
                if (handoff != null) main.post(handoff);
            }
        }, "ella-update-download").start();
    }

    void resumePendingInstall() {
        resumed = true;
        foreground = this;
        cleanupInstalledUpdate();
        if (downloading.get()) return;
        boolean waiting = prefs.getBoolean(KEY_WAITING_PERMISSION, false);
        boolean ready = prefs.getBoolean(KEY_INSTALL_READY, false);
        if (!waiting && !ready) return;
        if (waiting && Build.VERSION.SDK_INT >= 26
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            prefs.edit().remove(KEY_WAITING_PERMISSION).remove(KEY_INSTALL_READY).apply();
            postFailure(pendingCallback, "Install permission was not granted; use Download & Install to retry");
            return;
        }
        try {
            File apk = UpdateFileProvider.updateFile(activity);
            long version = verifyApk(apk, "");
            requestInstall(apk, version, pendingCallback);
        } catch (Exception error) {
            prefs.edit().remove(KEY_WAITING_PERMISSION).remove(KEY_INSTALL_READY).apply();
            postFailure(pendingCallback, shortMessage(error));
        }
    }

    private CheckResult parseReleases(JSONArray releases) {
        ReleaseInfo best = null;
        StringBuilder history = new StringBuilder();
        int currentCode = (int)Math.min(Integer.MAX_VALUE, currentVersionCode());
        String currentName = currentVersionName();
        int missingVersion = 0, blockedLink = 0, apkCount = 0;
        for (int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.optJSONObject(i);
            if (release == null || release.optBoolean("draft", false)) continue;
            String tag = release.optString("tag_name", "");
            String name = release.optString("name", tag);
            String body = release.optString("body", "").trim();
            boolean prerelease = release.optBoolean("prerelease", false);
            if (history.length() > 0) history.append("\n\n");
            history.append(name.length() == 0 ? tag : name);
            if (prerelease) history.append(" • prerelease");
            if (body.length() > 0) history.append("\n").append(body);
            JSONArray assets = release.optJSONArray("assets");
            boolean hasApk = false;
            if (assets != null) for (int j = 0; j < assets.length(); j++) {
                JSONObject asset = assets.optJSONObject(j);
                if (asset == null) continue;
                String assetName = asset.optString("name", "");
                if (!assetName.matches("Ella-monitoring-v[0-9]+\\.[0-9]{2}\\.apk")) continue;
                hasApk = true;
                apkCount++;
                String version = ReleaseVersion.fromRelease(tag, name, assetName);
                String url = asset.optString("browser_download_url", "");
                String reason;
                int code = ReleaseVersion.extractBuildCode(assetName + " " + tag + " " + name + " " + body);
                if (version.length() == 0) {
                    missingVersion++;
                    reason = "version could not be identified";
                } else if (!url.startsWith(RELEASE_DOWNLOAD_PREFIX)) {
                    blockedLink++;
                    reason = "APK link is not in the configured release repository";
                } else if (!ReleaseVersion.isNewer(version, code, currentName, currentCode)) {
                    reason = "installed or older; installed " + currentName + " / build " + currentCode;
                } else {
                    String digest = asset.optString("digest", "");
                    if (digest.toLowerCase(Locale.US).startsWith("sha256:")) digest = digest.substring(7);
                    else digest = "";
                    ReleaseInfo candidate = new ReleaseInfo(version, code,
                            body.length() == 0 ? "No changelog was supplied for this release." : body,
                            url, digest, asset.optLong("size", -1L), prerelease);
                    if (best == null || compareRelease(candidate, best) > 0) best = candidate;
                    reason = "newer release • " + version + (code > 0 ? " / build " + code : "");
                }
                history.append("\n").append(assetName).append(" — ").append(reason);
            }
            if (!hasApk) history.append("\nNo APK attached.");
        }
        String status = best != null ? "UPDATE AVAILABLE • " + best.versionName
                : missingVersion > 0 ? "NO INSTALLABLE UPDATE • release version missing"
                : blockedLink > 0 ? "NO INSTALLABLE UPDATE • invalid APK link"
                : apkCount == 0 ? "NO APK RELEASE AVAILABLE"
                : "UP TO DATE • " + currentName + " / build " + currentCode;
        return new CheckResult(best, history.toString(), null, false, status);
    }

    private int compareRelease(ReleaseInfo a, ReleaseInfo b) {
        if (a.advertisedVersionCode > 0 && b.advertisedVersionCode > 0) {
            return Integer.compare(a.advertisedVersionCode, b.advertisedVersionCode);
        }
        return ReleaseVersion.compare(a.versionName, b.versionName);
    }

    private void download(ReleaseInfo release, File target) throws Exception {
        HttpURLConnection connection = open(release.downloadUrl);
        int response = connection.getResponseCode();
        if (response < 200 || response >= 300) {
            connection.disconnect();
            throw new Exception("Download failed (HTTP " + response + ")");
        }
        long declared = UpdateFiles.contentLength(connection.getHeaderField("Content-Length"));
        if (declared > MAX_APK_BYTES || release.size > MAX_APK_BYTES) {
            connection.disconnect();
            throw new Exception("Release APK is unexpectedly large");
        }
        long written = 0L;
        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[32 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                written += count;
                if (written > MAX_APK_BYTES) throw new Exception("Release APK is unexpectedly large");
                output.write(buffer, 0, count);
            }
            output.getFD().sync();
        } finally {
            connection.disconnect();
        }
        if (written <= 0L) throw new Exception("Downloaded APK is empty");
        if (release.size > 0L && written != release.size) throw new Exception("Downloaded APK size does not match release");
    }

    private long verifyApk(File apk, String expectedSha256) throws Exception {
        if (!apk.isFile()) throw new Exception("Downloaded APK is unavailable");
        if (expectedSha256 != null && !expectedSha256.isEmpty()
                && !expectedSha256.equalsIgnoreCase(sha256(apk)))
            throw new Exception("Downloaded APK checksum failed");
        PackageManager pm = activity.getPackageManager();
        int flags = Build.VERSION.SDK_INT >= 28
                ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
        PackageInfo archive = pm.getPackageArchiveInfo(apk.getAbsolutePath(), flags);
        PackageInfo installed = pm.getPackageInfo(activity.getPackageName(), flags);
        if (archive == null || !activity.getPackageName().equals(archive.packageName))
            throw new Exception("Downloaded APK has the wrong package name");
        if (versionCode(archive) <= versionCode(installed))
            throw new Exception("Downloaded APK is not newer than the installed app");
        if (!signaturesMatch(installed, archive))
            throw new Exception("Downloaded APK signature does not match this app");
        return versionCode(archive);
    }

    private static long versionCode(PackageInfo info) {
        return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
    }

    private static boolean signaturesMatch(PackageInfo installed, PackageInfo archive) {
        if (Build.VERSION.SDK_INT < 28) return sameSignatures(installed.signatures, archive.signatures);
        if (installed.signingInfo == null || archive.signingInfo == null) return false;
        Signature[] current = installed.signingInfo.getApkContentsSigners();
        Signature[] next = archive.signingInfo.getApkContentsSigners();
        if (installed.signingInfo.hasMultipleSigners() || archive.signingInfo.hasMultipleSigners())
            return sameSignatures(current, next);
        if (sameSignatures(current, next)) return true;
        // Accept forward certificate rotation only when Android verified that
        // the new archive's signing lineage contains the currently installed signer.
        if (current == null || next == null || current.length != 1 || next.length != 1) return false;
        Signature[] history = archive.signingInfo.getSigningCertificateHistory();
        if (history != null) for (Signature signer : history) if (current[0].equals(signer)) return true;
        return false;
    }

    private void requestInstall(File apk, long version, InstallCallback callback) {
        if (!resumed || destroyed) return;
        if (Build.VERSION.SDK_INT >= 26
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            prefs.edit().putLong(KEY_PENDING_LONG, version)
                    .putBoolean(KEY_WAITING_PERMISSION, true).remove(KEY_INSTALL_READY).apply();
            if (callback != null) callback.onStatus("Allow Ella Monitoring to install updates, then return to the app.");
            try {
                activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + activity.getPackageName())));
            } catch (Exception error) {
                prefs.edit().remove(KEY_WAITING_PERMISSION).remove(KEY_INSTALL_READY).apply();
                postFailure(callback, "Cannot open Android's install permission screen");
            }
            return;
        }
        if (callback != null) callback.onStatus("Opening Android installer…");
        prefs.edit().remove(KEY_WAITING_PERMISSION).remove(KEY_INSTALL_READY).apply();
        if (launchInstaller()) {
            if (callback != null) callback.onInstallerOpened();
        } else postFailure(callback, "No Android package installer is available; retry Download & Install");
    }

    private boolean launchInstaller() {
        for (String action : new String[]{Intent.ACTION_INSTALL_PACKAGE, Intent.ACTION_VIEW}) {
            Intent install = new Intent(action);
            Uri uri = UpdateFileProvider.updateUri();
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.setClipData(ClipData.newRawUri("Ella Monitoring update", uri));
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try { activity.startActivity(install); return true; }
            catch (Exception ignored) { }
        }
        return false;
    }

    private static boolean sameSignatures(Signature[] a, Signature[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) return false;
        outer: for (Signature left : a) {
            for (Signature right : b) if (left.equals(right)) continue outer;
            return false;
        }
        return true;
    }

    private static String readUtf8(String url, int maxBytes) throws Exception {
        HttpURLConnection connection = open(url);
        int response = connection.getResponseCode();
        if (response < 200 || response >= 300) {
            connection.disconnect();
            throw new Exception("Update check failed (HTTP " + response + ")");
        }
        StringBuilder result = new StringBuilder();
        int read = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "UTF-8"))) {
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                read += count * 2;
                if (read > maxBytes) throw new Exception("Update response is unexpectedly large");
                result.append(buffer, 0, count);
            }
        } finally {
            connection.disconnect();
        }
        return result.toString();
    }

    private static HttpURLConnection open(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Accept", url.startsWith("https://raw.githubusercontent.com/")
                ? "text/plain" : "application/vnd.github+json");
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        connection.setRequestProperty("User-Agent", "Ella-Monitoring/" + BuildConfig.VERSION_NAME);
        return connection;
    }

    private long currentVersionCode() {
        try { return versionCode(activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)); }
        catch (Exception ignored) { return BuildConfig.VERSION_CODE; }
    }

    private String currentVersionName() {
        try {
            String value = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            return value == null ? BuildConfig.VERSION_NAME : value;
        } catch (Exception ignored) { return BuildConfig.VERSION_NAME; }
    }

    String currentChangelog() {
        String cached = prefs.getString(KEY_CHANGELOG, "");
        if (ChangelogText.valid(cached)) return cached;
        if (bundledChangelog == null) {
            try (InputStream input = activity.getAssets().open("changelog.md")) {
                bundledChangelog = readUtf8(input, MAX_RESPONSE_BYTES);
            } catch (Exception ignored) { bundledChangelog = ""; }
        }
        return bundledChangelog;
    }

    void loadChangelog(ChangelogCallback callback) {
        new Thread(() -> {
            String file;
            boolean fresh;
            try {
                file = readUtf8(CHANGELOG_URL, MAX_RESPONSE_BYTES);
                if (!ChangelogText.valid(file)) throw new Exception("Incomplete changelog file");
                prefs.edit().putString(KEY_CHANGELOG, file).apply();
                fresh = true;
            } catch (Exception ignored) { file = currentChangelog(); fresh = false; }
            final String result = file;
            final boolean downloaded = fresh;
            main.post(() -> { if (!destroyed && callback != null) callback.onComplete(result, downloaded); });
        }, "ella-changelog-load").start();
    }

    private static String readUtf8(InputStream input, int maxBytes) throws Exception {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            char[] buffer = new char[4096];
            int total = 0, count;
            while ((count = reader.read(buffer)) != -1) {
                total += count * 2;
                if (total > maxBytes) throw new Exception("Changelog is unexpectedly large");
                text.append(buffer, 0, count);
            }
        }
        return text.toString();
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[32 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        StringBuilder hex = new StringBuilder(64);
        for (byte value : digest.digest()) hex.append(String.format(Locale.US, "%02x", value & 0xff));
        return hex.toString();
    }

    private void post(CheckCallback callback, CheckResult result) {
        main.post(() -> { if (!destroyed && callback != null) callback.onComplete(result); });
    }

    private void postStatus(InstallCallback callback, String status) {
        main.post(() -> { if (!destroyed && callback != null) callback.onStatus(status); });
    }

    private void postFailure(InstallCallback callback, String message) {
        main.post(() -> {
            if (destroyed) return;
            if (callback != null) callback.onFailure(message);
            else android.widget.Toast.makeText(activity, "Update failed: " + message,
                    android.widget.Toast.LENGTH_LONG).show();
        });
    }

    private static String shortMessage(Exception error) {
        String message = error == null ? null : error.getMessage();
        if (message == null || message.trim().length() == 0) return "Unknown update error";
        return message.length() > 180 ? message.substring(0, 180) : message;
    }
}

