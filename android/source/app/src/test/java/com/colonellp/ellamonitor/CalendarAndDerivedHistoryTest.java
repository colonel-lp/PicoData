package com.colonellp.ellamonitor;

import org.json.*;
import org.junit.Test;
import java.time.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

/** Synthetic, host-JVM checks of calendar boundaries, recorded entries and derived totals. */
public class CalendarAndDerivedHistoryTest {
    private final ZoneId utc=ZoneOffset.UTC;
    private final Instant now=Instant.parse("2026-10-11T13:01:00Z");
    private MonitorData.Metric metric(String id,String role,int polarity)throws Exception {
        return new MonitorData.Metric(new JSONObject().put("id",id).put("name",id).put("source","pico").put("kind","electrical").put("role",role).put("polarity",polarity));
    }
    private HistoryData.Window hour(){return new HistoryData.Window(Instant.parse("2026-10-11T13:00:00Z"),Instant.parse("2026-10-11T14:00:00Z"),"minute");}
    private HistoryData.Record row(String start,double seconds,Number current,Number power,boolean partial,long samples)throws Exception {
        Double amps=current==null?null:current.doubleValue(),watts=power==null?null:power.doubleValue();
        Instant at=Instant.parse(start);JSONObject data=new JSONObject().put("start",at.toString()).put("end",at.plusSeconds(60).toString()).put("partial",partial)
            .put("values",new JSONArray().put(watts==null?JSONObject.NULL:watts).put(amps==null?JSONObject.NULL:amps).put(JSONObject.NULL))
            .put("ampCoverageSeconds",amps==null?0:seconds).put("powerCoverageSeconds",watts==null?0:seconds).put("samples",samples)
            .put("netAh",amps==null?JSONObject.NULL:amps*seconds/3600).put("netWh",watts==null?JSONObject.NULL:watts*seconds/3600);
        return new HistoryData.Record(data);
    }
    private HistoryData.Result result(HistoryData.Window window,String saved,HistoryData.Record...rows)throws Exception {
        return new HistoryData.Result(metric("input","load",-1),window,Arrays.asList(rows),saved);
    }
    @Test public void oneMinuteIntoHourIsNotATrailingHour(){HistoryData.Window w=SummaryPeriods.window(now,0,false,utc);assertEquals(Instant.parse("2026-10-11T13:00:00Z"),w.from);assertEquals(now,w.to);assertEquals(1,SummaryPeriods.expected(w));}
    @Test public void todayStartsAtMidnight(){HistoryData.Window w=SummaryPeriods.window(now,1,false,utc);assertEquals(Instant.parse("2026-10-11T00:00:00Z"),w.from);assertEquals(781,SummaryPeriods.expected(w));}
    @Test public void weekStartsOnMonday(){assertEquals(Instant.parse("2026-10-05T00:00:00Z"),SummaryPeriods.window(now,2,false,utc).from);}
    @Test public void monthStartsOnFirst(){assertEquals(Instant.parse("2026-10-01T00:00:00Z"),SummaryPeriods.window(now,3,false,utc).from);assertEquals("October",SummaryPeriods.label(now,3,false,utc));assertEquals("September",SummaryPeriods.label(now,3,true,utc));}
    @Test public void fixedPeriodsAreThePreviousCalendarPeriods(){
        String[] starts={"2026-10-11T12:00:00Z","2026-10-10T00:00:00Z","2026-09-28T00:00:00Z","2026-09-01T00:00:00Z"};
        String[] ends={"2026-10-11T13:00:00Z","2026-10-11T00:00:00Z","2026-10-05T00:00:00Z","2026-10-01T00:00:00Z"};
        for(int i=0;i<4;i++){HistoryData.Window w=SummaryPeriods.window(now,i,true,utc);assertEquals(Instant.parse(starts[i]),w.from);assertEquals(Instant.parse(ends[i]),w.to);}
    }
    @Test public void localDatesRespectDaylightSaving(){
        ZoneId london=ZoneId.of("Europe/London");HistoryData.Window spring=SummaryPeriods.window(Instant.parse("2026-03-30T12:00:00Z"),1,true,london),autumn=SummaryPeriods.window(Instant.parse("2026-10-26T12:00:00Z"),1,true,london);
        assertEquals(23,Duration.between(spring.from,spring.to).toHours());assertEquals(25,Duration.between(autumn.from,autumn.to).toHours());assertEquals(23,SummaryPeriods.expected(spring));assertEquals(25,SummaryPeriods.expected(autumn));
    }
    @Test public void emptyElapsedWindowHasNoExpectedEntries(){assertEquals(0,SummaryPeriods.expected(SummaryPeriods.window(Instant.parse("2026-10-11T13:00:00Z"),0,false,utc)));}
    @Test public void expectedCountsIncludeOnlyElapsedSlots(){HistoryData.Window w=SummaryPeriods.window(Instant.parse("2026-10-11T13:01:01Z"),0,false,utc);assertEquals(2,SummaryPeriods.expected(w));}
    @Test public void samplesCountEntriesNotRawPublisherSamples()throws Exception {
        SummaryPeriods.Totals t=SummaryPeriods.totals(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-2,24,false,100),row("2026-10-11T13:02:00Z",60,0,0,false,200)));
        assertEquals("Samples 2/60",t.samples());assertEquals(-2d/60,t.charge,1e-9);assertEquals(.4,t.energy,1e-9);assertTrue(t.text().startsWith("Current: -0.033 Ah\nEnergy: 0.400 Wh"));
    }
    @Test public void missingEnergyDoesNotEraseCurrentOrBecomeZero()throws Exception {
        SummaryPeriods.Totals t=SummaryPeriods.totals(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",20,-3,null,false,1)));assertEquals(-1d/60,t.charge,1e-9);assertNull(t.energy);assertEquals(1,t.captured);assertTrue(t.text().contains("Energy: —"));
    }
    @Test public void boundaryBucketsAreNotProrated()throws Exception {
        HistoryData.Window w=new HistoryData.Window(Instant.parse("2026-10-11T13:00:30Z"),Instant.parse("2026-10-11T13:02:30Z"),"minute");
        SummaryPeriods.Totals t=SummaryPeriods.totals(result(w,now.toString(),row("2026-10-11T13:00:00Z",60,60,60,false,1),row("2026-10-11T13:01:00Z",60,1,1,false,1),row("2026-10-11T13:02:00Z",60,60,60,false,1)));
        assertEquals(1,t.captured);assertEquals(1d/60,t.charge,1e-9);assertEquals(3,t.expected);
    }
    @Test public void currentCheckpointCannotContainDataAfterSelectionTime()throws Exception {
        HistoryData.Window w=SummaryPeriods.window(Instant.parse("2026-10-11T13:00:40Z"),0,false,utc);HistoryData.Record r=row("2026-10-11T13:00:00Z",30,2,20,true,1);
        assertEquals(1,SummaryPeriods.totals(result(w,"2026-10-11T13:00:30Z",r)).captured);assertEquals(0,SummaryPeriods.totals(result(w,"2026-10-11T13:00:50Z",r)).captured);
    }
    @Test public void derivationUsesTheLiveComponentSelectionAndSigns()throws Exception {
        MonitorData.Metric load=metric("load","load",-1),pv=metric("pv","charge",1),battery=new MonitorData.Metric(new JSONObject(metric("battery","battery",-1).definition.toString()).put("batteryChannel","picoBattery"));
        MonitorData.Metric total=DerivedHistory.metric(Arrays.asList(load,pv,battery),false),inverter=DerivedHistory.metric(Arrays.asList(load,pv,battery),true);
        assertEquals(1,total.definition.getJSONArray("components").length());assertEquals(-1,total.definition.getJSONArray("components").getJSONObject(0).getInt("factor"));assertEquals(2,inverter.definition.getJSONArray("components").length());assertEquals(1,inverter.definition.getJSONArray("components").getJSONObject(0).getInt("factor"));
        load.definition.put("polarity",JSONObject.NULL);assertNull(DerivedHistory.metric(Arrays.asList(load),false));
    }
    @Test public void derivedCurrentChargeAndEnergyUseSignedLoggedInputs()throws Exception {
        MonitorData.Metric virtual=DerivedHistory.metric(Arrays.asList(metric("a","load",-1),metric("b","load",1)),false);
        HistoryData.Result combined=DerivedHistory.combine(virtual,hour(),Arrays.asList(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-3,-36,false,10)),result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,2,24,false,15))),new double[]{-1,1});
        assertEquals(1,combined.rows.size());HistoryData.Record r=combined.rows.get(0);assertEquals(5,r.value("current","electrical"),0);assertEquals(60,r.value("watts","electrical"),0);assertEquals(5d/60,r.value("Ah","electrical"),1e-9);assertEquals(1,r.value("Wh","electrical"),1e-9);assertNull(r.value("voltage","electrical"));assertFalse(r.raw.has("forwardWh"));assertEquals(1,SummaryPeriods.totals(combined).captured);assertTrue(HistoryData.csv(combined,"Load").contains("derived_inputs"));assertTrue(DerivedHistory.inputReferences(virtual).contains("a × -1"));
    }
    @Test public void derivedMissingInputsStayGaps()throws Exception {
        List<HistoryData.Result> inputs=Arrays.asList(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-2,-24,false,1)),result(hour(),now.toString()));
        assertTrue(DerivedHistory.combine(metric("sum","load",1),hour(),inputs,new double[]{1,1}).rows.isEmpty());
    }
    @Test public void unmatchedCoverageCannotClaimACommonTotal()throws Exception {
        List<HistoryData.Result> inputs=Arrays.asList(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-2,-24,false,1)),result(hour(),now.toString(),row("2026-10-11T13:00:00Z",30,-1,-12,false,1)));
        assertTrue(DerivedHistory.combine(metric("sum","load",1),hour(),inputs,new double[]{1,1}).rows.isEmpty());
    }
    @Test public void derivedEnergyGapsRetainValidCurrent()throws Exception {
        List<HistoryData.Result> inputs=Arrays.asList(result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-2,-24,false,1)),result(hour(),now.toString(),row("2026-10-11T13:00:00Z",60,-1,null,false,1)));
        HistoryData.Record r=DerivedHistory.combine(metric("sum","load",1),hour(),inputs,new double[]{-1,-1}).rows.get(0);assertEquals(3,r.value("current","electrical"),0);assertNull(r.value("Wh","electrical"));
    }
    @Test public void checkpointRacesDoNotMixDifferentInputSnapshots()throws Exception {
        List<HistoryData.Result> inputs=Arrays.asList(result(hour(),"2026-10-11T13:00:30Z",row("2026-10-11T13:00:00Z",30,-2,-24,true,1)),result(hour(),"2026-10-11T13:00:40Z",row("2026-10-11T13:00:00Z",40,-1,-12,true,1)));
        assertTrue(DerivedHistory.combine(metric("sum","load",1),hour(),inputs,new double[]{1,1}).rows.isEmpty());
    }
    @Test public void fetchingDerivedHistoryRequestsOnlyRealComponentIdsAndCancellationStopsNextRequest()throws Exception {
        JSONObject recorded=row("2026-10-11T13:00:00Z",60,-2,-24,false,1).raw;
        Map<String,byte[]> responses=new HashMap<>();for(String id:new String[]{"a","b"})responses.put(id,new JSONObject().put("apiVersion",1).put("schemaVersion",2).put("metric",new JSONObject().put("id",id)).put("savedAt",now.toString()).put("rows",new JSONArray().put(recorded)).toString().getBytes(StandardCharsets.UTF_8));
        List<String> paths=new ArrayList<>();ApiClient client=new ApiClient("https://history.example.invalid",String.join("",Collections.nCopies(64,"a")),url->{
            paths.add(url.getPath());String id=url.getPath().substring(url.getPath().lastIndexOf('/')+1);
            byte[] bytes=responses.get(id);
            return new HttpURLConnection(url){public void connect(){}public void disconnect(){}public boolean usingProxy(){return false;}public int getResponseCode(){return 200;}public InputStream getInputStream(){return new ByteArrayInputStream(bytes);}};
        });
        MonitorData.Metric virtual=DerivedHistory.metric(Arrays.asList(metric("a","load",-1),metric("b","load",-1)),false);
        assertEquals(4,HistoryData.fetch(client,virtual,hour()).rows.get(0).value("current","electrical"),0);assertEquals(Arrays.asList("/api/v1/history/metrics/a","/api/v1/history/metrics/b"),paths);
        client.cancel();try{client.get("/api/v1/status");fail();}catch(IOException expected){}assertEquals(2,paths.size());
    }
}
