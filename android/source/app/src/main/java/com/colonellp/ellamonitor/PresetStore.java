package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Named preset files; active app settings and the encryption key remain private. */
final class PresetStore {
    private final SharedPreferences prefs;
    private final DownloadsFiles files;
    private String problem;
    PresetStore(Context context,SharedPreferences prefs){this.prefs=prefs;files=new DownloadsFiles(context,"presets");}
    private JSONObject parse(android.net.Uri uri,String name) throws Exception {
        try(InputStream input=files.read(uri)){
            JSONObject saved=SavedJson.read(input);if(saved.has("name")&&!name.equals(saved.get("name")))throw SavedJson.invalid();
            return PresetState.normalize(saved);
        }
    }
    private void migrate(){
        for(String key:new ArrayList<>(prefs.getAll().keySet()))if(key.startsWith("viewer.preset:"))try{
            String old=SavedJson.name(key.substring(14)),name=old;JSONObject saved=PresetState.normalize(SavedJson.object(prefs.getString(key,"")));
            if(!saved.has("connection")){JSONObject connection=new JSONObject().put("address",prefs.getString("address",""));if(prefs.contains("token"))connection.put("token",prefs.getString("token","")).put("tokenIv",prefs.getString("tokenIv",""));saved.put("connection",connection);}
            Set<String> existing=files.files().keySet();int suffix=1;
            while(existing.contains(name+".json")){
                JSONObject other=null;try{other=parse(files.files().get(name+".json"),name);}catch(Exception ignored){}
                if(other!=null&&same(saved,other))break;
                String tail=" (migrated "+suffix++ + ")";name=old.substring(0,Math.min(old.length(),80-tail.length()))+tail;
            }
            if(!existing.contains(name+".json"))write(name,saved);
            SharedPreferences.Editor edit=prefs.edit().remove(key);
            if(old.equals(prefs.getString("currentPreset","")))edit.putString("currentPreset",name);
            if(!edit.commit())throw new Exception();
        }catch(Exception e){problem="Some presets could not be migrated; their private originals were retained.";}
    }
    private static boolean same(JSONObject a,JSONObject b){
        if(a.length()!=b.length())return false;for(Iterator<String> it=a.keys();it.hasNext();){String k=it.next();Object x=a.opt(k),y=b.opt(k);if(x instanceof JSONObject&&y instanceof JSONObject){if(!same((JSONObject)x,(JSONObject)y))return false;}else if(!Objects.equals(x,y))return false;}return true;
    }
    List<String> names(){
        migrate();Set<String> names=new LinkedHashSet<>();
        try{for(Map.Entry<String,android.net.Uri> entry:files.files().entrySet())if(entry.getKey().endsWith(".json"))try{String name=SavedJson.name(entry.getKey().substring(0,entry.getKey().length()-5));parse(entry.getValue(),name);names.add(name);}catch(Exception e){problem="Malformed preset files were ignored; current settings were kept.";}}
        catch(Exception e){problem="Preset Downloads files could not be read.";}
        for(String key:prefs.getAll().keySet())if(key.startsWith("viewer.preset:"))names.add(key.substring(14));
        List<String> result=new ArrayList<>(names);Collections.sort(result,String.CASE_INSENSITIVE_ORDER);return result;
    }
    JSONObject load(String name) throws Exception {
        migrate();String valid=SavedJson.name(name);android.net.Uri uri=files.files().get(valid+".json");
        if(uri!=null)return parse(uri,valid);
        if(prefs.contains("viewer.preset:"+valid))return PresetState.normalize(SavedJson.object(prefs.getString("viewer.preset:"+valid,"")));
        throw new IllegalArgumentException("Preset file is unavailable.");
    }
    boolean exists(String name) throws Exception {return files.files().containsKey(SavedJson.name(name)+".json")||prefs.contains("viewer.preset:"+name);}
    void save(String name,JSONObject saved) throws Exception {migrate();write(name,saved);prefs.edit().remove("viewer.preset:"+name).apply();}
    private void write(String name,JSONObject saved) throws Exception {
        name=SavedJson.name(name);JSONObject normalized=PresetState.normalize(saved);normalized.put("presetFormat",1).put("name",name);
        byte[] bytes=normalized.toString(2).getBytes(StandardCharsets.UTF_8);if(bytes.length>SavedJson.MAX_BYTES)throw new IllegalArgumentException("Preset exceeds 64 KB.");
        files.write(name+".json","application/json",bytes);
    }
    void delete(String name) throws Exception {migrate();android.net.Uri uri=files.files().get(SavedJson.name(name)+".json");if(uri!=null)files.delete(uri);prefs.edit().remove("viewer.preset:"+name).apply();}
    void rename(String old,String name) throws Exception {name=SavedJson.name(name);if(exists(name))throw new IllegalArgumentException("That name already exists.");JSONObject saved=load(old);save(name,saved);delete(old);}
    String takeProblem(){String p=problem;problem=null;return p;}
}
