package androidx.window.core;

import android.util.Log;
import androidx.window.extensions.WindowExtensionsProvider;
import kotlin.Metadata;
import kotlin.jvm.internal.Reflection;

/* JADX INFO: compiled from: ExtensionsUtil.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\bÀ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u00068G¢\u0006\u0006\u001a\u0004\b\u0007\u0010\b¨\u0006\t"}, d2 = {"Landroidx/window/core/ExtensionsUtil;", "", "()V", "TAG", "", "safeVendorApiLevel", "", "getSafeVendorApiLevel", "()I", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class ExtensionsUtil {
    public static final ExtensionsUtil INSTANCE = new ExtensionsUtil();
    private static final String TAG = Reflection.getOrCreateKotlinClass(ExtensionsUtil.class).getSimpleName();

    private ExtensionsUtil() {
    }

    public final int getSafeVendorApiLevel() {
        try {
            return WindowExtensionsProvider.getWindowExtensions().getVendorApiLevel();
        } catch (NoClassDefFoundError unused) {
            if (BuildConfig.INSTANCE.getVerificationMode() != VerificationMode.LOG) {
                return 0;
            }
            Log.d(TAG, "Embedding extension version not found");
            return 0;
        } catch (UnsupportedOperationException unused2) {
            if (BuildConfig.INSTANCE.getVerificationMode() != VerificationMode.LOG) {
                return 0;
            }
            Log.d(TAG, "Stub Extension");
            return 0;
        }
    }
}
