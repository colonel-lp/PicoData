package androidx.navigation;

import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.flow.StateFlow;

/* JADX INFO: compiled from: NavGraphNavigator.kt */
/* JADX INFO: loaded from: classes.dex */
@Navigator.Name(NotificationCompat.CATEGORY_NAVIGATION)
@Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0017\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0002\u0010\u0005J\b\u0010\f\u001a\u00020\u0002H\u0016J$\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\t2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0002J*\u0010\r\u001a\u00020\u000e2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\b2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0016R\u001d\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00078F¢\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0015"}, d2 = {"Landroidx/navigation/NavGraphNavigator;", "Landroidx/navigation/Navigator;", "Landroidx/navigation/NavGraph;", "navigatorProvider", "Landroidx/navigation/NavigatorProvider;", "(Landroidx/navigation/NavigatorProvider;)V", "backStack", "Lkotlinx/coroutines/flow/StateFlow;", "", "Landroidx/navigation/NavBackStackEntry;", "getBackStack", "()Lkotlinx/coroutines/flow/StateFlow;", "createDestination", "navigate", "", "entry", "navOptions", "Landroidx/navigation/NavOptions;", "navigatorExtras", "Landroidx/navigation/Navigator$Extras;", "entries", "navigation-common_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public class NavGraphNavigator extends Navigator<NavGraph> {
    private final NavigatorProvider navigatorProvider;

    public NavGraphNavigator(NavigatorProvider navigatorProvider) {
        Intrinsics.checkNotNullParameter(navigatorProvider, "navigatorProvider");
        this.navigatorProvider = navigatorProvider;
    }

    public final StateFlow<List<NavBackStackEntry>> getBackStack() {
        return getState().getBackStack();
    }

    @Override // androidx.navigation.Navigator
    public NavGraph createDestination() {
        return new NavGraph(this);
    }

    @Override // androidx.navigation.Navigator
    public void navigate(List<NavBackStackEntry> entries, NavOptions navOptions, Navigator.Extras navigatorExtras) {
        Intrinsics.checkNotNullParameter(entries, "entries");
        Iterator<NavBackStackEntry> it = entries.iterator();
        while (it.hasNext()) {
            navigate(it.next(), navOptions, navigatorExtras);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v6, types: [T, android.os.Bundle] */
    /* JADX WARN: Type inference failed for: r5v1, types: [T, android.os.Bundle] */
    private final void navigate(NavBackStackEntry entry, NavOptions navOptions, Navigator.Extras navigatorExtras) {
        NavDestination navDestinationFindNode;
        NavDestination destination = entry.getDestination();
        Intrinsics.checkNotNull(destination, "null cannot be cast to non-null type androidx.navigation.NavGraph");
        NavGraph navGraph = (NavGraph) destination;
        final Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = entry.getArguments();
        int startDestId = navGraph.getStartDestId();
        String startDestinationRoute = navGraph.getStartDestinationRoute();
        if (startDestId == 0 && startDestinationRoute == null) {
            throw new IllegalStateException(("no start destination defined via app:startDestination for " + navGraph.getDisplayName()).toString());
        }
        if (startDestinationRoute != null) {
            navDestinationFindNode = navGraph.findNode(startDestinationRoute, false);
        } else {
            navDestinationFindNode = navGraph.getNodes().get(startDestId);
        }
        if (navDestinationFindNode == null) {
            throw new IllegalArgumentException("navigation destination " + navGraph.getStartDestDisplayName() + " is not a direct child of this NavGraph");
        }
        if (startDestinationRoute != null) {
            if (!Intrinsics.areEqual(startDestinationRoute, navDestinationFindNode.getRoute())) {
                NavDestination.DeepLinkMatch deepLinkMatchMatchRoute = navDestinationFindNode.matchRoute(startDestinationRoute);
                Bundle matchingArgs = deepLinkMatchMatchRoute != null ? deepLinkMatchMatchRoute.getMatchingArgs() : null;
                if (matchingArgs != null && !matchingArgs.isEmpty()) {
                    ?? bundle = new Bundle();
                    bundle.putAll(matchingArgs);
                    if (((Bundle) objectRef.element) != null) {
                        bundle.putAll((Bundle) objectRef.element);
                    }
                    objectRef.element = bundle;
                }
            }
            if (!navDestinationFindNode.getArguments().isEmpty()) {
                List<String> listMissingRequiredArguments = NavArgumentKt.missingRequiredArguments(navDestinationFindNode.getArguments(), new Function1<String, Boolean>() { // from class: androidx.navigation.NavGraphNavigator$navigate$missingRequiredArgs$1
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(1);
                    }

                    @Override // kotlin.jvm.functions.Function1
                    public final Boolean invoke(String key) {
                        Intrinsics.checkNotNullParameter(key, "key");
                        boolean z = true;
                        if (objectRef.element != null && objectRef.element.containsKey(key)) {
                            z = false;
                        }
                        return Boolean.valueOf(z);
                    }
                });
                if (!listMissingRequiredArguments.isEmpty()) {
                    throw new IllegalArgumentException(("Cannot navigate to startDestination " + navDestinationFindNode + ". Missing required arguments [" + listMissingRequiredArguments + ']').toString());
                }
            }
        }
        this.navigatorProvider.getNavigator(navDestinationFindNode.getNavigatorName()).navigate(CollectionsKt.listOf(getState().createBackStackEntry(navDestinationFindNode, navDestinationFindNode.addInDefaultArgs((Bundle) objectRef.element))), navOptions, navigatorExtras);
    }
}
