package com.colonellp.ellamonitor;

import android.content.*;
import android.net.Uri;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One global Downloads file, with a separate private cache for unavailable storage. */
final class PopupSettingsStore {
    static final String FILE="display-settings.json";
    interface Storage {InputStream read()throws Exception;void write(byte[] bytes)throws Exception;}
    private final Storage files;
    private static Storage downloads(Context context){
        DownloadsFiles downloads=new DownloadsFiles(context,"");return new Storage(){
            public InputStream read()throws Exception {Uri uri=downloads.files().get(FILE);return uri==null?null:downloads.read(uri);}
            public void write(byte[] bytes)throws Exception {downloads.write(FILE,"application/json",bytes);}
        };
    }
    private final SharedPreferences cache;
    private JSONObject entries=new JSONObject();
    private final Set<String> dirty=new LinkedHashSet<>();
    private boolean loaded,blocked;
    PopupSettingsStore(Context context){this(downloads(context),context.getSharedPreferences("chart-display",0));}
    PopupSettingsStore(Storage storage,SharedPreferences preferences){
        files=storage;cache=preferences;
        try{String saved=cache.getString("state",null);if(saved!=null)entries=validate(SavedJson.object(saved));}catch(Exception ignored){}
        for(String id:cache.getStringSet("pending",Collections.emptySet()))if(entries.has(id))dirty.add(id);
        try{load();}catch(Exception ignored){}
    }
    static JSONObject validate(JSONObject root)throws Exception {
        if(root.length()!=2||!root.has("format")||!root.has("entries"))throw SavedJson.invalid();
        SavedJson.integer(root,"format",1,1);JSONObject values=root.getJSONObject("entries");if(values.length()>128)throw SavedJson.invalid();
        JSONObject result=new JSONObject();for(Iterator<String> i=values.keys();i.hasNext();){String key=i.next();validKey(key);result.put(key,PopupSelection.parse(values.getJSONObject(key)).json());}return result;
    }
    private static void validKey(String id){if(id==null||id.isEmpty()||id.length()>1024)throw SavedJson.invalid();for(int i=0;i<id.length();i++)if(Character.isISOControl(id.charAt(i)))throw SavedJson.invalid();}
    private JSONObject document()throws Exception {return new JSONObject().put("format",1).put("entries",entries);}
    private void cache()throws Exception {cache.edit().putString("state",document().toString()).putStringSet("pending",new LinkedHashSet<>(dirty)).apply();}
    // Constructor or the single storage worker only. File IO never holds the UI's state lock.
    private void load()throws Exception {
        synchronized(this){if(loaded)return;if(blocked)throw new IOException("The display-settings file is malformed; it was not replaced.");}
        JSONObject disk=null;
        try(InputStream in=files.read()){if(in!=null)disk=validate(SavedJson.read(in));}catch(Exception e){if(e instanceof IllegalArgumentException||e instanceof JSONException||e instanceof java.nio.charset.CharacterCodingException)synchronized(this){blocked=true;}throw e;}
        synchronized(this){
            if(disk!=null){for(String key:dirty)disk.put(key,entries.getJSONObject(key));entries=disk;}
            loaded=true;cache();
        }
    }
    synchronized PopupSelection get(String id){try{return PopupSelection.parse(entries.optJSONObject(id)==null?new JSONObject():entries.getJSONObject(id));}catch(Exception e){return new PopupSelection();}}
    synchronized boolean hasChanges(){return !dirty.isEmpty();}
    synchronized void put(String id,PopupSelection selection)throws Exception {
        validKey(id);JSONObject copy=selection.json();PopupSelection.parse(copy);if(!entries.has(id)&&entries.length()>=128)throw new IOException("Too many saved chart selections.");
        JSONObject next=new JSONObject(entries.toString());next.put(id,copy);
        if(new JSONObject().put("format",1).put("entries",next).toString(2).getBytes(StandardCharsets.UTF_8).length>SavedJson.MAX_BYTES)throw new IOException("Display settings exceed 64 KB.");
        entries=next;dirty.add(id);cache();
    }
    void save()throws Exception {
        synchronized(this){if(dirty.isEmpty())return;}
        load();byte[] bytes;Map<String,String> written=new LinkedHashMap<>();
        synchronized(this){
            bytes=document().toString(2).getBytes(StandardCharsets.UTF_8);if(bytes.length>SavedJson.MAX_BYTES)throw new IOException("Display settings exceed 64 KB.");
            for(String id:dirty)written.put(id,entries.getJSONObject(id).toString());
        }
        files.write(bytes);
        synchronized(this){for(Map.Entry<String,String> e:written.entrySet())if(e.getValue().equals(entries.getJSONObject(e.getKey()).toString()))dirty.remove(e.getKey());cache();}
    }
}
