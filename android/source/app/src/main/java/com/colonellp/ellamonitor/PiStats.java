package com.colonellp.ellamonitor;

import java.util.*;

/** Readout parsing is independent of rendering and never substitutes zero for missing data. */
final class PiStats {
    final Map<String,String> values=new HashMap<>();final long at;final String endpoint;
    PiStats(String text,String endpoint,long at){
        if(text.length()>16384)throw new IllegalArgumentException("Pi response exceeds limit.");
        for(String line:text.split("\n")){int tab=line.indexOf('\t');if(tab>0){String key=line.substring(0,tab),value=line.substring(tab+1);values.put(key,key.equals("ssid")?value:value.trim());}}
        if(!"ella-pi-control-1".equals(values.get("protocol")))throw new IllegalArgumentException("Unexpected Pi response.");this.endpoint=endpoint;this.at=at;
    }
    String get(String key){String v=values.get(key);return v==null||v.isEmpty()?"—":v;}
    String memory(){try{String[] v=get("memory").split("\\s+");long total=Long.parseLong(v[0]),available=Long.parseLong(v[1]);if(total<=0||available<0||available>total)return "—";return String.format(Locale.UK,"%.0f / %.0f MiB used (%.0f MiB available)",(total-available)/1024d,total/1024d,available/1024d);}catch(Exception e){return "—";}}
    String temperature(){try{double c=Double.parseDouble(get("temperature"))/1000;if(!Double.isFinite(c)||c< -50||c>150)return "—";return String.format(Locale.UK,"%.1f °C",c);}catch(Exception e){return "—";}}
    String cpu(PiStats previous){
        if(previous==null||!get("boot").equals(previous.get("boot"))||get("boot").equals("—"))return "—";
        try{long[] a=counts(previous.get("cpu")),b=counts(get("cpu"));long total=0;for(int i=0;i<8;i++){if(b[i]<a[i])return "—";total+=b[i]-a[i];}long idle=b[3]-a[3]+b[4]-a[4];return total<=0?"—":String.format(Locale.UK,"%.0f%%",100d*(total-idle)/total);}catch(Exception e){return "—";}
    }
    private static long[] counts(String text){String[] a=text.split("\\s+");long[] v=new long[8];for(int i=0;i<8;i++)v[i]=Long.parseLong(a[i]);return v;}
}
