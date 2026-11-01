package com.colonellp.ellamonitor;

/** Display metadata can be redacted without changing the authorized physical route. */
final class NetworkIdentity {
    private long handle=Long.MIN_VALUE;
    private String route="",configuration="",knownSsid="";
    private boolean initialized;
    static String ssid(String value){
        if(value==null)return "";
        if(value.length()>=2&&value.startsWith("\"")&&value.endsWith("\""))value=value.substring(1,value.length()-1);
        return value.equals("<unknown ssid>")?"":value;
    }
    String observe(long nextHandle,String nextRoute,String reportedSsid,String nextConfiguration){
        reportedSsid=ssid(reportedSsid);
        if(initialized&&handle==nextHandle&&nextRoute.isEmpty())nextRoute=route;
        boolean routeChanged=initialized&&handle==nextHandle&&!route.isEmpty()&&!nextRoute.isEmpty()&&!route.equals(nextRoute);
        String reason=!initialized?"initial":handle!=nextHandle?"network_changed":routeChanged?"route_changed":
            !configuration.equals(nextConfiguration)?"configuration_changed":
            !reportedSsid.isEmpty()&&!knownSsid.isEmpty()&&!reportedSsid.equals(knownSsid)?"ssid_changed":"unchanged";
        if(!initialized||handle!=nextHandle||routeChanged)knownSsid="";
        if(!reportedSsid.isEmpty())knownSsid=reportedSsid;
        handle=nextHandle;route=nextRoute;configuration=nextConfiguration;initialized=true;
        return reason;
    }
    String routingSsid(){return knownSsid;}
}
