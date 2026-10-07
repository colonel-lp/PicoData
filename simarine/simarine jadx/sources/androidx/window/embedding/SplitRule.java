package androidx.window.embedding;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.WindowMetrics;
import androidx.core.util.Preconditions;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: SplitRule.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0000\n\u0002\b\u0007\b\u0016\u0018\u0000 -2\u00020\u0001:\u0004+,-.BM\b\u0000\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0003\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0003\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0003\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\u0006\u0010\u000b\u001a\u00020\f¢\u0006\u0002\u0010\rJ\u001d\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0000¢\u0006\u0002\b\u001dJ\u001d\u0010\u001e\u001a\u00020\u00182\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"H\u0000¢\u0006\u0002\b#J\u001a\u0010$\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u001a2\b\b\u0001\u0010%\u001a\u00020\u0005H\u0002J\u0013\u0010&\u001a\u00020\u00182\b\u0010'\u001a\u0004\u0018\u00010(H\u0096\u0002J\b\u0010)\u001a\u00020\u0005H\u0016J\b\u0010*\u001a\u00020\u0003H\u0016R\u0011\u0010\u000b\u001a\u00020\f¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\n\u001a\u00020\t¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\b\u001a\u00020\t¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0007\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014¨\u0006/"}, d2 = {"Landroidx/window/embedding/SplitRule;", "Landroidx/window/embedding/EmbeddingRule;", "tag", "", "minWidthDp", "", "minHeightDp", "minSmallestWidthDp", "maxAspectRatioInPortrait", "Landroidx/window/embedding/EmbeddingAspectRatio;", "maxAspectRatioInLandscape", "defaultSplitAttributes", "Landroidx/window/embedding/SplitAttributes;", "(Ljava/lang/String;IIILandroidx/window/embedding/EmbeddingAspectRatio;Landroidx/window/embedding/EmbeddingAspectRatio;Landroidx/window/embedding/SplitAttributes;)V", "getDefaultSplitAttributes", "()Landroidx/window/embedding/SplitAttributes;", "getMaxAspectRatioInLandscape", "()Landroidx/window/embedding/EmbeddingAspectRatio;", "getMaxAspectRatioInPortrait", "getMinHeightDp", "()I", "getMinSmallestWidthDp", "getMinWidthDp", "checkParentBounds", "", "density", "", "bounds", "Landroid/graphics/Rect;", "checkParentBounds$window_release", "checkParentMetrics", "context", "Landroid/content/Context;", "parentMetrics", "Landroid/view/WindowMetrics;", "checkParentMetrics$window_release", "convertDpToPx", "dimensionDp", "equals", "other", "", "hashCode", "toString", "Api30Impl", "Api34Impl", "Companion", "FinishBehavior", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public class SplitRule extends EmbeddingRule {
    public static final int SPLIT_MIN_DIMENSION_ALWAYS_ALLOW = 0;
    public static final int SPLIT_MIN_DIMENSION_DP_DEFAULT = 600;
    private final SplitAttributes defaultSplitAttributes;
    private final EmbeddingAspectRatio maxAspectRatioInLandscape;
    private final EmbeddingAspectRatio maxAspectRatioInPortrait;
    private final int minHeightDp;
    private final int minSmallestWidthDp;
    private final int minWidthDp;
    public static final EmbeddingAspectRatio SPLIT_MAX_ASPECT_RATIO_PORTRAIT_DEFAULT = EmbeddingAspectRatio.INSTANCE.ratio(1.4f);
    public static final EmbeddingAspectRatio SPLIT_MAX_ASPECT_RATIO_LANDSCAPE_DEFAULT = EmbeddingAspectRatio.ALWAYS_ALLOW;

    private final int convertDpToPx(float density, int dimensionDp) {
        return (int) ((dimensionDp * density) + 0.5f);
    }

    public final int getMinWidthDp() {
        return this.minWidthDp;
    }

    public final int getMinHeightDp() {
        return this.minHeightDp;
    }

    public final int getMinSmallestWidthDp() {
        return this.minSmallestWidthDp;
    }

    public /* synthetic */ SplitRule(String str, int i, int i2, int i3, EmbeddingAspectRatio embeddingAspectRatio, EmbeddingAspectRatio embeddingAspectRatio2, SplitAttributes splitAttributes, int i4, DefaultConstructorMarker defaultConstructorMarker) {
        this((i4 & 1) != 0 ? null : str, (i4 & 2) != 0 ? 600 : i, (i4 & 4) != 0 ? 600 : i2, (i4 & 8) != 0 ? 600 : i3, (i4 & 16) != 0 ? SPLIT_MAX_ASPECT_RATIO_PORTRAIT_DEFAULT : embeddingAspectRatio, (i4 & 32) != 0 ? SPLIT_MAX_ASPECT_RATIO_LANDSCAPE_DEFAULT : embeddingAspectRatio2, splitAttributes);
    }

    public final EmbeddingAspectRatio getMaxAspectRatioInPortrait() {
        return this.maxAspectRatioInPortrait;
    }

    public final EmbeddingAspectRatio getMaxAspectRatioInLandscape() {
        return this.maxAspectRatioInLandscape;
    }

    public final SplitAttributes getDefaultSplitAttributes() {
        return this.defaultSplitAttributes;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SplitRule(String str, int i, int i2, int i3, EmbeddingAspectRatio maxAspectRatioInPortrait, EmbeddingAspectRatio maxAspectRatioInLandscape, SplitAttributes defaultSplitAttributes) {
        super(str);
        Intrinsics.checkNotNullParameter(maxAspectRatioInPortrait, "maxAspectRatioInPortrait");
        Intrinsics.checkNotNullParameter(maxAspectRatioInLandscape, "maxAspectRatioInLandscape");
        Intrinsics.checkNotNullParameter(defaultSplitAttributes, "defaultSplitAttributes");
        this.minWidthDp = i;
        this.minHeightDp = i2;
        this.minSmallestWidthDp = i3;
        this.maxAspectRatioInPortrait = maxAspectRatioInPortrait;
        this.maxAspectRatioInLandscape = maxAspectRatioInLandscape;
        this.defaultSplitAttributes = defaultSplitAttributes;
        Preconditions.checkArgumentNonnegative(i, "minWidthDp must be non-negative");
        Preconditions.checkArgumentNonnegative(i2, "minHeightDp must be non-negative");
        Preconditions.checkArgumentNonnegative(i3, "minSmallestWidthDp must be non-negative");
    }

    /* JADX INFO: compiled from: SplitRule.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0017\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\b\u0010\t\u001a\u00020\u0003H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u0004\u001a\u00020\u0005X\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b¨\u0006\u000b"}, d2 = {"Landroidx/window/embedding/SplitRule$FinishBehavior;", "", "description", "", "value", "", "(Ljava/lang/String;I)V", "getValue$window_release", "()I", "toString", "Companion", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class FinishBehavior {
        private final String description;
        private final int value;

        /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
        public static final Companion INSTANCE = new Companion(null);
        public static final FinishBehavior NEVER = new FinishBehavior("NEVER", 0);
        public static final FinishBehavior ALWAYS = new FinishBehavior("ALWAYS", 1);
        public static final FinishBehavior ADJACENT = new FinishBehavior("ADJACENT", 2);

        private FinishBehavior(String str, int i) {
            this.description = str;
            this.value = i;
        }

        /* JADX INFO: renamed from: getValue$window_release, reason: from getter */
        public final int getValue() {
            return this.value;
        }

        /* JADX INFO: renamed from: toString, reason: from getter */
        public String getDescription() {
            return this.description;
        }

        /* JADX INFO: compiled from: SplitRule.kt */
        @Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0017\u0010\u0007\u001a\u00020\u00042\b\b\u0001\u0010\b\u001a\u00020\tH\u0001¢\u0006\u0002\b\nR\u0010\u0010\u0003\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000¨\u0006\u000b"}, d2 = {"Landroidx/window/embedding/SplitRule$FinishBehavior$Companion;", "", "()V", "ADJACENT", "Landroidx/window/embedding/SplitRule$FinishBehavior;", "ALWAYS", "NEVER", "getFinishBehaviorFromValue", "value", "", "getFinishBehaviorFromValue$window_release", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
        public static final class Companion {
            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            private Companion() {
            }

            @JvmStatic
            public final FinishBehavior getFinishBehaviorFromValue$window_release(int value) {
                if (value == FinishBehavior.NEVER.getValue()) {
                    return FinishBehavior.NEVER;
                }
                if (value == FinishBehavior.ALWAYS.getValue()) {
                    return FinishBehavior.ALWAYS;
                }
                if (value == FinishBehavior.ADJACENT.getValue()) {
                    return FinishBehavior.ADJACENT;
                }
                throw new IllegalArgumentException("Unknown finish behavior:" + value);
            }
        }
    }

    public final boolean checkParentMetrics$window_release(Context context, WindowMetrics parentMetrics) {
        float density;
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(parentMetrics, "parentMetrics");
        if (Build.VERSION.SDK_INT <= 30) {
            return false;
        }
        Rect bounds = Api30Impl.INSTANCE.getBounds(parentMetrics);
        if (Build.VERSION.SDK_INT <= 33) {
            density = context.getResources().getDisplayMetrics().density;
        } else {
            density = Api34Impl.INSTANCE.getDensity(parentMetrics, context);
        }
        return checkParentBounds$window_release(density, bounds);
    }

    public final boolean checkParentBounds$window_release(float density, Rect bounds) {
        Intrinsics.checkNotNullParameter(bounds, "bounds");
        int iWidth = bounds.width();
        int iHeight = bounds.height();
        if (iWidth == 0 || iHeight == 0) {
            return false;
        }
        return (this.minWidthDp == 0 || iWidth >= convertDpToPx(density, this.minWidthDp)) && (this.minHeightDp == 0 || iHeight >= convertDpToPx(density, this.minHeightDp)) && (this.minSmallestWidthDp == 0 || Math.min(iWidth, iHeight) >= convertDpToPx(density, this.minSmallestWidthDp)) && (iHeight < iWidth ? Intrinsics.areEqual(this.maxAspectRatioInLandscape, EmbeddingAspectRatio.ALWAYS_ALLOW) || (((((float) iWidth) * 1.0f) / ((float) iHeight)) > this.maxAspectRatioInLandscape.getValue() ? 1 : (((((float) iWidth) * 1.0f) / ((float) iHeight)) == this.maxAspectRatioInLandscape.getValue() ? 0 : -1)) <= 0 : Intrinsics.areEqual(this.maxAspectRatioInPortrait, EmbeddingAspectRatio.ALWAYS_ALLOW) || (((((float) iHeight) * 1.0f) / ((float) iWidth)) > this.maxAspectRatioInPortrait.getValue() ? 1 : (((((float) iHeight) * 1.0f) / ((float) iWidth)) == this.maxAspectRatioInPortrait.getValue() ? 0 : -1)) <= 0);
    }

    /* JADX INFO: compiled from: SplitRule.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bÁ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007¨\u0006\u0007"}, d2 = {"Landroidx/window/embedding/SplitRule$Api30Impl;", "", "()V", "getBounds", "Landroid/graphics/Rect;", "windowMetrics", "Landroid/view/WindowMetrics;", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Api30Impl {
        public static final Api30Impl INSTANCE = new Api30Impl();

        private Api30Impl() {
        }

        public final Rect getBounds(WindowMetrics windowMetrics) {
            Intrinsics.checkNotNullParameter(windowMetrics, "windowMetrics");
            Rect bounds = windowMetrics.getBounds();
            Intrinsics.checkNotNullExpressionValue(bounds, "windowMetrics.bounds");
            return bounds;
        }
    }

    /* JADX INFO: compiled from: SplitRule.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bÁ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007¨\u0006\t"}, d2 = {"Landroidx/window/embedding/SplitRule$Api34Impl;", "", "()V", "getDensity", "", "windowMetrics", "Landroid/view/WindowMetrics;", "context", "Landroid/content/Context;", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Api34Impl {
        public static final Api34Impl INSTANCE = new Api34Impl();

        private Api34Impl() {
        }

        public final float getDensity(WindowMetrics windowMetrics, Context context) {
            Intrinsics.checkNotNullParameter(windowMetrics, "windowMetrics");
            Intrinsics.checkNotNullParameter(context, "context");
            try {
                return windowMetrics.getDensity();
            } catch (NoSuchMethodError unused) {
                return context.getResources().getDisplayMetrics().density;
            }
        }
    }

    @Override // androidx.window.embedding.EmbeddingRule
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SplitRule) || !super.equals(other)) {
            return false;
        }
        SplitRule splitRule = (SplitRule) other;
        return this.minWidthDp == splitRule.minWidthDp && this.minHeightDp == splitRule.minHeightDp && this.minSmallestWidthDp == splitRule.minSmallestWidthDp && Intrinsics.areEqual(this.maxAspectRatioInPortrait, splitRule.maxAspectRatioInPortrait) && Intrinsics.areEqual(this.maxAspectRatioInLandscape, splitRule.maxAspectRatioInLandscape) && Intrinsics.areEqual(this.defaultSplitAttributes, splitRule.defaultSplitAttributes);
    }

    @Override // androidx.window.embedding.EmbeddingRule
    public int hashCode() {
        return (((((((((((super.hashCode() * 31) + this.minWidthDp) * 31) + this.minHeightDp) * 31) + this.minSmallestWidthDp) * 31) + this.maxAspectRatioInPortrait.hashCode()) * 31) + this.maxAspectRatioInLandscape.hashCode()) * 31) + this.defaultSplitAttributes.hashCode();
    }

    public String toString() {
        return "SplitRule{ tag=" + getTag() + ", defaultSplitAttributes=" + this.defaultSplitAttributes + ", minWidthDp=" + this.minWidthDp + ", minHeightDp=" + this.minHeightDp + ", minSmallestWidthDp=" + this.minSmallestWidthDp + ", maxAspectRatioInPortrait=" + this.maxAspectRatioInPortrait + ", maxAspectRatioInLandscape=" + this.maxAspectRatioInLandscape + '}';
    }
}
