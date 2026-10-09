package com.colonellp.ellamonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.content.SharedPreferences;
import android.view.WindowManager;

/** FYT Main only: module 0, display command 13, update 36. No audio/DSP commands. */
final class ScreenControl {
    private final Activity activity;
    private final SharedPreferences prefs;
    private IBinder main;
    private boolean bound, started, keep;
    private int timeout = -1;
    private PowerManager.WakeLock wake;
    private final android.os.Handler renewHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable renew = new Runnable() {
        @Override public void run() { if (started) { apply(); renewHandler.postDelayed(this, 60000); } }
    };
    private final Binder callback = new Binder() {
        @Override protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == INTERFACE_TRANSACTION) { if (reply != null) reply.writeString("com.syu.ipc.IModuleCallback"); return true; }
            if (code != 1) return super.onTransact(code, data, reply, flags);
            data.enforceInterface("com.syu.ipc.IModuleCallback"); int update = data.readInt(); int[] values = data.createIntArray(); data.createFloatArray(); data.createStringArray();
            if (update == 36 && values != null && values.length > 0) activity.runOnUiThread(() -> {
                timeout = values[0]; if (started && keep && timeout > 0 && !prefs.contains("fytTimeout")) prefs.edit().putInt("fytTimeout", timeout).apply(); apply();
            });
            return true;
        }
    };
    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            Parcel data = Parcel.obtain(), reply = Parcel.obtain();
            try {
                if (!"com.syu.ipc.IRemoteToolkit".equals(service.getInterfaceDescriptor())) return;
                data.writeInterfaceToken("com.syu.ipc.IRemoteToolkit"); data.writeInt(0);
                if (!service.transact(1, data, reply, 0)) return;
                reply.readException(); IBinder module = reply.readStrongBinder();
                if (module == null || !"com.syu.ipc.IRemoteModule".equals(module.getInterfaceDescriptor())) return;
                main = module; register(true); apply();
            } catch (Exception ignored) { main = null; }
            finally { data.recycle(); reply.recycle(); }
        }
        @Override public void onServiceDisconnected(ComponentName name) { main = null; releaseWake(); }
    };
    ScreenControl(Activity activity, SharedPreferences prefs) { this.activity = activity; this.prefs = prefs; }
    void start(boolean keep) {
        this.started = true; this.keep = keep;
        renewHandler.removeCallbacks(renew); renewHandler.postDelayed(renew, 60000);
        Intent intent = new Intent("com.syu.ms.toolkit").setPackage("com.syu.ms");
        try { if (!bound && !activity.getPackageManager().queryIntentServices(intent, 0).isEmpty()) bound = activity.bindService(intent, connection, Activity.BIND_AUTO_CREATE); }
        catch (Exception ignored) { bound = false; }
        apply();
    }
    void setKeep(boolean keep) { this.keep = keep; apply(); }
    @SuppressWarnings("deprecation") void apply() {
        boolean active = started && keep && !activity.isFinishing();
        if (active) activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.getWindow().getDecorView().setKeepScreenOn(active);
        if (main == null) { releaseWake(); return; }
        if (active) {
            if (timeout > 0 && !prefs.contains("fytTimeout")) prefs.edit().putInt("fytTimeout", timeout).apply();
            if (timeout != 0 && command(0)) { timeout = 0; prefs.edit().putBoolean("fytApplied", true).apply(); }
            // The old head-unit workaround is restricted to detected FYT on legacy Android.
            if (android.os.Build.VERSION.SDK_INT <= 28 && wake == null) {
                try {
                    PowerManager power = (PowerManager)activity.getSystemService(Activity.POWER_SERVICE);
                    wake = power.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "EllaMonitor:ForegroundScreen");
                    wake.setReferenceCounted(false);
                } catch (Exception ignored) { releaseWake(); }
            }
            if (wake != null) try { wake.acquire(120000L); } catch (Exception ignored) { releaseWake(); }
        } else {
            releaseWake();
            if (prefs.getBoolean("fytApplied", false)) {
                int restore = Math.max(30, prefs.getInt("fytTimeout", 30));
                if (command(restore)) { timeout = restore; prefs.edit().remove("fytTimeout").remove("fytApplied").apply(); }
            }
        }
    }
    private boolean command(int value) {
        Parcel data = Parcel.obtain();
        try { data.writeInterfaceToken("com.syu.ipc.IRemoteModule"); data.writeInt(13); data.writeIntArray(new int[]{value}); data.writeFloatArray(null); data.writeStringArray(null); return main.transact(1, data, null, IBinder.FLAG_ONEWAY); }
        catch (Exception e) { return false; } finally { data.recycle(); }
    }
    private void register(boolean add) {
        if (main == null) return;
        Parcel data = Parcel.obtain();
        try { data.writeInterfaceToken("com.syu.ipc.IRemoteModule"); data.writeStrongBinder(callback); data.writeInt(36); if (add) data.writeInt(1); main.transact(add ? 3 : 4, data, null, IBinder.FLAG_ONEWAY); }
        catch (Exception ignored) { } finally { data.recycle(); }
    }
    private void releaseWake() { try { if (wake != null && wake.isHeld()) wake.release(); } catch (Exception ignored) { } wake = null; }
    void stop() {
        started = false; renewHandler.removeCallbacks(renew); apply(); register(false);
        if (bound) try { activity.unbindService(connection); } catch (Exception ignored) { }
        bound = false; main = null; timeout = -1;
    }
}
