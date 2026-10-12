package com.colonellp.ellamonitor;

import org.json.*;
import java.time.Instant;
import java.util.*;

/** Read-only totals from the logged inputs of a live derived current. */
final class DerivedHistory {
    static MonitorData.Metric bmsLoad(List<MonitorData.Metric> metrics){
        try{MonitorData.Metric battery=null,pv=null;for(MonitorData.Metric m:metrics){if(m.channel.equals("sbmsBattery"))battery=m;if(m.channel.equals("pv1"))pv=m;}
            if(battery==null||pv==null)return null;
            return new MonitorData.Metric(new JSONObject().put("id","derived:sbms-load").put("name","Load Σ [BMS]").put("source","sbms").put("kind","electrical").put("role","load").put("batteryChannel","sbmsLoad").put("derived",true)
                .put("components",new JSONArray().put(new JSONObject().put("metric",pv.definition).put("factor",1)).put(new JSONObject().put("metric",battery.definition).put("factor",-1))));
        }catch(Exception e){return null;}
    }
    static MonitorData.Metric metric(List<MonitorData.Metric> metrics,boolean inverter){
        try{
            JSONArray components=new JSONArray();
            if(inverter){MonitorData.Metric battery=null;for(MonitorData.Metric m:metrics)if(m.channel.equals("picoBattery"))battery=m;if(battery==null)return null;components.put(new JSONObject().put("metric",battery.definition).put("factor",1));}
            int loads=0;
            for(MonitorData.Metric m:metrics)if(m.source.equals("pico")&&m.kind.equals("electrical")&&m.role.equals("load")){
                Double polarity=MonitorData.number(m.definition,"polarity");if(polarity==null||(polarity!=1&&polarity!=-1))return null;
                components.put(new JSONObject().put("metric",m.definition).put("factor",polarity));loads++;
            }
            if(loads==0)return null;
            return new MonitorData.Metric(new JSONObject().put("id",inverter?"derived:pico-inverter":"derived:pico-load").put("name",inverter?"Inverter":"Load Σ [Pico]")
                    .put("kind","electrical").put("source","pico").put("role","load").put("derived",true).put("components",components)
                    .put("units",new JSONObject().put("values",new JSONArray().put("W").put("A")).put("energy","Wh").put("charge","Ah")));
        }catch(Exception e){return null;}
    }
    static boolean isDerived(MonitorData.Metric m){return m!=null&&m.definition.optBoolean("derived");}
    static String inputReferences(MonitorData.Metric metric){
        if(!isDerived(metric))return "";
        JSONArray refs=new JSONArray(),inputs=metric.definition.optJSONArray("components");
        if(inputs!=null)for(int i=0;i<inputs.length();i++){JSONObject input=inputs.optJSONObject(i);if(input!=null)refs.put(MonitorData.object(input,"metric").optString("id")+" × "+input.optInt("factor"));}
        return refs.toString();
    }
    static HistoryData.Result fetch(ApiClient client,MonitorData.Metric metric,HistoryData.Window window)throws Exception{
        JSONArray components=metric.definition.getJSONArray("components");if(components.length()==0||components.length()>64)throw new Exception("Invalid derived inputs.");
        List<HistoryData.Result> inputs=new ArrayList<>();double[] factors=new double[components.length()];
        for(int i=0;i<components.length();i++){JSONObject item=components.getJSONObject(i);MonitorData.Metric input=new MonitorData.Metric(item.getJSONObject("metric"));if(isDerived(input))throw new Exception("Nested derived input.");factors[i]=item.getDouble("factor");if(factors[i]!=1&&factors[i]!=-1)throw new Exception("Invalid derived polarity.");inputs.add(HistoryData.fetch(client,input,window));}
        return combine(metric,window,inputs,factors);
    }
    static HistoryData.Result combine(MonitorData.Metric metric,HistoryData.Window window,List<HistoryData.Result> inputs,double[] factors)throws Exception{
        if(inputs.isEmpty()||inputs.size()!=factors.length)throw new Exception("Missing derived inputs.");
        List<Map<Instant,HistoryData.Record>> maps=new ArrayList<>();String saved=inputs.get(0).savedAt;
        for(HistoryData.Result input:inputs){Map<Instant,HistoryData.Record> rows=new LinkedHashMap<>();for(HistoryData.Record row:input.rows)rows.put(row.start,row);maps.add(rows);try{if(Instant.parse(input.savedAt).isBefore(Instant.parse(saved)))saved=input.savedAt;}catch(Exception ignored){saved="";}}
        List<HistoryData.Record> result=new ArrayList<>();
        for(HistoryData.Record first:inputs.get(0).rows){
            boolean current=true,power=true,partial=first.partial;double a=0,w=0,ah=0,wh=0;long samples=Long.MAX_VALUE;
            double seconds;try{Instant end=first.partial?Instant.parse(inputs.get(0).savedAt):first.end;seconds=(Math.min(first.end.toEpochMilli(),end.toEpochMilli())-first.start.toEpochMilli())/1000d;}catch(Exception e){continue;}
            if(seconds<=0)continue;
            for(int i=0;i<maps.size();i++){
                HistoryData.Record row=maps.get(i).get(first.start);
                if(row==null||!row.end.equals(first.end)||row.partial!=partial||(partial&&!inputs.get(i).savedAt.equals(inputs.get(0).savedAt))){current=false;break;}
                Double av=row.value("current","electrical"),ac=row.coverage("current","electrical"),wv=row.value("watts","electrical"),pc=row.coverage("watts","electrical");
                // Partial/mismatched input coverage does not establish a common
                // time interval. Never fill absent components with zero.
                // Battery/PV1 share one validated SBMS snapshot and freshness
                // lifetime. Matched coverage/timestamps also retain real gaps.
                Double firstCoverage=first.coverage("current","electrical"),lastAt=MonitorData.number(row.raw,"currentLastAt"),firstAt=MonitorData.number(first.raw,"currentLastAt");
                boolean cohort=metric.id.equals("derived:sbms-load")&&lastAt!=null&&lastAt.equals(firstAt)&&firstCoverage!=null&&ac!=null&&Math.abs(ac-firstCoverage)<.05&&row.raw.optLong("samples",-1)==first.raw.optLong("samples",-2);
                if(av==null||ac==null||ac<=0||ac>seconds+.05||Math.abs(ac-seconds)>.05&&!cohort){current=false;break;}
                a+=av*factors[i];Double charge=row.value("Ah","electrical");ah+=(charge==null?av*ac/3600:charge)*factors[i];
                Double firstPower=first.coverage("watts","electrical"),voltAt=MonitorData.number(row.raw,"voltageLastAt"),firstVoltAt=MonitorData.number(first.raw,"voltageLastAt");
                boolean commonPower=cohort&&voltAt!=null&&voltAt.equals(firstVoltAt)&&firstPower!=null&&pc!=null&&Math.abs(pc-firstPower)<.05;
                if(wv==null||pc==null||pc<=0||pc>seconds+.05||Math.abs(pc-seconds)>.05&&!commonPower)power=false;
                else{w+=wv*factors[i];Double energy=row.value("Wh","electrical");wh+=(energy==null?wv*pc/3600:energy)*factors[i];}
                samples=Math.min(samples,row.raw.optLong("samples",0));
            }
            if(!current)continue;
            JSONObject row=new JSONObject().put("start",first.start.toString()).put("end",first.end.toString()).put("partial",partial)
                    .put("values",new JSONArray().put(power?w:JSONObject.NULL).put(a).put(JSONObject.NULL))
                    .put("ampCoverageSeconds",first.coverage("current","electrical")).put("powerCoverageSeconds",power?first.coverage("watts","electrical"):0).put("voltCoverageSeconds",0)
                    .put("netAh",ah).put("netWh",power?wh:JSONObject.NULL).put("samples",samples==Long.MAX_VALUE?0:samples);
            result.add(new HistoryData.Record(row));
        }
        return new HistoryData.Result(metric,window,result,saved);
    }
}
