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
                .put("persistent",false).put("keepScreen",false).put("fullscreen",false)
                .put("locked",false).put("updateIntervalHours",24).put("fontFamily",ThemeConfig.defaults().fontFamily);
    }
    static JSONObject normalize(JSONObject input) throws Exception {
        JSONObject out=defaults();
        for(Iterator<String> keys=out.keys();keys.hasNext();){String key=keys.next();if(input.has(key))out.put(key,input.get(key));}
        if(!input.has("fontFamily")&&input.optJSONObject("theme")!=null)
            out.put("fontFamily",input.getJSONObject("theme").optString("fontFamily",out.getString("fontFamily")));
        for(Iterator<String> keys=input.keys();keys.hasNext();){String key=keys.next();if(key.startsWith("label:")||key.startsWith("gaugeHighlight:"))out.put(key,input.get(key));}
        if(input.optJSONObject("connection")!=null)out.put("connection",input.getJSONObject("connection"));
        return out;
    }
    static Set<String> differences(JSONObject current,JSONObject saved) throws Exception {
        Set<String> keys=new LinkedHashSet<>(),changed=new LinkedHashSet<>();
        current.keys().forEachRemaining(keys::add);saved.keys().forEachRemaining(keys::add);
        for(String key:keys){
            if(key.equals("connection"))continue; // Compare decrypted secrets in PrivateSettings, never cipher nonce identity.
            Object a=current.opt(key),b=saved.opt(key);
            if(key.startsWith("gaugeHighlight:")){a=current.optBoolean(key,false);b=saved.optBoolean(key,false);}
            if(a==null?b!=null:!a.equals(b))changed.add(key);
        }
        return changed;
    }
}
