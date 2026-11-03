package com.colonellp.ellamonitor;

import java.time.*;
import java.util.*;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BatteryHistoryTest {
    private final Instant now=Instant.parse("2026-10-14T12:00:00Z");
    private PopupHistory.Plan plan(int period){PopupSelection s=new PopupSelection();s.period=period;s.fixed=true;return PopupHistory.plan(now,s,ZoneOffset.UTC);}
    private MonitorData.Metric metric(String source,String channel)throws Exception{return new MonitorData.Metric(new JSONObject().put("id",channel).put("source",source).put("kind","electrical").put("role",channel.endsWith("Battery")?"battery":"supply").put("batteryChannel",channel));}
    private HistoryData.Record row(Instant start,double amps,double ah,double wh,double coverage,boolean partial)throws Exception{
        return new HistoryData.Record(new JSONObject().put("start",start.toString()).put("end",start.plusSeconds(3600).toString()).put("partial",partial).put("values",new JSONArray().put(amps*13).put(amps).put(13)).put("netAh",ah).put("netWh",wh).put("ampCoverageSeconds",coverage).put("powerCoverageSeconds",coverage).put("samples",60).put("currentLastAt",1000).put("voltageLastAt",1000));
    }
    private HistoryData.Result input(PopupHistory.Plan p,String source,String channel,double amps,double ah,double wh)throws Exception{
        return new HistoryData.Result(metric(source,channel),p.query,Collections.singletonList(row(p.query.from,amps,ah,wh,3600,false)),p.query.to.toString());
    }
    @Test public void bothBatteriesUseStoredAmountsAndSumStaysRecordedNet()throws Exception{
        PopupHistory.Plan p=plan(1);
        for(String source:new String[]{"sbms","pico"}){
            HistoryData.Result net=input(p,source,source.equals("sbms")?"sbmsBattery":"picoBattery",-2,-1.25,-17),pv1=input(p,"sbms","pv1",3,2.5,31),pv2=input(p,"sbms","pv2",1,.75,10);
            String original=net.rows.get(0).raw.toString();BatteryHistory split=BatteryHistory.combine(net,pv1,pv2);
            for(boolean bars:new boolean[]{false,true}){
                PopupHistory.Series ah=split.series(p,"current",bars),wh=split.series(p,"watts",bars);
                assertEquals(3.25,ah.points.get(0).value,0);assertEquals(-4.5,ah.companion.points.get(0).value,0);
                assertEquals(41,wh.points.get(0).value,0);assertEquals(-58,wh.companion.points.get(0).value,0);
                assertEquals(-1.25,PopupHistory.series(net,p,"current",true,false,bars).points.get(0).value,0);
                assertEquals(-17,PopupHistory.series(net,p,"watts",true,false,bars).points.get(0).value,0);
                double[] range=PopupHistory.axisRange(ah,false);assertTrue(range[0]<=-4.5);assertTrue(range[1]>=3.25);assertEquals(Math.rint(range[2]),range[2],0);
            }
            assertEquals(original,net.rows.get(0).raw.toString());
        }
    }
    @Test public void missingSolarNeverBecomesZeroAndCannotEraseRecordedNet()throws Exception{
        PopupHistory.Plan p=plan(1);HistoryData.Result net=input(p,"pico","picoBattery",-2,-2,-26),pv=input(p,"sbms","pv1",1,1,13);
        HistoryData.Result missing=new HistoryData.Result(metric("sbms","pv2"),p.query,Collections.emptyList(),p.query.to.toString());
        BatteryHistory split=BatteryHistory.combine(net,pv,missing);assertTrue(split.charge.rows.isEmpty());assertTrue(split.draw.rows.isEmpty());assertEquals(-2,PopupHistory.series(net,p,"current",true,false).points.get(0).value,0);
    }
    @Test public void recordedZeroSolarIsValidAndOtherChargingIsNotClamped()throws Exception{
        PopupHistory.Plan p=plan(1);HistoryData.Result net=input(p,"sbms","sbmsBattery",5,5,65),zero=input(p,"sbms","pv1",0,0,0),zero2=input(p,"sbms","pv2",0,0,0);
        BatteryHistory split=BatteryHistory.combine(net,zero,zero2);assertEquals(0,split.charge.rows.get(0).value("Ah","electrical"),0);assertEquals(5,split.draw.rows.get(0).value("Ah","electrical"),0);
    }
    @Test public void alignedSbmsGapsAreRetainedButCrossSourceGapsAreUnavailable()throws Exception{
        PopupHistory.Plan p=plan(1);List<HistoryData.Result> inputs=new ArrayList<>();
        for(String channel:new String[]{"sbmsBattery","pv1","pv2"})inputs.add(new HistoryData.Result(metric("sbms",channel),p.query,Collections.singletonList(row(p.query.from,1,.5,6.5,1800,false)),p.query.to.toString()));
        BatteryHistory split=BatteryHistory.combine(inputs.get(0),inputs.get(1),inputs.get(2));assertEquals(1,split.draw.rows.size());assertFalse(split.series(p,"current",false).points.get(0).complete);
        HistoryData.Result pico=new HistoryData.Result(metric("pico","picoBattery"),p.query,inputs.get(0).rows,p.query.to.toString());assertTrue(BatteryHistory.combine(pico,inputs.get(1),inputs.get(2)).draw.rows.isEmpty());
        inputs.get(2).rows.get(0).raw.put("currentLastAt",2000);assertTrue(BatteryHistory.combine(inputs.get(0),inputs.get(1),inputs.get(2)).draw.rows.isEmpty());
    }
    @Test public void unequalPartialCheckpointsDoNotInventCoincidentSolarCoverage()throws Exception{
        PopupHistory.Plan p=plan(1);HistoryData.Record r=row(p.query.from,2,1,13,1800,true);
        HistoryData.Result b=new HistoryData.Result(metric("sbms","sbmsBattery"),p.query,Collections.singletonList(r),p.query.from.plusSeconds(1800).toString());
        HistoryData.Result pv1=new HistoryData.Result(metric("sbms","pv1"),p.query,Collections.singletonList(r),b.savedAt),pv2=new HistoryData.Result(metric("sbms","pv2"),p.query,Collections.singletonList(r),p.query.from.plusSeconds(1801).toString());
        BatteryHistory split=BatteryHistory.combine(b,pv1,pv2);assertTrue(split.draw.rows.isEmpty());assertTrue(split.charge.rows.isEmpty());
    }
    @Test public void weekGroupingAndCumulativeNetUseSameRecordedIntervals()throws Exception{
        PopupHistory.Plan p=plan(2);List<HistoryData.Record> netRows=new ArrayList<>(),solarRows=new ArrayList<>();
        for(int i=0;i<24;i++){netRows.add(row(p.query.from.plusSeconds(3600L*i),-1,-1,-13,3600,false));solarRows.add(row(p.query.from.plusSeconds(3600L*i),1,1,13,3600,false));}
        HistoryData.Result net=new HistoryData.Result(metric("pico","picoBattery"),p.query,netRows,p.query.to.toString()),pv1=new HistoryData.Result(metric("sbms","pv1"),p.query,solarRows,net.savedAt),pv2=new HistoryData.Result(metric("sbms","pv2"),p.query,solarRows,net.savedAt);
        BatteryHistory split=BatteryHistory.combine(net,pv1,pv2);
        for(boolean bars:new boolean[]{false,true}){PopupHistory.Series s=split.series(p,"current",bars);assertEquals(bars?2:4,s.points.size());assertEquals(48,s.points.stream().mapToDouble(point->point.value).sum(),0);assertEquals(-72,s.companion.points.stream().mapToDouble(point->point.value).sum(),0);PopupHistory.Series sum=PopupHistory.series(net,p,"current",true,false,bars);assertEquals(-24,sum.points.get(sum.points.size()-1).value,0);}
    }
}
