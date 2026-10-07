package androidx.window.layout.adapter.sidecar;

import android.os.IBinder;
import androidx.window.sidecar.SidecarDeviceState;
import androidx.window.sidecar.SidecarInterface;
import androidx.window.sidecar.SidecarWindowLayoutInfo;
import java.util.Map;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes.dex */
public class DistinctElementSidecarCallback implements SidecarInterface.SidecarCallback {
    private final Map<IBinder, SidecarWindowLayoutInfo> mActivityWindowLayoutInfo;
    private final SidecarAdapter mAdapter;
    private final SidecarInterface.SidecarCallback mCallback;
    private SidecarDeviceState mLastDeviceState;
    private final Object mLock;

    DistinctElementSidecarCallback(SidecarAdapter sidecarAdapter, SidecarInterface.SidecarCallback sidecarCallback) {
        this.mLock = new Object();
        this.mActivityWindowLayoutInfo = new WeakHashMap();
        this.mAdapter = sidecarAdapter;
        this.mCallback = sidecarCallback;
    }

    public DistinctElementSidecarCallback(SidecarInterface.SidecarCallback sidecarCallback) {
        this.mLock = new Object();
        this.mActivityWindowLayoutInfo = new WeakHashMap();
        this.mAdapter = new SidecarAdapter();
        this.mCallback = sidecarCallback;
    }

    public void onDeviceStateChanged(SidecarDeviceState sidecarDeviceState) {
        if (sidecarDeviceState == null) {
            return;
        }
        synchronized (this.mLock) {
            if (this.mAdapter.isEqualSidecarDeviceState(this.mLastDeviceState, sidecarDeviceState)) {
                return;
            }
            this.mLastDeviceState = sidecarDeviceState;
            this.mCallback.onDeviceStateChanged(sidecarDeviceState);
        }
    }

    public void onWindowLayoutChanged(IBinder iBinder, SidecarWindowLayoutInfo sidecarWindowLayoutInfo) {
        synchronized (this.mLock) {
            if (this.mAdapter.isEqualSidecarWindowLayoutInfo(this.mActivityWindowLayoutInfo.get(iBinder), sidecarWindowLayoutInfo)) {
                return;
            }
            this.mActivityWindowLayoutInfo.put(iBinder, sidecarWindowLayoutInfo);
            this.mCallback.onWindowLayoutChanged(iBinder, sidecarWindowLayoutInfo);
        }
    }
}
