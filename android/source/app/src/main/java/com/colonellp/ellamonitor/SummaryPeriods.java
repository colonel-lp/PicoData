package com.colonellp.ellamonitor;

import java.time.*;
import java.time.temporal.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Calendar-to-now summaries and recorded history-entry counts. Storage stays UTC. */
final class SummaryPeriods {
    static HistoryData.Window window(Instant now,int period,boolean fixed,ZoneId zone) {
        ZonedDateTime at=now.atZone(zone),start;
        if(period==0)start=at.truncatedTo(ChronoUnit.HOURS);
        else if(period==1)start=at.toLocalDate().atStartOfDay(zone);
        else if(period==2)start=at.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zone);
        else start=at.toLocalDate().withDayOfMonth(1).atStartOfDay(zone);
        ZonedDateTime end=at;
        if(fixed){end=start;start=period==0?start.minusHours(1):period==1?start.minusDays(1):period==2?start.minusWeeks(1):start.minusMonths(1);}
        String resolution=period==0||period==1&&!fixed?"minute":period==1||period==2&&!fixed?"hour":"day";
        return new HistoryData.Window(start.toInstant(),end.toInstant(),resolution);
    }
    static String label(Instant now,int period,boolean fixed,ZoneId zone){
        if(period==0)return fixed?"Last hour":"This hour";
        if(period==1)return fixed?"Yesterday":"Today";
        if(period==2)return fixed?"Last week":"This week";
        return now.atZone(zone).minusMonths(fixed?1:0).format(DateTimeFormatter.ofPattern("MMMM",Locale.UK));
    }
    static long expected(HistoryData.Window window){
        if(!window.to.isAfter(window.from))return 0;
        long size=window.resolution.equals("minute")?60000:window.resolution.equals("hour")?3600000:86400000;
        return Math.floorDiv(window.to.toEpochMilli()-1,size)-Math.floorDiv(window.from.toEpochMilli(),size)+1;
    }
    static boolean included(HistoryData.Record row,HistoryData.Result result){
        if(row.start.isBefore(result.window.from)||!row.start.isBefore(result.window.to))return false;
        if(!row.end.isAfter(result.window.to))return true;
        // A current checkpoint contains no future readings. Closed edge buckets
        // cannot be prorated: omit them instead of counting data outside the period.
        if(!row.partial)return false;
        try{return !Instant.parse(result.savedAt).isAfter(result.window.to);}catch(Exception e){return false;}
    }
    static final class Totals {
        Double charge,energy;
        long captured,expected;
        String text(){return "Current: "+(charge==null?"—":String.format(Locale.UK,"%.3f Ah",charge))+"\nEnergy: "+(energy==null?"—":String.format(Locale.UK,"%.3f Wh",energy));}
        String samples(){return "Samples "+captured+"/"+expected;}
    }
    static Totals totals(HistoryData.Result result){
        Totals total=new Totals();total.expected=expected(result.window);
        Set<Instant> starts=new HashSet<>();
        for(HistoryData.Record row:result.rows){
            if(!included(row,result))continue;
            Double ac=row.coverage("current","electrical"),pc=row.coverage("watts","electrical");
            Double a=row.value("current","electrical"),w=row.value("watts","electrical");
            boolean current=a!=null&&ac!=null&&ac>0,power=w!=null&&pc!=null&&pc>0;
            if(current){Double ah=row.value("Ah","electrical");double value=ah==null?a*ac/3600:ah;total.charge=(total.charge==null?0:total.charge)+value;}
            if(power){Double wh=row.value("Wh","electrical");double value=wh==null?w*pc/3600:wh;total.energy=(total.energy==null?0:total.energy)+value;}
            if((current||power)&&starts.add(row.start))total.captured++;
        }
        return total;
    }
}
