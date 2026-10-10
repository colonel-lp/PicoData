package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** EQ file-based named themes; active/unsaved edits remain private preferences. */
final class AppearanceStore {
    static final String FOLDER = "Download/ella-monitoring/themes/";
    private final Context context;
    final SharedPreferences prefs;
    AppearanceStore(Context context, SharedPreferences prefs) { this.context=context;this.prefs=prefs; }
    ThemeConfig active() { if(!prefs.contains("appearance.active"))return ThemeConfig.defaults();try { return ThemeConfig.fromJson(new JSONObject(prefs.getString("appearance.active", "{}"))); } catch(Exception e) { return ThemeConfig.defaults(); } }
    void active(ThemeConfig t) { try { prefs.edit().putString("appearance.active",t.toJson().toString()).apply(); } catch(Exception ignored) {} }
    private File directory() { return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"ella-monitoring/themes"); }
    private String fileName(String name) throws Exception {
        if(name==null || name.trim().isEmpty() || name.length()>80 || name.matches(".*[\\\\/:*?\"<>|].*") || name.endsWith(".") || name.equals("Default")) throw new IllegalArgumentException("Use a theme name of 1–80 characters without file separators.");
        return name.trim()+".json";
    }
    private Map<String,Uri> files() {
        Map<String,Uri> out=new LinkedHashMap<>();
        if(Build.VERSION.SDK_INT>=29) {
            String[] columns={MediaStore.Downloads._ID,MediaStore.Downloads.DISPLAY_NAME};
            try(Cursor c=context.getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,columns,MediaStore.Downloads.RELATIVE_PATH+" = ?",new String[]{FOLDER},null)) {
                if(c!=null)while(c.moveToNext()) if(c.getString(1).endsWith(".json"))out.put(c.getString(1),Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI,Long.toString(c.getLong(0))));
            } catch(RuntimeException ignored) {}
        } else {
            File[] found=directory().listFiles((d,n)->n.endsWith(".json"));
            if(found!=null)for(File file:found)out.put(file.getName(),Uri.fromFile(file));
        }
        return out;
    }
    private InputStream read(Uri uri) throws Exception { return "file".equals(uri.getScheme())?new FileInputStream(new File(uri.getPath())):context.getContentResolver().openInputStream(uri); }
    private ThemeConfig parse(Uri uri) {
        try(InputStream input=read(uri)) {
            if(input==null)return null;ByteArrayOutputStream output=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;
            while((n=input.read(buffer))!=-1) { if(output.size()+n>65536)return null;output.write(buffer,0,n); }
            return ThemeConfig.fromJson(new JSONObject(output.toString("UTF-8")));
        } catch(Exception e) { return null; }
    }
    private void migrate() {
        for(String key:prefs.getAll().keySet())if(key.startsWith("appearance.saved:"))try {
            String name=key.substring(17);
            if(!files().containsKey(fileName(name)))write(name,ThemeConfig.fromJson(new JSONObject(prefs.getString(key,"{}"))));
            prefs.edit().remove(key).apply();
        } catch(Exception ignored) { /* Keep the preference until its file is written successfully. */ }
    }
    List<String> names() {
        migrate();Set<String> names=new LinkedHashSet<>();
        for(Uri uri:files().values()) { ThemeConfig t=parse(uri);if(t!=null)names.add(t.name); }
        for(String key:prefs.getAll().keySet())if(key.startsWith("appearance.saved:"))names.add(key.substring(17));
        List<String> result=new ArrayList<>(names);Collections.sort(result,String.CASE_INSENSITIVE_ORDER);return result;
    }
    ThemeConfig load(String name) {
        migrate();for(Uri uri:files().values()) { ThemeConfig t=parse(uri);if(t!=null&&name.equals(t.name))return t; }
        try { return ThemeConfig.fromJson(new JSONObject(prefs.getString("appearance.saved:"+name,""))); } catch(Exception e) { return null; }
    }
    void save(String name,ThemeConfig t) throws Exception { migrate();write(name,t); }
    private void write(String name,ThemeConfig t) throws Exception {
        String filename=fileName(name);ThemeConfig copy=t.copy();copy.name=name.trim();byte[] bytes=copy.toJson().toString(2).getBytes(StandardCharsets.UTF_8);
        if(Build.VERSION.SDK_INT>=29) {
            Uri uri=files().get(filename);boolean created=uri==null;
            if(created) { ContentValues values=new ContentValues();values.put(MediaStore.Downloads.DISPLAY_NAME,filename);values.put(MediaStore.Downloads.MIME_TYPE,"application/json");values.put(MediaStore.Downloads.RELATIVE_PATH,FOLDER);values.put(MediaStore.Downloads.IS_PENDING,1);uri=context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values); }
            if(uri==null)throw new IOException("Could not create the theme file.");
            try(OutputStream out=context.getContentResolver().openOutputStream(uri,"wt")) { if(out==null)throw new IOException("Could not write the theme file.");out.write(bytes); }
            catch(Exception e) { if(created)context.getContentResolver().delete(uri,null,null);throw e; }
            if(created) { ContentValues done=new ContentValues();done.put(MediaStore.Downloads.IS_PENDING,0);context.getContentResolver().update(uri,done,null,null); }
        } else {
            File dir=directory();if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Could not create Downloads/ella-monitoring/themes.");
            File file=new File(dir,filename),temp=new File(dir,filename+".part");
            try(FileOutputStream out=new FileOutputStream(temp)) { out.write(bytes);out.getFD().sync(); }
            if(!temp.renameTo(file)) { temp.delete();throw new IOException("Could not save the theme file."); }
        }
    }
    void delete(String name) throws Exception {
        migrate();for(Uri uri:files().values()) { ThemeConfig t=parse(uri);if(t!=null&&name.equals(t.name)) {
            if("file".equals(uri.getScheme())) { if(!new File(uri.getPath()).delete())throw new IOException("Could not delete theme."); }
            else if(context.getContentResolver().delete(uri,null,null)==0)throw new IOException("Could not delete theme.");
        }}prefs.edit().remove("appearance.saved:"+name).apply();
    }
    void rename(String old,String name) throws Exception { if(names().contains(name.trim()))throw new IllegalArgumentException("That name already exists.");ThemeConfig t=load(old);if(t==null)throw new IOException("Theme unavailable.");save(name,t);delete(old); }
}
