package androidx.window.embedding;

import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: EmbeddingAspectRatio.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u0017\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\b\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u0010\u001a\u00020\u0003H\u0016R\u0014\u0010\u0002\u001a\u00020\u0003X\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0014\u0010\u0004\u001a\u00020\u0005X\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n¨\u0006\u0012"}, d2 = {"Landroidx/window/embedding/EmbeddingAspectRatio;", "", "description", "", "value", "", "(Ljava/lang/String;F)V", "getDescription$window_release", "()Ljava/lang/String;", "getValue$window_release", "()F", "equals", "", "other", "hashCode", "", "toString", "Companion", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class EmbeddingAspectRatio {
    private final String description;
    private final float value;

    /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    public static final EmbeddingAspectRatio ALWAYS_ALLOW = new EmbeddingAspectRatio("ALWAYS_ALLOW", 0.0f);
    public static final EmbeddingAspectRatio ALWAYS_DISALLOW = new EmbeddingAspectRatio("ALWAYS_DISALLOW", -1.0f);

    public /* synthetic */ EmbeddingAspectRatio(String str, float f, DefaultConstructorMarker defaultConstructorMarker) {
        this(str, f);
    }

    @JvmStatic
    public static final EmbeddingAspectRatio ratio(float f) {
        return INSTANCE.ratio(f);
    }

    private EmbeddingAspectRatio(String str, float f) {
        this.description = str;
        this.value = f;
    }

    /* JADX INFO: renamed from: getDescription$window_release, reason: from getter */
    public final String getDescription() {
        return this.description;
    }

    /* JADX INFO: renamed from: getValue$window_release, reason: from getter */
    public final float getValue() {
        return this.value;
    }

    public String toString() {
        return "EmbeddingAspectRatio(" + this.description + ')';
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EmbeddingAspectRatio)) {
            return false;
        }
        EmbeddingAspectRatio embeddingAspectRatio = (EmbeddingAspectRatio) other;
        return this.value == embeddingAspectRatio.value && Intrinsics.areEqual(this.description, embeddingAspectRatio.description);
    }

    public int hashCode() {
        return this.description.hashCode() + (Float.hashCode(this.value) * 31);
    }

    /* JADX INFO: compiled from: EmbeddingAspectRatio.kt */
    @Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0015\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0000¢\u0006\u0002\b\tJ\u0012\u0010\n\u001a\u00020\u00042\b\b\u0001\u0010\n\u001a\u00020\bH\u0007R\u0010\u0010\u0003\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000¨\u0006\u000b"}, d2 = {"Landroidx/window/embedding/EmbeddingAspectRatio$Companion;", "", "()V", "ALWAYS_ALLOW", "Landroidx/window/embedding/EmbeddingAspectRatio;", "ALWAYS_DISALLOW", "buildAspectRatioFromValue", "value", "", "buildAspectRatioFromValue$window_release", "ratio", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @JvmStatic
        public final EmbeddingAspectRatio ratio(float ratio) {
            if (ratio <= 1.0f) {
                throw new IllegalArgumentException("Ratio must be greater than 1.".toString());
            }
            return new EmbeddingAspectRatio("ratio:" + ratio, ratio, null);
        }

        public final EmbeddingAspectRatio buildAspectRatioFromValue$window_release(float value) {
            if (value == EmbeddingAspectRatio.ALWAYS_ALLOW.getValue()) {
                return EmbeddingAspectRatio.ALWAYS_ALLOW;
            }
            if (value == EmbeddingAspectRatio.ALWAYS_DISALLOW.getValue()) {
                return EmbeddingAspectRatio.ALWAYS_DISALLOW;
            }
            return ratio(value);
        }
    }
}
