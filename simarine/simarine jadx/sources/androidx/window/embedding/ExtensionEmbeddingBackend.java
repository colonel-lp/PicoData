package androidx.window.embedding;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.collection.ArraySet;
import androidx.core.util.Consumer;
import androidx.window.WindowProperties;
import androidx.window.core.BuildConfig;
import androidx.window.core.ConsumerAdapter;
import androidx.window.core.ExtensionsUtil;
import androidx.window.core.PredicateAdapter;
import androidx.window.core.VerificationMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000\u0096\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\b\u0000\u0018\u0000 E2\u00020\u0001:\u0005DEFGHB\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005¢\u0006\u0002\u0010\u0006J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0017J,\u0010 \u001a\u00020\u001d2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\u0012\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0'0&H\u0016J\b\u0010)\u001a\u00020*H\u0002J\b\u0010+\u001a\u00020\u001dH\u0017J\u0012\u0010,\u001a\u0004\u0018\u00010-2\u0006\u0010!\u001a\u00020\"H\u0016J\u000e\u0010.\u001a\b\u0012\u0004\u0012\u00020\u001f0/H\u0017J\b\u00100\u001a\u00020\u001dH\u0017J\u0010\u00101\u001a\u00020*2\u0006\u0010!\u001a\u00020\"H\u0016J\u0010\u00102\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0017J\u001c\u00103\u001a\u00020\u001d2\u0012\u00104\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0'0&H\u0016J\u0018\u00105\u001a\u0002062\u0006\u00107\u001a\u0002062\u0006\u00108\u001a\u000209H\u0017J\u0016\u0010:\u001a\u00020\u001d2\f\u0010;\u001a\b\u0012\u0004\u0012\u00020\u001f0/H\u0017J\u001c\u0010<\u001a\u00020\u001d2\u0012\u0010=\u001a\u000e\u0012\u0004\u0012\u00020?\u0012\u0004\u0012\u00020@0>H\u0017J\u0018\u0010A\u001a\u00020\u001d2\u0006\u0010B\u001a\u00020(2\u0006\u0010C\u001a\u00020@H\u0017R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R \u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006@\u0006X\u0087\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0010\u0010\u000b\u001a\u00020\f8\u0002X\u0083\u0004¢\u0006\u0002\n\u0000R\"\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e8\u0006X\u0087\u0004¢\u0006\u000e\n\u0000\u0012\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u0012\u0010\u0013R\u0012\u0010\u0014\u001a\u00060\u0015R\u00020\u0000X\u0082\u0004¢\u0006\u0002\n\u0000R\u001b\u0010\u0016\u001a\u00020\u00178VX\u0096\u0084\u0002¢\u0006\f\n\u0004\b\u001a\u0010\u001b\u001a\u0004\b\u0018\u0010\u0019¨\u0006I"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend;", "Landroidx/window/embedding/EmbeddingBackend;", "applicationContext", "Landroid/content/Context;", "embeddingExtension", "Landroidx/window/embedding/EmbeddingInterfaceCompat;", "(Landroid/content/Context;Landroidx/window/embedding/EmbeddingInterfaceCompat;)V", "getEmbeddingExtension", "()Landroidx/window/embedding/EmbeddingInterfaceCompat;", "setEmbeddingExtension", "(Landroidx/window/embedding/EmbeddingInterfaceCompat;)V", "ruleTracker", "Landroidx/window/embedding/ExtensionEmbeddingBackend$RuleTracker;", "splitChangeCallbacks", "Ljava/util/concurrent/CopyOnWriteArrayList;", "Landroidx/window/embedding/ExtensionEmbeddingBackend$SplitListenerWrapper;", "getSplitChangeCallbacks$annotations", "()V", "getSplitChangeCallbacks", "()Ljava/util/concurrent/CopyOnWriteArrayList;", "splitInfoEmbeddingCallback", "Landroidx/window/embedding/ExtensionEmbeddingBackend$EmbeddingCallbackImpl;", "splitSupportStatus", "Landroidx/window/embedding/SplitController$SplitSupportStatus;", "getSplitSupportStatus", "()Landroidx/window/embedding/SplitController$SplitSupportStatus;", "splitSupportStatus$delegate", "Lkotlin/Lazy;", "addRule", "", "rule", "Landroidx/window/embedding/EmbeddingRule;", "addSplitListenerForActivity", "activity", "Landroid/app/Activity;", "executor", "Ljava/util/concurrent/Executor;", "callback", "Landroidx/core/util/Consumer;", "", "Landroidx/window/embedding/SplitInfo;", "areExtensionsAvailable", "", "clearSplitAttributesCalculator", "getActivityStack", "Landroidx/window/embedding/ActivityStack;", "getRules", "", "invalidateTopVisibleSplitAttributes", "isActivityEmbedded", "removeRule", "removeSplitListenerForActivity", "consumer", "setLaunchingActivityStack", "Landroid/app/ActivityOptions;", "options", "token", "Landroid/os/IBinder;", "setRules", "rules", "setSplitAttributesCalculator", "calculator", "Lkotlin/Function1;", "Landroidx/window/embedding/SplitAttributesCalculatorParams;", "Landroidx/window/embedding/SplitAttributes;", "updateSplitAttributes", "splitInfo", "splitAttributes", "Api31Impl", "Companion", "EmbeddingCallbackImpl", "RuleTracker", "SplitListenerWrapper", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class ExtensionEmbeddingBackend implements EmbeddingBackend {
    private static final String TAG = "EmbeddingBackend";
    private static volatile ExtensionEmbeddingBackend globalInstance;
    private final Context applicationContext;
    private EmbeddingInterfaceCompat embeddingExtension;
    private final RuleTracker ruleTracker;
    private final CopyOnWriteArrayList<SplitListenerWrapper> splitChangeCallbacks;
    private final EmbeddingCallbackImpl splitInfoEmbeddingCallback;

    /* JADX INFO: renamed from: splitSupportStatus$delegate, reason: from kotlin metadata */
    private final Lazy splitSupportStatus;

    /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final ReentrantLock globalLock = new ReentrantLock();

    public static /* synthetic */ void getSplitChangeCallbacks$annotations() {
    }

    public ExtensionEmbeddingBackend(Context applicationContext, EmbeddingInterfaceCompat embeddingInterfaceCompat) {
        Intrinsics.checkNotNullParameter(applicationContext, "applicationContext");
        this.applicationContext = applicationContext;
        this.embeddingExtension = embeddingInterfaceCompat;
        EmbeddingCallbackImpl embeddingCallbackImpl = new EmbeddingCallbackImpl();
        this.splitInfoEmbeddingCallback = embeddingCallbackImpl;
        this.splitChangeCallbacks = new CopyOnWriteArrayList<>();
        EmbeddingInterfaceCompat embeddingInterfaceCompat2 = this.embeddingExtension;
        if (embeddingInterfaceCompat2 != null) {
            embeddingInterfaceCompat2.setEmbeddingCallback(embeddingCallbackImpl);
        }
        this.ruleTracker = new RuleTracker();
        this.splitSupportStatus = LazyKt.lazy(new Function0<SplitController.SplitSupportStatus>() { // from class: androidx.window.embedding.ExtensionEmbeddingBackend$splitSupportStatus$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final SplitController.SplitSupportStatus invoke() {
                if (!this.this$0.areExtensionsAvailable()) {
                    return SplitController.SplitSupportStatus.SPLIT_UNAVAILABLE;
                }
                if (Build.VERSION.SDK_INT >= 31) {
                    return ExtensionEmbeddingBackend.Api31Impl.INSTANCE.isSplitPropertyEnabled(this.this$0.applicationContext);
                }
                return SplitController.SplitSupportStatus.SPLIT_AVAILABLE;
            }
        });
    }

    public final EmbeddingInterfaceCompat getEmbeddingExtension() {
        return this.embeddingExtension;
    }

    public final void setEmbeddingExtension(EmbeddingInterfaceCompat embeddingInterfaceCompat) {
        this.embeddingExtension = embeddingInterfaceCompat;
    }

    public final CopyOnWriteArrayList<SplitListenerWrapper> getSplitChangeCallbacks() {
        return this.splitChangeCallbacks;
    }

    /* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0012\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J\u0017\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0007¢\u0006\u0002\u0010\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0015"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend$Companion;", "", "()V", "TAG", "", "globalInstance", "Landroidx/window/embedding/ExtensionEmbeddingBackend;", "globalLock", "Ljava/util/concurrent/locks/ReentrantLock;", "getInstance", "Landroidx/window/embedding/EmbeddingBackend;", "context", "Landroid/content/Context;", "initAndVerifyEmbeddingExtension", "Landroidx/window/embedding/EmbeddingInterfaceCompat;", "applicationContext", "isExtensionVersionSupported", "", "extensionVersion", "", "(Ljava/lang/Integer;)Z", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final EmbeddingBackend getInstance(Context context) {
            Intrinsics.checkNotNullParameter(context, "context");
            if (ExtensionEmbeddingBackend.globalInstance == null) {
                ReentrantLock reentrantLock = ExtensionEmbeddingBackend.globalLock;
                reentrantLock.lock();
                try {
                    if (ExtensionEmbeddingBackend.globalInstance == null) {
                        Context applicationContext = context.getApplicationContext();
                        Companion companion = ExtensionEmbeddingBackend.INSTANCE;
                        Intrinsics.checkNotNullExpressionValue(applicationContext, "applicationContext");
                        EmbeddingInterfaceCompat embeddingInterfaceCompatInitAndVerifyEmbeddingExtension = companion.initAndVerifyEmbeddingExtension(applicationContext);
                        Companion companion2 = ExtensionEmbeddingBackend.INSTANCE;
                        ExtensionEmbeddingBackend.globalInstance = new ExtensionEmbeddingBackend(applicationContext, embeddingInterfaceCompatInitAndVerifyEmbeddingExtension);
                    }
                    Unit unit = Unit.INSTANCE;
                    reentrantLock.unlock();
                } catch (Throwable th) {
                    reentrantLock.unlock();
                    throw th;
                }
            }
            ExtensionEmbeddingBackend extensionEmbeddingBackend = ExtensionEmbeddingBackend.globalInstance;
            Intrinsics.checkNotNull(extensionEmbeddingBackend);
            return extensionEmbeddingBackend;
        }

        private final EmbeddingInterfaceCompat initAndVerifyEmbeddingExtension(Context applicationContext) {
            EmbeddingCompat embeddingCompat = null;
            try {
                if (isExtensionVersionSupported(Integer.valueOf(ExtensionsUtil.INSTANCE.getSafeVendorApiLevel())) && EmbeddingCompat.INSTANCE.isEmbeddingAvailable()) {
                    ClassLoader classLoader = EmbeddingBackend.class.getClassLoader();
                    embeddingCompat = classLoader != null ? new EmbeddingCompat(EmbeddingCompat.INSTANCE.embeddingComponent(), new EmbeddingAdapter(new PredicateAdapter(classLoader)), new ConsumerAdapter(classLoader), applicationContext) : null;
                }
            } catch (Throwable th) {
                Log.d(ExtensionEmbeddingBackend.TAG, "Failed to load embedding extension: " + th);
            }
            if (embeddingCompat == null) {
                Log.d(ExtensionEmbeddingBackend.TAG, "No supported embedding extension found");
            }
            return embeddingCompat;
        }

        public final boolean isExtensionVersionSupported(Integer extensionVersion) {
            return extensionVersion != null && extensionVersion.intValue() >= 1;
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public Set<EmbeddingRule> getRules() {
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            return CollectionsKt.toSet(this.ruleTracker.getSplitRules());
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void setRules(Set<? extends EmbeddingRule> rules) {
        Intrinsics.checkNotNullParameter(rules, "rules");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            this.ruleTracker.setRules(rules);
            EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
            if (embeddingInterfaceCompat != null) {
                embeddingInterfaceCompat.setRules(getRules());
                Unit unit = Unit.INSTANCE;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void addRule(EmbeddingRule rule) {
        Intrinsics.checkNotNullParameter(rule, "rule");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            if (!this.ruleTracker.contains(rule)) {
                RuleTracker.addOrUpdateRule$default(this.ruleTracker, rule, false, 2, null);
                EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
                if (embeddingInterfaceCompat != null) {
                    embeddingInterfaceCompat.setRules(getRules());
                }
            }
            Unit unit = Unit.INSTANCE;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void removeRule(EmbeddingRule rule) {
        Intrinsics.checkNotNullParameter(rule, "rule");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            if (this.ruleTracker.contains(rule)) {
                this.ruleTracker.removeRule(rule);
                EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
                if (embeddingInterfaceCompat != null) {
                    embeddingInterfaceCompat.setRules(getRules());
                }
            }
            Unit unit = Unit.INSTANCE;
        } finally {
            reentrantLock.unlock();
        }
    }

    /* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\"\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00052\b\b\u0002\u0010\u000f\u001a\u00020\u0010J\u0006\u0010\u0011\u001a\u00020\rJ\u0011\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u000e\u001a\u00020\u0005H\u0086\u0002J\u000e\u0010\u0013\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0005J\u0014\u0010\u0014\u001a\u00020\r2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\u0016R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R*\u0010\b\u001a\u001e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00050\tj\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0005`\u000bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0017"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend$RuleTracker;", "", "()V", "splitRules", "Landroidx/collection/ArraySet;", "Landroidx/window/embedding/EmbeddingRule;", "getSplitRules", "()Landroidx/collection/ArraySet;", "tagRuleMap", "Ljava/util/HashMap;", "", "Lkotlin/collections/HashMap;", "addOrUpdateRule", "", "rule", "throwOnDuplicateTag", "", "clearRules", "contains", "removeRule", "setRules", "rules", "", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    private static final class RuleTracker {
        private final ArraySet<EmbeddingRule> splitRules = new ArraySet<>();
        private final HashMap<String, EmbeddingRule> tagRuleMap = new HashMap<>();

        public final ArraySet<EmbeddingRule> getSplitRules() {
            return this.splitRules;
        }

        public final void setRules(Set<? extends EmbeddingRule> rules) {
            Intrinsics.checkNotNullParameter(rules, "rules");
            clearRules();
            Iterator<T> it = rules.iterator();
            while (it.hasNext()) {
                addOrUpdateRule((EmbeddingRule) it.next(), true);
            }
        }

        public final void clearRules() {
            this.splitRules.clear();
            this.tagRuleMap.clear();
        }

        public static /* synthetic */ void addOrUpdateRule$default(RuleTracker ruleTracker, EmbeddingRule embeddingRule, boolean z, int i, Object obj) {
            if ((i & 2) != 0) {
                z = false;
            }
            ruleTracker.addOrUpdateRule(embeddingRule, z);
        }

        public final void addOrUpdateRule(EmbeddingRule rule, boolean throwOnDuplicateTag) {
            Intrinsics.checkNotNullParameter(rule, "rule");
            if (this.splitRules.contains(rule)) {
                return;
            }
            String tag = rule.getTag();
            if (tag == null) {
                this.splitRules.add(rule);
                return;
            }
            if (!this.tagRuleMap.containsKey(tag)) {
                this.tagRuleMap.put(tag, rule);
                this.splitRules.add(rule);
            } else {
                if (throwOnDuplicateTag) {
                    throw new IllegalArgumentException("Duplicated tag: " + tag + ". Tag must be unique among all registered rules");
                }
                this.splitRules.remove(this.tagRuleMap.get(tag));
                this.tagRuleMap.put(tag, rule);
                this.splitRules.add(rule);
            }
        }

        public final void removeRule(EmbeddingRule rule) {
            Intrinsics.checkNotNullParameter(rule, "rule");
            if (this.splitRules.contains(rule)) {
                this.splitRules.remove(rule);
                if (rule.getTag() != null) {
                    this.tagRuleMap.remove(rule.getTag());
                }
            }
        }

        public final boolean contains(EmbeddingRule rule) {
            Intrinsics.checkNotNullParameter(rule, "rule");
            return this.splitRules.contains(rule);
        }
    }

    /* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0000\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0012\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007¢\u0006\u0002\u0010\nJ\u0014\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\t0\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u001d\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u0016\u0010\r\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\bX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0011"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend$SplitListenerWrapper;", "", "activity", "Landroid/app/Activity;", "executor", "Ljava/util/concurrent/Executor;", "callback", "Landroidx/core/util/Consumer;", "", "Landroidx/window/embedding/SplitInfo;", "(Landroid/app/Activity;Ljava/util/concurrent/Executor;Landroidx/core/util/Consumer;)V", "getCallback", "()Landroidx/core/util/Consumer;", "lastValue", "accept", "", "splitInfoList", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class SplitListenerWrapper {
        private final Activity activity;
        private final Consumer<List<SplitInfo>> callback;
        private final Executor executor;
        private List<SplitInfo> lastValue;

        public SplitListenerWrapper(Activity activity, Executor executor, Consumer<List<SplitInfo>> callback) {
            Intrinsics.checkNotNullParameter(activity, "activity");
            Intrinsics.checkNotNullParameter(executor, "executor");
            Intrinsics.checkNotNullParameter(callback, "callback");
            this.activity = activity;
            this.executor = executor;
            this.callback = callback;
        }

        public final Consumer<List<SplitInfo>> getCallback() {
            return this.callback;
        }

        public final void accept(List<SplitInfo> splitInfoList) {
            Intrinsics.checkNotNullParameter(splitInfoList, "splitInfoList");
            ArrayList arrayList = new ArrayList();
            for (Object obj : splitInfoList) {
                if (((SplitInfo) obj).contains(this.activity)) {
                    arrayList.add(obj);
                }
            }
            final ArrayList arrayList2 = arrayList;
            if (Intrinsics.areEqual(arrayList2, this.lastValue)) {
                return;
            }
            this.lastValue = arrayList2;
            this.executor.execute(new Runnable() { // from class: androidx.window.embedding.ExtensionEmbeddingBackend$SplitListenerWrapper$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    ExtensionEmbeddingBackend.SplitListenerWrapper.accept$lambda$1(this.f$0, arrayList2);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final void accept$lambda$1(SplitListenerWrapper this$0, List splitsWithActivity) {
            Intrinsics.checkNotNullParameter(this$0, "this$0");
            Intrinsics.checkNotNullParameter(splitsWithActivity, "$splitsWithActivity");
            this$0.callback.accept(splitsWithActivity);
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void addSplitListenerForActivity(Activity activity, Executor executor, Consumer<List<SplitInfo>> callback) {
        Intrinsics.checkNotNullParameter(activity, "activity");
        Intrinsics.checkNotNullParameter(executor, "executor");
        Intrinsics.checkNotNullParameter(callback, "callback");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            if (this.embeddingExtension == null) {
                Log.v(TAG, "Extension not loaded, skipping callback registration.");
                callback.accept(CollectionsKt.emptyList());
                return;
            }
            SplitListenerWrapper splitListenerWrapper = new SplitListenerWrapper(activity, executor, callback);
            this.splitChangeCallbacks.add(splitListenerWrapper);
            if (this.splitInfoEmbeddingCallback.getLastInfo() != null) {
                List<SplitInfo> lastInfo = this.splitInfoEmbeddingCallback.getLastInfo();
                Intrinsics.checkNotNull(lastInfo);
                splitListenerWrapper.accept(lastInfo);
            } else {
                splitListenerWrapper.accept(CollectionsKt.emptyList());
            }
            Unit unit = Unit.INSTANCE;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void removeSplitListenerForActivity(Consumer<List<SplitInfo>> consumer) {
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            for (SplitListenerWrapper splitListenerWrapper : this.splitChangeCallbacks) {
                if (Intrinsics.areEqual(splitListenerWrapper.getCallback(), consumer)) {
                    this.splitChangeCallbacks.remove(splitListenerWrapper);
                    break;
                }
            }
            Unit unit = Unit.INSTANCE;
        } finally {
            reentrantLock.unlock();
        }
    }

    /* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0080\u0004\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0016\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0016R\"\u0010\u0003\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\r"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend$EmbeddingCallbackImpl;", "Landroidx/window/embedding/EmbeddingInterfaceCompat$EmbeddingCallbackInterface;", "(Landroidx/window/embedding/ExtensionEmbeddingBackend;)V", "lastInfo", "", "Landroidx/window/embedding/SplitInfo;", "getLastInfo", "()Ljava/util/List;", "setLastInfo", "(Ljava/util/List;)V", "onSplitInfoChanged", "", "splitInfo", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public final class EmbeddingCallbackImpl implements EmbeddingInterfaceCompat.EmbeddingCallbackInterface {
        private List<SplitInfo> lastInfo;

        public EmbeddingCallbackImpl() {
        }

        public final List<SplitInfo> getLastInfo() {
            return this.lastInfo;
        }

        public final void setLastInfo(List<SplitInfo> list) {
            this.lastInfo = list;
        }

        @Override // androidx.window.embedding.EmbeddingInterfaceCompat.EmbeddingCallbackInterface
        public void onSplitInfoChanged(List<SplitInfo> splitInfo) {
            Intrinsics.checkNotNullParameter(splitInfo, "splitInfo");
            this.lastInfo = splitInfo;
            Iterator<SplitListenerWrapper> it = ExtensionEmbeddingBackend.this.getSplitChangeCallbacks().iterator();
            while (it.hasNext()) {
                it.next().accept(splitInfo);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean areExtensionsAvailable() {
        return this.embeddingExtension != null;
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public SplitController.SplitSupportStatus getSplitSupportStatus() {
        return (SplitController.SplitSupportStatus) this.splitSupportStatus.getValue();
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public boolean isActivityEmbedded(Activity activity) {
        Intrinsics.checkNotNullParameter(activity, "activity");
        EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
        if (embeddingInterfaceCompat != null) {
            return embeddingInterfaceCompat.isActivityEmbedded(activity);
        }
        return false;
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void setSplitAttributesCalculator(Function1<? super SplitAttributesCalculatorParams, SplitAttributes> calculator) {
        Intrinsics.checkNotNullParameter(calculator, "calculator");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
            if (embeddingInterfaceCompat != null) {
                embeddingInterfaceCompat.setSplitAttributesCalculator(calculator);
                Unit unit = Unit.INSTANCE;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void clearSplitAttributesCalculator() {
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
            if (embeddingInterfaceCompat != null) {
                embeddingInterfaceCompat.clearSplitAttributesCalculator();
                Unit unit = Unit.INSTANCE;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public ActivityStack getActivityStack(Activity activity) {
        Intrinsics.checkNotNullParameter(activity, "activity");
        ReentrantLock reentrantLock = globalLock;
        reentrantLock.lock();
        try {
            List<SplitInfo> lastInfo = this.splitInfoEmbeddingCallback.getLastInfo();
            if (lastInfo == null) {
                reentrantLock.unlock();
                return null;
            }
            for (SplitInfo splitInfo : lastInfo) {
                if (splitInfo.contains(activity)) {
                    if (splitInfo.getPrimaryActivityStack().contains(activity)) {
                        ActivityStack primaryActivityStack = splitInfo.getPrimaryActivityStack();
                        reentrantLock.unlock();
                        return primaryActivityStack;
                    }
                    if (splitInfo.getSecondaryActivityStack().contains(activity)) {
                        ActivityStack secondaryActivityStack = splitInfo.getSecondaryActivityStack();
                        reentrantLock.unlock();
                        return secondaryActivityStack;
                    }
                }
            }
            reentrantLock.unlock();
            return null;
        } catch (Throwable th) {
            reentrantLock.unlock();
            throw th;
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public ActivityOptions setLaunchingActivityStack(ActivityOptions options, IBinder token) {
        ActivityOptions launchingActivityStack;
        Intrinsics.checkNotNullParameter(options, "options");
        Intrinsics.checkNotNullParameter(token, "token");
        EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
        return (embeddingInterfaceCompat == null || (launchingActivityStack = embeddingInterfaceCompat.setLaunchingActivityStack(options, token)) == null) ? options : launchingActivityStack;
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void invalidateTopVisibleSplitAttributes() {
        EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
        if (embeddingInterfaceCompat != null) {
            embeddingInterfaceCompat.invalidateTopVisibleSplitAttributes();
        }
    }

    @Override // androidx.window.embedding.EmbeddingBackend
    public void updateSplitAttributes(SplitInfo splitInfo, SplitAttributes splitAttributes) {
        Intrinsics.checkNotNullParameter(splitInfo, "splitInfo");
        Intrinsics.checkNotNullParameter(splitAttributes, "splitAttributes");
        EmbeddingInterfaceCompat embeddingInterfaceCompat = this.embeddingExtension;
        if (embeddingInterfaceCompat != null) {
            embeddingInterfaceCompat.updateSplitAttributes(splitInfo, splitAttributes);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: ExtensionEmbeddingBackend.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bÃ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007¨\u0006\u0007"}, d2 = {"Landroidx/window/embedding/ExtensionEmbeddingBackend$Api31Impl;", "", "()V", "isSplitPropertyEnabled", "Landroidx/window/embedding/SplitController$SplitSupportStatus;", "context", "Landroid/content/Context;", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    static final class Api31Impl {
        public static final Api31Impl INSTANCE = new Api31Impl();

        private Api31Impl() {
        }

        public final SplitController.SplitSupportStatus isSplitPropertyEnabled(Context context) {
            Intrinsics.checkNotNullParameter(context, "context");
            try {
                PackageManager.Property property = context.getPackageManager().getProperty(WindowProperties.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED, context.getPackageName());
                Intrinsics.checkNotNullExpressionValue(property, "try {\n                co…OT_DECLARED\n            }");
                if (!property.isBoolean()) {
                    if (BuildConfig.INSTANCE.getVerificationMode() == VerificationMode.LOG) {
                        Log.w(ExtensionEmbeddingBackend.TAG, "android.window.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED must have a boolean value");
                    }
                    return SplitController.SplitSupportStatus.SPLIT_ERROR_PROPERTY_NOT_DECLARED;
                }
                if (property.getBoolean()) {
                    return SplitController.SplitSupportStatus.SPLIT_AVAILABLE;
                }
                return SplitController.SplitSupportStatus.SPLIT_UNAVAILABLE;
            } catch (PackageManager.NameNotFoundException unused) {
                if (BuildConfig.INSTANCE.getVerificationMode() == VerificationMode.LOG) {
                    Log.w(ExtensionEmbeddingBackend.TAG, "android.window.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED must be set and enabled in AndroidManifest.xml to use splits APIs.");
                }
                return SplitController.SplitSupportStatus.SPLIT_ERROR_PROPERTY_NOT_DECLARED;
            } catch (Exception e) {
                if (BuildConfig.INSTANCE.getVerificationMode() == VerificationMode.LOG) {
                    Log.e(ExtensionEmbeddingBackend.TAG, "PackageManager.getProperty is not supported", e);
                }
                return SplitController.SplitSupportStatus.SPLIT_ERROR_PROPERTY_NOT_DECLARED;
            }
        }
    }
}
