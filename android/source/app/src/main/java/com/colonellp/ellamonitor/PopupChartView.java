package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Full calendar axes; future time and missing history remain empty. */
final class PopupChartView extends View {
    private final ThemeConfig theme;
    private final float scale;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private PopupHistory.Plan plan;
    private PopupHistory.Series data;
    private boolean bars;
    PopupChartView(Context context,ThemeConfig t,float s){super(context);theme=t;scale=s;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void show(PopupHistory.Plan p,PopupHistory.Series s,boolean b){plan=p;data=s;bars=b;invalidate();}
    private float x(Instant at,float left,float right){return left+(float)((double)(at.toEpochMilli()-plan.query.from.toEpochMilli())/(plan.end.toEpochMilli()-plan.query.from.toEpochMilli()))*(right-left);}
    @Override protected void onDraw(Canvas canvas){
        canvas.drawColor(theme.graphBackground);if(plan==null)return;
        float left=46*scale,right=getWidth()-6*scale,top=48*scale,bottom=getHeight()-26*scale;
        if(right<=left||bottom<=top)return;
        paint.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));paint.setTextSize(10*scale);paint.setStrokeWidth(scale);paint.setStyle(Paint.Style.FILL);
        double min=0,max=0;if(data!=null)for(PopupHistory.Point p:data.points){min=Math.min(min,p.value);max=Math.max(max,p.value);}
        double step=PopupHistory.tickStep(min,max);min=Math.floor(min/step)*step;max=Math.max(step,Math.ceil(max/step)*step);
        for(double v=min;v<=max+step*.01;v+=step){float y=bottom-(float)((v-min)/(max-min))*(bottom-top);paint.setColor(v==0?theme.border:theme.grid);canvas.drawLine(left,y,right,y,paint);paint.setColor(theme.text);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(String.format(Locale.UK,"%.0f",v),left-5*scale,y+3*scale,paint);}
        paint.setColor(theme.text);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(data==null?"":data.unit,left,top-8*scale,paint);
        List<Instant> ticks=new ArrayList<>();ZonedDateTime start=plan.query.from.atZone(plan.zone);
        if(plan.period==0)for(int i=0;i<=60;i+=10)ticks.add(plan.query.from.plusSeconds(i*60));
        else if(plan.period==1){for(int i=0;i<24;i+=2)ticks.add(start.toLocalDate().atTime(i,0).atZone(plan.zone).toInstant());ticks.add(plan.end);}
        else {int days=(int)java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(),plan.end.atZone(plan.zone).toLocalDate());for(int i=0;i<=days;i++)ticks.add(start.toLocalDate().plusDays(i).atStartOfDay(plan.zone).toInstant());}
        for(int i=0;i<ticks.size();i++){
            boolean major=plan.period!=3||i%7==0||i==ticks.size()-1;float x=x(ticks.get(i),left,right);
            paint.setColor(major?theme.verticalGrid:ThemeConfig.blend(theme.verticalGrid,theme.graphBackground,.4f));canvas.drawLine(x,top,x,bottom,paint);
            if(major){paint.setColor(theme.text);paint.setTextAlign(i==0?Paint.Align.LEFT:i==ticks.size()-1?Paint.Align.RIGHT:Paint.Align.CENTER);
                String pattern=plan.period<2?"HH:mm":plan.period==2?"EEE":"d MMM";String label=DateTimeFormatter.ofPattern(pattern,Locale.UK).withZone(plan.zone).format(ticks.get(i));canvas.drawText(label,x,bottom+16*scale,paint);}
        }
        if(data==null||data.points.isEmpty()){paint.setTextAlign(Paint.Align.CENTER);paint.setColor(theme.text);canvas.drawText("—",(left+right)/2,(top+bottom)/2,paint);return;}
        canvas.save();canvas.clipRect(left,top,right,bottom);PopupHistory.Point previous=null;
        for(PopupHistory.Point point:data.points){
            float a=x(point.start,left,right),b=x(point.end,left,right),at=data.cumulative?b:(a+b)/2,y=bottom-(float)((point.value-min)/(max-min))*(bottom-top);
            paint.setColor(point.value<0?theme.rearEq:theme.frontEq);
            if(bars){float zero=bottom-(float)((0-min)/(max-min))*(bottom-top);canvas.drawRect(a+scale,Math.min(y,zero),Math.max(a+2*scale,b-scale),Math.max(y,zero),paint);}
            else {if(previous!=null&&previous.end.equals(point.start)&&previous.complete&&point.complete){float prevAt=data.cumulative?x(previous.end,left,right):(x(previous.start,left,right)+x(previous.end,left,right))/2,prevY=bottom-(float)((previous.value-min)/(max-min))*(bottom-top);paint.setStrokeWidth(1.6f*scale);canvas.drawLine(prevAt,prevY,at,y,paint);}canvas.drawCircle(at,y,1.8f*scale,paint);}
            previous=point;
        }canvas.restore();
    }
}
