package androidx.navigation;

import androidx.exifinterface.media.ExifInterface;
import androidx.navigation.serialization.RouteSerializerKt;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.MagicApiIntrinsics;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;
import kotlinx.serialization.SerializersKt;

/* JADX INFO: compiled from: NavGraph.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u00002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u001a$\u0010\u0000\u001a\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\u00020\u00042\u0006\u0010\u0005\u001a\u0002H\u0002H\u0086\u0002¢\u0006\u0002\u0010\u0006\u001a\u0017\u0010\u0000\u001a\u00020\u0001*\u00020\u00042\b\b\u0001\u0010\u0007\u001a\u00020\bH\u0086\u0002\u001a\u0015\u0010\u0000\u001a\u00020\u0001*\u00020\u00042\u0006\u0010\u0005\u001a\u00020\tH\u0086\u0002\u001a'\u0010\u0000\u001a\u00020\u0001\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u0002H\u00020\nH\u0086\n\u001a$\u0010\u000b\u001a\u00020\f\"\b\b\u0000\u0010\u0002*\u00020\u0003*\u00020\u00042\u0006\u0010\u0005\u001a\u0002H\u0002H\u0086\n¢\u0006\u0002\u0010\r\u001a\u0017\u0010\u000b\u001a\u00020\f*\u00020\u00042\b\b\u0001\u0010\u0007\u001a\u00020\bH\u0086\n\u001a\u0015\u0010\u000b\u001a\u00020\f*\u00020\u00042\u0006\u0010\u0005\u001a\u00020\tH\u0086\n\u001a'\u0010\u000b\u001a\u00020\f\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u0002H\u00020\nH\u0086\n\u001a\u0015\u0010\u000e\u001a\u00020\u000f*\u00020\u00042\u0006\u0010\u0010\u001a\u00020\fH\u0086\n\u001a\u0015\u0010\u0011\u001a\u00020\u000f*\u00020\u00042\u0006\u0010\u0010\u001a\u00020\fH\u0086\n\u001a\u0015\u0010\u0011\u001a\u00020\u000f*\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004H\u0086\n¨\u0006\u0013"}, d2 = {"contains", "", ExifInterface.GPS_DIRECTION_TRUE, "", "Landroidx/navigation/NavGraph;", "route", "(Landroidx/navigation/NavGraph;Ljava/lang/Object;)Z", "id", "", "", "Lkotlin/reflect/KClass;", "get", "Landroidx/navigation/NavDestination;", "(Landroidx/navigation/NavGraph;Ljava/lang/Object;)Landroidx/navigation/NavDestination;", "minusAssign", "", "node", "plusAssign", "other", "navigation-common_release"}, k = 2, mv = {1, 8, 0}, xi = 48)
public final class NavGraphKt {
    public static final NavDestination get(NavGraph navGraph, int i) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        NavDestination navDestinationFindNode = navGraph.findNode(i);
        if (navDestinationFindNode != null) {
            return navDestinationFindNode;
        }
        throw new IllegalArgumentException("No destination for " + i + " was found in " + navGraph);
    }

    public static final NavDestination get(NavGraph navGraph, String route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        NavDestination navDestinationFindNode = navGraph.findNode(route);
        if (navDestinationFindNode != null) {
            return navDestinationFindNode;
        }
        throw new IllegalArgumentException("No destination for " + route + " was found in " + navGraph);
    }

    public static final <T> NavDestination get(NavGraph navGraph, T route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        NavDestination navDestinationFindNode = navGraph.findNode(route);
        if (navDestinationFindNode != null) {
            return navDestinationFindNode;
        }
        throw new IllegalArgumentException("No destination for " + route + " was found in " + navGraph);
    }

    public static final boolean contains(NavGraph navGraph, int i) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        return navGraph.findNode(i) != null;
    }

    public static final boolean contains(NavGraph navGraph, String route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        return navGraph.findNode(route) != null;
    }

    public static final <T> boolean contains(NavGraph navGraph, T route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        return navGraph.findNode(route) != null;
    }

    public static final void plusAssign(NavGraph navGraph, NavDestination node) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(node, "node");
        navGraph.addDestination(node);
    }

    public static final void plusAssign(NavGraph navGraph, NavGraph other) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(other, "other");
        navGraph.addAll(other);
    }

    public static final void minusAssign(NavGraph navGraph, NavDestination node) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(node, "node");
        navGraph.remove(node);
    }

    public static final /* synthetic */ <T> NavDestination get(NavGraph navGraph, KClass<T> route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        Intrinsics.reifiedOperationMarker(6, ExifInterface.GPS_DIRECTION_TRUE);
        MagicApiIntrinsics.voidMagicApiCall("kotlinx.serialization.serializer.simple");
        NavDestination navDestinationFindNode = navGraph.findNode(RouteSerializerKt.generateHashCode(SerializersKt.serializer((KType) null)));
        if (navDestinationFindNode != null) {
            return navDestinationFindNode;
        }
        throw new IllegalArgumentException("No destination for " + route + " was found in " + navGraph);
    }

    public static final /* synthetic */ <T> boolean contains(NavGraph navGraph, KClass<T> route) {
        Intrinsics.checkNotNullParameter(navGraph, "<this>");
        Intrinsics.checkNotNullParameter(route, "route");
        Intrinsics.reifiedOperationMarker(6, ExifInterface.GPS_DIRECTION_TRUE);
        MagicApiIntrinsics.voidMagicApiCall("kotlinx.serialization.serializer.simple");
        return navGraph.findNode(RouteSerializerKt.generateHashCode(SerializersKt.serializer((KType) null))) != null;
    }
}
