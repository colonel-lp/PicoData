package androidx.window.embedding;

import java.util.Set;
import kotlin.Metadata;
import kotlin.collections.SetsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: ActivityRule.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u00002\u00020\u0001:\u0001\u0017B)\b\u0000\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b¢\u0006\u0002\u0010\tJ\u0013\u0010\u000e\u001a\u00020\b2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0096\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0016J\u0016\u0010\u0013\u001a\u00020\u00002\u0006\u0010\u0014\u001a\u00020\u0006H\u0080\u0002¢\u0006\u0002\b\u0015J\b\u0010\u0016\u001a\u00020\u0003H\u0016R\u0011\u0010\u0007\u001a\u00020\b¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Landroidx/window/embedding/ActivityRule;", "Landroidx/window/embedding/EmbeddingRule;", "tag", "", "filters", "", "Landroidx/window/embedding/ActivityFilter;", "alwaysExpand", "", "(Ljava/lang/String;Ljava/util/Set;Z)V", "getAlwaysExpand", "()Z", "getFilters", "()Ljava/util/Set;", "equals", "other", "", "hashCode", "", "plus", "filter", "plus$window_release", "toString", "Builder", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class ActivityRule extends EmbeddingRule {
    private final boolean alwaysExpand;
    private final Set<ActivityFilter> filters;

    public /* synthetic */ ActivityRule(String str, Set set, boolean z, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(str, set, (i & 4) != 0 ? false : z);
    }

    public final Set<ActivityFilter> getFilters() {
        return this.filters;
    }

    public final boolean getAlwaysExpand() {
        return this.alwaysExpand;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ActivityRule(String str, Set<ActivityFilter> filters, boolean z) {
        super(str);
        Intrinsics.checkNotNullParameter(filters, "filters");
        this.filters = filters;
        this.alwaysExpand = z;
    }

    /* JADX INFO: compiled from: ActivityRule.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0013\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\u0002\u0010\u0005J\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u0010\u0010\r\u001a\u00020\u00002\b\u0010\b\u001a\u0004\u0018\u00010\tR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Landroidx/window/embedding/ActivityRule$Builder;", "", "filters", "", "Landroidx/window/embedding/ActivityFilter;", "(Ljava/util/Set;)V", "alwaysExpand", "", "tag", "", "build", "Landroidx/window/embedding/ActivityRule;", "setAlwaysExpand", "setTag", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Builder {
        private boolean alwaysExpand;
        private final Set<ActivityFilter> filters;
        private String tag;

        public Builder(Set<ActivityFilter> filters) {
            Intrinsics.checkNotNullParameter(filters, "filters");
            this.filters = filters;
        }

        public final Builder setAlwaysExpand(boolean alwaysExpand) {
            this.alwaysExpand = alwaysExpand;
            return this;
        }

        public final Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public final ActivityRule build() {
            return new ActivityRule(this.tag, this.filters, this.alwaysExpand);
        }
    }

    public final ActivityRule plus$window_release(ActivityFilter filter) {
        Intrinsics.checkNotNullParameter(filter, "filter");
        return new ActivityRule(getTag(), SetsKt.plus(this.filters, filter), this.alwaysExpand);
    }

    @Override // androidx.window.embedding.EmbeddingRule
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ActivityRule) || !super.equals(other)) {
            return false;
        }
        ActivityRule activityRule = (ActivityRule) other;
        return Intrinsics.areEqual(this.filters, activityRule.filters) && this.alwaysExpand == activityRule.alwaysExpand;
    }

    @Override // androidx.window.embedding.EmbeddingRule
    public int hashCode() {
        return (((super.hashCode() * 31) + this.filters.hashCode()) * 31) + Boolean.hashCode(this.alwaysExpand);
    }

    public String toString() {
        return "ActivityRule:{tag={" + getTag() + "},filters={" + this.filters + "}, alwaysExpand={" + this.alwaysExpand + "}}";
    }
}
