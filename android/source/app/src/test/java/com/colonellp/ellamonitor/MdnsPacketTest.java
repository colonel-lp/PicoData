package com.colonellp.ellamonitor;

import org.junit.Test;
import java.io.*;
import java.net.*;
import static org.junit.Assert.*;

public class MdnsPacketTest {
    private byte[] reply(String host,int flags,int ttl)throws Exception{
        byte[] query=MdnsPacket.query(host);ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeShort(0);out.writeShort(flags);out.writeShort(2);out.writeShort(1);out.writeShort(0);out.writeShort(0);out.write(query,12,query.length-12);
        out.writeShort(0xc00c);out.writeShort(1);out.writeShort(0x8001);out.writeInt(ttl);out.writeShort(4);out.writeInt(0xc000020a);return bytes.toByteArray();
    }
    @Test public void compressedAnswerMustMatchConfiguredHostnameAndPositiveTtl()throws Exception{
        byte[] b=reply("test-device.local",0x8400,120);
        assertEquals("192.0.2.10",MdnsPacket.addresses(b,b.length,"TEST-device.local.").get(0).getHostAddress());
        assertTrue(MdnsPacket.addresses(b,b.length,"other.local").isEmpty());
        b=reply("test-device.local",0x8400,0);assertTrue(MdnsPacket.addresses(b,b.length,"test-device.local").isEmpty());
        b=reply("test-device.local",0x8600,120);assertTrue(MdnsPacket.addresses(b,b.length,"test-device.local").isEmpty());
    }
    @Test public void cyclicNamesAndTruncatedPacketsAreRejected()throws Exception{
        byte[] b=reply("test-device.local",0x8400,120);b[12]=(byte)0xc0;b[13]=12;
        try{MdnsPacket.addresses(b,b.length,"test-device.local");fail();}catch(IOException expected){}
        try{MdnsPacket.addresses(new byte[2],2,"test-device.local");fail();}catch(IOException expected){}
    }
    @Test public void scopePrefixesKeepAddressFamiliesAndSubnetsSeparate()throws Exception{
        assertTrue(MdnsPacket.prefix(InetAddress.getByName("192.0.2.10").getAddress(),InetAddress.getByName("192.0.2.1").getAddress(),24));
        assertFalse(MdnsPacket.prefix(InetAddress.getByName("192.0.3.10").getAddress(),InetAddress.getByName("192.0.2.1").getAddress(),24));
        assertFalse(MdnsPacket.prefix(new byte[4],new byte[16],8));
    }
}
