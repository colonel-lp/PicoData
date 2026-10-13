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
        HistoryData.Result result=HistoryData.fetch(client,metric,new HistoryData.Window(Instant.EPOCH,now,"day"));
        Set<YearMonth> available=new TreeSet<>(Collections.reverseOrder());
        for(HistoryData.Record row:result.rows){Double coverage=row.coverage(field,metric.kind);if(row.value(field,metric.kind)!=null&&(coverage==null||coverage>0))available.add(YearMonth.from(row.start.atZone(zone)));}
        return new ArrayList<>(available);
    }
    /** Calendar boundaries, not fixed 24-hour delays. Explicit older months stay selected. */
    static Instant nextRefresh(Instant now,PopupSelection s,ZoneId zone){
        ZonedDateTime at=now.atZone(zone);
        if(s.period==0)return s.fixed?at.truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant():at.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1).toInstant();
        if(s.fixed){
            if(s.period==1)return at.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant();
            if(s.period==2)return at.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay(zone).toInstant();
            return s.month.isEmpty()?YearMonth.from(at).plusMonths(1).atDay(1).atStartOfDay(zone).toInstant():null;
        }
        if(s.period<3)return at.truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant();
        Instant local=at.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant(),utc=now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return local.isBefore(utc)?local:utc; // Also consume finalized UTC daily checkpoints.
    }
    static boolean awaitingFinalCheckpoint(HistoryData.Result data,PopupSelection selection){
        if(!selection.fixed)return false;try{return Instant.parse(data.savedAt).isBefore(data.window.to);}catch(Exception e){return true;}
    }
    static String dayTick(Instant at,Plan plan){return at.equals(plan.end)?"24":DateTimeFormatter.ofPattern("H",Locale.UK).withZone(plan.zone).format(at);}
    static final class Point {
        final Instant start,end,fullEnd;final double value;final boolean complete;final Double low,high;
        Point(Instant s,Instant e,double v,boolean c){this(s,e,e,v,c,null,null);}
        Point(Instant s,Instant e,Instant f,double v,boolean c,Double l,Double h){start=s;end=e;fullEnd=f;value=v;complete=c;low=l;high=h;}
    }
    static final class Series {
        final List<Point> points=new ArrayList<>();long captured,expected;String unit;boolean cumulative,temperature,usage;Double minimum,maximum,latest;
    }
    private static final class Bin {Instant start,end,fullEnd;double sum,coverage;long rows;Double last,low,high;boolean complete=true;}
    static Series series(HistoryData.Result data,Plan plan,String field,boolean cumulative,boolean positive){return series(data,plan,field,cumulative,positive,false);}
    static Series series(HistoryData.Result data,Plan plan,String field,boolean cumulative,boolean positive,boolean bars){
        boolean integrated=data.metric.kind.equals("electrical")&&(field.equals("current")||field.equals("watts"));
        Series result=new Series();result.expected=SummaryPeriods.expected(data.window,plan.zone);result.cumulative=integrated&&cumulative;result.temperature=data.metric.kind.equals("temperature");result.usage=integrated&&positive&&data.metric.role.equals("load");
        result.unit=integrated?(field.equals("watts")?"Wh":"Ah"):field.equals("stateOfCharge")?"%":field.equals("voltage")?"V":data.metric.kind.equals("barometer")?"hPa":"°C";
        Map<Instant,Bin> bins=new TreeMap<>();Set<Instant> counted=new HashSet<>();int groupHours=bars?12:6;
        for(HistoryData.Record row:data.rows){
            if(!SummaryPeriods.included(row,data)||!counted.add(row.start))continue;
            Double value=row.value(field,data.metric.kind),coverage=row.coverage(field,data.metric.kind),low=result.temperature?row.value("min",data.metric.kind):null,high=result.temperature?row.value("max",data.metric.kind):null;
            if(result.temperature?low==null&&high==null:value==null||coverage!=null&&coverage<=0||integrated&&coverage==null)continue;
            Instant effective=row.end;if(row.partial)try{effective=Instant.parse(data.savedAt);if(effective.isAfter(row.end))effective=row.end;}catch(Exception e){continue;}
            if(!effective.isAfter(row.start)||effective.isAfter(data.window.to))continue;
            Instant start=row.start,fullEnd=row.end;
            if(plan.period==2){ZonedDateTime at=start.atZone(plan.zone);int hour=at.getHour()/groupHours*groupHours;start=at.toLocalDate().atTime(hour,0).atZone(plan.zone).toInstant();fullEnd=hour+groupHours==24?at.toLocalDate().plusDays(1).atStartOfDay(plan.zone).toInstant():at.toLocalDate().atTime(hour+groupHours,0).atZone(plan.zone).toInstant();}
            Bin bin=bins.get(start);if(bin==null){bin=new Bin();bin.start=start;bins.put(start,bin);}bin.end=effective;bin.fullEnd=fullEnd;bin.rows++;result.captured++;
            double duration=Duration.between(row.start,effective).toMillis()/1000d;bin.complete&=coverage==null||Math.abs(coverage-duration)<.05;bin.complete&=!result.temperature||low!=null&&high!=null;
            if(integrated){Double integral=row.value(field.equals("watts")?"Wh":"Ah",data.metric.kind);if(integral==null)integral=value*coverage/3600;bin.sum+=positive?Math.abs(integral):integral;}
            else if(result.temperature){if(low!=null)bin.low=bin.low==null?low:Math.min(bin.low,low);if(high!=null)bin.high=bin.high==null?high:Math.max(bin.high,high);}
            else if(coverage!=null){bin.sum+=value*coverage;bin.coverage+=coverage;}
            else bin.last=value;
            if(value!=null)result.latest=value;
            if(low!=null)result.minimum=result.minimum==null?low:Math.min(result.minimum,low);
            if(high!=null)result.maximum=result.maximum==null?high:Math.max(result.maximum,high);
        }
        double total=0;for(Bin bin:bins.values()){
            boolean complete=bin.complete;
            if(plan.period==2){Instant end=bin.fullEnd.isAfter(data.window.to)?data.window.to:bin.fullEnd;long expected=(long)Math.ceil(Duration.between(bin.start,end).toMillis()/3600000d);complete&=bin.rows==expected;}
            double value=integrated?(result.cumulative?(total+=bin.sum):bin.sum):result.temperature?(bin.high!=null?bin.high:bin.low):bin.last!=null?bin.last:bin.sum/bin.coverage;
            if(Double.isFinite(value))result.points.add(new Point(bin.start,bin.end,bin.fullEnd,value,complete,bin.low,bin.high));
        }
        return result;
    }
    static double[] axisRange(Series data,boolean pressure){
        if(pressure)return new double[]{900,1100,50};double min=0,max=0;
        if(data!=null)for(Point p:data.points){min=Math.min(min,p.value);max=Math.max(max,p.value);if(p.low!=null)min=Math.min(min,p.low);if(p.high!=null)max=Math.max(max,p.high);}
        double step=tickStep(min,max);return new double[]{Math.floor(min/step)*step,Math.max(step,Math.ceil(max/step)*step),step};
    }
    static double tickStep(double min,double max){double target=Math.max(1,(max-min)/4),unit=Math.pow(10,Math.floor(Math.log10(target)));double factor=target/unit;return Math.max(1,(factor<=1?1:factor<=2?2:factor<=5?5:10)*unit);}
}
