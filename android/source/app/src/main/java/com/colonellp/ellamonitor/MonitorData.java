package com.colonellp.ellamonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Source units stay unchanged; display aliases never participate in binding. */
final class MonitorData {
    static Double number(JSONObject o, String key) {
        Object v = o == null ? null : o.opt(key);
        if (!(v instanceof Number)) return null;
        double d = ((Number)v).doubleValue(); return Double.isFinite(d) ? d : null;
    }
    static JSONObject object(JSONObject o, String key) { JSONObject v = o == null ? null : o.optJSONObject(key); return v == null ? new JSONObject() : v; }
    static final class Metric {
        final JSONObject definition;
        final String id, name, kind, source, role, channel, alias;
        Metric(JSONObject d) {
            definition = d; id = d.optString("id"); name = d.optString("name", id); kind = d.optString("kind"); source = d.optString("source");
            role = d.optString("role"); channel = d.optString("batteryChannel", "");
            alias = "metric:" + id + ":" + source + ":" + d.optString("sensorId") + ":" + d.optString("sensorType") + ":" + d.optString("field");
        }
    }
    static List<Metric> catalogue(JSONObject body) throws Exception {
        if (body.optInt("schemaVersion") != 2) throw new Exception("Unsupported history schema.");
        JSONArray array = body.getJSONArray("metrics"); if (array.length() > 64) throw new Exception("Too many metrics.");
        List<Metric> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Metric m = new Metric(array.getJSONObject(i));
            if (!m.id.matches("[a-zA-Z0-9_-]{1,80}")) throw new Exception("Invalid metric identifier.");
            if (result.stream().anyMatch(x -> x.id.equals(m.id))) throw new Exception("Duplicate metric identifier.");
            result.add(m);
        }
        return result;
    }
    static boolean fresh(JSONObject source, double elapsedSeconds, double limit) {
        Double age = number(source, "ageSeconds");
        return source != null && source.optBoolean("fresh", false) && "connected".equals(source.optString("state"))
                && age != null && age >= 0 && elapsedSeconds >= 0 && age + elapsedSeconds <= limit;
    }
    static final class Datum {
        final String id, name, unit, group, quantity;
        final Double value;
        final Boolean flag;
        final Metric metric;
        final String receivedAt;
        Datum(String id, String name, String unit, String group, String quantity, Double value, Boolean flag, Metric metric, String receivedAt) {
            this.id = id; this.name = name; this.unit = unit; this.group = group; this.quantity = quantity;
            this.value = value; this.flag = flag; this.metric = metric; this.receivedAt = receivedAt;
        }
    }
    static Map<String, Datum> display(List<Metric> metrics, JSONObject live, double elapsed) {
        Map<String, Datum> result = new LinkedHashMap<>();
        JSONObject pico = object(live, "pico"), sbms = object(live, "sbms");
        boolean pFresh = fresh(pico, elapsed, 2), sFresh = fresh(sbms, elapsed, 3);
        JSONObject rawPico = object(pico, "readings"), rawSbms = object(object(sbms, "reading"), "broadcast");
        JSONArray measured = live == null ? null : live.optJSONArray("measurements");
        double picoLoad = 0; boolean allLoads = true; int loadCount = 0;
        for (Metric m : metrics) {
            JSONObject record = new JSONObject();
            if (measured != null) for (int i = 0; i < measured.length(); i++) {
                JSONObject r = measured.optJSONObject(i); if (r != null && m.id.equals(r.optString("id"))) { record = r; break; }
            }
            boolean available = (m.source.equals("pico") ? pFresh : sFresh) && record.optBoolean("fresh") && record.optBoolean("mappingValid");
            JSONObject values = object(record, "values");
            String group = m.kind.equals("temperature") ? "temps" : m.kind.equals("barometer") ? "environment" : m.kind.equals("electrical") && m.role.equals("load") && m.source.equals("pico") ? "loads" : "battery";
            String[] fields = m.kind.equals("electrical") ? (m.role.equals("battery") ? new String[]{"current", "watts", "voltage", "stateOfCharge"} : new String[]{"current", "watts", "voltage"}) : new String[]{m.kind.equals("barometer") ? "pressure" : m.kind};
            for (String field : fields) {
                Double value = available ? number(values, field) : null;
                if ((field.equals("watts") || field.equals("voltage")) && m.definition.optString("voltage").equals("sbms") && !sFresh) value = null;
                String unit = field.equals("current") ? "A" : field.equals("watts") ? "W" : field.equals("stateOfCharge") ? "%" : field.equals("temperature") ? "°C" : field.equals("pressure") ? "hPa" : "V";
                add(result, m.alias + ":" + field, m.name + " · " + fieldName(field), unit, group, field, value, null, m, record.optString("receivedAt", ""));
            }
            if (group.equals("loads")) {
                loadCount++; Double v = available ? number(values, "current") : null, polarity = number(m.definition, "polarity");
                if (v == null || polarity == null || !(polarity == 1 || polarity == -1)) allLoads = false;
                else picoLoad += v * polarity; // Verified consumption orientation, never abs(raw current).
            }
            if (m.channel.equals("picoBattery")) {
                JSONObject raw = object(rawPico, m.definition.optString("sensorId"));
                for (String field : new String[]{"capacity.remaining", "capacity.timeRemaining"}) add(result, "battery:" + field, field.endsWith("timeRemaining") ? "Runtime · Pico estimate" : "Remaining capacity · Pico", field.endsWith("timeRemaining") ? "s" : "Ah", "summary", field, available ? number(raw, field) : null, null, null, pico.optString("receivedAt"));
            }
        }
        add(result, "pico:load-sum", "Load Σ · Pico", "A", "battery", "current", allLoads && loadCount > 0 ? picoLoad : null, null, null, pico.optString("receivedAt"));
        JSONObject flags = object(object(sbms, "reading"), "flags");
        for (String key : new String[]{"CFET", "DFET", "OVLK", "UVLK", "EOC", "IOT", "LVC", "CELF"}) {
            Object value = flags.opt(key); add(result, "flag:" + key, key, "", "flags", key, null, sFresh && value instanceof Boolean ? (Boolean)value : null, null, sbms.optString("receivedAt"));
        }
        // Only the safe Pico decoder's metadata/readings, never MQTT label re-parsing.
        java.util.Iterator<String> keys = rawPico.keys(); int count = 0;
        while (keys.hasNext() && count++ < 256) {
            String sensorId = keys.next(); JSONObject sensor = rawPico.optJSONObject(sensorId); if (sensor == null) continue;
            String type = sensor.optString("type"), name = sensor.optString("name", sensorId);
            // Conservative fingerprint includes available configuration metadata, not positional ID alone.
            String binding = rawBinding(sensorId, sensor);
            String field = type.equals("thermometer") ? "temperature" : type.equals("barometer") ? "pressure" : type.equals("inclinometer") ? "degree" : type.equals("tank") ? "percentage" : null;
            if (field == null) continue;
            Metric selected = null;
            for (Metric m : metrics) if (m.source.equals("pico") && m.definition.optString("sensorId").equals(sensorId)) selected = m;
            if (selected != null && !type.equals("tank")) continue;
            String group = type.equals("thermometer") ? "temps" : "environment";
            String unit = type.equals("thermometer") ? "°C" : type.equals("barometer") ? "hPa" : type.equals("inclinometer") ? "°" : "%";
            add(result, binding + ":" + field, name, unit, group, field, pFresh ? number(sensor, field) : null, null, null, pico.optString("receivedAt"));
            if (type.equals("tank")) add(result, binding + ":litres", name + " · remaining", "L", group, "remainingCapacity", pFresh ? number(sensor, "remainingCapacity") : null, null, null, pico.optString("receivedAt"));
        }
        for (String f : new String[]{"tempInt", "tempExt"}) add(result, "sbms:" + f, f.equals("tempInt") ? "SBMS internal" : "SBMS external", "°C", "temps", f, sFresh ? number(rawSbms, f) : null, null, null, sbms.optString("receivedAt"));
        JSONObject rawFlags = object(rawSbms, "flags");
        add(result, "sbms:delta", "Cell Δ", "mV", "summary", "delta", sFresh ? number(rawFlags, "delta") : null, null, null, sbms.optString("receivedAt"));
        JSONArray cells = rawSbms.optJSONArray("cellsMV");
        for (int i = 0; i < 8; i++) {
            Object cell = cells == null ? null : cells.opt(i); Double v = sFresh && cell instanceof Number && ((Number)cell).doubleValue() > 0 && ((Number)cell).doubleValue() <= 10000 ? ((Number)cell).doubleValue() / 1000 : null;
            add(result, "cell:" + i, "Cell " + (i + 1), "V", "cells", "voltage", v, null, null, sbms.optString("receivedAt"));
        }
        return result;
    }
    static String rawBinding(String id, JSONObject sensor) {
        List<String> keys = new ArrayList<>(); sensor.keys().forEachRemaining(keys::add); java.util.Collections.sort(keys);
        StringBuilder fingerprint = new StringBuilder("raw:" + id + ":");
        for (String k : keys) if (!java.util.Arrays.asList("pressure", "temperature", "voltage", "current", "degree", "ohm", "currentLevel", "remainingCapacity", "percentage", "stateOfCharge", "capacity.remaining", "capacity.timeRemaining", "capacity.nominal").contains(k)) fingerprint.append(k).append('=').append(sensor.opt(k)).append(';');
        return fingerprint.toString();
    }
    static String fieldName(String field) { return field.equals("stateOfCharge") ? "SOC" : field.equals("watts") ? "power" : field; }
    private static void add(Map<String, Datum> r, String id, String name, String unit, String group, String q, Double v, Boolean f, Metric m, String at) { r.put(id, new Datum(id, name, unit, group, q, v, f, m, at)); }
}
