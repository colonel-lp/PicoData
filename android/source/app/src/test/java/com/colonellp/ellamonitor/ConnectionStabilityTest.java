package com.colonellp.ellamonitor;

import org.junit.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class ConnectionStabilityTest {
    @Test public void redactedNameKeepsSameRouteAndKnownEndpointRole(){
        NetworkIdentity n=new NetworkIdentity();
        assertEquals("initial",n.observe(1,"route-a","Test home","config-a"));
        for(String name:new String[]{"","Test home","\"<unknown ssid>\"",null,"Test home"})assertEquals("unchanged",n.observe(1,"route-a",name,"config-a"));
        assertEquals("unchanged",n.observe(1,"","","config-a"));
        assertEquals("Test home",n.routingSsid());
        NetworkIdentity early=new NetworkIdentity();early.observe(1,"","Test home","config-a");
        assertEquals("unchanged",early.observe(1,"route-a","","config-a"));assertEquals("Test home",early.routingSsid());
    }
    @Test public void actualRouteConfigAndKnownNetworkChangesInvalidateSelection(){
        NetworkIdentity n=new NetworkIdentity();n.observe(1,"route-a","Test home","a");
        assertEquals("ssid_changed",n.observe(1,"route-a","Test mobile","a"));
        assertEquals("configuration_changed",n.observe(1,"route-a","","b"));
        assertEquals("Test mobile",n.routingSsid());
        assertEquals("route_changed",n.observe(1,"route-b","","b"));assertEquals("",n.routingSsid());
        assertEquals("network_changed",n.observe(2,"route-b","","b"));assertEquals("",n.routingSsid());
    }
    @Test public void diagnosticsAreBoundedAndExcludeExceptionMessagesAndMetricIdentifiers(){
        long[] now={100};ConnectionDiagnostics d=new ConnectionDiagnostics(()->now[0],()->0);
        for(int i=0;i<400;i++){now[0]++;d.request(ConnectionDiagnostics.operation("/api/v1/history/metrics/private-id"),"headers",i,0,new IOException("TOKEN secret hostname private-id"));}
        String text=d.text("0.32");assertEquals(256,text.split("request history",-1).length-1);
        assertFalse(text.contains("TOKEN"));assertFalse(text.contains("secret"));assertFalse(text.contains("hostname"));assertFalse(text.contains("private-id"));assertTrue(text.contains("IOException"));assertTrue(text.contains("0.32"));
    }
    @Test public void snapshotExpiryIsRecordedOncePerTransition(){
        ConnectionDiagnostics d=new ConnectionDiagnostics(()->0,()->0);
        d.snapshot(100,true);d.snapshot(1000,true);d.snapshot(3001,true);d.snapshot(5000,true);d.snapshot(100,true);
        String text=d.text("0.32");assertEquals(1,text.split("snapshot expired",-1).length-1);assertEquals(2,text.split("snapshot fresh",-1).length-1);
    }
    @Test public void apiDiagnosticsCaptureFailureStageWithoutRecordingSecrets()throws Exception{
        ConnectionDiagnostics d=new ConnectionDiagnostics(()->0,()->0);
        ApiClient c=new ApiClient("http://pi.example.invalid:8080",repeat('a'),url->new HttpURLConnection(url){
            public void connect(){}public void disconnect(){}public boolean usingProxy(){return false;}
            public int getResponseCode()throws IOException{throw new SocketException("secret endpoint and token");}
        },d);
        try{c.get("/api/v1/live");fail();}catch(SocketException expected){}
        String text=d.text("0.32");assertTrue(text.contains("stage=headers"));assertTrue(text.contains("SocketException"));assertFalse(text.contains("secret"));assertFalse(text.contains("pi.example"));assertFalse(text.contains(repeat('a')));
    }
    static String repeat(char c){char[] value=new char[64];java.util.Arrays.fill(value,c);return new String(value);}
}
