package com.microsoft.maui;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformMauiAppCompatActivity {
    public static void onCreate(AppCompatActivity appCompatActivity, Bundle bundle, boolean z, int i, int i2) {
        if (!z && bundle != null) {
            bundle.remove("android:support:fragments");
            bundle.remove("androidx.lifecycle.BundlableSavedStateRegistry.key");
        }
        TypedArray typedArrayObtainStyledAttributes = null;
        try {
            typedArrayObtainStyledAttributes = appCompatActivity.obtainStyledAttributes(new int[]{i});
            boolean z2 = typedArrayObtainStyledAttributes.getBoolean(0, false);
            if (typedArrayObtainStyledAttributes != null) {
                typedArrayObtainStyledAttributes.recycle();
            }
            if (z2) {
                appCompatActivity.setTheme(i2);
            }
        } catch (Throwable th) {
            if (typedArrayObtainStyledAttributes != null) {
                typedArrayObtainStyledAttributes.recycle();
            }
            throw th;
        }
    }
}
