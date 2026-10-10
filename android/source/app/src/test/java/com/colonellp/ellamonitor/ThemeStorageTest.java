package com.colonellp.ellamonitor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class ThemeStorageTest {
    /** Filesystem-backed stand-in for the owned MediaStore Downloads contract. */
    static final class DownloadsProvider extends ContentProvider {
        final Map<Long,ContentValues> entries=new LinkedHashMap<>();final Map<Long,File> files=new LinkedHashMap<>();long next=1;
        public boolean onCreate(){return true;}public String getType(Uri uri){return "application/json";}
        public Uri insert(Uri uri,ContentValues values){long id=next++;entries.put(id,new ContentValues(values));try{files.put(id,File.createTempFile("theme-test", ".json"));}catch(Exception e){throw new RuntimeException(e);}return Uri.withAppendedPath(uri,Long.toString(id));}
        public Cursor query(Uri uri,String[] columns,String selection,String[] args,String order){MatrixCursor c=new MatrixCursor(columns);for(Map.Entry<Long,ContentValues> e:entries.entrySet()){if(args!=null&&!args[0].equals(e.getValue().getAsString(MediaStore.Downloads.RELATIVE_PATH)))continue;Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)row[i]=columns[i].equals(MediaStore.Downloads._ID)?e.getKey():e.getValue().get(columns[i]);c.addRow(row);}return c;}
        public int update(Uri uri,ContentValues values,String selection,String[] args){entries.get(Long.parseLong(uri.getLastPathSegment())).putAll(values);return 1;}
        public int delete(Uri uri,String selection,String[] args){long id=Long.parseLong(uri.getLastPathSegment());entries.remove(id);files.remove(id).delete();return 1;}
        public ParcelFileDescriptor openFile(Uri uri,String mode)throws java.io.FileNotFoundException{return ParcelFileDescriptor.open(files.get(Long.parseLong(uri.getLastPathSegment())),mode.contains("w")?ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_TRUNCATE:ParcelFileDescriptor.MODE_READ_ONLY);}
    }
    static void installProvider(){DownloadsProvider provider=new DownloadsProvider();android.content.pm.ProviderInfo info=new android.content.pm.ProviderInfo();info.authority="media";info.exported=true;provider.attachInfo(RuntimeEnvironment.getApplication(),info);ShadowContentResolver.registerProviderInternal("media",provider);}
    @Test public void migratedThemesRemainFilesAndRejectPathTraversal()throws Exception{
        installProvider();android.content.SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("migration-test",0);prefs.edit().clear().commit();ThemeConfig theme=ThemeConfig.defaults();theme.name="Legacy";theme.background=0xff123456;prefs.edit().putString("appearance.saved:Legacy",theme.toJson().toString()).commit();AppearanceStore store=new AppearanceStore(RuntimeEnvironment.getApplication(),prefs);
        assertTrue(store.names().contains("Legacy"));assertFalse(prefs.contains("appearance.saved:Legacy"));assertEquals(theme.background,store.load("Legacy").background);store.rename("Legacy","Renamed");assertNull(store.load("Legacy"));assertNotNull(store.load("Renamed"));store.delete("Renamed");assertTrue(store.names().isEmpty());
        try{store.save("../escape",theme);fail();}catch(IllegalArgumentException expected){}
    }
}
