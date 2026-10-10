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
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RenderTest {
    @org.junit.Before public void clearSettings(){org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    @Test public void nativeRendererFitsReferenceGroupsAndBottomControls()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();ActivityTest.fixture(a);DesignViewport view=(DesignViewport)ActivityTest.field(a,"viewport");
            for(int[] size:new int[][]{{1024,600},{1280,720},{800,480},{360,760}}){
                view.measure(View.MeasureSpec.makeMeasureSpec(size[0],View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(size[1],View.MeasureSpec.EXACTLY));view.layout(0,0,size[0],size[1]);android.widget.LinearLayout root=(android.widget.LinearLayout)ActivityTest.field(a,"root");View bar=root.getChildAt(root.getChildCount()-1);assertEquals(root.getHeight(),bar.getBottom());
                LiveDashboard d=(LiveDashboard)ActivityTest.field(a,"liveDashboard");assertTrue(d.getHeight()>300);assertTrue(root.getWidth()>root.getHeight());int gauges=0;
                for(int i=0;i<d.getChildCount();i++)if(d.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)d.getChildAt(i);if(r.getVisibility()!=View.VISIBLE)continue;assertTrue(r.getRight()<=d.getWidth());assertTrue(r.getBottom()<=d.getHeight());assertTrue(r.name.getHeight()>0);if(!r.fallback.isEmpty()){assertTrue(r.name.getWidth()>0);assertNotNull(r.name.getLayout());assertTrue("Text layout escapes its readout",r.name.getLayout().getWidth()<=r.name.getWidth());}if(r.value!=null){assertTrue(r.value.getWidth()>20);assertTrue(r.value.getLayout().getWidth()<=r.value.getWidth());}if(r.isGauge){gauges++;assertTrue(r.gauge.getHeight()>50);}else if(!r.fallback.isEmpty()){assertEquals(r.name.getTop(),r.value.getTop());assertEquals(1,r.name.getMaxLines());}}
                assertEquals(13,gauges);
                Bitmap b=Bitmap.createBitmap(size[0],size[1],Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));assertNotEquals(0,b.getPixel(20,20));String path=System.getProperty("ella.render.path");if(path!=null&&size[0]==1024)try(java.io.OutputStream out=new java.io.FileOutputStream(path)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
            }
            ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();view=(DesignViewport)ActivityTest.field(a,"viewport");view.measure(View.MeasureSpec.makeMeasureSpec(1024,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));view.layout(0,0,1024,600);
            String path=System.getProperty("ella.render.path");if(path!=null){Bitmap settings=Bitmap.createBitmap(1024,600,Bitmap.Config.ARGB_8888);view.draw(new Canvas(settings));try(java.io.OutputStream out=new java.io.FileOutputStream(path.replace(".png","-settings.png"))){settings.compress(Bitmap.CompressFormat.PNG,100,out);}settings.recycle();}
        }
    }
}
