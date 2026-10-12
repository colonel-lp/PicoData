package com.colonellp.ellamonitor;

import android.content.SharedPreferences;
import org.json.*;
import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** In-memory storage stand-ins exercise persistence and failure boundaries, not Android providers. */
public class PopupSettingsStoreTest {
    static class Cache implements SharedPreferences {
        final Map<String,Object> values=new HashMap<>();
        public Map<String,?> getAll(){return new HashMap<>(values);}
        public String getString(String k,String d){return (String)values.getOrDefault(k,d);}
        @SuppressWarnings("unchecked") public Set<String> getStringSet(String k,Set<String> d){return new HashSet<>((Set<String>)values.getOrDefault(k,d));}
        public int getInt(String k,int d){return (Integer)values.getOrDefault(k,d);}
        public long getLong(String k,long d){return (Long)values.getOrDefault(k,d);}
        public float getFloat(String k,float d){return (Float)values.getOrDefault(k,d);}
        public boolean getBoolean(String k,boolean d){return (Boolean)values.getOrDefault(k,d);}
        public boolean contains(String k){return values.containsKey(k);}
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){}
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){}
        public Editor edit(){return new Editor(){Map<String,Object> next=new HashMap<>(values);
            public Editor putString(String k,String v){next.put(k,v);return this;}
            public Editor putStringSet(String k,Set<String> v){next.put(k,new HashSet<>(v));return this;}
            public Editor putInt(String k,int v){next.put(k,v);return this;}
            public Editor putLong(String k,long v){next.put(k,v);return this;}
            public Editor putFloat(String k,float v){next.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){next.put(k,v);return this;}
            public Editor remove(String k){next.remove(k);return this;}
            public Editor clear(){next.clear();return this;}
            public boolean commit(){values.clear();values.putAll(next);return true;}
            public void apply(){commit();}
        };}
    }
    static class Storage implements PopupSettingsStore.Storage {
        byte[] bytes;int writes;boolean unavailable,failWrite;Runnable duringWrite;
        public InputStream read()throws Exception {if(unavailable)throw new IOException("Synthetic unavailable storage");return bytes==null?null:new ByteArrayInputStream(bytes);}
        public void write(byte[] value)throws Exception{writes++;if(failWrite)throw new IOException("Synthetic write failure");if(duringWrite!=null){Runnable run=duringWrite;duringWrite=null;run.run();}bytes=value.clone();}
    }
    PopupSelection selected(int period){PopupSelection s=new PopupSelection();s.period=period;s.fixed=period>0;s.bars=true;return s;}
    @Test public void eachPopupRoundTripsThroughOneGlobalFileOutsideViewerPreferences()throws Exception{
        Storage disk=new Storage();Cache cache=new Cache();PopupSettingsStore store=new PopupSettingsStore(disk,cache);store.put("battery",selected(2));store.put("load",selected(1));store.save();assertEquals(1,disk.writes);assertFalse(store.hasChanges());JSONObject file=SavedJson.read(new ByteArrayInputStream(disk.bytes));assertEquals(1,file.getInt("format"));assertEquals(2,file.getJSONObject("entries").length());PopupSettingsStore reopened=new PopupSettingsStore(disk,new Cache());assertEquals(2,reopened.get("battery").period);assertEquals(1,reopened.get("load").period);assertEquals(0,reopened.get("other").period);assertTrue(reopened.get("battery").fixed);assertEquals(new HashSet<>(Arrays.asList("state","pending")),cache.values.keySet());
    }
    @Test public void pendingCachedChangeSurvivesProcessRestartBeforeFileFlush()throws Exception{
        Storage disk=new Storage();Cache cache=new Cache();PopupSettingsStore store=new PopupSettingsStore(disk,cache);store.put("battery",selected(1));store.save();store.put("battery",selected(2));PopupSettingsStore restarted=new PopupSettingsStore(disk,cache);assertEquals(2,restarted.get("battery").period);assertTrue(restarted.hasChanges());restarted.save();assertEquals(2,new PopupSettingsStore(disk,new Cache()).get("battery").period);assertFalse(restarted.hasChanges());
    }
    @Test public void inaccessibleStorageKeepsPrivateStateAndRetriesWithoutResetting()throws Exception{
        Storage disk=new Storage();disk.unavailable=true;Cache cache=new Cache();PopupSettingsStore store=new PopupSettingsStore(disk,cache);store.put("battery",selected(3));try{store.save();fail();}catch(IOException expected){}assertEquals(3,store.get("battery").period);disk.unavailable=false;store.save();assertEquals(3,new PopupSettingsStore(disk,new Cache()).get("battery").period);
    }
    @Test public void malformedFileIsNeverOverwrittenByAutosave()throws Exception{
        Storage disk=new Storage();disk.bytes="{\"format\":1,\"entries\":{\"battery\":{\"period\":99}}}".getBytes(StandardCharsets.UTF_8);byte[] original=disk.bytes.clone();PopupSettingsStore store=new PopupSettingsStore(disk,new Cache());store.put("battery",selected(2));try{store.save();fail();}catch(IOException expected){}assertArrayEquals(original,disk.bytes);assertEquals(0,disk.writes);assertTrue(store.hasChanges());
    }
    @Test public void failedWriteKeepsPendingStateForRetry()throws Exception{
        Storage disk=new Storage();Cache cache=new Cache();PopupSettingsStore store=new PopupSettingsStore(disk,cache);store.put("battery",selected(1));store.save();byte[] original=disk.bytes.clone();store.put("battery",selected(2));disk.failWrite=true;try{store.save();fail();}catch(IOException expected){}assertArrayEquals(original,disk.bytes);assertTrue(store.hasChanges());disk.failWrite=false;store.save();assertEquals(2,new PopupSettingsStore(disk,new Cache()).get("battery").period);
    }
    @Test public void slowFileWriteDoesNotHoldUiLockOrDiscardAConcurrentChoice()throws Exception{
        Storage disk=new Storage();PopupSettingsStore store=new PopupSettingsStore(disk,new Cache());store.put("battery",selected(1));ExecutorService ui=Executors.newSingleThreadExecutor();try{disk.duringWrite=()->{try{ui.submit(()->{try{store.put("battery",selected(2));}catch(Exception e){throw new RuntimeException(e);}}).get(1,TimeUnit.SECONDS);}catch(Exception e){throw new AssertionError("Storage write blocked the selection thread",e);}};store.save();assertTrue(store.hasChanges());assertEquals(2,store.get("battery").period);store.save();assertFalse(store.hasChanges());assertEquals(2,new PopupSettingsStore(disk,new Cache()).get("battery").period);}finally{ui.shutdownNow();}
    }
    @Test public void limitsRejectInvalidIdentitiesAndOversizedStateBeforeReplacingIt()throws Exception{
        Storage disk=new Storage();PopupSettingsStore store=new PopupSettingsStore(disk,new Cache());store.put("battery",selected(1));for(String id:new String[]{"","a\n",String.join("",Collections.nCopies(1025,"x"))})try{store.put(id,selected(2));fail();}catch(IllegalArgumentException expected){}boolean rejected=false;for(int i=0;i<128;i++){String key=i+String.join("",Collections.nCopies(1000,"x"));try{store.put(key,selected(2));}catch(IOException expected){rejected=true;break;}}assertTrue(rejected);assertEquals(1,store.get("battery").period);store.save();assertTrue(disk.bytes.length<=SavedJson.MAX_BYTES);
    }
}
