package com.colonellp.ellamonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class PrivateSettings {
    final SharedPreferences prefs;
    interface KeySource { SecretKey get() throws Exception; }
    private final KeySource keySource;
    private static final String KEY = "ella-monitor-api-token";
    PrivateSettings(Context context) { this(context,null); }
    PrivateSettings(Context context,KeySource source) { prefs = context.getSharedPreferences("viewer", Context.MODE_PRIVATE);keySource=source; }
    private SecretKey key() throws Exception {
        if(keySource!=null)return keySource.get();
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        if (store.containsAlias(KEY)) return (SecretKey)store.getKey(KEY, null);
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());
        return generator.generateKey();
    }
    void saveConnection(String address, String token) throws Exception {
        address = ApiClient.validateBase(address);
        if (!token.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Enter the 64-character API token.");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key());
        String encrypted = Base64.encodeToString(cipher.doFinal(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)), Base64.NO_WRAP);
        String iv = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP);
        if (!prefs.edit().putString("address", address).putString("token", encrypted).putString("tokenIv", iv).commit()) throw new Exception("Cannot save connection.");
    }
    org.json.JSONObject connectionSnapshot() throws Exception {
        org.json.JSONObject out=new org.json.JSONObject().put("address",prefs.getString("address",""));
        if(prefs.contains("token"))out.put("token",prefs.getString("token","")).put("tokenIv",prefs.getString("tokenIv",""));
        return out;
    }
    private String decrypt(org.json.JSONObject snapshot) throws Exception {
        if(!snapshot.has("token"))return "";
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(snapshot.getString("tokenIv"),Base64.NO_WRAP)));
        return new String(cipher.doFinal(Base64.decode(snapshot.getString("token"),Base64.NO_WRAP)),java.nio.charset.StandardCharsets.UTF_8);
    }
    String token() throws Exception { return decrypt(connectionSnapshot()); }
    java.util.Set<String> connectionDifferences(org.json.JSONObject saved) throws Exception {
        java.util.Set<String> out=new java.util.LinkedHashSet<>();
        org.json.JSONObject current=connectionSnapshot();
        if(!current.optString("address").equals(saved.optString("address")))out.add("connection.address");
        if(!java.security.MessageDigest.isEqual(decrypt(current).getBytes(java.nio.charset.StandardCharsets.UTF_8),decrypt(saved).getBytes(java.nio.charset.StandardCharsets.UTF_8)))out.add("connection.token");
        return out;
    }
    void stageConnection(SharedPreferences.Editor edit,org.json.JSONObject saved) throws Exception {
        String address=saved.optString("address","");
        if(!address.isEmpty())address=ApiClient.validateBase(address);
        if(saved.has("token")){
            String value=decrypt(saved);
            if(address.isEmpty()||!value.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Saved connection is invalid.");
            edit.putString("token",saved.getString("token")).putString("tokenIv",saved.getString("tokenIv"));
        }else edit.remove("token").remove("tokenIv");
        edit.putString("address",address);
    }
    String label(String id, String original) { return prefs.getString("label:" + id, original); }
    void setLabel(String id, String value) { prefs.edit().putString("label:" + id, value).apply(); }
    void resetLabel(String id) { prefs.edit().remove("label:" + id).apply(); }
    int highlight(String id) { return Math.max(0,Math.min(2,prefs.getInt("elementHighlight:"+id,prefs.getBoolean("gaugeHighlight:"+id,false)?1:0))); }
    boolean hidden(String id) { return prefs.getBoolean("elementHidden:"+id,false); }
    void setHighlight(String id,int choice) {
        SharedPreferences.Editor edit=prefs.edit().remove("gaugeHighlight:"+id);
        if(choice==0)edit.remove("elementHighlight:"+id);else edit.putInt("elementHighlight:"+id,Math.max(1,Math.min(2,choice)));
        edit.apply();
    }
}
