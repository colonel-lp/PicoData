package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** EQ file-based named themes; active/unsaved edits remain private preferences. */
final class AppearanceStore {
    static final String FOLDER="Download/ella-monitoring/themes/";
    final SharedPreferences prefs;
    private final DownloadsFiles files;
    private String problem;
    private final Map<String,String> migratedNames=new LinkedHashMap<>();
    AppearanceStore(Context context,SharedPreferences prefs){this.prefs=prefs;files=new DownloadsFiles(context,"themes");}
    ThemeConfig active(){if(!prefs.contains("appearance.active"))return ThemeConfig.defaults();try{return ThemeConfig.fromJson(SavedJson.object(prefs.getString("appearance.active","{}")));}catch(Exception e){return ThemeConfig.defaults();}}
    void active(ThemeConfig t){try{prefs.edit().putString("appearance.active",t.toJson().toString()).apply();}catch(Exception ignored){}}
    private ThemeConfig parse(Uri uri,String name) throws Exception {
        try(InputStream input=files.read(uri)){JSONObject json=SavedJson.read(input);if(json.has("name")&&!name.equals(json.get("name")))throw SavedJson.invalid();ThemeConfig result=ThemeConfig.validatedSaved(json);result.name=name;return result;}
    }
    private void migrate(){
        for(String key:new ArrayList<>(prefs.getAll().keySet()))if(key.startsWith("appearance.saved:"))try{
            String old=SavedJson.name(key.substring(17)),name=old;ThemeConfig saved=ThemeConfig.validatedSaved(SavedJson.object(prefs.getString(key,"")));
            Set<String> existing=files.files().keySet();int suffix=1;
            while(existing.contains(name+".json")){
                ThemeConfig other=null;try{other=parse(files.files().get(name+".json"),name);}catch(Exception ignored){}
                if(saved.sameColours(other))break;
                String tail=" (migrated "+suffix++ + ")";name=old.substring(0,Math.min(old.length(),80-tail.length()))+tail;
            }
            if(!existing.contains(name+".json"))write(name,saved);
            SharedPreferences.Editor edit=prefs.edit().remove(key);ThemeConfig active=active();if(active.name.equals(old)&&!name.equals(old)){active.name=name;edit.putString("appearance.active",active.toJson().toString());}if(!edit.commit())throw new IOException("Could not finish theme migration.");if(!old.equals(name))migratedNames.put(old,name);
        }catch(Exception e){problem="Some themes could not be migrated; their private originals were retained.";}
    }
    List<String> names(){
        migrate();Set<String> names=new LinkedHashSet<>();
        try{for(Map.Entry<String,Uri> entry:files.files().entrySet())if(entry.getKey().endsWith(".json"))try{String name=SavedJson.name(entry.getKey().substring(0,entry.getKey().length()-5));parse(entry.getValue(),name);names.add(name);}catch(Exception e){problem="Malformed theme files were ignored; current colours were kept.";}}
        catch(Exception e){problem="Theme Downloads files could not be read.";}
        for(String key:prefs.getAll().keySet())if(key.startsWith("appearance.saved:"))names.add(key.substring(17));
        List<String> result=new ArrayList<>(names);Collections.sort(result,String.CASE_INSENSITIVE_ORDER);return result;
    }
    ThemeConfig load(String name){
        migrate();try{String valid=SavedJson.name(name);Uri uri=files.files().get(valid+".json");if(uri!=null)return parse(uri,valid);return ThemeConfig.validatedSaved(SavedJson.object(prefs.getString("appearance.saved:"+valid,"")));}
        catch(Exception e){problem="Theme file is unavailable or malformed; current colours were kept.";return null;}
    }
    boolean exists(String name) throws Exception {return files.files().containsKey(SavedJson.name(name)+".json")||prefs.contains("appearance.saved:"+name);}
    void save(String name,ThemeConfig t) throws Exception {migrate();write(name,t);}
    private void write(String name,ThemeConfig t) throws Exception {
        name=SavedJson.name(name);ThemeConfig copy=t.copy();copy.name=name;JSONObject json=copy.toJson();ThemeConfig.validatedSaved(json);
        byte[] bytes=json.toString(2).getBytes(StandardCharsets.UTF_8);if(bytes.length>SavedJson.MAX_BYTES)throw new IllegalArgumentException("Theme exceeds 64 KB.");files.write(name+".json","application/json",bytes);
    }
    void delete(String name) throws Exception {migrate();Uri uri=files.files().get(SavedJson.name(name)+".json");if(uri!=null)files.delete(uri);prefs.edit().remove("appearance.saved:"+name).apply();}
    void rename(String old,String name) throws Exception {name=SavedJson.name(name);if(exists(name))throw new IllegalArgumentException("That name already exists.");ThemeConfig t=load(old);if(t==null)throw new IOException("Theme unavailable or malformed.");save(name,t);delete(old);}
    String takeProblem(){String p=problem;problem=null;return p;}
    String migratedName(String name){return migratedNames.containsKey(name)?migratedNames.get(name):name;}
}
