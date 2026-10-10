package com.colonellp.ellamonitor;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import java.io.File;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={27,35},qualifiers="w1024dp-h600dp-land-mdpi")
public class UpdateTest {
    @org.junit.Before public void clear(){org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences("viewer",0).edit().clear().commit();}
    private JSONObject release(String asset,String url,int code)throws Exception{return new JSONObject().put("tag_name","v0.22").put("name","Ella Monitoring v0.22").put("body","Android versionCode "+code).put("assets",new JSONArray().put(new JSONObject().put("name",asset).put("browser_download_url",url).put("size",1234)));}
    @Test public void updateFeedSelectsOnlyNewEllaApksAndOffStopsAutoChecks()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();AppUpdateManager m=new AppUpdateManager(a,a.viewerPreferences());Method parse=AppUpdateManager.class.getDeclaredMethod("parseReleases",JSONArray.class);parse.setAccessible(true);
            String valid="https://github.com/colonel-lp/PicoData/releases/download/v0.22/Ella-monitoring-v0.22.apk";
            AppUpdateManager.CheckResult r=(AppUpdateManager.CheckResult)parse.invoke(m,new JSONArray().put(release("Other-v0.22.apk",valid,4)).put(release("Ella-monitoring-v0.22.apk","https://example.invalid/app.apk",4)).put(release("Ella-monitoring-v0.22.apk",valid,4)));
            assertNotNull(r.update);assertEquals(valid,r.update.downloadUrl);assertEquals(4,r.update.advertisedVersionCode);
            assertNull(((AppUpdateManager.CheckResult)parse.invoke(m,new JSONArray().put(release("Ella-monitoring-v0.22.apk",valid,BuildConfig.VERSION_CODE)))).update);
            for(int hours:new int[]{0,1,3,6,12,24}){a.viewerPreferences().edit().putInt("update.intervalHours",hours).apply();assertEquals(hours*3600000L,m.updateIntervalMillis());}
            a.viewerPreferences().edit().putInt("update.intervalHours",0).apply();final boolean[] skipped={false};m.check(false,result->skipped[0]=result.skipped);org.robolectric.shadows.ShadowLooper.idleMainLooper();assertTrue(skipped[0]);m.destroy();
        }
    }
    @Test public void downloadedApkCleanupWaitsForConfirmedInstalledVersion()throws Exception{
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=c.get();AppUpdateManager m=new AppUpdateManager(a,a.viewerPreferences());File file=UpdateFileProvider.updateFile(a);java.nio.file.Files.write(file.toPath(),new byte[]{1,2,3});
            a.viewerPreferences().edit().putLong("update_pending_version_code_v2",BuildConfig.VERSION_CODE+1).apply();m.cleanupInstalledUpdate();assertTrue(file.exists());
            a.viewerPreferences().edit().putLong("update_pending_version_code_v2",BuildConfig.VERSION_CODE).apply();m.cleanupInstalledUpdate();assertFalse(file.exists());m.destroy();
        }
    }
    @Test @Config(sdk=27) public void signaturesMustMatchAndCompleteChangelogIsBundled()throws Exception{
        Method method=AppUpdateManager.class.getDeclaredMethod("signaturesMatch",PackageInfo.class,PackageInfo.class);method.setAccessible(true);PackageInfo installed=new PackageInfo(),archive=new PackageInfo();installed.signatures=new Signature[]{new Signature("0123")};archive.signatures=new Signature[]{new Signature("4567")};assertFalse((Boolean)method.invoke(null,installed,archive));archive.signatures=installed.signatures;assertTrue((Boolean)method.invoke(null,installed,archive));
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){AppUpdateManager m=new AppUpdateManager(c.get(),c.get().viewerPreferences());assertTrue(ChangelogText.valid(m.currentChangelog()));assertTrue(ChangelogText.display(m.currentChangelog(),null).startsWith("0.21"));m.destroy();}
    }
}
