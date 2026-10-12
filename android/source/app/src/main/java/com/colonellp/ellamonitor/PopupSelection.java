package com.colonellp.ellamonitor;

import org.json.*;
import java.time.YearMonth;
import java.util.*;

/** Display state only: never part of viewer presets or the data contract. */
final class PopupSelection {
    int period;
    boolean fixed,bars,sum,power;
    String month="";
    JSONObject json() throws Exception {return new JSONObject().put("period",period).put("fixed",fixed).put("bars",bars).put("sum",sum).put("power",power).put("month",month);}
    static PopupSelection parse(JSONObject o)throws Exception {
        PopupSelection s=new PopupSelection();
        for(Iterator<String> i=o.keys();i.hasNext();)if(!Arrays.asList("period","fixed","bars","sum","power","month").contains(i.next()))throw SavedJson.invalid();
        if(o.has("period")){SavedJson.integer(o,"period",0,3);s.period=o.getInt("period");}
        for(String k:new String[]{"fixed","bars","sum","power"})if(o.has(k)&&!(o.get(k) instanceof Boolean))throw SavedJson.invalid();
        s.fixed=o.optBoolean("fixed");s.bars=o.optBoolean("bars");s.sum=o.optBoolean("sum");s.power=o.optBoolean("power");
        if(o.has("month")){SavedJson.string(o,"month",0,7);s.month=o.getString("month");if(!s.month.isEmpty()){YearMonth m=YearMonth.parse(s.month);if(m.getYear()<1970||m.isAfter(YearMonth.now()))throw SavedJson.invalid();}}
        return s;
    }
}
