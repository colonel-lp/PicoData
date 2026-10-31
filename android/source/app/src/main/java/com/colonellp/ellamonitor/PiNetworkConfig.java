package com.colonellp.ellamonitor;

import java.net.URI;
import org.json.JSONArray;
import org.json.JSONObject;

/** Non-secret, preset-owned network names/endpoints. Never embeds installation defaults. */
final class PiNetworkConfig {
    static final String[] ROLES={"Hotspot","Home","Mobile"};
    static String host(String value) {
        String v=value.trim();
        if(v.isEmpty())return "";
        try {
            URI u=new URI("ssh://"+(v.contains(":")&&!v.startsWith("[")?"["+v+"]":v));
            if(u.getHost()==null||u.getPort()!=-1||u.getUserInfo()!=null||!u.getPath().isEmpty()||u.getQuery()!=null||u.getFragment()!=null
                    ||v.length()>253||v.matches(".*[\\s/\\\\@?#].*"))throw new Exception();
            return u.getHost().replace("[","").replace("]","");
        }catch(Exception e){throw new IllegalArgumentException("Enter a Pi IP address or hostname, without a scheme, port or path.");}
    }
    static String ssid(String value) {
        if(value.indexOf('\n')>=0||value.indexOf('\r')>=0||value.indexOf('\0')>=0||value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>32)
            throw new IllegalArgumentException("Use a Wi-Fi name of at most 32 UTF-8 bytes.");
        return value;
    }
    static JSONArray validate(Object value) throws Exception {
        if(!(value instanceof JSONArray)||((JSONArray)value).length()!=3)throw new IllegalArgumentException("Invalid network settings.");
        JSONArray a=(JSONArray)value, result=new JSONArray();java.util.Set<String> names=new java.util.HashSet<>();
        for(int i=0;i<3;i++){
            JSONObject o=a.getJSONObject(i);if(o.length()!=2||!(o.get("ssid") instanceof String)||!(o.get("host") instanceof String))throw new IllegalArgumentException("Invalid network settings.");
            String name=ssid(o.getString("ssid")),address=host(o.getString("host"));
            if(!name.isEmpty()&&!names.add(name))throw new IllegalArgumentException("Each network must have a different Wi-Fi name.");
            result.put(new JSONObject().put("ssid",name).put("host",address));
        }
        return result;
    }
    static JSONArray empty() throws Exception {JSONArray a=new JSONArray();for(int i=0;i<3;i++)a.put(new JSONObject().put("ssid","").put("host",""));return a;}
    static String replaceApiHost(String base,String host){
        try{URI u=new URI(ApiClient.validateBase(base));return ApiClient.validateBase(new URI(u.getScheme(),null,host,u.getPort(),null,null,null).toString());}
        catch(Exception e){throw new IllegalArgumentException("Save a valid API connection before selecting a network.");}
    }
}
