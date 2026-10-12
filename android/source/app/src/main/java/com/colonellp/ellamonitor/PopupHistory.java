package com.colonellp.ellamonitor;

import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;

/** Calendar layout and read-only plot calculations. Whole stored intervals are never prorated. */
final class PopupHistory {
    static boolean positiveCurrent(MonitorData.Metric metric,String field){return metric!=null&&metric.kind.equals("electrical")&&!metric.role.equals("battery")&&Arrays.asList("current","watts","Ah","Wh").contains(field);}
    static Double displayValue(MonitorData.Metric metric,String field,Double value){if(value==null)return null;return positiveCurrent(metric,field)?Math.abs(value):value;}
    static final class Plan {
        final HistoryData.Window query;
        final Instant end;
        final ZoneId zone;
        final int period;final Instant now;
        Plan(HistoryData.Window q,Instant e,ZoneId z,int p,Instant n){query=q;end=e;zone=z;period=p;now=n;}
    }
    static Plan plan(Instant now,PopupSelection s,ZoneId zone){
        ZonedDateTime at=now.atZone(zone),start;
        if(s.period==0)start=at.truncatedTo(ChronoUnit.HOURS);
        else if(s.period==1)start=at.toLocalDate().atStartOfDay(zone);
        else if(s.period==2)start=at.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zone);
        else start=YearMonth.from(at).atDay(1).atStartOfDay(zone);
        if(s.fixed)start=s.period==0?start.minusHours(1):s.period==1?start.minusDays(1):s.period==2?start.minusWeeks(1):
            (s.month.isEmpty()?YearMonth.from(at).minusMonths(1):YearMonth.parse(s.month)).atDay(1).atStartOfDay(zone);
        ZonedDateTime end=s.period==0?start.plusHours(1):s.period==1?start.plusDays(1):s.period==2?start.plusWeeks(1):start.plusMonths(1);
        return new Plan(new HistoryData.Window(start.toInstant(),s.fixed?end.toInstant():now,s.period==0?"minute":s.period<3?"hour":"day"),end.toInstant(),zone,s.period,now);
    }
    static String monthLabel(YearMonth month,Instant now,ZoneId zone){return month.format(DateTimeFormatter.ofPattern("MMMM",Locale.UK))+(month.getYear()==now.atZone(zone).getYear()?"":" '"+String.format(Locale.UK,"%02d",month.getYear()%100));}
    private static String date(LocalDate date){int d=date.getDayOfMonth();String suffix=d%100>=11&&d%100<=13?"th":d%10==1?"st":d%10==2?"nd":d%10==3?"rd":"th";return d+suffix+" "+date.format(DateTimeFormatter.ofPattern("MMMM",Locale.UK));}
    static String caption(Plan p,boolean fixed){
        ZonedDateTime from=p.query.from.atZone(p.zone),to=p.query.to.atZone(p.zone);
        if(!fixed){double seconds=Math.max(0,Duration.between(p.query.from,p.query.to).toMillis()/1000d);double size=p.period==0?60:p.period==1?3600:86400;return String.format(Locale.UK,"%.1f %s",seconds/size,p.period==0?"minutes":p.period==1?"hours":"days");}
        if(p.period==0){DateTimeFormatter f=DateTimeFormatter.ofPattern("ha",Locale.UK);return from.format(f).toLowerCase(Locale.UK)+" to "+to.format(f).toLowerCase(Locale.UK);}
        if(p.period==1)return from.format(DateTimeFormatter.ofPattern("EEEE",Locale.UK))+" "+date(from.toLocalDate());
        if(p.period==2)return date(from.toLocalDate())+" to "+date(to.toLocalDate().minusDays(1));
        return monthLabel(YearMonth.from(from),p.now,p.zone);
    }
    static boolean supports(MonitorData.Metric metric,String resolution){
        if(metric==null)return false;
        if(DerivedHistory.isDerived(metric)){JSONArray a=metric.definition.optJSONArray("components");if(a==null)return false;for(int i=0;i<a.length();i++)if(!supports(new MonitorData.Metric(MonitorData.object(a.optJSONObject(i),"metric")),resolution))return false;return true;}
        JSONArray supported=metric.definition.optJSONArray("resolutions");if(supported!=null){for(int i=0;i<supported.length();i++)if(resolution.equals(supported.optString(i)))return true;return false;}
        return !(metric.kind.equals("temperature")||metric.kind.equals("barometer"))||!resolution.equals("minute");
    }
    static MonitorData.Metric binding(String id,MonitorData.Datum d,List<MonitorData.Metric> metrics){
        if(d!=null&&d.metric!=null)return d.metric;
        String sensor="";
        if(id.equals("battery:pico-voltage")){for(MonitorData.Metric m:metrics)if(m.channel.equals("picoBattery"))sensor=m.definition.optString("sensorId");}
        else if(d!=null&&d.quantity.equals("starterVoltage")&&id.startsWith("raw:"))sensor=id.substring(4,id.indexOf(':',4));
        if(!sensor.isEmpty())for(MonitorData.Metric m:metrics)if(m.source.equals("pico")&&sensor.equals(m.definition.optString("sensorId"))&&
            (m.kind.equals("voltage")||m.kind.equals("electrical")&&m.definition.optString("voltage").equals("self")))return m;
        return null;
    }
    static String title(String id,MonitorData.Datum d,String fallback){
        if(id.equals("battery:pico-voltage"))return "Voltage Pico";
        if(d!=null&&d.quantity.equals("starterVoltage"))return "Voltage Starter Battery";
        if(id.equals("battery:capacity.remaining"))return "Remaining Battery Capacity";
        if(id.equals("battery:capacity.timeRemaining"))return "Time Remaining";
        if(id.startsWith("cell:"))try{return "Cell ["+(Integer.parseInt(id.substring(5))+1)+"]";}catch(Exception ignored){}
        if(d!=null&&d.metric!=null&&d.metric.channel.equals("sbmsBattery")&&d.quantity.equals("voltage"))return "SBMS0 Voltage";
        if(d!=null&&d.unit.equals("°C"))return fallback.toLowerCase(Locale.UK).endsWith("temperature")?fallback:fallback+" temperature";
        return fallback.isEmpty()&&d!=null?d.name:fallback;
    }
    static List<YearMonth> months(ApiClient client,MonitorData.Metric metric,Instant now,ZoneId zone)throws Exception {return months(client,metric,now,zone,"current");}
    static List<YearMonth> months(ApiClient client,MonitorData.Metric metric,Instant now,ZoneId zone,String field)throws Exception {
        if(DerivedHistory.isDerived(metric)){
            Set<YearMonth> common=null;JSONArray c=metric.definition.getJSONArray("components");for(int i=0;i<c.length();i++){
                Set<YearMonth> available=new TreeSet<>(months(client,new MonitorData.Metric(c.getJSONObject(i).getJSONObject("metric")),now,zone,field));if(common==null)common=available;else common.retainAll(available);
            }List<YearMonth> result=new ArrayList<>(common==null?Collections.emptySet():common);result.sort(Collections.reverseOrder());return result;
        }
        String resolution="day";
        HistoryData.Result result=HistoryData.fetch(client,metric,new HistoryData.Window(Instant.EPOCH,now,resolution));Set<YearMonth> available=new TreeSet<>(Collections.reverseOrder());
        for(HistoryData.Record row:result.rows){Double coverage=row.coverage(field,metric.kind);if(row.value(field,metric.kind)!=null&&(coverage==null||coverage>0))available.add(YearMonth.from(row.start.atZone(zone)));}return new ArrayList<>(available);
    }
    static final class Point {
        final Instant start,end;final double value;final boolean complete;
        Point(Instant s,Instant e,double v,boolean c){start=s;end=e;value=v;complete=c;}
    }
    static final class Series {
        final List<Point> points=new ArrayList<>();long captured,expected;String unit;boolean cumulative;
    }
    private static final class Bin {Instant start,end;double sum,coverage;long rows;Double last;boolean complete=true;}
    static Series series(HistoryData.Result data,Plan plan,String field,boolean cumulative,boolean positive){
        Series result=new Series();result.expected=SummaryPeriods.expected(data.window,plan.zone);result.cumulative=cumulative;
        result.unit=cumulative?(field.equals("watts")?"Wh":"Ah"):field.equals("watts")?"W":field.equals("current")?"A":field.equals("stateOfCharge")?"%":field.equals("voltage")?"V":data.metric.kind.equals("barometer")?"hPa":"°C";
        Map<Instant,Bin> bins=new TreeMap<>();Set<Instant> counted=new HashSet<>();
        for(HistoryData.Record row:data.rows){
            if(!SummaryPeriods.included(row,data))continue;Double value=row.value(field,data.metric.kind),coverage=row.coverage(field,data.metric.kind);
            if(value==null||coverage!=null&&coverage<=0||cumulative&&coverage==null)continue;
            Instant effective=row.end;if(row.partial)try{effective=Instant.parse(data.savedAt);if(effective.isAfter(row.end))effective=row.end;}catch(Exception e){continue;}
            if(effective.isAfter(data.window.to))continue;
            Instant start=row.start;
            if(plan.period==2){ZonedDateTime at=start.atZone(plan.zone);start=at.toLocalDate().atTime(at.getHour()/6*6,0).atZone(plan.zone).toInstant();}
            Bin bin=bins.get(start);if(bin==null){bin=new Bin();bin.start=start;bins.put(start,bin);}
            bin.end=effective;bin.rows++;if(counted.add(row.start))result.captured++;
            double duration=Duration.between(row.start,effective).toMillis()/1000d;
            bin.complete&=coverage==null||Math.abs(coverage-duration)<.05;
            if(cumulative){if(coverage==null)continue;Double integral=row.value(field.equals("watts")?"Wh":"Ah",data.metric.kind);if(integral==null)integral=value*coverage/3600;bin.sum+=integral;}
            else if(coverage!=null){bin.sum+=value*coverage;bin.coverage+=coverage;}
            else bin.last=value;
        }
        double total=0;for(Bin bin:bins.values()){
            boolean complete=bin.complete;
            if(plan.period==2){ZonedDateTime at=bin.start.atZone(plan.zone);int next=at.getHour()+6;Instant end=next==24?at.toLocalDate().plusDays(1).atStartOfDay(plan.zone).toInstant():at.toLocalDate().atTime(next,0).atZone(plan.zone).toInstant();if(end.isAfter(data.window.to))end=data.window.to;long expected=(long)Math.ceil(Duration.between(bin.start,end).toMillis()/3600000d);complete&=bin.rows==expected;}
            double value=cumulative?(total+=bin.sum):bin.last!=null?bin.last:bin.sum/bin.coverage;
            if(Double.isFinite(value))result.points.add(new Point(bin.start,bin.end,positive?Math.abs(value):value,complete));
        }return result;
    }
    static double tickStep(double min,double max){double target=Math.max(1,(max-min)/4),unit=Math.pow(10,Math.floor(Math.log10(target)));double factor=target/unit;return Math.max(1,(factor<=1?1:factor<=2?2:factor<=5?5:10)*unit);}
}
