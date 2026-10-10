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
    static void renderFixture(MainActivity a)throws Exception{
        org.json.JSONObject catalogue=ContractTest.catalogue(),live=ContractTest.live();org.json.JSONArray metrics=catalogue.getJSONArray("metrics"),measurements=live.getJSONArray("measurements");org.json.JSONObject raw=live.getJSONObject("pico").getJSONObject("readings");
        for(int i=1;i<9;i++){String id="synthetic-load-"+i;metrics.put(ContractTest.metric(id,"pico","electrical","load","",300+i).put("name","Load "+(i+1)));measurements.put(new org.json.JSONObject().put("id",id).put("fresh",true).put("mappingValid",true).put("values",new org.json.JSONObject().put("current",-.2*i).put("voltage",12.4).put("watts",-.2*i*12.4)));}
        for(String[] extra:new String[][]{{"pv1-test","supply","pv1"},{"external-test","load","externalLoad"}}){metrics.put(ContractTest.metric(extra[0],"sbms","electrical",extra[1],extra[2],0));measurements.put(new org.json.JSONObject().put("id",extra[0]).put("fresh",true).put("mappingValid",true).put("values",new org.json.JSONObject().put("current",1.5).put("voltage",12.4).put("watts",18.6)));}
        String[] temperatures={"Inside","Outside","Alternator","Fridge cabinet","Fridge interior","Water","Aux 1","Aux 2","Battery temperature"};raw.remove("202");for(int i=0;i<temperatures.length;i++)raw.put("temp-"+i,new org.json.JSONObject().put("type","thermometer").put("name",temperatures[i]).put("temperature",10+i));
        raw.put("angle-pitch",new org.json.JSONObject().put("type","inclinometer").put("name","Pitch").put("degree",-3));raw.put("angle-roll",new org.json.JSONObject().put("type","inclinometer").put("name","Roll").put("degree",2));raw.put("pressure",new org.json.JSONObject().put("type","barometer").put("name","Pressure").put("pressure",1002));raw.put("203",new org.json.JSONObject().put("type","tank").put("name","Water").put("percentage",60).put("remainingCapacity",72));raw.put("lpg",new org.json.JSONObject().put("type","tank").put("name","LPG").put("percentage",40));raw.put("starter",new org.json.JSONObject().put("type","battery").put("name","Starter").put("voltage",25.2));
        org.json.JSONObject flags=live.getJSONObject("sbms").getJSONObject("reading").getJSONObject("flags");for(String flag:new String[]{"CFET","DFET","EOC","OVLK","UVLK","IOT","LVC","CELF"})flags.put(flag,flag.equals("CFET")||flag.equals("DFET"));
        ActivityTest.set(a,"metrics",MonitorData.catalogue(catalogue));ActivityTest.set(a,"live",live);ActivityTest.set(a,"receivedMono",android.os.SystemClock.elapsedRealtime());ActivityTest.call(a,"updateLive");
    }
    @Test public void nativeRendererFitsReferenceGroupsAndBottomControls()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();renderFixture(a);DesignViewport view=(DesignViewport)ActivityTest.field(a,"viewport");
            for(int[] size:new int[][]{{1024,600},{1280,720},{800,480},{360,760}}){
                view.measure(View.MeasureSpec.makeMeasureSpec(size[0],View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(size[1],View.MeasureSpec.EXACTLY));view.layout(0,0,size[0],size[1]);android.widget.LinearLayout root=(android.widget.LinearLayout)ActivityTest.field(a,"root");View bar=root.getChildAt(root.getChildCount()-1);assertEquals(root.getHeight(),bar.getBottom());
                LiveDashboard d=(LiveDashboard)ActivityTest.field(a,"liveDashboard");assertTrue(d.getHeight()>300);assertTrue(root.getWidth()>root.getHeight());int gauges=0;float lastLoadBottom=-1;int loadHeight=-1;
                for(int i=0;i<d.getChildCount();i++)if(d.getChildAt(i) instanceof LiveDashboard.Readout){LiveDashboard.Readout r=(LiveDashboard.Readout)d.getChildAt(i);if(r.getVisibility()!=View.VISIBLE)continue;assertTrue(r.getRight()<=d.getWidth());assertTrue(r.getBottom()<=d.getHeight());assertTrue(r.name.getHeight()>0);if(!r.fallback.isEmpty()){assertTrue(r.name.getWidth()>0);assertNotNull(r.name.getLayout());assertTrue("Text layout escapes its readout",r.name.getLayout().getWidth()<=r.name.getWidth());}if(r.value!=null){assertTrue(r.value.getWidth()>20);assertTrue(r.value.getLayout().getWidth()<=r.value.getWidth());}if(r.datum!=null&&r.datum.group.equals("loads")){if(lastLoadBottom>=0)assertEquals(6,r.getTop()-lastLoadBottom,1);lastLoadBottom=r.getBottom();if(loadHeight<0)loadHeight=r.getHeight();else assertEquals(loadHeight,r.getHeight(),1);}assertNotNull(r.getBackground());if(r.isGauge){gauges++;assertTrue(r.gauge.getHeight()>50);}else if(!r.fallback.isEmpty()){assertEquals(r.name.getTop(),r.value.getTop());assertEquals(1,r.name.getMaxLines());}}
                assertEquals(13,gauges);
                Bitmap b=Bitmap.createBitmap(size[0],size[1],Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));assertNotEquals(0,b.getPixel(20,20));String path=System.getProperty("ella.render.path");if(path!=null&&size[0]==1024)try(java.io.OutputStream out=new java.io.FileOutputStream(path)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
            }
            ActivityTest.description(a.getWindow().getDecorView(),"Settings").performClick();view=(DesignViewport)ActivityTest.field(a,"viewport");view.measure(View.MeasureSpec.makeMeasureSpec(1024,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));view.layout(0,0,1024,600);
            String path=System.getProperty("ella.render.path");if(path!=null){Bitmap settings=Bitmap.createBitmap(1024,600,Bitmap.Config.ARGB_8888);view.draw(new Canvas(settings));try(java.io.OutputStream out=new java.io.FileOutputStream(path.replace(".png","-settings.png"))){settings.compress(Bitmap.CompressFormat.PNG,100,out);}settings.recycle();}
        }
    }
}

