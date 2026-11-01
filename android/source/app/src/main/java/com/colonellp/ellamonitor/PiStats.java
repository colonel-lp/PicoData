package com.colonellp.ellamonitor;

import org.json.JSONObject;
import java.util.Locale;

/** Read-only live system sample from the authenticated collector API. */
final class PiStats {
    final JSONObject pi;final long at;final String endpoint;
    PiStats(JSONObject response,String endpoint,long at){
        if(response.optInt("apiVersion")!=1||!(response.opt("pi") instanceof JSONObject))throw new IllegalArgumentException("Unexpected Pi response.");
        pi=response.optJSONObject("pi");this.endpoint=endpoint;
        double age=number(pi,"ageSeconds");if(!Double.isFinite(age)||age<0||age>45)throw new IllegalArgumentException("Pi sample is stale.");
        this.at=at-Math.round(age*1000);
    }
    private JSONObject object(String key){JSONObject o=pi.optJSONObject(key);return o==null?new JSONObject():o;}
    private static double number(JSONObject o,String key){Object n=o.opt(key);return n instanceof Number?((Number)n).doubleValue():Double.NaN;}
    String get(String key){JSONObject o=key.equals("mode")||key.equals("controlState")?object("wifi"):object("network");Object v=o.opt(key.equals("ip")?"address":key);return v instanceof String&&!((String)v).isEmpty()?(String)v:"—";}
    boolean controlEnabled(){return object("wifi").optBoolean("controlEnabled",false);}
    String memory(){JSONObject m=object("ram");double total=number(m,"totalBytes"),available=number(m,"availableBytes");return Double.isFinite(total)&&Double.isFinite(available)&&total>0&&available>=0&&available<=total?String.format(Locale.UK,"%.0f / %.0f MiB used (%.0f MiB available)",(total-available)/1048576d,total/1048576d,available/1048576d):"—";}
    String temperature(){double c=number(pi,"temperatureC");return Double.isFinite(c)&&c>=-50&&c<=150?String.format(Locale.UK,"%.1f °C",c):"—";}
    String cpu(){double n=number(object("cpu"),"percent");return Double.isFinite(n)&&n>=0&&n<=100?String.format(Locale.UK,"%.0f%%",n):"—";}
    String load(){org.json.JSONArray a=object("cpu").optJSONArray("load");if(a==null||a.length()!=3)return "—";StringBuilder s=new StringBuilder();for(int i=0;i<3;i++){Object o=a.opt(i);if(!(o instanceof Number)||!Double.isFinite(((Number)o).doubleValue()))return "—";if(i>0)s.append(" / ");s.append(String.format(Locale.UK,"%.2f",((Number)o).doubleValue()));}return s.toString();}
}
