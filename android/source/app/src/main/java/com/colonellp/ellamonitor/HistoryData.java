package com.colonellp.ellamonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

final class HistoryData {
    static final String[] RANGES = {"6h", "12h", "24h", "Week", "Month"};
    static final class Window {
        final Instant from, to;
        final String resolution;
        Window(Instant from, Instant to, String resolution) { this.from = from; this.to = to; this.resolution = resolution; }
    }
    static Window window(Instant now, int range, boolean defined, int offset, String kind) {
        ZonedDateTime at = now.atZone(ZoneOffset.UTC), end = at;
        ZonedDateTime start;
        if (!defined) {
            long hours = range < 3 ? new long[]{6, 12, 24}[range] : range == 3 ? 168 : 720;
            end = at.plusHours(hours * offset); start = end.minusHours(hours);
        } else if (range < 3) {
            int hours = new int[]{6, 12, 24}[range];
            start = at.truncatedTo(ChronoUnit.DAYS).plusHours((at.getHour() / hours) * hours + (long)hours * offset);
            end = start.plusHours(hours);
        } else if (range == 3) {
            start = at.truncatedTo(ChronoUnit.DAYS).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset); end = start.plusWeeks(1);
        } else {
            start = at.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).plusMonths(offset); end = start.plusMonths(1);
        }
        String resolution = range < 3 ? "minute" : range == 3 ? "hour" : "day";
        if (kind.equals("temperature") || kind.equals("barometer")) resolution = range < 4 ? "hour" : "day";
        return new Window(start.toInstant(), end.toInstant(), resolution);
    }
    static String route(MonitorData.Metric metric, Window window) throws Exception {
        // Include the intersecting first bucket, labelled as a boundary bucket, never prorated.
        Instant from = window.from.truncatedTo(window.resolution.equals("minute") ? ChronoUnit.MINUTES : window.resolution.equals("hour") ? ChronoUnit.HOURS : ChronoUnit.DAYS);
        return "/api/v1/history/metrics/" + metric.id + "?resolution=" + window.resolution + "&from=" + encode(from.toString()) + "&to=" + encode(window.to.toString()) + "&limit=1000";
    }
    static String encode(String v) throws Exception { return URLEncoder.encode(v, "UTF-8"); }
    static final class Record {
        final JSONObject raw;
        final Instant start, end;
        final boolean partial;
        Record(JSONObject r) throws Exception {
            raw = r; start = Instant.parse(r.getString("start")); end = Instant.parse(r.getString("end")); partial = r.optBoolean("partial");
            if (!end.isAfter(start)) throw new Exception("Invalid history interval.");
        }
        Double value(String quantity, String kind) {
            if (quantity.equals("Wh")) return MonitorData.number(raw, "netWh");
            if (quantity.equals("Ah")) return MonitorData.number(raw, "netAh");
            if (!kind.equals("electrical")) return MonitorData.number(raw, quantity.equals("min") || quantity.equals("max") ? quantity : "value");
            JSONArray values = raw.optJSONArray("values"); int index = quantity.equals("watts") ? 0 : quantity.equals("current") ? 1 : quantity.equals("voltage") ? 2 : 3;
            Object v = values == null ? null : values.opt(index);
            return v instanceof Number && Double.isFinite(((Number)v).doubleValue()) ? ((Number)v).doubleValue() : null;
        }
        Double coverage(String quantity, String kind) {
            if (kind.equals("temperature") || kind.equals("barometer") || quantity.equals("stateOfCharge")) return null;
            return MonitorData.number(raw, quantity.equals("watts") || quantity.equals("Wh") ? "powerCoverageSeconds" : quantity.equals("current") || quantity.equals("Ah") ? "ampCoverageSeconds" : "voltCoverageSeconds");
        }
    }
    static final class Result {
        final MonitorData.Metric metric;
        final Window window;
        final List<Record> rows;
        final String savedAt;
        Result(MonitorData.Metric metric, Window window, List<Record> rows, String savedAt) { this.metric = metric; this.window = window; this.rows = rows; this.savedAt = savedAt; }
    }
    static Result fetch(ApiClient client, MonitorData.Metric metric, Window window) throws Exception {
        String route = route(metric, window); List<Record> rows = new ArrayList<>(); String saved = ""; Instant previous = null;
        for (int page = 0; page < 10; page++) {
            JSONObject body = client.get(route);
            if (body.optInt("schemaVersion") != 2 || !metric.id.equals(MonitorData.object(body, "metric").optString("id"))) throw new Exception("History identity mismatch.");
            saved = body.optString("savedAt"); JSONArray array = body.getJSONArray("rows");
            if (array.length() > 1000) throw new Exception("History page limit exceeded.");
            for (int i = 0; i < array.length(); i++) {
                Record record = new Record(array.getJSONObject(i));
                if (previous != null && !record.start.isAfter(previous)) throw new Exception("History pagination did not advance.");
                previous = record.start; rows.add(record);
            }
            JSONObject next = body.optJSONObject("next");
            if (next == null) return new Result(metric, window, rows, saved);
            if (array.length() == 0 || !next.optString("resolution").equals(window.resolution)
                    || !Instant.parse(next.getString("to")).equals(window.to)
                    || !Instant.parse(next.getString("from")).isAfter(previous)) throw new Exception("Invalid history continuation.");
            route = "/api/v1/history/metrics/" + metric.id + "?resolution=" + window.resolution + "&from=" + encode(next.getString("from")) + "&to=" + encode(next.getString("to")) + "&limit=1000";
        }
        throw new Exception("History range too large; select a shorter range.");
    }
    static String csv(Result result, String label) {
        StringBuilder text = new StringBuilder("display_label,metric_id,original_name,source,source_name,sensor_id,field,role,polarity,voltage_source,resolution,start_utc,end_utc,partial,boundary_bucket,average_W,average_A,average_V,soc_percent,value,value_unit,min,max,net_Wh,net_Ah,forward_Wh,reverse_Wh,forward_Ah,reverse_Ah,power_coverage_s,current_coverage_s,voltage_coverage_s,samples,last_scalar_received_utc,current_received_utc,voltage_received_utc,soc_received_utc,saved_at_utc\r\n");
        MonitorData.Metric m = result.metric;
        for (Record r : result.rows) {
            JSONArray v = r.raw.optJSONArray("values");
            Object[] fields = {label, m.id, m.name, m.source, m.definition.opt("sourceName"), m.definition.opt("sensorId"), m.definition.opt("field"), m.role, m.definition.opt("polarity"), m.definition.opt("voltage"), result.window.resolution, r.start, r.end, r.partial, r.start.isBefore(result.window.from) || r.end.isAfter(result.window.to), v == null ? null : v.opt(0), v == null ? null : v.opt(1), v == null ? null : v.opt(2), v == null ? null : v.opt(3), r.raw.opt("value"), MonitorData.object(m.definition, "units").opt("value"), r.raw.opt("min"), r.raw.opt("max"), r.raw.opt("netWh"), r.raw.opt("netAh"), r.raw.opt("forwardWh"), r.raw.opt("reverseWh"), r.raw.opt("forwardAh"), r.raw.opt("reverseAh"), r.raw.opt("powerCoverageSeconds"), r.raw.opt("ampCoverageSeconds"), r.raw.opt("voltCoverageSeconds"), r.raw.opt("samples"), receipt(r.raw, "lastAt"), receipt(r.raw, "currentLastAt"), receipt(r.raw, "voltageLastAt"), receipt(r.raw, "socAt"), result.savedAt};
            for (int i = 0; i < fields.length; i++) { if (i > 0) text.append(','); text.append(csvCell(fields[i])); }
            text.append("\r\n");
        }
        return text.toString();
    }
    private static Object receipt(JSONObject record, String field) {
        Double value = MonitorData.number(record, field); if (value == null) return null;
        try { return Instant.ofEpochMilli(value.longValue()).toString(); } catch (Exception e) { return null; }
    }
    static String csvCell(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        String s = value.toString();
        // Prevent user labels / source names becoming spreadsheet formulae; signed numbers remain numbers.
        if (!(value instanceof Number) && s.matches("(?s)^[\\s]*[=+\\-@].*")) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
