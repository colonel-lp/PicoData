package androidx.window.layout;

import android.graphics.Rect;
import androidx.core.view.WindowInsetsCompat;
import androidx.window.core.Bounds;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: WindowMetrics.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\u0018\u00002\u00020\u0001B\u0019\b\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006B\u0017\b\u0000\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\u0005¢\u0006\u0002\u0010\nJ\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\b\u0010\u0010\u001a\u00020\u0005H\u0007J\b\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010\u0013\u001a\u00020\u0014H\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u0002\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u000b\u0010\f¨\u0006\u0015"}, d2 = {"Landroidx/window/layout/WindowMetrics;", "", "bounds", "Landroid/graphics/Rect;", "insets", "Landroidx/core/view/WindowInsetsCompat;", "(Landroid/graphics/Rect;Landroidx/core/view/WindowInsetsCompat;)V", "_bounds", "Landroidx/window/core/Bounds;", "_windowInsetsCompat", "(Landroidx/window/core/Bounds;Landroidx/core/view/WindowInsetsCompat;)V", "getBounds", "()Landroid/graphics/Rect;", "equals", "", "other", "getWindowInsets", "hashCode", "", "toString", "", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class WindowMetrics {
    private final Bounds _bounds;
    private final WindowInsetsCompat _windowInsetsCompat;

    public WindowMetrics(Bounds _bounds, WindowInsetsCompat _windowInsetsCompat) {
        Intrinsics.checkNotNullParameter(_bounds, "_bounds");
        Intrinsics.checkNotNullParameter(_windowInsetsCompat, "_windowInsetsCompat");
        this._bounds = _bounds;
        this._windowInsetsCompat = _windowInsetsCompat;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public /* synthetic */ WindowMetrics(Rect rect, WindowInsetsCompat windowInsetsCompat, int i, DefaultConstructorMarker defaultConstructorMarker) {
        if ((i & 2) != 0) {
            windowInsetsCompat = new WindowInsetsCompat.Builder().build();
            Intrinsics.checkNotNullExpressionValue(windowInsetsCompat, "Builder().build()");
        }
        this(rect, windowInsetsCompat);
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public WindowMetrics(Rect bounds, WindowInsetsCompat insets) {
        this(new Bounds(bounds), insets);
        Intrinsics.checkNotNullParameter(bounds, "bounds");
        Intrinsics.checkNotNullParameter(insets, "insets");
    }

    public final Rect getBounds() {
        return this._bounds.toRect();
    }

    public String toString() {
        return "WindowMetrics( bounds=" + this._bounds + ", windowInsetsCompat=" + this._windowInsetsCompat + ')';
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!Intrinsics.areEqual(getClass(), other != null ? other.getClass() : null)) {
            return false;
        }
        Intrinsics.checkNotNull(other, "null cannot be cast to non-null type androidx.window.layout.WindowMetrics");
        WindowMetrics windowMetrics = (WindowMetrics) other;
        return Intrinsics.areEqual(this._bounds, windowMetrics._bounds) && Intrinsics.areEqual(this._windowInsetsCompat, windowMetrics._windowInsetsCompat);
    }

    public int hashCode() {
        return (this._bounds.hashCode() * 31) + this._windowInsetsCompat.hashCode();
    }

    /* JADX INFO: renamed from: getWindowInsets, reason: from getter */
    public final WindowInsetsCompat get_windowInsetsCompat() {
        return this._windowInsetsCompat;
    }
}
