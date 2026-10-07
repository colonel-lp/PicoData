package androidx.window.embedding;

import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: SplitPairRule.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0000\n\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001%By\b\u0000\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\n\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0003\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0003\u0010\u0010\u001a\u00020\u000f\u0012\b\b\u0003\u0010\u0011\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0013¢\u0006\u0002\u0010\u0015J\u0013\u0010\u001d\u001a\u00020\r2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0096\u0002J\b\u0010 \u001a\u00020\u000fH\u0016J\u0016\u0010!\u001a\u00020\u00002\u0006\u0010\"\u001a\u00020\u0004H\u0080\u0002¢\u0006\u0002\b#J\b\u0010$\u001a\u00020\bH\u0016R\u0011\u0010\f\u001a\u00020\r¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\t\u001a\u00020\n¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u000b\u001a\u00020\n¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001b¨\u0006&"}, d2 = {"Landroidx/window/embedding/SplitPairRule;", "Landroidx/window/embedding/SplitRule;", "filters", "", "Landroidx/window/embedding/SplitPairFilter;", "defaultSplitAttributes", "Landroidx/window/embedding/SplitAttributes;", "tag", "", "finishPrimaryWithSecondary", "Landroidx/window/embedding/SplitRule$FinishBehavior;", "finishSecondaryWithPrimary", "clearTop", "", "minWidthDp", "", "minHeightDp", "minSmallestWidthDp", "maxAspectRatioInPortrait", "Landroidx/window/embedding/EmbeddingAspectRatio;", "maxAspectRatioInLandscape", "(Ljava/util/Set;Landroidx/window/embedding/SplitAttributes;Ljava/lang/String;Landroidx/window/embedding/SplitRule$FinishBehavior;Landroidx/window/embedding/SplitRule$FinishBehavior;ZIIILandroidx/window/embedding/EmbeddingAspectRatio;Landroidx/window/embedding/EmbeddingAspectRatio;)V", "getClearTop", "()Z", "getFilters", "()Ljava/util/Set;", "getFinishPrimaryWithSecondary", "()Landroidx/window/embedding/SplitRule$FinishBehavior;", "getFinishSecondaryWithPrimary", "equals", "other", "", "hashCode", "plus", "filter", "plus$window_release", "toString", "Builder", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class SplitPairRule extends SplitRule {
    private final boolean clearTop;
    private final Set<SplitPairFilter> filters;
    private final SplitRule.FinishBehavior finishPrimaryWithSecondary;
    private final SplitRule.FinishBehavior finishSecondaryWithPrimary;

    public final Set<SplitPairFilter> getFilters() {
        return this.filters;
    }

    public /* synthetic */ SplitPairRule(Set set, SplitAttributes splitAttributes, String str, SplitRule.FinishBehavior finishBehavior, SplitRule.FinishBehavior finishBehavior2, boolean z, int i, int i2, int i3, EmbeddingAspectRatio embeddingAspectRatio, EmbeddingAspectRatio embeddingAspectRatio2, int i4, DefaultConstructorMarker defaultConstructorMarker) {
        this(set, splitAttributes, (i4 & 4) != 0 ? null : str, (i4 & 8) != 0 ? SplitRule.FinishBehavior.NEVER : finishBehavior, (i4 & 16) != 0 ? SplitRule.FinishBehavior.ALWAYS : finishBehavior2, (i4 & 32) != 0 ? false : z, (i4 & 64) != 0 ? 600 : i, (i4 & 128) != 0 ? 600 : i2, (i4 & 256) != 0 ? 600 : i3, (i4 & 512) != 0 ? SplitRule.SPLIT_MAX_ASPECT_RATIO_PORTRAIT_DEFAULT : embeddingAspectRatio, (i4 & 1024) != 0 ? SplitRule.SPLIT_MAX_ASPECT_RATIO_LANDSCAPE_DEFAULT : embeddingAspectRatio2);
    }

    public final SplitRule.FinishBehavior getFinishPrimaryWithSecondary() {
        return this.finishPrimaryWithSecondary;
    }

    public final SplitRule.FinishBehavior getFinishSecondaryWithPrimary() {
        return this.finishSecondaryWithPrimary;
    }

    public final boolean getClearTop() {
        return this.clearTop;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SplitPairRule(Set<SplitPairFilter> filters, SplitAttributes defaultSplitAttributes, String str, SplitRule.FinishBehavior finishPrimaryWithSecondary, SplitRule.FinishBehavior finishSecondaryWithPrimary, boolean z, int i, int i2, int i3, EmbeddingAspectRatio maxAspectRatioInPortrait, EmbeddingAspectRatio maxAspectRatioInLandscape) {
        super(str, i, i2, i3, maxAspectRatioInPortrait, maxAspectRatioInLandscape, defaultSplitAttributes);
        Intrinsics.checkNotNullParameter(filters, "filters");
        Intrinsics.checkNotNullParameter(defaultSplitAttributes, "defaultSplitAttributes");
        Intrinsics.checkNotNullParameter(finishPrimaryWithSecondary, "finishPrimaryWithSecondary");
        Intrinsics.checkNotNullParameter(finishSecondaryWithPrimary, "finishSecondaryWithPrimary");
        Intrinsics.checkNotNullParameter(maxAspectRatioInPortrait, "maxAspectRatioInPortrait");
        Intrinsics.checkNotNullParameter(maxAspectRatioInLandscape, "maxAspectRatioInLandscape");
        this.filters = filters;
        this.finishPrimaryWithSecondary = finishPrimaryWithSecondary;
        this.finishSecondaryWithPrimary = finishSecondaryWithPrimary;
        this.clearTop = z;
    }

    /* JADX INFO: compiled from: SplitPairRule.kt */
    @Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u00002\u00020\u0001B\u0013\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\u0002\u0010\u0005J\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u0019\u001a\u00020\u00002\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\u001a\u001a\u00020\u00002\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u001b\u001a\u00020\u00002\u0006\u0010\f\u001a\u00020\u000bJ\u000e\u0010\u001c\u001a\u00020\u00002\u0006\u0010\u001d\u001a\u00020\u000eJ\u000e\u0010\u001e\u001a\u00020\u00002\u0006\u0010\u001d\u001a\u00020\u000eJ\u0010\u0010\u001f\u001a\u00020\u00002\b\b\u0001\u0010\u0010\u001a\u00020\u0011J\u0010\u0010 \u001a\u00020\u00002\b\b\u0001\u0010\u0012\u001a\u00020\u0011J\u0010\u0010!\u001a\u00020\u00002\b\b\u0001\u0010\u0013\u001a\u00020\u0011J\u0010\u0010\"\u001a\u00020\u00002\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000eX\u0082\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0010\u001a\u00020\u00118\u0002@\u0002X\u0083\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0012\u001a\u00020\u00118\u0002@\u0002X\u0083\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0013\u001a\u00020\u00118\u0002@\u0002X\u0083\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006#"}, d2 = {"Landroidx/window/embedding/SplitPairRule$Builder;", "", "filters", "", "Landroidx/window/embedding/SplitPairFilter;", "(Ljava/util/Set;)V", "clearTop", "", "defaultSplitAttributes", "Landroidx/window/embedding/SplitAttributes;", "finishPrimaryWithSecondary", "Landroidx/window/embedding/SplitRule$FinishBehavior;", "finishSecondaryWithPrimary", "maxAspectRatioInLandscape", "Landroidx/window/embedding/EmbeddingAspectRatio;", "maxAspectRatioInPortrait", "minHeightDp", "", "minSmallestWidthDp", "minWidthDp", "tag", "", "build", "Landroidx/window/embedding/SplitPairRule;", "setClearTop", "setDefaultSplitAttributes", "setFinishPrimaryWithSecondary", "setFinishSecondaryWithPrimary", "setMaxAspectRatioInLandscape", "aspectRatio", "setMaxAspectRatioInPortrait", "setMinHeightDp", "setMinSmallestWidthDp", "setMinWidthDp", "setTag", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Builder {
        private boolean clearTop;
        private SplitAttributes defaultSplitAttributes;
        private final Set<SplitPairFilter> filters;
        private SplitRule.FinishBehavior finishPrimaryWithSecondary;
        private SplitRule.FinishBehavior finishSecondaryWithPrimary;
        private EmbeddingAspectRatio maxAspectRatioInLandscape;
        private EmbeddingAspectRatio maxAspectRatioInPortrait;
        private int minHeightDp;
        private int minSmallestWidthDp;
        private int minWidthDp;
        private String tag;

        public Builder(Set<SplitPairFilter> filters) {
            Intrinsics.checkNotNullParameter(filters, "filters");
            this.filters = filters;
            this.minWidthDp = 600;
            this.minHeightDp = 600;
            this.minSmallestWidthDp = 600;
            this.maxAspectRatioInPortrait = SplitRule.SPLIT_MAX_ASPECT_RATIO_PORTRAIT_DEFAULT;
            this.maxAspectRatioInLandscape = SplitRule.SPLIT_MAX_ASPECT_RATIO_LANDSCAPE_DEFAULT;
            this.finishPrimaryWithSecondary = SplitRule.FinishBehavior.NEVER;
            this.finishSecondaryWithPrimary = SplitRule.FinishBehavior.ALWAYS;
            this.defaultSplitAttributes = new SplitAttributes.Builder().build();
        }

        public final Builder setMinWidthDp(int minWidthDp) {
            this.minWidthDp = minWidthDp;
            return this;
        }

        public final Builder setMinHeightDp(int minHeightDp) {
            this.minHeightDp = minHeightDp;
            return this;
        }

        public final Builder setMinSmallestWidthDp(int minSmallestWidthDp) {
            this.minSmallestWidthDp = minSmallestWidthDp;
            return this;
        }

        public final Builder setMaxAspectRatioInPortrait(EmbeddingAspectRatio aspectRatio) {
            Intrinsics.checkNotNullParameter(aspectRatio, "aspectRatio");
            this.maxAspectRatioInPortrait = aspectRatio;
            return this;
        }

        public final Builder setMaxAspectRatioInLandscape(EmbeddingAspectRatio aspectRatio) {
            Intrinsics.checkNotNullParameter(aspectRatio, "aspectRatio");
            this.maxAspectRatioInLandscape = aspectRatio;
            return this;
        }

        public final Builder setFinishPrimaryWithSecondary(SplitRule.FinishBehavior finishPrimaryWithSecondary) {
            Intrinsics.checkNotNullParameter(finishPrimaryWithSecondary, "finishPrimaryWithSecondary");
            this.finishPrimaryWithSecondary = finishPrimaryWithSecondary;
            return this;
        }

        public final Builder setFinishSecondaryWithPrimary(SplitRule.FinishBehavior finishSecondaryWithPrimary) {
            Intrinsics.checkNotNullParameter(finishSecondaryWithPrimary, "finishSecondaryWithPrimary");
            this.finishSecondaryWithPrimary = finishSecondaryWithPrimary;
            return this;
        }

        public final Builder setClearTop(boolean clearTop) {
            this.clearTop = clearTop;
            return this;
        }

        public final Builder setDefaultSplitAttributes(SplitAttributes defaultSplitAttributes) {
            Intrinsics.checkNotNullParameter(defaultSplitAttributes, "defaultSplitAttributes");
            this.defaultSplitAttributes = defaultSplitAttributes;
            return this;
        }

        public final Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public final SplitPairRule build() {
            return new SplitPairRule(this.filters, this.defaultSplitAttributes, this.tag, this.finishPrimaryWithSecondary, this.finishSecondaryWithPrimary, this.clearTop, this.minWidthDp, this.minHeightDp, this.minSmallestWidthDp, this.maxAspectRatioInPortrait, this.maxAspectRatioInLandscape);
        }
    }

    public final SplitPairRule plus$window_release(SplitPairFilter filter) {
        Intrinsics.checkNotNullParameter(filter, "filter");
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.addAll(this.filters);
        linkedHashSet.add(filter);
        return new Builder(CollectionsKt.toSet(linkedHashSet)).setTag(getTag()).setMinWidthDp(getMinWidthDp()).setMinHeightDp(getMinHeightDp()).setMinSmallestWidthDp(getMinSmallestWidthDp()).setMaxAspectRatioInPortrait(getMaxAspectRatioInPortrait()).setMaxAspectRatioInLandscape(getMaxAspectRatioInLandscape()).setFinishPrimaryWithSecondary(this.finishPrimaryWithSecondary).setFinishSecondaryWithPrimary(this.finishSecondaryWithPrimary).setClearTop(this.clearTop).setDefaultSplitAttributes(getDefaultSplitAttributes()).build();
    }

    @Override // androidx.window.embedding.SplitRule, androidx.window.embedding.EmbeddingRule
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SplitPairRule) || !super.equals(other)) {
            return false;
        }
        SplitPairRule splitPairRule = (SplitPairRule) other;
        return Intrinsics.areEqual(this.filters, splitPairRule.filters) && Intrinsics.areEqual(this.finishPrimaryWithSecondary, splitPairRule.finishPrimaryWithSecondary) && Intrinsics.areEqual(this.finishSecondaryWithPrimary, splitPairRule.finishSecondaryWithPrimary) && this.clearTop == splitPairRule.clearTop;
    }

    @Override // androidx.window.embedding.SplitRule, androidx.window.embedding.EmbeddingRule
    public int hashCode() {
        return (((((((super.hashCode() * 31) + this.filters.hashCode()) * 31) + this.finishPrimaryWithSecondary.hashCode()) * 31) + this.finishSecondaryWithPrimary.hashCode()) * 31) + Boolean.hashCode(this.clearTop);
    }

    @Override // androidx.window.embedding.SplitRule
    public String toString() {
        return "SplitPairRule{tag=" + getTag() + ", defaultSplitAttributes=" + getDefaultSplitAttributes() + ", minWidthDp=" + getMinWidthDp() + ", minHeightDp=" + getMinHeightDp() + ", minSmallestWidthDp=" + getMinSmallestWidthDp() + ", maxAspectRatioInPortrait=" + getMaxAspectRatioInPortrait() + ", maxAspectRatioInLandscape=" + getMaxAspectRatioInLandscape() + ", clearTop=" + this.clearTop + ", finishPrimaryWithSecondary=" + this.finishPrimaryWithSecondary + ", finishSecondaryWithPrimary=" + this.finishSecondaryWithPrimary + ", filters=" + this.filters + '}';
    }
}
