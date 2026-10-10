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
    interface Host { String label(String id,String original); void detail(MonitorData.Datum d,String name,boolean gauge); void rename(String id,String original); boolean locked(); boolean highlighted(String id); }
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
        group("loads","Currents:",8,162,true);group("flags","Ella Monitoring",168,623,true);group("gauges","",162,629,false);group("environment","",629,866,false);group("temps","Temps:",872,1016,true);group("summary","",168,630,false);group("cells","",636,866,false);
        for(MonitorData.Datum d:readings.values())if(d.group.equals("loads")&&d.quantity.equals("current"))add("loads",d,clean(d.name),false);
        if(!bySlot.containsKey("loads:0"))add("loads",null,"Waiting for current data",false);
        for(String f:new String[]{"CFET","OVLK","EOC","LVC","DFET","UVLK","IOT","CELF"})add("flags",readings.get("flag:"+f),f,false);
        String[] channels={"externalLoad","pv1","sbmsBattery","sbmsBattery","picoLoad","pv2","picoBattery","picoBattery"};
        String[] names={"Load Σ [BMS]","PV1 [BMS]","Battery [BMS]","SOC [BMS]","Load Σ [Pico]","PV2 [BMS]","Battery [Pico]","SOC [Pico]"};
        for(int i=0;i<channels.length;i++)add("gauges",channels[i].equals("picoLoad")?readings.get("pico:load-sum"):find(channels[i],i%4==3?"stateOfCharge":"current"),names[i],true);
        List<MonitorData.Datum> tanks=new ArrayList<>(),angles=new ArrayList<>(),temperatures=new ArrayList<>();MonitorData.Datum pressure=null;
        for(MonitorData.Datum d:readings.values()){
            if(d.group.equals("temps"))temperatures.add(d);
            if(d.group.equals("environment")) {if(d.unit.equals("hPa"))pressure=d;else if(d.unit.equals("°"))angles.add(d);else if(d.unit.equals("%")||d.unit.equals("L"))tanks.add(d);}
        }
        temperatures.sort(java.util.Comparator.comparingInt(LiveDashboard::temperatureOrder).thenComparing(d->d.id));
        for(MonitorData.Datum d:temperatures)add("temps",d,clean(d.name),false);
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
        add("voltage",readings.get("battery:pico-voltage"),"V [P]",false);
        MonitorData.Datum starter=null;for(MonitorData.Datum d:readings.values())if(d.quantity.equals("starterVoltage"))starter=d;add("voltage",starter,"V [S]",false);
        for(MonitorData.Datum d:readings.values())if(d.group.equals("cells"))add("cells",d,"["+(Integer.parseInt(d.id.substring(5))+1)+"]",false);
        update(readings);
    }
    private static int temperatureOrder(MonitorData.Datum d){
        String n=d.name.toLowerCase(Locale.UK);
        if(d.id.equals("sbms:tempExt"))return 80;
        if(d.id.equals("sbms:tempInt"))return 100;
        if(n.contains("inside"))return 0;if(n.contains("outside"))return 10;if(n.contains("alternator")||n.equals("altr"))return 20;
        if(n.contains("fridge"))return n.contains("int")?40:30;
        if(n.contains("water"))return 50;
        if(n.contains("battery")||n.startsWith("bat "))return 90;
        return 60;
    }
    private String clean(String name){return name.replace(" · current","").replace(" · temperature","").replace(" · remaining","");}
    private int count(String slot){int n=0;for(Item i:items)if(i.slot.equals(slot))n++;return n;}
    private MonitorData.Datum find(String channel,String quantity){for(MonitorData.Datum d:readings.values())if(d.metric!=null&&d.metric.channel.equals(channel)&&d.quantity.equals(quantity))return d;return null;}
    private GradientDrawable bg(int color){return bg(color,theme.border);}
    private GradientDrawable bg(int color,int outline){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(6);d.setStroke(1,outline);return d;}
    private void group(String key,String title,int left,int right,boolean heading){View g=new View(getContext());g.setBackground(bg(key.equals("gauges")?theme.gaugePanel1:key.equals("environment")?theme.gaugePanel2:theme.panel,key.equals("gauges")?theme.gaugeOutline1:key.equals("environment")?theme.gaugeOutline2:theme.border));g.setTag(new Object[]{key,left,right});groups.add(g);addView(g);TextView t=label(host.label("group:"+key,title),14);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));t.setTag(key);t.setBackground(bg(theme.titleBackground,theme.titleOutline));t.setOnLongClickListener(v->{host.rename("group:"+key,title);return true;});if(heading){titles.add(t);addView(t);}}
    private TextView label(String text,float size){TextView t=new TextView(getContext());t.setText(text);t.setTextColor(theme.text);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));t.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);t.setSingleLine();t.setHorizontallyScrolling(false);t.setEllipsize(TextUtils.TruncateAt.END);t.setIncludeFontPadding(false);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void add(String slot,MonitorData.Datum d,String fallback,boolean gauge){Readout v=new Readout(slot,d,fallback,gauge);int index=count(slot);items.add(new Item(slot,index,v));bySlot.put(slot+":"+index,v);addView(v);}
    private final class Item{final String slot;final int index;final Readout view;Item(String s,int i,Readout v){slot=s;index=i;view=v;}}
    final class Readout extends LinearLayout {
        final String id,fallback,slot;final boolean isGauge;final TextView name,value;final GaugeView gauge;
        MonitorData.Datum datum;
        Readout(String slot,MonitorData.Datum d,String fallback,boolean isGauge){super(LiveDashboard.this.getContext());this.slot=slot;this.datum=d;this.fallback=fallback;this.isGauge=isGauge;id=d==null?"pending:"+fallback:d.id;int pad=slot.equals("cells")||(d!=null&&d.id.equals("sbms:delta"))?20:6;setPadding(pad,isGauge?6:0,pad,0);setOrientation(isGauge?VERTICAL:HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);
            name=label(host.label(id,fallback),isGauge?12:11.5f);
            if(isGauge){name.setGravity(Gravity.CENTER);addView(name,new LayoutParams(-1,20));gauge=new GaugeView(getContext(),gaugeColors,d==null?"A":d.unit);addView(gauge,new LayoutParams(-1,0,1));value=null;}
            else {gauge=null;boolean centre=centred();addView(name,new LayoutParams(0,-1,centre?0:1));value=label("—",valueSize());value.setGravity((centre?Gravity.CENTER:Gravity.END)|Gravity.CENTER_VERTICAL);value.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));addView(value,new LayoutParams(0,-1,centre?1:.85f));}
            setFocusable(true);setClickable(true);setOnClickListener(v->{if(!host.locked())host.detail(datum,fallback,isGauge);});setOnLongClickListener(v->{if(host.locked())return true;host.rename(id,fallback);return true;});
        }
        boolean centred(){return slot.equals("summary")||slot.equals("capacity")||slot.equals("runtime")||slot.equals("cells");}
        float valueSize(){return slot.equals("capacity")||slot.equals("runtime")?22:14;}
        @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
        @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);drawText(canvas,name,isGauge?12:11.5f,isGauge,false);if(value!=null)drawText(canvas,value,valueSize(),centred(),!centred());}
        void update(MonitorData.Datum d){datum=d;
            boolean available=d!=null&&(d.value!=null||d.flag!=null);
            android.graphics.drawable.StateListDrawable background=new android.graphics.drawable.StateListDrawable();
            GradientDrawable highlighted=bg(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f));highlighted.setStroke(2,theme.controls);
            background.addState(new int[]{android.R.attr.state_pressed},highlighted);background.addState(new int[]{android.R.attr.state_focused},highlighted);
            int fill=isGauge?(slot.equals("gauges")?theme.gaugePanel1:theme.gaugePanel2):theme.buttonBackground;int outline=isGauge?(slot.equals("gauges")?theme.gaugeOutline1:theme.gaugeOutline2):(available?theme.controls:theme.offButtonBorder);if(isGauge&&host.highlighted(id))fill=theme.gaugeHighlight;GradientDrawable normal=bg(fill,outline);background.addState(new int[]{},normal);setBackground(background);
            name.setTextColor(available?theme.buttonTextOn:theme.buttonTextOff);if(gauge!=null)gauge.value(d==null?null:d.value);String displayed=d==null?"—":format(d);if(value!=null){value.setText(centred()&&!fallback.isEmpty()?host.label(id,fallback)+" "+displayed:displayed);value.setTextColor(d!=null&&d.flag!=null?(d.flag?(d.id.equals("flag:CFET")||d.id.equals("flag:DFET")||d.id.equals("flag:EOC")?0xff32dc68:0xffff4545):theme.buttonTextOff):available?theme.buttonTextOn:theme.buttonTextOff);}setContentDescription(host.label(id,fallback)+", "+(d!=null&&d.group.equals("flags")?(d.flag==null?"Unavailable":d.flag?"On":"Off"):displayed));invalidate();}
    }
    static String format(MonitorData.Datum d){if(d.group.equals("flags"))return d.flag==null?"—":d.flag?"●":"○";if(d.value==null)return "—";if(d.unit.equals("h"))return runtimeText(d.value);return String.format(Locale.UK,d.unit.equals("V")?"%.3f %s":d.unit.equals("°C")?"%.1f%s":d.unit.equals("A")?"%.2f %s":"%.0f %s",d.value,d.unit);}
    static String runtimeText(double value){
        double hours=Math.abs(value);long totalMinutes=(long)Math.floor(hours*60);long wholeHours=totalMinutes/60;
        String result=hours>=24?String.format(Locale.UK,"%dd:%02dh",wholeHours/24,wholeHours%24):String.format(Locale.UK,"%dh:%02dm",wholeHours,totalMinutes%60);
        return (value<0?"-":"")+result;
    }
    void update(Map<String,MonitorData.Datum> r){readings=r;boolean changed=false;for(Item i:items){MonitorData.Datum d=r.get(i.view.id);i.view.update(d);if(i.slot.equals("cells")){if(d!=null&&d.value!=null)changed|=knownCells.add(d.id);i.view.setVisibility(knownCells.contains(i.view.id)?VISIBLE:GONE);}}if(changed)requestLayout();}
    @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
    @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);for(TextView title:titles){canvas.save();canvas.translate(title.getLeft(),title.getTop());if(title.getBackground()!=null){title.getBackground().setBounds(0,0,title.getWidth(),title.getHeight());title.getBackground().draw(canvas);}canvas.restore();drawText(canvas,title,14,true,false);}}
    private void drawText(Canvas canvas,TextView view,float size,boolean center,boolean end){
        if(view.getWidth()<=0)return;TextPaint paint=new TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG);paint.setTypeface(view.getTypeface());paint.setColor(view.getCurrentTextColor());paint.setTextSize(size);
        String text=view.getText().toString();float available=view.getWidth();if(paint.measureText(text)>available){paint.setTextSize(Math.max(9,size*.94f*available/paint.measureText(text)));}
        text=TextUtils.ellipsize(text,paint,available,TextUtils.TruncateAt.END).toString();paint.setTextAlign(center?android.graphics.Paint.Align.CENTER:end?android.graphics.Paint.Align.RIGHT:android.graphics.Paint.Align.LEFT);
        float x=view.getLeft()+(center?available/2:end?available:0);android.graphics.Paint.FontMetrics metrics=paint.getFontMetrics();float y=view.getTop()+view.getHeight()/2f-(metrics.ascent+metrics.descent)/2f;
        canvas.save();canvas.clipRect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());canvas.drawText(text,x,y,paint);canvas.restore();
    }
    @Override protected void onMeasure(int ws,int hs){setMeasuredDimension(MeasureSpec.getSize(ws),MeasureSpec.getSize(hs));layoutChildren(false);}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){layoutChildren(true);}
    private void place(View v,float l,float t,float r,float b,boolean layout){int w=Math.max(1,Math.round(r-l)),h=Math.max(1,Math.round(b-t));v.measure(MeasureSpec.makeMeasureSpec(w,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(h,MeasureSpec.EXACTLY));if(layout)v.layout(Math.round(l),Math.round(t),Math.round(r),Math.round(b));}
    private void layoutChildren(boolean layout){float sx=getMeasuredWidth()/1024f,h=getMeasuredHeight(),bottom=h-6,summaryTop=bottom-70,mainBottom=summaryTop-6;
        for(View g:groups){Object[] pos=(Object[])g.getTag();String key=(String)pos[0];float top=0,end=bottom;
            if(key.equals("flags"))end=102;
            if(key.equals("gauges")){top=108;end=mainBottom;}
            if(key.equals("environment"))end=mainBottom;
            if(key.equals("summary")||key.equals("cells")){top=summaryTop;end=bottom;}
            place(g,(Integer)pos[1]*sx,top,(Integer)pos[2]*sx,end,layout);
        }
        for(TextView v:titles){String key=(String)v.getTag();float left=key.equals("loads")?14:key.equals("flags")?174:878,right=key.equals("loads")?156:key.equals("flags")?617:1010;place(v,left*sx,6,right*sx,28,layout);}
        float gaugesTop=114,gaugeHeight=(mainBottom-6-gaugesTop-6)/2;
        for(Item i:items){float l=0,t=0,r=0,b=0;int n=i.index;
            switch(i.slot){
                case "loads":case "temps":{boolean load=i.slot.equals("loads");l=load?14:878;r=load?156:1010;int total=count(i.slot);float rowHeight=(bottom-40-6*Math.max(0,total-1))/Math.max(1,total);t=34+n*(rowHeight+6);b=t+rowHeight;break;}
                case "flags":l=168+(n%4)*115.25f;r=l+109.25f;t=32+(n/4)*35;b=t+29;break;
                case "gauges":l=168+(n%4)*115.25f;r=l+109.25f;t=gaugesTop+(n/4)*(gaugeHeight+6);b=t+gaugeHeight;break;
                case "environment":if(n==0){l=635;r=860;t=6;b=mainBottom/3-3;}else{int row=n<3?1:2;l=635+((n-1)%2)*115.5f;r=l+109.5f;t=row*(mainBottom/3)+3;b=(row+1)*(mainBottom/3)-6;}break;
                case "summary":l=174;r=284;t=summaryTop+6+n*32;b=t+26;break;
                case "capacity":l=290;r=400;t=summaryTop+6;b=bottom-6;break;
                case "runtime":l=406;r=516;t=summaryTop+6;b=bottom-6;break;
                case "voltage":l=522;r=630;t=summaryTop+6+n*32;b=t+26;break;
                case "cells":{if(!knownCells.contains(i.view.id))continue;n=0;for(Item cell:items)if(cell.slot.equals("cells")&&knownCells.contains(cell.view.id)){if(cell==i)break;n++;}int rows=2;int columns=Math.max(1,(knownCells.size()+1)/2);float width=(218f-6*(columns-1))/columns;l=642+(n/rows)*(width+6);r=l+width;t=summaryTop+6+(n%rows)*32;b=t+26;break;}
            }
            place(i.view,l*sx,t,r*sx,b,layout);
        }
    }
}

