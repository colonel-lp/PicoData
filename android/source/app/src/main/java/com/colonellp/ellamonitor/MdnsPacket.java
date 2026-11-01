package com.colonellp.ellamonitor;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded RFC 6762 one-shot hostname queries; no service advertisement required. */
final class MdnsPacket {
    static String name(String host){String s=host.toLowerCase(Locale.ROOT);return s.endsWith(".")?s.substring(0,s.length()-1):s;}
    static byte[] query(String host)throws IOException{
        String n=name(host);if(!n.endsWith(".local")||n.length()>253)throw new IOException("Invalid local hostname.");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeShort(0);out.writeShort(0);out.writeShort(2);out.writeShort(0);out.writeShort(0);out.writeShort(0);
        for(int type:new int[]{1,28}){for(String label:n.split("\\.",-1)){byte[] b=label.getBytes(StandardCharsets.US_ASCII);if(b.length<1||b.length>63||!label.matches("[a-z0-9_-]+"))throw new IOException("Invalid local hostname.");out.writeByte(b.length);out.write(b);}out.writeByte(0);out.writeShort(type);out.writeShort(1);}
        return bytes.toByteArray();
    }
    static List<InetAddress> addresses(byte[] data,int length,String host)throws IOException{
        if(length<12||length>9000||length>data.length)throw new IOException("Invalid mDNS packet.");
        Parser p=new Parser(Arrays.copyOf(data,length));
        if(p.u16()!=0||(p.u16()&0xfa0f)!=0x8000)return Collections.emptyList();
        int questions=p.u16(),answers=p.u16(),authority=p.u16(),additional=p.u16();
        if(questions>16||answers+authority+additional>128)throw new IOException("Oversized mDNS reply.");
        for(int i=0;i<questions;i++){p.readName();p.skip(4);}
        List<InetAddress> result=new ArrayList<>();
        for(int i=0;i<answers+authority+additional;i++){
            String owner=p.readName();int type=p.u16(),cl=p.u16();long ttl=((long)p.u16()<<16)|p.u16();int size=p.u16(),at=p.at;p.skip(size);
            if(!name(host).equals(owner)||(cl&0x7fff)!=1||ttl==0||!(type==1&&size==4||type==28&&size==16))continue;
            InetAddress a=InetAddress.getByAddress(host,Arrays.copyOfRange(p.data,at,at+size));
            if(!a.isAnyLocalAddress()&&!a.isLoopbackAddress()&&!a.isMulticastAddress()&&!result.contains(a)&&result.size()<16)result.add(a);
        }
        return result;
    }
    static boolean prefix(byte[] a,byte[] b,int bits){
        if(a.length!=b.length||bits<0||bits>a.length*8)return false;
        for(int i=0;i<bits;i++)if(((a[i/8]^b[i/8])&(1<<(7-i%8)))!=0)return false;return true;
    }
    private static final class Parser {
        final byte[] data;int at;Parser(byte[] data){this.data=data;}
        int u16()throws IOException{if(at+2>data.length)throw new IOException("Truncated mDNS packet.");return ((data[at++]&255)<<8)|(data[at++]&255);}
        void skip(int n)throws IOException{if(n<0||at+n>data.length)throw new IOException("Truncated mDNS packet.");at+=n;}
        String readName()throws IOException{
            int pos=at,end=-1,jumps=0,labels=0;StringBuilder text=new StringBuilder();Set<Integer> visited=new HashSet<>();
            while(true){
                if(pos>=data.length||!visited.add(pos)||++labels>128)throw new IOException("Invalid mDNS name.");
                int size=data[pos++]&255;
                if(size==0){if(end<0)end=pos;break;}
                if((size&0xc0)==0xc0){if(pos>=data.length||++jumps>16)throw new IOException("Invalid mDNS pointer.");int next=((size&63)<<8)|(data[pos++]&255);if(end<0)end=pos;pos=next;continue;}
                if(size>63||pos+size>data.length)throw new IOException("Invalid mDNS label.");
                if(text.length()>0)text.append('.');text.append(new String(data,pos,size,StandardCharsets.US_ASCII));pos+=size;
                if(text.length()>253)throw new IOException("Long mDNS name.");
            }
            at=end;return text.toString().toLowerCase(Locale.ROOT);
        }
    }
}
