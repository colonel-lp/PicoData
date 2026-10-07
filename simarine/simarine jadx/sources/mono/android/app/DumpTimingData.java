package mono.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import mono.android.Runtime;

/* JADX INFO: loaded from: classes2.dex */
public class DumpTimingData extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Runtime.dumpTimingData();
    }
}
