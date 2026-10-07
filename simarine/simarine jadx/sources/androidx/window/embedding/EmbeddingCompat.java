package androidx.window.embedding;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import androidx.window.WindowSdkExtensions;
import androidx.window.core.BuildConfig;
import androidx.window.core.ConsumerAdapter;
import androidx.window.core.ExtensionsUtil;
import androidx.window.core.VerificationMode;
import androidx.window.extensions.WindowExtensions;
import androidx.window.extensions.WindowExtensionsProvider;
import androidx.window.extensions.core.util.function.Consumer;
import androidx.window.extensions.embedding.ActivityEmbeddingComponent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;

/* JADX INFO: compiled from: EmbeddingCompat.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0000\u0018\u0000 '2\u00020\u0001:\u0001'B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t¢\u0006\u0002\u0010\nJ\b\u0010\u000b\u001a\u00020\fH\u0017J\b\u0010\r\u001a\u00020\fH\u0017J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\u0010\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\u0018\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019H\u0017J\u0016\u0010\u001a\u001a\u00020\f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cH\u0016J\u001c\u0010\u001e\u001a\u00020\f2\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\"0 H\u0017J\u0018\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\"H\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006("}, d2 = {"Landroidx/window/embedding/EmbeddingCompat;", "Landroidx/window/embedding/EmbeddingInterfaceCompat;", "embeddingExtension", "Landroidx/window/extensions/embedding/ActivityEmbeddingComponent;", "adapter", "Landroidx/window/embedding/EmbeddingAdapter;", "consumerAdapter", "Landroidx/window/core/ConsumerAdapter;", "applicationContext", "Landroid/content/Context;", "(Landroidx/window/extensions/embedding/ActivityEmbeddingComponent;Landroidx/window/embedding/EmbeddingAdapter;Landroidx/window/core/ConsumerAdapter;Landroid/content/Context;)V", "clearSplitAttributesCalculator", "", "invalidateTopVisibleSplitAttributes", "isActivityEmbedded", "", "activity", "Landroid/app/Activity;", "setEmbeddingCallback", "embeddingCallback", "Landroidx/window/embedding/EmbeddingInterfaceCompat$EmbeddingCallbackInterface;", "setLaunchingActivityStack", "Landroid/app/ActivityOptions;", "options", "token", "Landroid/os/IBinder;", "setRules", "rules", "", "Landroidx/window/embedding/EmbeddingRule;", "setSplitAttributesCalculator", "calculator", "Lkotlin/Function1;", "Landroidx/window/embedding/SplitAttributesCalculatorParams;", "Landroidx/window/embedding/SplitAttributes;", "updateSplitAttributes", "splitInfo", "Landroidx/window/embedding/SplitInfo;", "splitAttributes", "Companion", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class EmbeddingCompat implements EmbeddingInterfaceCompat {

    /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    public static final boolean DEBUG = true;
    private static final String TAG = "EmbeddingCompat";
    private final EmbeddingAdapter adapter;
    private final Context applicationContext;
    private final ConsumerAdapter consumerAdapter;
    private final ActivityEmbeddingComponent embeddingExtension;

    public EmbeddingCompat(ActivityEmbeddingComponent embeddingExtension, EmbeddingAdapter adapter, ConsumerAdapter consumerAdapter, Context applicationContext) {
        Intrinsics.checkNotNullParameter(embeddingExtension, "embeddingExtension");
        Intrinsics.checkNotNullParameter(adapter, "adapter");
        Intrinsics.checkNotNullParameter(consumerAdapter, "consumerAdapter");
        Intrinsics.checkNotNullParameter(applicationContext, "applicationContext");
        this.embeddingExtension = embeddingExtension;
        this.adapter = adapter;
        this.consumerAdapter = consumerAdapter;
        this.applicationContext = applicationContext;
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void setRules(Set<? extends EmbeddingRule> rules) {
        Intrinsics.checkNotNullParameter(rules, "rules");
        Iterator<? extends EmbeddingRule> it = rules.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof SplitRule) {
                if (Intrinsics.areEqual(SplitController.INSTANCE.getInstance(this.applicationContext).getSplitSupportStatus(), SplitController.SplitSupportStatus.SPLIT_AVAILABLE)) {
                    break;
                }
                if (BuildConfig.INSTANCE.getVerificationMode() == VerificationMode.LOG) {
                    Log.w(TAG, "Cannot set SplitRule because ActivityEmbedding Split is not supported or PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED is not set.");
                    return;
                }
                return;
            }
        }
        this.embeddingExtension.setEmbeddingRules(this.adapter.translate(this.applicationContext, rules));
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void setEmbeddingCallback(final EmbeddingInterfaceCompat.EmbeddingCallbackInterface embeddingCallback) throws IllegalAccessException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(embeddingCallback, "embeddingCallback");
        if (ExtensionsUtil.INSTANCE.getSafeVendorApiLevel() < 2) {
            this.consumerAdapter.addConsumer(this.embeddingExtension, Reflection.getOrCreateKotlinClass(List.class), "setSplitInfoCallback", new Function1<List<?>, Unit>() { // from class: androidx.window.embedding.EmbeddingCompat.setEmbeddingCallback.1
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(1);
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ Unit invoke(List<?> list) {
                    invoke2(list);
                    return Unit.INSTANCE;
                }

                /* JADX INFO: renamed from: invoke, reason: avoid collision after fix types in other method */
                public final void invoke2(List<?> values) {
                    Intrinsics.checkNotNullParameter(values, "values");
                    ArrayList arrayList = new ArrayList();
                    for (Object obj : values) {
                        if (obj instanceof androidx.window.extensions.embedding.SplitInfo) {
                            arrayList.add(obj);
                        }
                    }
                    embeddingCallback.onSplitInfoChanged(this.adapter.translate(arrayList));
                }
            });
        } else {
            this.embeddingExtension.setSplitInfoCallback(new Consumer() { // from class: androidx.window.embedding.EmbeddingCompat$$ExternalSyntheticLambda0
                @Override // androidx.window.extensions.core.util.function.Consumer
                public final void accept(Object obj) {
                    EmbeddingCompat.setEmbeddingCallback$lambda$0(embeddingCallback, this, (List) obj);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void setEmbeddingCallback$lambda$0(EmbeddingInterfaceCompat.EmbeddingCallbackInterface embeddingCallback, EmbeddingCompat this$0, List splitInfoList) {
        Intrinsics.checkNotNullParameter(embeddingCallback, "$embeddingCallback");
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        EmbeddingAdapter embeddingAdapter = this$0.adapter;
        Intrinsics.checkNotNullExpressionValue(splitInfoList, "splitInfoList");
        embeddingCallback.onSplitInfoChanged(embeddingAdapter.translate((List<? extends androidx.window.extensions.embedding.SplitInfo>) splitInfoList));
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public boolean isActivityEmbedded(Activity activity) {
        Intrinsics.checkNotNullParameter(activity, "activity");
        return this.embeddingExtension.isActivityEmbedded(activity);
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void setSplitAttributesCalculator(Function1<? super SplitAttributesCalculatorParams, SplitAttributes> calculator) {
        Intrinsics.checkNotNullParameter(calculator, "calculator");
        WindowSdkExtensions.INSTANCE.getInstance().requireExtensionVersion$window_release(2);
        this.embeddingExtension.setSplitAttributesCalculator(this.adapter.translateSplitAttributesCalculator(calculator));
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void clearSplitAttributesCalculator() {
        WindowSdkExtensions.INSTANCE.getInstance().requireExtensionVersion$window_release(2);
        this.embeddingExtension.clearSplitAttributesCalculator();
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void invalidateTopVisibleSplitAttributes() {
        WindowSdkExtensions.INSTANCE.getInstance().requireExtensionVersion$window_release(3);
        this.embeddingExtension.invalidateTopVisibleSplitAttributes();
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public void updateSplitAttributes(SplitInfo splitInfo, SplitAttributes splitAttributes) {
        Intrinsics.checkNotNullParameter(splitInfo, "splitInfo");
        Intrinsics.checkNotNullParameter(splitAttributes, "splitAttributes");
        WindowSdkExtensions.INSTANCE.getInstance().requireExtensionVersion$window_release(3);
        this.embeddingExtension.updateSplitAttributes(splitInfo.getToken(), this.adapter.translateSplitAttributes(splitAttributes));
    }

    @Override // androidx.window.embedding.EmbeddingInterfaceCompat
    public ActivityOptions setLaunchingActivityStack(ActivityOptions options, IBinder token) {
        Intrinsics.checkNotNullParameter(options, "options");
        Intrinsics.checkNotNullParameter(token, "token");
        WindowSdkExtensions.INSTANCE.getInstance().requireExtensionVersion$window_release(3);
        ActivityOptions launchingActivityStack = this.embeddingExtension.setLaunchingActivityStack(options, token);
        Intrinsics.checkNotNullExpressionValue(launchingActivityStack, "embeddingExtension.setLa…vityStack(options, token)");
        return launchingActivityStack;
    }

    /* JADX INFO: compiled from: EmbeddingCompat.kt */
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0007\u001a\u00020\bJ\b\u0010\t\u001a\u00020\bH\u0002J\u0006\u0010\n\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u000b"}, d2 = {"Landroidx/window/embedding/EmbeddingCompat$Companion;", "", "()V", "DEBUG", "", "TAG", "", "embeddingComponent", "Landroidx/window/extensions/embedding/ActivityEmbeddingComponent;", "emptyActivityEmbeddingProxy", "isEmbeddingAvailable", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final boolean isEmbeddingAvailable() {
            try {
                ClassLoader classLoader = EmbeddingCompat.class.getClassLoader();
                if (classLoader == null) {
                    return false;
                }
                ConsumerAdapter consumerAdapter = new ConsumerAdapter(classLoader);
                WindowExtensions windowExtensions = WindowExtensionsProvider.getWindowExtensions();
                Intrinsics.checkNotNullExpressionValue(windowExtensions, "getWindowExtensions()");
                return new SafeActivityEmbeddingComponentProvider(classLoader, consumerAdapter, windowExtensions).getActivityEmbeddingComponent() != null;
            } catch (NoClassDefFoundError unused) {
                Log.d(EmbeddingCompat.TAG, "Embedding extension version not found");
                return false;
            } catch (UnsupportedOperationException unused2) {
                Log.d(EmbeddingCompat.TAG, "Stub Extension");
                return false;
            }
        }

        public final ActivityEmbeddingComponent embeddingComponent() {
            if (isEmbeddingAvailable()) {
                ClassLoader classLoader = EmbeddingCompat.class.getClassLoader();
                if (classLoader != null) {
                    ConsumerAdapter consumerAdapter = new ConsumerAdapter(classLoader);
                    WindowExtensions windowExtensions = WindowExtensionsProvider.getWindowExtensions();
                    Intrinsics.checkNotNullExpressionValue(windowExtensions, "getWindowExtensions()");
                    ActivityEmbeddingComponent activityEmbeddingComponent = new SafeActivityEmbeddingComponentProvider(classLoader, consumerAdapter, windowExtensions).getActivityEmbeddingComponent();
                    if (activityEmbeddingComponent != null) {
                        return activityEmbeddingComponent;
                    }
                }
                return emptyActivityEmbeddingProxy();
            }
            return emptyActivityEmbeddingProxy();
        }

        private final ActivityEmbeddingComponent emptyActivityEmbeddingProxy() {
            Object objNewProxyInstance = Proxy.newProxyInstance(EmbeddingCompat.class.getClassLoader(), new Class[]{ActivityEmbeddingComponent.class}, new InvocationHandler() { // from class: androidx.window.embedding.EmbeddingCompat$Companion$$ExternalSyntheticLambda0
                @Override // java.lang.reflect.InvocationHandler
                public final Object invoke(Object obj, Method method, Object[] objArr) {
                    return EmbeddingCompat.Companion.emptyActivityEmbeddingProxy$lambda$2(obj, method, objArr);
                }
            });
            Intrinsics.checkNotNull(objNewProxyInstance, "null cannot be cast to non-null type androidx.window.extensions.embedding.ActivityEmbeddingComponent");
            return (ActivityEmbeddingComponent) objNewProxyInstance;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final Unit emptyActivityEmbeddingProxy$lambda$2(Object obj, Method method, Object[] objArr) {
            return Unit.INSTANCE;
        }
    }
}
