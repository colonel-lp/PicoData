package com.colonellp.ellamonitor;

import java.util.*;
import org.json.*;

/** Popup-only decomposition. Actual battery history remains the source for totals and Sum. */
final class BatteryHistory {
    final HistoryData.Result charge,draw;
    BatteryHistory(HistoryData.Result c,HistoryData.Result d){charge=c;draw=d;}
    static boolean applies(MonitorData.Metric m){return m!=null&&m.kind.equals("electrical")&&m.role.equals("battery")&&(m.channel.equals("sbmsBattery")||m.channel.equals("picoBattery"));}
    static BatteryHistory fetch(ApiClient client,HistoryData.Result battery,List<MonitorData.Metric> catalogue)throws Exception{
        MonitorData.Metric first=null,second=null;
        for(MonitorData.Metric m:catalogue)if(m.source.equals("sbms")){if(m.channel.equals("pv1"))first=m;if(m.channel.equals("pv2"))second=m;}
        if(first==null||second==null)return null;
        return combine(battery,HistoryData.fetch(client,first,battery.window),HistoryData.fetch(client,second,battery.window));
    }
    static BatteryHistory combine(HistoryData.Result battery,HistoryData.Result pv1,HistoryData.Result pv2)throws Exception{
        MonitorData.Metric charge=metric("charge"),draw=metric("draw");
        HistoryData.Result solar=DerivedHistory.combine(charge,battery.window,Arrays.asList(pv1,pv2),new double[]{1,1});
        HistoryData.Result load=DerivedHistory.combine(draw,battery.window,Arrays.asList(battery,pv1,pv2),new double[]{1,-1,-1});
        return new BatteryHistory(solar,load);
    }
    private static MonitorData.Metric metric(String part)throws Exception{
        return new MonitorData.Metric(new JSONObject().put("id","derived:popup-battery-"+part).put("kind","electrical").put("role","battery").put("name",part));
    }
    PopupHistory.Series series(PopupHistory.Plan plan,String field,boolean bars){
        PopupHistory.Series positive=PopupHistory.series(charge,plan,field,false,false,bars);
        positive.companion=PopupHistory.series(draw,plan,field,false,false,bars);positive.batterySplit=true;positive.emptyMessage="No matched solar coverage";
        return positive;
    }
}
