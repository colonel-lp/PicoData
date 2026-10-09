package com.colonellp.ellamonitor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {27, 35}, qualifiers = "w1024dp-h600dp-land-mdpi")
public class ActivityTest {
    private static View text(View view, String value) {
        if (view instanceof TextView && ((TextView)view).getText().toString().equals(value)) return view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) { View match = text(((ViewGroup)view).getChildAt(i), value); if (match != null) return match; }
        return null;
    }
    @Test public void launchesOfflineWithControlsAndNeverClaimsFreshZero() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a = controller.get(); View view = a.getWindow().getDecorView();
            assertNotNull(text(view, "Main")); assertNotNull(text(view, "Settings")); assertNotNull(text(view, "Keep screen")); assertNotNull(text(view, "Month")); assertNotNull(text(view, "Unavailable"));
            ((Button)text(view, "Theme")).performClick(); assertNotNull(text(a.getWindow().getDecorView(), "Settings"));
            ((Button)text(a.getWindow().getDecorView(), "6h")).performClick(); assertNotNull(text(a.getWindow().getDecorView(), "Rolling"));
        }
    }
    @Test public void liveDashboardBuildsFromSyntheticCatalogueAndExpiresOnStop() throws Exception {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a = controller.get();
            Field metrics = MainActivity.class.getDeclaredField("metrics"); metrics.setAccessible(true); metrics.set(a, MonitorData.catalogue(ContractTest.catalogue()));
            Field live = MainActivity.class.getDeclaredField("live"); live.setAccessible(true); live.set(a, ContractTest.live());
            Field at = MainActivity.class.getDeclaredField("receivedMono"); at.setAccessible(true); at.setLong(a, android.os.SystemClock.elapsedRealtime());
            Method update = MainActivity.class.getDeclaredMethod("updateLive"); update.setAccessible(true); update.invoke(a);
            assertNotNull(text(a.getWindow().getDecorView(), "● On")); assertNotNull(text(a.getWindow().getDecorView(), "○ Off")); assertNotNull(text(a.getWindow().getDecorView(), "Synthetic temperature"));
            controller.pause().stop(); assertNull(live.get(a));
        }
    }
    @Test @Config(qualifiers = "w360dp-h760dp-port-mdpi") public void portraitReflowsGroupsAndKeepsNavigation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            View root = controller.get().getWindow().getDecorView(); assertNotNull(text(root, "Battery monitoring")); assertNotNull(text(root, "Temperatures")); assertNotNull(text(root, "Settings"));
        }
    }
}
