package androidx.core.location;

import android.os.CancellationSignal;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class LocationManagerCompat$$ExternalSyntheticLambda1 implements CancellationSignal.OnCancelListener {
    public final /* synthetic */ LocationManagerCompat.CancellableLocationListener f$0;

    @Override // android.os.CancellationSignal.OnCancelListener
    public final void onCancel() {
        this.f$0.cancel();
    }
}
