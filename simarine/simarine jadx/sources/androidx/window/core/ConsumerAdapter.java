package androidx.window.core;

import android.app.Activity;
import android.content.Context;
import androidx.exifinterface.media.ExifInterface;
import androidx.window.reflection.WindowExtensionsConstants;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KClass;
import kotlin.reflect.KClasses;

/* JADX INFO: compiled from: ConsumerAdapter.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0001\u0018\u00002\u00020\u0001:\u0002\u001e\u001fB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004JB\u0010\u0005\u001a\u00020\u0006\"\b\b\u0000\u0010\u0007*\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eJ4\u0010\u000f\u001a\u00020\u0001\"\b\b\u0000\u0010\u0007*\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eH\u0002J\u0013\u0010\u0010\u001a\b\u0012\u0002\b\u0003\u0018\u00010\u0011H\u0000¢\u0006\u0002\b\u0012JJ\u0010\u0013\u001a\u00020\u0006\"\b\b\u0000\u0010\u0007*\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0006\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u00162\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eJT\u0010\u0017\u001a\u00020\u0018\"\b\b\u0000\u0010\u0007*\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0006\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u00162\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eH\u0007JT\u0010\u0017\u001a\u00020\u0018\"\b\b\u0000\u0010\u0007*\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0006\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001b2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eH\u0007JL\u0010\u001c\u001a\u00020\u0018\"\b\b\u0000\u0010\u0007*\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00070\n2\u0006\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\f2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u0002H\u0007\u0012\u0004\u0012\u00020\u00060\u000eH\u0007J\f\u0010\u001d\u001a\u0006\u0012\u0002\b\u00030\u0011H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006 "}, d2 = {"Landroidx/window/core/ConsumerAdapter;", "", "loader", "Ljava/lang/ClassLoader;", "(Ljava/lang/ClassLoader;)V", "addConsumer", "", ExifInterface.GPS_DIRECTION_TRUE, "obj", "clazz", "Lkotlin/reflect/KClass;", "methodName", "", "consumer", "Lkotlin/Function1;", "buildConsumer", "consumerClassOrNull", "Ljava/lang/Class;", "consumerClassOrNull$window_release", "createConsumer", "addMethodName", "activity", "Landroid/app/Activity;", "createSubscription", "Landroidx/window/core/ConsumerAdapter$Subscription;", "removeMethodName", "context", "Landroid/content/Context;", "createSubscriptionNoActivity", "unsafeConsumerClass", "ConsumerHandler", "Subscription", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class ConsumerAdapter {
    private final ClassLoader loader;

    /* JADX INFO: compiled from: ConsumerAdapter.kt */
    @Metadata(d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\b`\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&ø\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001¨\u0006\u0004À\u0006\u0001"}, d2 = {"Landroidx/window/core/ConsumerAdapter$Subscription;", "", "dispose", "", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public interface Subscription {
        void dispose();
    }

    public ConsumerAdapter(ClassLoader loader) {
        Intrinsics.checkNotNullParameter(loader, "loader");
        this.loader = loader;
    }

    public final Class<?> consumerClassOrNull$window_release() {
        try {
            return unsafeConsumerClass();
        } catch (ClassNotFoundException unused) {
            return null;
        }
    }

    private final Class<?> unsafeConsumerClass() throws ClassNotFoundException {
        Class<?> clsLoadClass = this.loader.loadClass(WindowExtensionsConstants.JAVA_CONSUMER);
        Intrinsics.checkNotNullExpressionValue(clsLoadClass, "loader.loadClass(\"java.util.function.Consumer\")");
        return clsLoadClass;
    }

    private final <T> Object buildConsumer(KClass<T> clazz, Function1<? super T, Unit> consumer) {
        Object objNewProxyInstance = Proxy.newProxyInstance(this.loader, new Class[]{unsafeConsumerClass()}, new ConsumerHandler(clazz, consumer));
        Intrinsics.checkNotNullExpressionValue(objNewProxyInstance, "newProxyInstance(loader,…onsumerClass()), handler)");
        return objNewProxyInstance;
    }

    public final <T> void addConsumer(Object obj, KClass<T> clazz, String methodName, Function1<? super T, Unit> consumer) throws IllegalAccessException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(obj, "obj");
        Intrinsics.checkNotNullParameter(clazz, "clazz");
        Intrinsics.checkNotNullParameter(methodName, "methodName");
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        obj.getClass().getMethod(methodName, unsafeConsumerClass()).invoke(obj, buildConsumer(clazz, consumer));
    }

    public final <T> Subscription createSubscription(final Object obj, KClass<T> clazz, String addMethodName, String removeMethodName, Activity activity, Function1<? super T, Unit> consumer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(obj, "obj");
        Intrinsics.checkNotNullParameter(clazz, "clazz");
        Intrinsics.checkNotNullParameter(addMethodName, "addMethodName");
        Intrinsics.checkNotNullParameter(removeMethodName, "removeMethodName");
        Intrinsics.checkNotNullParameter(activity, "activity");
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        final Object objBuildConsumer = buildConsumer(clazz, consumer);
        obj.getClass().getMethod(addMethodName, Activity.class, unsafeConsumerClass()).invoke(obj, activity, objBuildConsumer);
        final Method method = obj.getClass().getMethod(removeMethodName, unsafeConsumerClass());
        return new Subscription() { // from class: androidx.window.core.ConsumerAdapter.createSubscription.1
            @Override // androidx.window.core.ConsumerAdapter.Subscription
            public void dispose() throws IllegalAccessException, InvocationTargetException {
                method.invoke(obj, objBuildConsumer);
            }
        };
    }

    public final <T> Subscription createSubscriptionNoActivity(final Object obj, KClass<T> clazz, String addMethodName, String removeMethodName, Function1<? super T, Unit> consumer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(obj, "obj");
        Intrinsics.checkNotNullParameter(clazz, "clazz");
        Intrinsics.checkNotNullParameter(addMethodName, "addMethodName");
        Intrinsics.checkNotNullParameter(removeMethodName, "removeMethodName");
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        final Object objBuildConsumer = buildConsumer(clazz, consumer);
        obj.getClass().getMethod(addMethodName, unsafeConsumerClass()).invoke(obj, objBuildConsumer);
        final Method method = obj.getClass().getMethod(removeMethodName, unsafeConsumerClass());
        return new Subscription() { // from class: androidx.window.core.ConsumerAdapter.createSubscriptionNoActivity.1
            @Override // androidx.window.core.ConsumerAdapter.Subscription
            public void dispose() throws IllegalAccessException, InvocationTargetException {
                method.invoke(obj, objBuildConsumer);
            }
        };
    }

    public final <T> Subscription createSubscription(final Object obj, KClass<T> clazz, String addMethodName, String removeMethodName, Context context, Function1<? super T, Unit> consumer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(obj, "obj");
        Intrinsics.checkNotNullParameter(clazz, "clazz");
        Intrinsics.checkNotNullParameter(addMethodName, "addMethodName");
        Intrinsics.checkNotNullParameter(removeMethodName, "removeMethodName");
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        final Object objBuildConsumer = buildConsumer(clazz, consumer);
        obj.getClass().getMethod(addMethodName, Context.class, unsafeConsumerClass()).invoke(obj, context, objBuildConsumer);
        final Method method = obj.getClass().getMethod(removeMethodName, unsafeConsumerClass());
        return new Subscription() { // from class: androidx.window.core.ConsumerAdapter.createSubscription.2
            @Override // androidx.window.core.ConsumerAdapter.Subscription
            public void dispose() throws IllegalAccessException, InvocationTargetException {
                method.invoke(obj, objBuildConsumer);
            }
        };
    }

    public final <T> void createConsumer(Object obj, KClass<T> clazz, String addMethodName, Activity activity, Function1<? super T, Unit> consumer) throws IllegalAccessException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(obj, "obj");
        Intrinsics.checkNotNullParameter(clazz, "clazz");
        Intrinsics.checkNotNullParameter(addMethodName, "addMethodName");
        Intrinsics.checkNotNullParameter(activity, "activity");
        Intrinsics.checkNotNullParameter(consumer, "consumer");
        obj.getClass().getMethod(addMethodName, Activity.class, unsafeConsumerClass()).invoke(obj, activity, buildConsumer(clazz, consumer));
    }

    /* JADX INFO: compiled from: ConsumerAdapter.kt */
    @Metadata(d1 = {"\u00008\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0002\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0003B'\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\u0005\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\b0\u0007¢\u0006\u0002\u0010\tJ0\u0010\n\u001a\u00020\u00022\u0006\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\r2\u0010\u0010\u000e\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0002\u0018\u00010\u000fH\u0096\u0002¢\u0006\u0002\u0010\u0010J\u0013\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00028\u0000¢\u0006\u0002\u0010\u0013J#\u0010\u0014\u001a\u00020\u0015*\u00020\r2\u0010\u0010\u0016\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0002\u0018\u00010\u000fH\u0002¢\u0006\u0002\u0010\u0017J#\u0010\u0018\u001a\u00020\u0015*\u00020\r2\u0010\u0010\u0016\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0002\u0018\u00010\u000fH\u0002¢\u0006\u0002\u0010\u0017J#\u0010\u0019\u001a\u00020\u0015*\u00020\r2\u0010\u0010\u0016\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0002\u0018\u00010\u000fH\u0002¢\u0006\u0002\u0010\u0017J#\u0010\u001a\u001a\u00020\u0015*\u00020\r2\u0010\u0010\u0016\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0002\u0018\u00010\u000fH\u0002¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001b"}, d2 = {"Landroidx/window/core/ConsumerAdapter$ConsumerHandler;", ExifInterface.GPS_DIRECTION_TRUE, "", "Ljava/lang/reflect/InvocationHandler;", "clazz", "Lkotlin/reflect/KClass;", "consumer", "Lkotlin/Function1;", "", "(Lkotlin/reflect/KClass;Lkotlin/jvm/functions/Function1;)V", "invoke", "obj", "method", "Ljava/lang/reflect/Method;", "parameters", "", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", "invokeAccept", "parameter", "(Ljava/lang/Object;)V", "isAccept", "", "args", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Z", "isEquals", "isHashCode", "isToString", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    private static final class ConsumerHandler<T> implements InvocationHandler {
        private final KClass<T> clazz;
        private final Function1<T, Unit> consumer;

        /* JADX WARN: Multi-variable type inference failed */
        public ConsumerHandler(KClass<T> clazz, Function1<? super T, Unit> consumer) {
            Intrinsics.checkNotNullParameter(clazz, "clazz");
            Intrinsics.checkNotNullParameter(consumer, "consumer");
            this.clazz = clazz;
            this.consumer = consumer;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object obj, Method method, Object[] parameters) {
            Intrinsics.checkNotNullParameter(obj, "obj");
            Intrinsics.checkNotNullParameter(method, "method");
            if (isAccept(method, parameters)) {
                invokeAccept(KClasses.cast(this.clazz, parameters != null ? parameters[0] : null));
                return Unit.INSTANCE;
            }
            if (isEquals(method, parameters)) {
                return Boolean.valueOf(obj == (parameters != null ? parameters[0] : null));
            }
            if (isHashCode(method, parameters)) {
                return Integer.valueOf(this.consumer.hashCode());
            }
            if (isToString(method, parameters)) {
                return this.consumer.toString();
            }
            throw new UnsupportedOperationException("Unexpected method call object:" + obj + ", method: " + method + ", args: " + parameters);
        }

        public final void invokeAccept(T parameter) {
            Intrinsics.checkNotNullParameter(parameter, "parameter");
            this.consumer.invoke(parameter);
        }

        private final boolean isEquals(Method method, Object[] objArr) {
            return Intrinsics.areEqual(method.getName(), "equals") && method.getReturnType().equals(Boolean.TYPE) && objArr != null && objArr.length == 1;
        }

        private final boolean isHashCode(Method method, Object[] objArr) {
            return Intrinsics.areEqual(method.getName(), "hashCode") && method.getReturnType().equals(Integer.TYPE) && objArr == null;
        }

        private final boolean isAccept(Method method, Object[] objArr) {
            return Intrinsics.areEqual(method.getName(), "accept") && objArr != null && objArr.length == 1;
        }

        private final boolean isToString(Method method, Object[] objArr) {
            return Intrinsics.areEqual(method.getName(), "toString") && method.getReturnType().equals(String.class) && objArr == null;
        }
    }
}
