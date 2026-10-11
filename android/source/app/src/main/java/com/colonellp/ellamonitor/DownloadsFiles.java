package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** The existing theme Downloads paths, shared by presets and picker-free exports. */
final class DownloadsFiles {
    final Context context;
    final String folder;
    DownloadsFiles(Context context,String folder){this.context=context;this.folder=folder;}
    private File directory(){return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"ella-monitoring/"+folder);}
    private String relative(){return "Download/ella-monitoring/"+folder+"/";}
    Map<String,Uri> files() throws Exception {
        Map<String,Uri> result=new LinkedHashMap<>();
        if(Build.VERSION.SDK_INT>=29){
            String[] columns={MediaStore.Downloads._ID,MediaStore.Downloads.DISPLAY_NAME};
            try(Cursor c=context.getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,columns,MediaStore.Downloads.RELATIVE_PATH+" = ?",new String[]{relative()},null)){
                if(c!=null)while(c.moveToNext()){
                    String name=c.getString(1);if(result.containsKey(name))throw new IOException("Duplicate saved filenames; rename the duplicate before saving.");
                    result.put(name,Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI,Long.toString(c.getLong(0))));
                }
            }
        }else{File[] entries=directory().listFiles();if(entries!=null)for(File f:entries)if(f.isFile()&&f.getCanonicalFile().getParentFile().equals(directory().getCanonicalFile()))result.put(f.getName(),Uri.fromFile(f));}
        return result;
    }
    InputStream read(Uri uri) throws Exception {return "file".equals(uri.getScheme())?new FileInputStream(new File(uri.getPath())):context.getContentResolver().openInputStream(uri);}
    void delete(Uri uri) throws Exception {
        if("file".equals(uri.getScheme())){if(!new File(uri.getPath()).delete())throw new IOException("Could not delete saved file.");}
        else if(context.getContentResolver().delete(uri,null,null)==0)throw new IOException("Could not delete saved file.");
    }
    private byte[] backup(Uri uri) throws Exception {
        try(InputStream input=read(uri)){
            if(input==null)throw new IOException("Could not read existing saved file.");
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;
            while((n=input.read(buffer))!=-1){if(out.size()+n>SavedJson.MAX_BYTES)throw new IOException("Existing saved file exceeds 64 KB; it was not replaced.");out.write(buffer,0,n);}return out.toByteArray();
        }
    }
    private void stream(Uri uri,byte[] bytes) throws Exception {try(OutputStream out=context.getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Could not write saved file.");out.write(bytes);}}
    void write(String filename,String mime,byte[] bytes) throws Exception {
        if(filename==null||filename.isEmpty()||filename.length()>160)throw SavedJson.invalid();
        for(int i=0;i<filename.length();i++)if(Character.isISOControl(filename.charAt(i))||"\\/:*?\"<>|".indexOf(filename.charAt(i))>=0)throw SavedJson.invalid();
        if(Build.VERSION.SDK_INT>=29){
            Uri uri=files().get(filename);boolean created=uri==null;byte[] old=created?null:backup(uri);
            if(created){ContentValues values=new ContentValues();values.put(MediaStore.Downloads.DISPLAY_NAME,filename);values.put(MediaStore.Downloads.MIME_TYPE,mime);values.put(MediaStore.Downloads.RELATIVE_PATH,relative());values.put(MediaStore.Downloads.IS_PENDING,1);uri=context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);}
            if(uri==null)throw new IOException("Could not create saved file.");
            try{stream(uri,bytes);if(created){ContentValues done=new ContentValues();done.put(MediaStore.Downloads.IS_PENDING,0);if(context.getContentResolver().update(uri,done,null,null)==0)throw new IOException("Could not finish saved file.");}}
            catch(Exception e){try{if(created)delete(uri);else stream(uri,old);}catch(Exception restore){throw new IOException("Save failed and the previous file could not be restored.",restore);}throw e;}
        }else{
            File dir=directory();if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Could not create the Downloads folder.");
            File target=new File(dir,filename);if(!target.getCanonicalFile().getParentFile().equals(dir.getCanonicalFile()))throw new IOException("Invalid saved-file path.");
            File temp=File.createTempFile("ella-save-",".part",dir);
            try{try(FileOutputStream out=new FileOutputStream(temp)){out.write(bytes);out.getFD().sync();}if(!temp.renameTo(target))throw new IOException("Could not save file.");}finally{if(temp.exists())temp.delete();}
        }
    }
}
