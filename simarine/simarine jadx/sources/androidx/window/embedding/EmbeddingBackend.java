package androidx.window.embedding;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.os.IBinder;
import androidx.core.util.Consumer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: EmbeddingBackend.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u0000 -2\u00020\u0001:\u0001-J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&J,\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0010H&J\b\u0010\u0013\u001a\u00020\u0007H'J\u0012\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u000b\u001a\u00020\fH&J\u000e\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\t0\u0017H&J\b\u0010\u0018\u001a\u00020\u0007H'J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u000b\u001a\u00020\fH&J\u0010\u0010\u001b\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&J\u001c\u0010\u001c\u001a\u00020\u00072\u0012\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0010H&J\u0018\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\"H'J\u0016\u0010#\u001a\u00020\u00072\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\t0\u0017H&J\u001c\u0010%\u001a\u00020\u00072\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020)0'H'J\u0018\u0010*\u001a\u00020\u00072\u0006\u0010+\u001a\u00020\u00122\u0006\u0010,\u001a\u00020)H'R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005ø\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001¨\u0006.À\u0006\u0001"}, d2 = {"Landroidx/window/embedding/EmbeddingBackend;", "", "splitSupportStatus", "Landroidx/window/embedding/SplitController$SplitSupportStatus;", "getSplitSupportStatus", "()Landroidx/window/embedding/SplitController$SplitSupportStatus;", "addRule", "", "rule", "Landroidx/window/embedding/EmbeddingRule;", "addSplitListenerForActivity", "activity", "Landroid/app/Activity;", "executor", "Ljava/util/concurrent/Executor;", "callback", "Landroidx/core/util/Consumer;", "", "Landroidx/window/embedding/SplitInfo;", "clearSplitAttributesCalculator", "getActivityStack", "Landroidx/window/embedding/ActivityStack;", "getRules", "", "invalidateTopVisibleSplitAttributes", "isActivityEmbedded", "", "removeRule", "removeSplitListenerForActivity", "consumer", "setLaunchingActivityStack", "Landroid/app/ActivityOptions;", "options", "token", "Landroid/os/IBinder;", "setRules", "rules", "setSplitAttributesCalculator", "calculator", "Lkotlin/Function1;", "Landroidx/window/embedding/SplitAttributesCalculatorParams;", "Landroidx/window/embedding/SplitAttributes;", "updateSplitAttributes", "splitInfo", "splitAttributes", "Companion", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public interface EmbeddingBackend {

    /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = Companion.$$INSTANCE;

    @JvmStatic
    static EmbeddingBackend getInstance(Context context) {
        return INSTANCE.getInstance(context);
    }

    @JvmStatic
    static void overrideDecorator(EmbeddingBackendDecorator embeddingBackendDecorator) {
        INSTANCE.overrideDecorator(embeddingBackendDecorator);
    }

    @JvmStatic
    static void reset() {
        INSTANCE.reset();
    }

    void addRule(EmbeddingRule rule);

    void addSplitListenerForActivity(Activity activity, Executor executor, Consumer<List<SplitInfo>> callback);

    void clearSplitAttributesCalculator();

    ActivityStack getActivityStack(Activity activity);

    Set<EmbeddingRule> getRules();

    SplitController.SplitSupportStatus getSplitSupportStatus();

    void invalidateTopVisibleSplitAttributes();

    boolean isActivityEmbedded(Activity activity);

    void removeRule(EmbeddingRule rule);

    void removeSplitListenerForActivity(Consumer<List<SplitInfo>> consumer);

    ActivityOptions setLaunchingActivityStack(ActivityOptions options, IBinder token);

    void setRules(Set<? extends EmbeddingRule> rules);

    void setSplitAttributesCalculator(Function1<? super SplitAttributesCalculatorParams, SplitAttributes> calculator);

    void updateSplitAttributes(SplitInfo splitInfo, SplitAttributes splitAttributes);

    /* JADX INFO: compiled from: EmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\b\u0010\r\u001a\u00020\nH\u0007R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Landroidx/window/embedding/EmbeddingBackend$Companion;", "", "()V", "decorator", "Lkotlin/Function1;", "Landroidx/window/embedding/EmbeddingBackend;", "getInstance", "context", "Landroid/content/Context;", "overrideDecorator", "", "overridingDecorator", "Landroidx/window/embedding/EmbeddingBackendDecorator;", "reset", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();
        private static Function1<? super EmbeddingBackend, ? extends EmbeddingBackend> decorator = new Function1<EmbeddingBackend, EmbeddingBackend>() { // from class: androidx.window.embedding.EmbeddingBackend$Companion$decorator$1
            @Override // kotlin.jvm.functions.Function1
            public final EmbeddingBackend invoke(EmbeddingBackend it) {
                Intrinsics.checkNotNullParameter(it, "it");
                return it;
            }
        };

        private Companion() {
        }

        @JvmStatic
        public final EmbeddingBackend getInstance(Context context) {
            Intrinsics.checkNotNullParameter(context, "context");
            return decorator.invoke(ExtensionEmbeddingBackend.INSTANCE.getInstance(context));
        }

        @JvmStatic
        public final void overrideDecorator(EmbeddingBackendDecorator overridingDecorator) {
            Intrinsics.checkNotNullParameter(overridingDecorator, "overridingDecorator");
            decorator = new EmbeddingBackend$Companion$overrideDecorator$1(overridingDecorator);
        }

        @JvmStatic
        public final void reset() {
            decorator = new Function1<EmbeddingBackend, EmbeddingBackend>() { // from class: androidx.window.embedding.EmbeddingBackend$Companion$reset$1
                @Override // kotlin.jvm.functions.Function1
                public final EmbeddingBackend invoke(EmbeddingBackend it) {
                    Intrinsics.checkNotNullParameter(it, "it");
                    return it;
                }
            };
        }
    }
}
