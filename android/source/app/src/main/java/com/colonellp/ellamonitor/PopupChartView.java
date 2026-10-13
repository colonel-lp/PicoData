package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Native popup plot: recorded amounts/ranges, full calendar axes and honest gaps. */
final class PopupChartView extends View {
    static final float LABEL_INSET=6;
    private final ThemeConfig theme;
    private final float scale;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private PopupHistory.Plan plan;
    private PopupHistory.Series data;
    private boolean bars,values,pressure;
    PopupChartView(Context context,ThemeConfig t,float s){super(context);theme=t;scale=s;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void show(PopupHistory.Plan p,PopupHistory.Series s,boolean b){show(p,s,b,true,s!=null&&"hPa".equals(s.unit));}
    void show(PopupHistory.Plan p,PopupHistory.Series s,boolean b,boolean v,boolean barometer){plan=p;data=s;bars=b;values=v;pressure=barometer;invalidate();}
    private float x(Instant at,float left,float right){return left+(float)((double)(at.toEpochMilli()-plan.query.from.toEpochMilli())/(plan.end.toEpochMilli()-plan.query.from.toEpochMilli()))*(right-left);}
    private float y(double value,double min,double max,float top,float bottom){return bottom-(float)((value-min)/(max-min))*(bottom-top);}
    @Override protected void onDraw(Canvas canvas){
        canvas.drawColor(theme.graphBackground);if(plan==null)return;
        float top=14*scale,bottom=getHeight()-30*scale,right=getWidth()-6*scale;
        paint.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));paint.setTextSize(10*scale);paint.setStrokeWidth(scale);paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);paint.setPathEffect(null);
        double[] bounds=PopupHistory.axisRange(data,pressure);double min=bounds[0],max=bounds[1],step=bounds[2];
        float labelWidth=Math.max(paint.measureText(String.format(Locale.UK,"%.0f",min)),paint.measureText(String.format(Locale.UK,"%.0f",max))),left=Math.max(46*scale,(LABEL_INSET+5)*scale+labelWidth);
        if(right<=left||bottom<=top)return;
        for(double value=min;value<=max+step*.01;value+=step){
            float at=y(value,min,max,top,bottom);paint.setColor(theme.grid);canvas.drawLine(left,at,right,at,paint);
            paint.setColor(theme.graphAxes);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(String.format(Locale.UK,"%.0f",value),LABEL_INSET*scale,at+3*scale,paint);canvas.drawLine(left-3*scale,at,left,at,paint);
        }
        paint.setColor(theme.graphAxes);canvas.drawLine(left,top,left,bottom,paint);canvas.drawLine(left,bottom,right,bottom,paint);if(min<0&&max>0)canvas.drawLine(left,y(0,min,max,top,bottom),right,y(0,min,max,top,bottom),paint);
        paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(data==null?"":data.unit,left,10*scale,paint);
        List<Instant> ticks=new ArrayList<>();ZonedDateTime start=plan.query.from.atZone(plan.zone);
        if(plan.period==0)for(int i=0;i<=60;i+=10)ticks.add(plan.query.from.plusSeconds(i*60));
        else if(plan.period==1){for(int i=0;i<24;i+=2)ticks.add(start.toLocalDate().atTime(i,0).atZone(plan.zone).toInstant());ticks.add(plan.end);}
        else {int days=(int)java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(),plan.end.atZone(plan.zone).toLocalDate());for(int i=0;i<=days;i++)ticks.add(start.toLocalDate().plusDays(i).atStartOfDay(plan.zone).toInstant());}
        for(int i=0;i<ticks.size();i++){
            boolean major=plan.period!=3||i%7==0||i==ticks.size()-1;float at=x(ticks.get(i),left,right);
            paint.setColor(major?theme.verticalGrid:ThemeConfig.blend(theme.verticalGrid,theme.graphBackground,.4f));canvas.drawLine(at,top,at,bottom,paint);
            paint.setColor(theme.graphAxes);canvas.drawLine(at,bottom,at,bottom+(major?4:2)*scale,paint);
            if(major){paint.setTextAlign(i==0?Paint.Align.LEFT:i==ticks.size()-1?Paint.Align.RIGHT:Paint.Align.CENTER);
                String label=plan.period==1?PopupHistory.dayTick(ticks.get(i),plan):DateTimeFormatter.ofPattern(plan.period==0?"HH:mm":plan.period==2?"EEE":"d MMM",Locale.UK).withZone(plan.zone).format(ticks.get(i));canvas.drawText(label,at,bottom+18*scale,paint);}
        }
        if(data==null||data.points.isEmpty()){paint.setTextAlign(Paint.Align.CENTER);paint.setColor(theme.text);canvas.drawText("—",(left+right)/2,(top+bottom)/2,paint);return;}
        PopupHistory.Point previous=null;
        for(PopupHistory.Point point:data.points){
            float a=x(point.start,left,right),b=x(point.fullEnd,left,right),at=data.cumulative?x(point.end,left,right):(x(point.start,left,right)+x(point.end,left,right))/2;
            boolean partial=!point.complete||point.end.isBefore(point.fullEnd);
            if(bars){
                double low=data.temperature?(point.low==null?Double.NaN:point.low):pressure?900:0,high=data.temperature?(point.high==null?Double.NaN:point.high):point.value;
                if(Double.isFinite(low)&&Double.isFinite(high)){
                    float lowY=y(low,min,max,top,bottom),highY=y(high,min,max,top,bottom);
                    canvas.save();canvas.clipRect(left,top,right,bottom);paint.setStyle(Paint.Style.FILL);paint.setAlpha(partial?120:210);paint.setColor(data.temperature?theme.frontEq:point.value<0||data.usage?theme.rearEq:theme.frontEq);
                    canvas.drawRect(a+scale,Math.min(lowY,highY),Math.max(a+2*scale,b-scale),Math.max(lowY,highY),paint);paint.setAlpha(255);
                    if(data.temperature){paint.setColor(theme.rearEq);paint.setStrokeWidth(2*scale);canvas.drawLine(a+scale,lowY,b-scale,lowY,paint);paint.setColor(theme.frontEq);canvas.drawLine(a+scale,highY,b-scale,highY,paint);}
                    canvas.restore();
                    if(values){float middle=(a+b)/2;paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(10*scale);paint.setColor(theme.text);float above=Math.max(10*scale,Math.min(bottom+10*scale,highY-4*scale));
                        if(data.temperature){canvas.drawText(String.format(Locale.UK,"%.0f",high),middle,above,paint);canvas.drawText(String.format(Locale.UK,"%.0f",low),middle,Math.min(bottom+12*scale,lowY+11*scale),paint);}
                        else canvas.drawText(String.format(Locale.UK,"%.0f",point.value),middle,point.value<0?Math.min(bottom+12*scale,y(point.value,min,max,top,bottom)+11*scale):above,paint);
                    }
                }
            }else if(data.temperature){
                trace(canvas,previous,point,point.low,previous==null?null:previous.low,theme.rearEq,left,right,top,bottom,min,max,at);
                trace(canvas,previous,point,point.high,previous==null?null:previous.high,theme.frontEq,left,right,top,bottom,min,max,at);
            }else trace(canvas,previous,point,point.value,previous==null?null:previous.value,point.value<0||data.usage?theme.rearEq:theme.frontEq,left,right,top,bottom,min,max,at);
            previous=point;
        }
        paint.setAlpha(255);paint.setStyle(Paint.Style.FILL);
    }
    private void trace(Canvas canvas,PopupHistory.Point previous,PopupHistory.Point point,Double value,Double previousValue,int colour,float left,float right,float top,float bottom,double min,double max,float at){
        if(value==null)return;float atY=y(value,min,max,top,bottom);canvas.save();canvas.clipRect(left,top,right,bottom);paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);paint.setColor(colour);paint.setStrokeWidth(1.6f*scale);
        if(previous!=null&&previousValue!=null&&previous.end.equals(point.start)&&previous.complete&&point.complete){float prevAt=data.cumulative?x(previous.end,left,right):(x(previous.start,left,right)+x(previous.end,left,right))/2;canvas.drawLine(prevAt,y(previousValue,min,max,top,bottom),at,atY,paint);}
        if(previous==null&&data.cumulative&&point.complete&&point.start.equals(plan.query.from))canvas.drawLine(x(point.start,left,right),y(0,min,max,top,bottom),at,atY,paint);
        paint.setColor(theme.graphAxes);canvas.drawCircle(at,atY,1.8f*scale,paint);canvas.restore();
    }
}
