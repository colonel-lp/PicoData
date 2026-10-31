package com.colonellp.ellamonitor;

import org.json.JSONObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Editable viewer settings only. Colours and live/display fallback values are excluded. */
final class PresetState {
    static JSONObject defaults() throws Exception {
        return new JSONObject().put("localClock",false).put("hideStatus",false).put("range",0)
                .put("defined",false).put("metric","").put("quantity","watts").put("bars",false)
                .put("persistent",false).put("updateIntervalHours",24).put("fontFamily",ThemeConfig.defaults().fontFamily);
    }
    static JSONObject normalize(JSONObject input) throws Exception {
        validate(input);
        JSONObject out=defaults();
        for(Iterator<String> keys=out.keys();keys.hasNext();){String key=keys.next();if(input.has(key))out.put(key,input.get(key));}
        if(!input.has("fontFamily")&&input.optJSONObject("theme")!=null)
            out.put("fontFamily",input.getJSONObject("theme").optString("fontFamily",out.getString("fontFamily")));
        for(Iterator<String> keys=input.keys();keys.hasNext();){String key=keys.next();if(key.startsWith("label:")||key.startsWith("elementHidden:")||key.startsWith("elementHighlight:"))out.put(key,input.get(key));
            if(key.startsWith("gaugeHighlight:")&&input.optBoolean(key,false)&&!input.has("elementHighlight:"+key.substring(15)))out.put("elementHighlight:"+key.substring(15),1);}
        if(input.optJSONObject("connection")!=null)out.put("connection",input.getJSONObject("connection"));
        return out;
    }
    private static void validate(JSONObject input) throws Exception {
        if(input.length()==0)throw SavedJson.invalid();
        if(input.has("presetFormat"))SavedJson.integer(input,"presetFormat",1,1);
        if(input.has("name")){SavedJson.string(input,"name",1,80);SavedJson.name(input.getString("name"));}
        boolean recognized=false;
        for(Iterator<String> keys=input.keys();keys.hasNext();){String key=keys.next();
            if(key.equals("localClock")||key.equals("hideStatus")||key.equals("defined")||key.equals("bars")||key.equals("persistent")||key.equals("keepScreen")||key.equals("fullscreen")||key.equals("locked")){SavedJson.bool(input,key);recognized=true;}
            else if(key.equals("range")){SavedJson.integer(input,key,0,4);recognized=true;}
            else if(key.equals("updateIntervalHours")){long hours=SavedJson.integer(input,key,0,24);if(hours!=0&&hours!=1&&hours!=3&&hours!=6&&hours!=12&&hours!=24)throw SavedJson.invalid();recognized=true;}
            else if(key.equals("fontFamily")){font(input,key);recognized=true;}
            else if(key.equals("metric")){SavedJson.string(input,key,0,512);recognized=true;}
            else if(key.equals("quantity")){SavedJson.string(input,key,1,32);if(!java.util.Arrays.asList("watts","current","voltage","stateOfCharge","Wh","Ah","value","min","max").contains(input.getString(key)))throw SavedJson.invalid();recognized=true;}
            else if(key.startsWith("label:")){elementKey(key,6);SavedJson.string(input,key,1,80);recognized=true;}
            else if(key.startsWith("elementHidden:")){elementKey(key,14);SavedJson.bool(input,key);recognized=true;}
            else if(key.startsWith("elementHighlight:")){elementKey(key,17);SavedJson.integer(input,key,0,2);recognized=true;}
            else if(key.startsWith("gaugeHighlight:")){elementKey(key,15);SavedJson.bool(input,key);recognized=true;}
            else if(key.equals("theme")){if(!(input.get(key) instanceof JSONObject))throw SavedJson.invalid();JSONObject legacy=input.getJSONObject(key);if(legacy.has("fontFamily"))font(legacy,"fontFamily");recognized=true;}
            else if(key.equals("connection")){connection(input.get(key));recognized=true;}
            else if(input.has("presetFormat")&&!key.equals("name")&&!key.equals("presetFormat"))throw SavedJson.invalid(); // Older snapshots carried irrelevant settings; discard those after checking known fields.
        }
        if(!recognized)throw SavedJson.invalid();
    }
    private static void elementKey(String key,int prefix){if(key.length()<=prefix||key.length()>prefix+512)throw SavedJson.invalid();}
    private static void font(JSONObject input,String key) throws Exception {
        SavedJson.string(input,key,1,40);if(!java.util.Arrays.asList("sans-serif","sans-serif-condensed","sans-serif-medium","sans-serif-light","monospace").contains(input.getString(key)))throw SavedJson.invalid();
    }
    private static void connection(Object value) throws Exception {
        if(!(value instanceof JSONObject))throw SavedJson.invalid();JSONObject c=(JSONObject)value;
        SavedJson.string(c,"address",0,2048);if(!c.getString("address").isEmpty())ApiClient.validateBase(c.getString("address"));
        for(Iterator<String> keys=c.keys();keys.hasNext();)if(!java.util.Arrays.asList("address","token","tokenIv","networks").contains(keys.next()))throw SavedJson.invalid();
        if(c.has("networks"))PiNetworkConfig.validate(c.get("networks"));
        if(c.has("token")!=c.has("tokenIv"))throw SavedJson.invalid();
        if(c.has("token")){
            for(String key:new String[]{"token","tokenIv"}){SavedJson.string(c,key,1,512);if(!c.getString(key).matches("[A-Za-z0-9+/]+={0,2}"))throw SavedJson.invalid();}
            if(android.util.Base64.decode(c.getString("token"),android.util.Base64.NO_WRAP).length!=80||android.util.Base64.decode(c.getString("tokenIv"),android.util.Base64.NO_WRAP).length!=12)throw SavedJson.invalid();
        }
    }
    static Set<String> differences(JSONObject current,JSONObject saved) throws Exception {
        Set<String> keys=new LinkedHashSet<>(),changed=new LinkedHashSet<>();
        current.keys().forEachRemaining(keys::add);saved.keys().forEachRemaining(keys::add);
        for(String key:keys){
            if(key.equals("connection"))continue; // Compare decrypted secrets in PrivateSettings, never cipher nonce identity.
            Object a=current.opt(key),b=saved.opt(key);
            if(key.startsWith("elementHidden:")){a=current.optBoolean(key,false);b=saved.optBoolean(key,false);}
            if(key.startsWith("elementHighlight:")){a=current.optInt(key,0);b=saved.optInt(key,0);}
            if(a==null?b!=null:!a.equals(b))changed.add(key);
        }
        return changed;
    }
}
