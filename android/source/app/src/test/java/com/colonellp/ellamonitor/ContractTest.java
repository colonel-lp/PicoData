package com.colonellp.ellamonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ContractTest {
    static JSONObject metric(String id, String source, String kind, String role, String channel, int sensor) throws Exception {
        return new JSONObject().put("id", id).put("name", "Synthetic " + id).put("source", source).put("kind", kind).put("role", role)
                .put("batteryChannel", channel).put("sensorId", sensor).put("sensorType", role.equals("battery") ? "battery" : "current").put("field", "battery").put("voltage", "sbms").put("polarity", role.equals("battery") ? 1 : -1);
    }
    static JSONObject catalogue() throws Exception {
        return new JSONObject().put("schemaVersion", 2).put("metrics", new JSONArray()
                .put(metric("battery-test", "pico", "electrical", "battery", "picoBattery", 101))
                .put(metric("bms-test", "sbms", "electrical", "battery", "sbmsBattery", 0))
                .put(metric("load-test", "pico", "electrical", "load", "", 102))
                .put(metric("pv2-test", "sbms", "electrical", "supply", "pv2", 0)));
    }
    static JSONObject source(JSONObject data, String key, double age) throws Exception { return new JSONObject().put("state", "connected").put("fresh", true).put("ageSeconds", age).put("receivedAt", "2026-01-01T00:00:00Z").put(key, data); }
    static JSONObject live() throws Exception {
        JSONObject raw = new JSONObject().put("101", new JSONObject().put("type", "battery").put("name", "Synthetic battery").put("capacity.remaining", 125).put("capacity.timeRemaining", 36000))
                .put("202", new JSONObject().put("type", "thermometer").put("name", "Synthetic temperature").put("pos", 55).put("temperature", 8.5))
                .put("203", new JSONObject().put("type", "tank").put("name", "Synthetic tank").put("pos", 56).put("percentage", 0).put("remainingCapacity", 0));
        JSONObject broadcast = new JSONObject().put("cellsMV", new JSONArray(new int[]{3101,3102,0,0,3103,3104,0,0})).put("tempInt", 18.5).put("tempExt", 10).put("flags", new JSONObject().put("delta", 3));
        JSONObject bms = new JSONObject().put("flags", new JSONObject().put("CFET", true).put("DFET", false).put("OVLK", JSONObject.NULL)).put("broadcast", broadcast);
        JSONArray measurements = new JSONArray();
        for (String id : new String[]{"battery-test", "bms-test", "load-test", "pv2-test"}) measurements.put(new JSONObject().put("id", id).put("fresh", true).put("mappingValid", true).put("values", new JSONObject().put("current", id.equals("pv2-test") ? 0 : -2.5).put("voltage", 12.4).put("watts", id.equals("pv2-test") ? 0 : -31).put("stateOfCharge", 50)));
        return new JSONObject().put("apiVersion", 1).put("now", "2026-01-01T00:00:00Z").put("pico", source(raw, "readings", .1)).put("sbms", source(bms, "reading", .2)).put("measurements", measurements);
    }
    static MonitorData.Datum find(Map<String, MonitorData.Datum> data, String channel, String field) {
        return data.values().stream().filter(d -> d.metric != null && d.metric.channel.equals(channel) && d.quantity.equals(field)).findFirst().get();
    }
    @Test public void signedValuesSocAndValidZeroKeepSourceIdentity() throws Exception {
        Map<String, MonitorData.Datum> data = MonitorData.display(MonitorData.catalogue(catalogue()), live(), 0);
        assertEquals(-2.5, find(data, "picoBattery", "current").value, 0);
        assertEquals(-31, find(data, "sbmsBattery", "watts").value, 0);
        assertEquals(50, find(data, "picoBattery", "stateOfCharge").value, 0);
        assertEquals(0, find(data, "pv2", "watts").value, 0);
        assertEquals(2.5, data.get("pico:load-sum").value, 0);
        assertEquals(3.101, data.get("cell:0").value, 0); assertNull(data.get("cell:2").value);
    }
    @Test public void flagsHaveThreeStatesAndNoCoercion() throws Exception {
        List<MonitorData.Metric> m = MonitorData.catalogue(catalogue()); JSONObject l = live();
        Map<String, MonitorData.Datum> d = MonitorData.display(m, l, 0);
        assertEquals(Boolean.TRUE, d.get("flag:CFET").flag); assertEquals(Boolean.FALSE, d.get("flag:DFET").flag); assertNull(d.get("flag:OVLK").flag); assertNull(d.get("flag:CELF").flag);
        l.getJSONObject("sbms").getJSONObject("reading").getJSONObject("flags").put("CFET", "false");
        assertNull(MonitorData.display(m, l, 0).get("flag:CFET").flag);
    }
    @Test public void freshnessExpiresIndependentlyIncludingVoltageDependency() throws Exception {
        List<MonitorData.Metric> m = MonitorData.catalogue(catalogue()); JSONObject l = live();
        Map<String, MonitorData.Datum> d = MonitorData.display(m, l, 2);
        assertNull(find(d, "picoBattery", "current").value); assertNotNull(find(d, "sbmsBattery", "current").value);
        l.getJSONObject("sbms").put("fresh", false); d = MonitorData.display(m, l, 0);
        assertNotNull(find(d, "picoBattery", "current").value); assertNull(find(d, "picoBattery", "watts").value); assertNull(find(d, "picoBattery", "voltage").value);
        assertNull(d.get("flag:DFET").flag); assertNull(d.get("cell:0").value);
        assertNull(MonitorData.display(m, null, 0).get("pico:load-sum").value);
    }
    @Test public void bindingMismatchCannotProduceLiveMeasurements() throws Exception {
        JSONObject l = live(); l.getJSONArray("measurements").getJSONObject(2).put("mappingValid", false);
        assertNull(MonitorData.display(MonitorData.catalogue(catalogue()), l, 0).get("pico:load-sum").value);
    }
    @Test public void loadSumUsesVerifiedIndividualSignsAndRejectsUnknownPolarity() throws Exception {
        JSONObject c = catalogue();
        c.getJSONArray("metrics").getJSONObject(2).put("polarity", JSONObject.NULL);
        assertNull(MonitorData.display(MonitorData.catalogue(c), live(), 0).get("pico:load-sum").value);
        c.getJSONArray("metrics").getJSONObject(2).put("polarity", 1);
        assertEquals(-2.5, MonitorData.display(MonitorData.catalogue(c), live(), 0).get("pico:load-sum").value, 0);
    }
    @Test public void aliasesUseAvailableConfigurationAndNeverReadingValue() throws Exception {
        JSONObject s = new JSONObject().put("type", "thermometer").put("name", "A").put("pos", 5).put("module", 8).put("temperature", 10);
        String key = MonitorData.rawBinding("202", s); s.put("temperature", 20); assertEquals(key, MonitorData.rawBinding("202", s));
        s.put("module", 9); assertNotEquals(key, MonitorData.rawBinding("202", s));
    }
    @Test public void numericStringsAndInvalidNumbersAreUnavailable() throws Exception {
        JSONObject o = new JSONObject().put("v", "0"); assertNull(MonitorData.number(o, "v")); assertNull(MonitorData.number(null, "v"));
        assertFalse(MonitorData.fresh(new JSONObject().put("state", "connected").put("fresh", true).put("ageSeconds", -1), 0, 3));
    }
    @Test public void utcWindowsDoNotDependOnBstOrLocalClock() {
        Instant now = Instant.parse("2026-03-29T01:30:00Z");
        HistoryData.Window rolling = HistoryData.window(now, 2, false, 0, "electrical"); assertEquals(86400, rolling.to.getEpochSecond() - rolling.from.getEpochSecond());
        HistoryData.Window defined = HistoryData.window(now, 2, true, 0, "electrical"); assertEquals("2026-03-29T00:00:00Z", defined.from.toString());
        assertEquals("2026-03-23T00:00:00Z", HistoryData.window(now, 3, true, 0, "electrical").from.toString());
        assertEquals(30 * 86400, HistoryData.window(now, 4, false, 0, "electrical").to.getEpochSecond() - HistoryData.window(now, 4, false, 0, "electrical").from.getEpochSecond());
    }
    @Test public void definedMonthUsesCalendarAndEnvironmentalHours() {
        HistoryData.Window w = HistoryData.window(Instant.parse("2028-02-15T12:00:00Z"), 4, true, 0, "electrical"); assertEquals(29 * 86400, w.to.getEpochSecond() - w.from.getEpochSecond());
        assertEquals("hour", HistoryData.window(w.from, 0, false, 0, "temperature").resolution);
        assertEquals("2028-01-01T00:00:00Z", HistoryData.window(w.from, 4, true, -1, "electrical").from.toString());
    }
    @Test public void historyKeepsWholeBucketSignedEnergyAndCoverage() throws Exception {
        HistoryData.Record r = new HistoryData.Record(new JSONObject().put("start", "2026-01-01T00:00:00Z").put("end", "2026-01-01T00:01:00Z").put("partial", true).put("values", new JSONArray().put(-31).put(-2.5).put(12.4).put(50)).put("netWh", -31d / 120).put("powerCoverageSeconds", 30));
        assertEquals(-31, r.value("watts", "electrical"), 0); assertEquals(30, r.coverage("Wh", "electrical"), 0); assertEquals(-31d / 120, r.value("Wh", "electrical"), 0); assertTrue(r.partial);
    }
    @Test public void csvEscapesLabelsAndPreservesSignedNumbers() {
        assertEquals("\"'=1+1\"", HistoryData.csvCell("=1+1")); assertEquals("\"-2.5\"", HistoryData.csvCell(-2.5));
        assertEquals("\"a,\"\"b\"\"\ntext\"", HistoryData.csvCell("a,\"b\"\ntext")); assertEquals("", HistoryData.csvCell(JSONObject.NULL));
    }
    @Test public void environmentalExtremaAndCsvReceiptTimesKeepTheirMeaning() throws Exception {
        JSONObject definition = metric("temperature-test", "pico", "temperature", "", "", 202).put("units", new JSONObject().put("value", "°C"));
        HistoryData.Record row = new HistoryData.Record(new JSONObject().put("start", "2026-01-01T00:00:00Z").put("end", "2026-01-01T01:00:00Z").put("value", 7.2).put("min", -1.5).put("max", 8.4).put("lastAt", Instant.parse("2026-01-01T00:00:12Z").toEpochMilli()));
        assertEquals(-1.5, row.value("min", "temperature"), 0); assertEquals(8.4, row.value("max", "temperature"), 0);
        HistoryData.Result result = new HistoryData.Result(new MonitorData.Metric(definition), new HistoryData.Window(row.start, row.end, "hour"), java.util.Collections.singletonList(row), "2026-01-01T01:00:00Z");
        String csv = HistoryData.csv(result, "Outside alias"); assertTrue(csv.contains("\"2026-01-01T00:00:12Z\"")); assertTrue(csv.contains("\"°C\"")); assertTrue(csv.contains("\"Outside alias\""));
    }
    @Test public void originCannotCarryCredentialsOrRoutes() {
        assertEquals("http://pi.example:8080", ApiClient.validateBase("http://pi.example:8080/"));
        for (String u : new String[]{"http://user:secret@pi.example", "http://pi.example/api/v1", "http://pi.example?token=secret", "file:///tmp/test", "http://pi.example:0", "https://pi.example#x"}) {
            try { ApiClient.validateBase(u); fail(u); } catch (IllegalArgumentException expected) { assertFalse(expected.getMessage().contains("secret")); }
        }
    }
}
