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
    private static final int GAP=6, CENTRE_LEFT=168, CENTRE_RIGHT=630, ENV_LEFT=636, ENV_RIGHT=866;
    interface Host { String label(String id,String original); void detail(String id,MonitorData.Datum d,String name,boolean gauge); void rename(String id,String original); boolean locked(); int highlight(String id); boolean hidden(String id); boolean changed(String id,String original); }
    final ThemeConfig theme;
    final Host host;
    private Map<String,MonitorData.Datum> readings;
    private final List<Item> items = new ArrayList<>();
    private final List<View> groups = new ArrayList<>();
    private final List<TextView> titles = new ArrayList<>();
    private final java.util.Set<String> bindings=new java.util.LinkedHashSet<>();
    private int styleRevision;
    private final Map<String,Readout> bySlot = new LinkedHashMap<>();
    private final Palette gaugeColors;
    private final java.util.Set<String> knownCells;
    LiveDashboard(Context context,ThemeConfig theme,Map<String,MonitorData.Datum> readings,java.util.Set<String> knownCells,Host host) {
        super(context);this.theme=theme;this.readings=readings;this.host=host;this.knownCells=knownCells;gaugeColors=new Palette(theme);bindings.addAll(readings.keySet());setClipChildren(true);
        group("loads","Currents:",8,162,true);group("flags","Ella Monitoring",CENTRE_LEFT,CENTRE_RIGHT,true);group("gauges","",CENTRE_LEFT,CENTRE_RIGHT,false);group("environment","",ENV_LEFT,ENV_RIGHT,false);group("temps","Temps:",872,1016,true);group("summary","",CENTRE_LEFT,CENTRE_RIGHT,false);group("cells","",ENV_LEFT,ENV_RIGHT,false);
        for(MonitorData.Datum d:readings.values())if(d.group.equals("loads")&&d.quantity.equals("current")&&!d.id.equals("pico:inverter"))add("loads",d,clean(d.name),false);
        if(!bySlot.containsKey("loads:0"))add("loads",null,"Waiting for current data",false);
        add("loads",readings.get("pico:inverter"),"Inverter",false);
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
        update(readings);refreshTheme();
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
    private int groupFill(String key){return key.equals("gauges")?theme.gaugePanel1:key.equals("environment")?theme.gaugePanel2:key.equals("flags")?theme.indicatorBackground:key.equals("summary")||key.equals("cells")?theme.voltagesBackground:theme.panel;}
    private void group(String key,String title,int left,int right,boolean heading){
        View g=new View(getContext());g.setBackground(bg(groupFill(key),key.equals("gauges")?theme.gaugeOutline1:key.equals("environment")?theme.gaugeOutline2:theme.border));g.setTag(new Object[]{key,left,right});groups.add(g);addView(g);
        if(!heading)return;
        TextView t=label(host.label("group:"+key,title),14);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));t.setTag(key);
        t.setOnClickListener(v->{if(!host.locked())host.detail("group:"+key,null,title,false);});t.setOnLongClickListener(v->{if(!host.locked())host.rename("group:"+key,title);return true;});titles.add(t);addView(t);
    }
    private TextView label(String text,float size){TextView t=new TextView(getContext());t.setText(text);t.setTextColor(theme.text);t.setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));t.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);t.setSingleLine();t.setHorizontallyScrolling(false);t.setEllipsize(TextUtils.TruncateAt.END);t.setIncludeFontPadding(false);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void add(String slot,MonitorData.Datum d,String fallback,boolean gauge){Readout v=new Readout(slot,d,fallback,gauge);int index=count(slot);items.add(new Item(slot,index,v));bySlot.put(slot+":"+index,v);addView(v);}
    private final class Item{final String slot;final int index;final Readout view;Item(String s,int i,Readout v){slot=s;index=i;view=v;}}
    final class Readout extends LinearLayout {
        final String id,fallback,slot;final boolean isGauge;final TextView name,value;final GaugeView gauge;
        MonitorData.Datum datum;
        private int appliedStyleRevision=-1;
        boolean modified;
        Readout(String slot,MonitorData.Datum d,String fallback,boolean isGauge){super(LiveDashboard.this.getContext());this.slot=slot;this.datum=d;this.fallback=fallback;this.isGauge=isGauge;id=d==null?"pending:"+fallback:d.id;int pad=slot.equals("cells")||slot.equals("voltage")||(d!=null&&d.id.equals("sbms:delta"))?10:6;setPadding(pad,isGauge?6:0,pad,0);setOrientation(isGauge?VERTICAL:HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);
            name=label(host.label(id,fallback),nameSize());
            if(isGauge){name.setGravity(Gravity.CENTER);addView(name,new LayoutParams(-1,20));gauge=new GaugeView(getContext(),gaugeColors,d==null?"A":d.unit,GaugeView.kind(slot,fallback));addView(gauge,new LayoutParams(-1,0,1));value=null;}
            else {gauge=null;boolean centre=centred();addView(name,new LayoutParams(0,-1,centre?0:1));value=label("—",valueSize());value.setGravity((centre?Gravity.CENTER:Gravity.END)|Gravity.CENTER_VERTICAL);value.setTypeface(Typeface.create(theme.fontFamily,Typeface.BOLD));addView(value,new LayoutParams(0,-1,centre?1:.85f));}
            setFocusable(true);setClickable(true);setOnClickListener(v->{if(!host.locked())host.detail(id,datum,fallback,isGauge);});setOnLongClickListener(v->{if(host.locked())return true;host.rename(id,fallback);return true;});
        }
        boolean centred(){return slot.equals("summary")||slot.equals("capacity")||slot.equals("runtime")||slot.equals("cells")||slot.equals("voltage");}
        float nameSize(){return isGauge?12:11.5f;}
        float valueSize(){return slot.equals("capacity")||slot.equals("runtime")?22:14;}
        @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
        @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);drawText(canvas,name,nameSize(),isGauge,false);if(value!=null)drawText(canvas,value,valueSize(),centred(),!centred());}
        void update(MonitorData.Datum d){MonitorData.Datum previous=datum;datum=d;
            if(appliedStyleRevision==styleRevision&&java.util.Objects.equals(previous==null?null:previous.value,d==null?null:d.value)&&java.util.Objects.equals(previous==null?null:previous.flag,d==null?null:d.flag))return;
            appliedStyleRevision=styleRevision;
            boolean hidden=host.hidden(id);name.setVisibility(hidden?INVISIBLE:VISIBLE);if(value!=null)value.setVisibility(hidden?INVISIBLE:VISIBLE);if(gauge!=null)gauge.setVisibility(hidden?INVISIBLE:VISIBLE);
            boolean available=d!=null&&(d.value!=null||d.flag!=null);
            android.graphics.drawable.StateListDrawable background=new android.graphics.drawable.StateListDrawable();
            GradientDrawable highlighted=bg(ThemeConfig.blend(theme.buttonBackground,theme.controls,.12f));highlighted.setStroke(2,isGauge?(slot.equals("gauges")?theme.gaugeOutline1:theme.gaugeOutline2):theme.buttonBorder);
            background.addState(new int[]{android.R.attr.state_pressed},highlighted);background.addState(new int[]{android.R.attr.state_focused},highlighted);
            int fill=isGauge?(slot.equals("gauges")?theme.gaugePanel1:theme.gaugePanel2):theme.buttonBackground;int outline=isGauge?(slot.equals("gauges")?theme.gaugeOutline1:theme.gaugeOutline2):theme.buttonBorder;int choice=host.highlight(id);if(choice>0)fill=choice==1?theme.gaugeHighlight:theme.highlight2;GradientDrawable normal=bg(fill,outline);background.addState(new int[]{},normal);setBackground(background);
            name.setText(host.label(id,fallback));name.setTextColor(modified?theme.changedIndicator:available?theme.buttonTextOn:theme.buttonTextOff);if(gauge!=null)gauge.value(d==null?null:d.value);String displayed=d==null?"—":format(d);if(value!=null){String label=host.label(id,fallback);value.setText(slot.equals("voltage")&&label.equals(fallback)?label.replace(" ","")+displayed.replace(" ",""):centred()&&!fallback.isEmpty()?label+" "+displayed:displayed);value.setTextColor(modified&&centred()?theme.changedIndicator:d!=null&&d.flag!=null?(d.flag?(d.id.equals("flag:CFET")||d.id.equals("flag:DFET")||d.id.equals("flag:EOC")?0xff32dc68:0xffff4545):theme.buttonTextOff):available?theme.buttonTextOn:theme.buttonTextOff);}setContentDescription(host.label(id,fallback)+(hidden?", contents hidden":", "+(d!=null&&d.group.equals("flags")?(d.flag==null?"Unavailable":d.flag?"On":"Off"):displayed)));invalidate();}
    }
    static String format(MonitorData.Datum d){if(d.group.equals("flags"))return d.flag==null?"—":d.flag?"●":"○";if(d.value==null)return "—";if(d.unit.equals("h"))return runtimeText(d.value);return String.format(Locale.UK,d.unit.equals("V")?"%.3f %s":d.unit.equals("°C")?"%.1f%s":d.unit.equals("A")?"%.2f %s":"%.0f %s",d.value,d.unit);}
    static String runtimeText(double value){
        double hours=Math.abs(value);long totalMinutes=(long)Math.floor(hours*60);long wholeHours=totalMinutes/60;
        String result=hours>=24?String.format(Locale.UK,"%dd:%02dh",wholeHours/24,wholeHours%24):String.format(Locale.UK,"%dh:%02dm",wholeHours,totalMinutes%60);
        return (value<0?"-":"")+result;
    }
    void update(Map<String,MonitorData.Datum> r){readings=r;boolean changed=false;for(Item i:items){MonitorData.Datum d=r.get(i.view.id);i.view.update(d);if(i.slot.equals("cells")){if(d!=null&&d.value!=null)changed|=knownCells.add(d.id);i.view.setVisibility(knownCells.contains(i.view.id)?VISIBLE:GONE);}}if(changed)requestLayout();}
    String originalLabel(String id){
        if(id.equals("group:loads"))return "Currents:";if(id.equals("group:flags"))return "Ella Monitoring";if(id.equals("group:temps"))return "Temps:";
        for(Item item:items)if(item.view.id.equals(id))return item.view.fallback;return null;
    }
    void refreshChanges(){refreshTheme();}
    void refreshTheme(){
        styleRevision++;
        gaugeColors.update(theme);
        for(View group:groups){String key=(String)((Object[])group.getTag())[0];group.setBackground(bg(groupFill(key),key.equals("gauges")?theme.gaugeOutline1:key.equals("environment")?theme.gaugeOutline2:theme.border));}
        for(TextView title:titles){String key=(String)title.getTag(),id="group:"+key;String original=key.equals("loads")?"Currents:":key.equals("flags")?"Ella Monitoring":"Temps:";
            title.setText(host.hidden(id)?"":host.label(id,original));title.setContentDescription(host.label(id,original));title.setTextColor(host.changed(id,original)?theme.changedIndicator:theme.titleText);
            int choice=host.highlight(id);title.setBackground(bg(choice==0?theme.titleBackground:choice==1?theme.gaugeHighlight:theme.highlight2,theme.titleOutline));}
        for(Item item:items){item.view.modified=host.changed(item.view.id,item.view.fallback);item.view.update(item.view.datum);if(item.view.gauge!=null)item.view.gauge.refreshTheme();}invalidate();
    }
    boolean accepts(Map<String,MonitorData.Datum> data){
        // Missing transient fields do not replace the view tree. Genuine new
        // bindings/configuration still rebuild the affected layout.
        for(MonitorData.Datum d:data.values())if(!bindings.contains(d.id))return false;
        return true;
    }
    @Override protected boolean drawChild(Canvas canvas,View child,long time){return child instanceof TextView || super.drawChild(canvas,child,time);}
    @Override protected void dispatchDraw(Canvas canvas){super.dispatchDraw(canvas);for(TextView title:titles){canvas.save();canvas.translate(title.getLeft(),title.getTop());if(title.getBackground()!=null){title.getBackground().setBounds(0,0,title.getWidth(),title.getHeight());title.getBackground().draw(canvas);}canvas.restore();drawText(canvas,title,14,true,false);}}
    private void drawText(Canvas canvas,TextView view,float size,boolean center,boolean end){
        if(view.getWidth()<=0||view.getVisibility()!=VISIBLE)return;TextPaint paint=new TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG);paint.setTypeface(view.getTypeface());paint.setColor(view.getCurrentTextColor());paint.setTextSize(size);
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
        for(TextView v:titles){String key=(String)v.getTag();float left=key.equals("loads")?14:key.equals("flags")?CENTRE_LEFT+GAP:878,right=key.equals("loads")?156:key.equals("flags")?CENTRE_RIGHT-GAP:1010;place(v,left*sx,6,right*sx,28,layout);}
        float gaugesTop=114,gaugeHeight=(mainBottom-6-gaugesTop-6)/2;
        float centralWidth=(CENTRE_RIGHT-CENTRE_LEFT-5*GAP)/4f,environmentWidth=(ENV_RIGHT-ENV_LEFT-3*GAP)/2f;
        for(Item i:items){float l=0,t=0,r=0,b=0;int n=i.index;
            switch(i.slot){
                case "loads":case "temps":{boolean load=i.slot.equals("loads");l=load?14:878;r=load?156:1010;int total=count(i.slot);float rowHeight=(bottom-40-6*Math.max(0,total-1))/Math.max(1,total);t=34+n*(rowHeight+6);b=t+rowHeight;break;}
                case "flags":l=CENTRE_LEFT+GAP+(n%4)*(centralWidth+GAP);r=l+centralWidth;t=32+(n/4)*35;b=t+29;break;
                case "gauges":l=CENTRE_LEFT+GAP+(n%4)*(centralWidth+GAP);r=l+centralWidth;t=gaugesTop+(n/4)*(gaugeHeight+6);b=t+gaugeHeight;break;
                case "environment":if(n==0){l=ENV_LEFT+GAP;r=ENV_RIGHT-GAP;t=6;b=mainBottom/3-3;}else{int row=n<3?1:2;l=ENV_LEFT+GAP+((n-1)%2)*(environmentWidth+GAP);r=l+environmentWidth;t=row*(mainBottom/3)+3;b=(row+1)*(mainBottom/3)-6;}break;
                case "summary":l=174;r=284;t=summaryTop+6+n*32;b=t+26;break;
                case "capacity":l=290;r=400;t=summaryTop+6;b=bottom-6;break;
                case "runtime":l=406;r=516;t=summaryTop+6;b=bottom-6;break;
                case "voltage":l=522;r=CENTRE_RIGHT-GAP;t=summaryTop+6+n*32;b=t+26;break;
                case "cells":{if(!knownCells.contains(i.view.id))continue;n=0;for(Item cell:items)if(cell.slot.equals("cells")&&knownCells.contains(cell.view.id)){if(cell==i)break;n++;}int rows=2;int columns=Math.max(1,(knownCells.size()+1)/2);float width=(218f-6*(columns-1))/columns;l=642+(n/rows)*(width+6);r=l+width;t=summaryTop+6+(n%rows)*32;b=t+26;break;}
            }
            place(i.view,l*sx,t,r*sx,b,layout);
        }
    }
}

