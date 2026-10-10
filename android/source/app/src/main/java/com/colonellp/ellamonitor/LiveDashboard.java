package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/** Fixed landscape arrangement from the owner's dashboard, scaled by DesignViewport. */
final class LiveDashboard extends ViewGroup {
    interface Host { String label(String id,String original); void detail(MonitorData.Datum d,String name); void rename(String id,String original); boolean locked(); }
    final ThemeConfig theme;
    final Host host;
    private Map<String,MonitorData.Datum> readings;
    private final List<Item> items = new ArrayList<>();
    private final List<View> groups = new ArrayList<>();
    private final List<TextView> titles = new ArrayList<>();
    private final Map<String,Readout> bySlot = new LinkedHashMap<>();
    private final Palette gaugeColors;
    private final java.util.Set<String> knownCells;
    LiveDashboard(Context context,ThemeConfig theme,Map<String,MonitorData.Datum> readings,java.util.Set<String> knownCells,Host host) {
        super(context);this.theme=theme;this.readings=readings;this.host=host;this.knownCells=knownCells;gaugeColors=new Palette(theme);setClipChildren(true);
        group("loads","Current Draw",8,162,true);group("flags","",168,623,false);group("environment","",629,866,false);group("temps","Temps:",872,1016,true);group("summary","",168,866,false);
        for(MonitorData.Datum d:readings.values())if(d.group.equals("loads")&&d.quantity.equals("current"))add("loads",d,clean(d.name),false);
        if(!bySlot.containsKey("loads:0"))add("loads",null,"Waiting for current data",false);
        for(String f:new String[]{"CFET","OVLK","EOC","LVC","DFET","UVLK","IOT","CELF"})add("flags",readings.get("flag:"+f),f,false);
        String[] channels={"externalLoad","pv1","sbmsBattery","sbmsBattery","picoLoad","pv2","picoBattery","picoBattery"};
        String[] names={"Load Σ [BMS]","PV1 [BMS]","Battery [BMS]","SOC [BMS]","Load Σ [Pico]","PV2 [BMS]","Battery [Pico]","SOC [Pico]"};
        for(int i=0;i<channels.length;i++)add("gauges",channels[i].equals("picoLoad")?readings.get("pico:load-sum"):find(channels[i],i%4==3?"stateOfCharge":"current"),names[i],true);
        List<MonitorData.Datum> tanks=new ArrayList<>(),angles=new ArrayList<>();MonitorData.Datum pressure=null;
        for(MonitorData.Datum d:readings.values()){
            if(d.group.equals("temps"))add("temps",d,clean(d.name),false);
            if(d.group.equals("environment")) {if(d.unit.equals("hPa"))pressure=d;else if(d.unit.equals("°"))angles.add(d);else if(d.unit.equals("%")||d.unit.equals("L"))tanks.add(d);}
        }
        add("environment",pressure,"Barometer",true);
        // Each tank occupies one instrument; water litres are paired with its own percentage metadata.
        Map<String,MonitorData.Datum> selectedTanks=new LinkedHashMap<>();
        for(MonitorData.Datum d:tanks)if(d.unit.equals("%"))selectedTanks.put(d.name,d);
        for(MonitorData.Datum d:tanks)if(d.unit.equals("L")){String base=d.name.replace(" · remaining","");if(!base.toLowerCase(Locale.UK).contains("lpg"))selectedTanks.put(base,d);}
        MonitorData.Datum lpg=null,water=null;
        for(Map.Entry<String,MonitorData.Datum> e:selectedTanks.entrySet()){String tankName=e.getKey().toLowerCase(Locale.UK);if(tankName.contains("lpg")&&e.getValue().unit.equals("%"))lpg=e.getValue();else if(e.getValue().unit.equals("L")&&(water==null||tankName.contains("water")))water=e.getValue();}
        add("environment",lpg,lpg==null?"LPG (%)":clean(lpg.name)+" (%)",true);
        add("environment",water,water==null?"Water (l)":clean(water.name)+" (l)",true);
        for(String axis:new String[]{"pitch","roll"}){MonitorData.Datum match=null;for(MonitorData.Datum d:angles)if(d.name.equalsIgnoreCase(axis)){match=d;break;}add("environment",match,axis.equals("pitch")?"Pitch":"Roll",true);}
        if(count("temps")==0)add("temps",null,"Waiting for temperatures",false);
        add("summary",find("sbmsBattery","voltage"),"",false);add("summary",readings.get("sbms:delta"),"Δ",false);
        add("capacity",readings.get("battery:capacity.remaining"),"",false);add("runtime",readings.get("battery:capacity.timeRemaining"),"",false);
        add("voltage",find("secondaryVoltage","voltage"),"V [P]",false);
        for(MonitorData.Datum d:readings.values())if(d.group.equals("cells"))add("cells",d,"["+(Integer.parseInt(d.id.substring(5))+1)+"]",false);
        update(readings);
    }
    private String clean(String name){return name.replace(" · current","").replace(" · temperature","").replace(" · remaining","");}
    private int count(String slot){int n=0;for(Item i:items)if(i.slot.equals(slot))n++;return n;}
    private MonitorData.Datum find(String channel,String quantity){for(MonitorData.Datum d:readings.values())if(d.metric!=null&&d.metric.channel.equals(channel)&&d.quantity.equals(quantity))return d;return null;}
    private GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(6);d.setStroke(1,theme.border);return d;}
    private void group(String key,String title,int left,int right,boolean heading){View g=new View(getContext());g.setBackground(bg(theme.panel));g.setTag(new int[]{left,right});groups.add(g);addView(g);TextView t=label(host.label("group:"+key,title),14);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));t.setTag(key);t.setOnLongClickListener(v->{host.rename("group:"+key,title);return true;});if(heading){titles.add(t);addView(t);}}
    private TextView label(String text,float size){TextView t=new TextView(getContext());t.setText(text);t.setTextColor(theme.text);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));t.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);t.setSingleLine();t.setHorizontallyScrolling(false);t.setEllipsize(TextUtils.TruncateAt.END);t.setIncludeFontPadding(false);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void add(String slot,MonitorData.Datum d,String fallback,boolean gauge){Readout v=new Readout(d,fallback,gauge);int index=count(slot);items.add(new Item(slot,index,v));bySlot.put(slot+":"+index,v);addView(v);}
    private final class Item{final String slot;final int index;final Readout view;Item(String s,int i,Readout v){slot=s;index=i;view=v;}}
    final class Readout extends LinearLayout {
        final String id,fallback;final boolean isGauge;final TextView name,value;final GaugeView gauge;
        MonitorData.Datum datum;
        Readout(MonitorData.Datum d,String fallback,boolean isGauge){super(LiveDashboard.this.getContext());this.datum=d;this.fallback=fallback;this.isGauge=isGauge;id=d==null?"pending:"+fallback:d.id;setPadding(6,isGauge?3:0,6,0);setOrientation(isGauge?VERTICAL:HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);
            name=label(host.label(id,fallback),isGauge?12:11.5f);
            if(isGauge){setBackground(bg(theme.panel));name.setGravity(Gravity.CENTER);addView(name,new LayoutParams(-1,20));gauge=new GaugeView(getContext(),gaugeColors,d==null?"A":d.unit);addView(gauge,new LayoutParams(-1,0,1));value=null;}
            else {gauge=null;addView(name,new LayoutParams(0,-1,fallback.isEmpty()?0:d!=null&&d.group.equals("cells")?.35f:1));value=label("—",14);value.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);value.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));addView(value,new LayoutParams(0,-1,fallback.isEmpty()||d!=null&&d.group.equals("cells")?1:.85f));}
            setFocusable(true);setClickable(true);setOnClickListener(v->{if(!host.locked())host.detail(datum,fallback);});setOnLongClickListener(v->{if(host.locked())return true;host.rename(id,fallback);return true;});
        }
        @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
        @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);drawText(canvas,name,isGauge?12:11.5f,isGauge,false);if(value!=null)drawText(canvas,value,14,false,true);}
        void update(MonitorData.Datum d){datum=d;if(gauge!=null)gauge.value(d==null?null:d.value);String displayed=d==null?"—":format(d);if(value!=null){value.setText(displayed);value.setTextColor(d!=null&&d.flag!=null?(d.flag?(d.id.equals("flag:CFET")||d.id.equals("flag:DFET")||d.id.equals("flag:EOC")?0xff32dc68:0xffff4545):theme.buttonTextOff):theme.text);}setContentDescription(host.label(id,fallback)+", "+(d!=null&&d.group.equals("flags")?(d.flag==null?"Unavailable":d.flag?"On":"Off"):displayed));invalidate();}
    }
    static String format(MonitorData.Datum d){if(d.group.equals("flags"))return d.flag==null?"—":d.flag?"●":"○";if(d.value==null)return "—";if(d.unit.equals("s")){long s=Math.max(0,d.value.longValue());return String.format(Locale.UK,"%dd:%02dh",s/86400,s/3600%24);}return String.format(Locale.UK,d.unit.equals("V")?"%.3f %s":d.unit.equals("°C")?"%.1f%s":d.unit.equals("A")?"%.2f %s":"%.0f %s",d.value,d.unit);}
    void update(Map<String,MonitorData.Datum> r){readings=r;boolean changed=false;for(Item i:items){MonitorData.Datum d=r.get(i.view.id);i.view.update(d);if(i.slot.equals("cells")){if(d!=null&&d.value!=null)changed|=knownCells.add(d.id);i.view.setVisibility(knownCells.contains(i.view.id)?VISIBLE:GONE);}}if(changed)requestLayout();}
    @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
    @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);for(TextView title:titles)drawText(canvas,title,14,true,false);}
    private void drawText(Canvas canvas,TextView view,float size,boolean center,boolean end){
        if(view.getWidth()<=0)return;TextPaint paint=new TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG);paint.setTypeface(view.getTypeface());paint.setColor(view.getCurrentTextColor());paint.setTextSize(size);
        String text=view.getText().toString();float available=view.getWidth();if(end&&paint.measureText(text)>available){paint.setTextSize(Math.max(9,size*.94f*available/paint.measureText(text)));}
        text=TextUtils.ellipsize(text,paint,available,TextUtils.TruncateAt.END).toString();paint.setTextAlign(center?android.graphics.Paint.Align.CENTER:end?android.graphics.Paint.Align.RIGHT:android.graphics.Paint.Align.LEFT);
        float x=view.getLeft()+(center?available/2:end?available:0);android.graphics.Paint.FontMetrics metrics=paint.getFontMetrics();float y=view.getTop()+view.getHeight()/2f-(metrics.ascent+metrics.descent)/2f;
        canvas.save();canvas.clipRect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());canvas.drawText(text,x,y,paint);canvas.restore();
    }
    @Override protected void onMeasure(int ws,int hs){setMeasuredDimension(MeasureSpec.getSize(ws),MeasureSpec.getSize(hs));layoutChildren(false);}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){layoutChildren(true);}
    private void place(View v,float l,float t,float r,float b,boolean layout){int w=Math.max(1,Math.round(r-l)),h=Math.max(1,Math.round(b-t));v.measure(MeasureSpec.makeMeasureSpec(w,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(h,MeasureSpec.EXACTLY));if(layout)v.layout(Math.round(l),Math.round(t),Math.round(r),Math.round(b));}
    private void layoutChildren(boolean layout){float sx=getMeasuredWidth()/1024f,h=getMeasuredHeight(),bottom=h-6,summaryTop=bottom-70,mainBottom=summaryTop-6;
        for(int j=0;j<groups.size();j++){View g=groups.get(j);int[] pos=(int[])g.getTag();float top=0,end=bottom;if(j==1)end=76;if(j==2)end=mainBottom;if(j==4){top=summaryTop;end=bottom;}place(g,pos[0]*sx,top,pos[1]*sx,end,layout);}
        for(TextView v:titles){boolean load=v.getTag().equals("loads");place(v,(load?14:878)*sx,6,(load?156:1010)*sx,28,layout);}
        float gaugesTop=82,gaugeHeight=(mainBottom-gaugesTop-6)/2;
        for(Item i:items){float l=0,t=0,r=0,b=0;int n=i.index;
            switch(i.slot){
                case "loads":case "temps":{boolean load=i.slot.equals("loads");l=load?14:878;r=load?156:1010;int total=count(i.slot);float rowHeight=Math.min(39,(bottom-40-6*Math.max(0,total-1))/Math.max(1,total));float gap=total>1?(bottom-34-total*rowHeight)/(total-1):0;t=34+n*(rowHeight+Math.max(6,gap));b=t+rowHeight;break;}
                case "flags":l=174+(n%4)*112.25f;r=l+106.25f;t=6+(n/4)*35;b=t+29;break;
                case "gauges":l=168+(n%4)*115.25f;r=l+109.25f;t=gaugesTop+(n/4)*(gaugeHeight+6);b=t+gaugeHeight;break;
                case "environment":if(n==0){l=635;r=860;t=6;b=mainBottom/3-3;}else{int row=n<3?1:2;l=635+((n-1)%2)*115.5f;r=l+109.5f;t=row*(mainBottom/3)+3;b=(row+1)*(mainBottom/3)-6;}break;
                case "summary":l=174;r=284;t=summaryTop+6+n*32;b=t+26;break;
                case "capacity":l=290;r=400;t=summaryTop+6;b=bottom-6;break;
                case "runtime":l=406;r=516;t=summaryTop+6;b=bottom-6;break;
                case "voltage":l=522;r=630;t=summaryTop+6+n*32;b=t+26;break;
                case "cells":{if(!knownCells.contains(i.view.id))continue;n=0;for(Item cell:items)if(cell.slot.equals("cells")&&knownCells.contains(cell.view.id)){if(cell==i)break;n++;}int rows=2;int columns=Math.max(1,(knownCells.size()+1)/2);float width=(224f-6*(columns-1))/columns;l=636+(n/rows)*(width+6);r=l+width;t=summaryTop+6+(n%rows)*32;b=t+26;break;}
            }
            place(i.view,l*sx,t,r*sx,b,layout);
        }
    }
}
