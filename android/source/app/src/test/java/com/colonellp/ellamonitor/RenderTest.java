package com.colonellp.ellamonitor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, qualifiers = "w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RenderTest {
    @Test public void nativeRendererKeepsNavigationOutsideScrollableDashboard() throws Exception {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a = controller.get();
            Field metrics = MainActivity.class.getDeclaredField("metrics"); metrics.setAccessible(true); metrics.set(a, MonitorData.catalogue(ContractTest.catalogue()));
            Field live = MainActivity.class.getDeclaredField("live"); live.setAccessible(true); live.set(a, ContractTest.live());
            Field at = MainActivity.class.getDeclaredField("receivedMono"); at.setAccessible(true); at.setLong(a, android.os.SystemClock.elapsedRealtime());
            Method update = MainActivity.class.getDeclaredMethod("updateLive"); update.setAccessible(true); update.invoke(a);
            Field root = MainActivity.class.getDeclaredField("root"); root.setAccessible(true); android.widget.LinearLayout view = (android.widget.LinearLayout)root.get(a);
            view.measure(View.MeasureSpec.makeMeasureSpec(1280, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY)); view.layout(0, 0, 1280, 720);
            View navigation = view.getChildAt(view.getChildCount() - 1); assertTrue(navigation.getTop() >= 650); assertTrue(navigation.getBottom() <= 720);
            Field tiles = MainActivity.class.getDeclaredField("tiles"); tiles.setAccessible(true);
            for (Object tile : ((java.util.Map<?, ?>)tiles.get(a)).values()) {
                Field label = tile.getClass().getDeclaredField("label"); label.setAccessible(true); android.widget.TextView t = (android.widget.TextView)label.get(tile);
                assertTrue("Label has no measured height: " + t.getText(), t.getHeight() > 0);
                assertTrue("Label has no measured width: " + t.getText(), t.getWidth() > 16);
                assertNotNull("Label has no text layout", t.getLayout());
                assertTrue("Label has no text layout width", t.getLayout().getWidth() > 16);
            }
            Bitmap bitmap = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888); view.draw(new Canvas(bitmap));
            assertNotEquals(0, bitmap.getPixel(20, 20));
            String path = System.getProperty("ella.render.path");
            if (path != null) try (java.io.OutputStream out = new java.io.FileOutputStream(path)) { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)); }
            bitmap.recycle();
        }
    }
}
