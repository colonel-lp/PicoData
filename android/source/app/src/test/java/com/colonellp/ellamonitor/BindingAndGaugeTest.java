package com.colonellp.ellamonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class BindingAndGaugeTest {
    static JSONObject instruments() throws Exception {
        JSONObject catalogue=ContractTest.catalogue();
        catalogue.getJSONArray("metrics").put(ContractTest.metric("outside-test","pico","temperature","","",202).put("sensorType","thermometer").put("name","Outside"));
        catalogue.getJSONArray("metrics").put(ContractTest.metric("pressure-test","pico","barometer","","",204).put("sensorType","barometer").put("name","Barometer"));
        return catalogue;
    }
    static JSONObject sample() throws Exception {
        JSONObject live=ContractTest.live();live.getJSONObject("pico").getJSONObject("readings").getJSONObject("202").put("name","Outside");
        live.getJSONObject("pico").getJSONObject("readings").put("204",new JSONObject().put("type","barometer").put("name","Barometer").put("pressure",1007));
        live.getJSONArray("measurements").put(new JSONObject().put("id","outside-test").put("fresh",true).put("mappingValid",true).put("values",new JSONObject().put("temperature",8.5)));
        live.getJSONArray("measurements").put(new JSONObject().put("id","pressure-test").put("fresh",true).put("mappingValid",true).put("values",new JSONObject().put("pressure",1007)));
        return live;
    }
    @Test public void catalogueArrivalKeepsSinglePhysicalInstrumentsAndHistory() throws Exception {
        JSONObject live=sample();Map<String,MonitorData.Datum> old=MonitorData.display(MonitorData.catalogue(ContractTest.catalogue()),live,0);
        Map<String,MonitorData.Datum> next=MonitorData.display(MonitorData.catalogue(instruments()),live,0);MonitorData.retainRawBindings(old,next);
        for(MonitorData.Datum d:old.values())if(d.id.startsWith("raw:202:")||d.id.startsWith("raw:204:")) {
            assertEquals(d.value,next.get(d.id).value);assertNotNull(next.get(d.id).metric);
            assertEquals(1,next.values().stream().filter(x->x.physicalKey().equals(d.physicalKey())).count());
        }
        // A genuinely different sensor with the same label remains a separate instrument.
        live.getJSONObject("pico").getJSONObject("readings").put("205",new JSONObject().put("type","thermometer").put("name","Outside").put("temperature",9));
        next=MonitorData.display(MonitorData.catalogue(instruments()),live,0);MonitorData.retainRawBindings(old,next);
        assertEquals(2,next.values().stream().filter(d->d.group.equals("temps")&&d.name.startsWith("Outside")).count());
    }
    @Test public void staleAndRecoveryKeepBindingWithoutOldValuesOrDuplicateRows() throws Exception {
        JSONObject live=sample();Map<String,MonitorData.Datum> old=MonitorData.display(MonitorData.catalogue(instruments()),live,0);
        live.getJSONObject("pico").put("fresh",false).put("readings",JSONObject.NULL);
        Map<String,MonitorData.Datum> next=MonitorData.display(MonitorData.catalogue(instruments()),live,0);MonitorData.retainRawBindings(old,next);
        for(MonitorData.Datum d:old.values())if(d.id.startsWith("raw:202:")||d.id.startsWith("raw:204:")) {assertTrue(next.containsKey(d.id));assertNull(next.get(d.id).value);assertNotNull(next.get(d.id).metric);}
        Map<String,MonitorData.Datum> recovered=MonitorData.display(MonitorData.catalogue(instruments()),sample(),0);MonitorData.retainRawBindings(next,recovered);
        assertEquals(1,recovered.values().stream().filter(d->d.unit.equals("hPa")).count());
        assertEquals(1007,recovered.values().stream().filter(d->d.unit.equals("hPa")).findFirst().get().value,0);
    }
    @Test public void changedRawConfigurationDoesNotInheritOldSettingsBinding() throws Exception {
        JSONObject live=sample();Map<String,MonitorData.Datum> old=MonitorData.display(MonitorData.catalogue(instruments()),live,0);
        live.getJSONObject("pico").getJSONObject("readings").getJSONObject("202").put("name","Replacement sensor");
        Map<String,MonitorData.Datum> next=MonitorData.display(MonitorData.catalogue(instruments()),live,0);MonitorData.retainRawBindings(old,next);
        for(String key:old.keySet())if(key.startsWith("raw:202:"))assertFalse(next.containsKey(key));
    }
    @Test public void loadPvBatteryAndSocScalesAreFixedAndClampOnlyTheArc() {
        assertEquals(10,GaugeScale.maximum(GaugeScale.Kind.LOAD,"A",500d),0);assertEquals(0,GaugeScale.minimum(GaugeScale.Kind.LOAD,"A",-500d),0);
        assertEquals(20,GaugeScale.maximum(GaugeScale.Kind.PV,"A",500d),0);assertEquals(125,GaugeScale.sweep(GaugeScale.Kind.PV,"A",10),0);
        assertEquals(-10,GaugeScale.minimum(GaugeScale.Kind.BATTERY,"A",0d),0);assertEquals(10,GaugeScale.maximum(GaugeScale.Kind.BATTERY,"A",0d),0);
        assertEquals(0,GaugeScale.sweep(GaugeScale.Kind.BATTERY,"A",0),0);assertEquals(-62.5,GaugeScale.sweep(GaugeScale.Kind.BATTERY,"A",-5),0);assertEquals(62.5,GaugeScale.sweep(GaugeScale.Kind.BATTERY,"A",5),0);assertEquals(-125,GaugeScale.sweep(GaugeScale.Kind.BATTERY,"A",-50),0);
        assertEquals(0,GaugeScale.sweep(GaugeScale.Kind.LOAD,"A",-2),0);assertEquals(250,GaugeScale.sweep(GaugeScale.Kind.LOAD,"A",12),0);
        assertEquals(-90,GaugeScale.sweep(GaugeScale.Kind.SOC,"%",25),0);assertEquals(-180,GaugeScale.sweep(GaugeScale.Kind.SOC,"%",50),0);assertEquals(-360,GaugeScale.sweep(GaugeScale.Kind.SOC,"%",100),0);
        assertEquals(0,GaugeScale.socBandSweep(0,0),0);assertEquals(-90,GaugeScale.socBandSweep(25,0),0);assertEquals(0,GaugeScale.socBandSweep(25,1),0);assertEquals(-90,GaugeScale.socBandSweep(50,1),0);assertEquals(-180,GaugeScale.socBandSweep(100,2),0);
        assertEquals(GaugeScale.Kind.DEFAULT,GaugeScale.kind("environment","Water (%)"));
    }
}
