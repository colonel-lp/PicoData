package com.colonellp.ellamonitor;

import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/** Bounded, strict JSON for files that can be edited outside the app. */
final class SavedJson {
    static final int MAX_BYTES=65536;
    static JSONObject read(InputStream input) throws Exception {
        if(input==null)throw invalid();
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;
        while((n=input.read(buffer))!=-1){if(out.size()+n>MAX_BYTES)throw new IllegalArgumentException("Saved file is larger than 64 KB.");out.write(buffer,0,n);}
        String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(out.toByteArray())).toString();
        return object(text);
    }
    static JSONObject object(String text) throws Exception {
        if(text==null||text.getBytes(StandardCharsets.UTF_8).length>MAX_BYTES)throw invalid();
        Parser parser=new Parser(text);parser.value(0);parser.space();if(parser.at!=text.length()||!text.trim().startsWith("{"))throw invalid();
        return new JSONObject(text);
    }
    static String name(String input) {
        if(input==null)throw new IllegalArgumentException("Use a name of 1–80 characters without file separators.");
        String name=input.trim();
        if(name.isEmpty()||name.length()>80||name.endsWith(".")||name.equals("Default")||name.equals("System default"))throw new IllegalArgumentException("Use a name of 1–80 characters without file separators.");
        for(int i=0;i<name.length();i++)if(Character.isISOControl(name.charAt(i))||"\\/:*?\"<>|".indexOf(name.charAt(i))>=0)throw new IllegalArgumentException("Use a name of 1–80 characters without file separators.");
        return name;
    }
    static void string(JSONObject o,String key,int min,int max) throws Exception {
        Object v=o.get(key);if(!(v instanceof String)||((String)v).length()<min||((String)v).length()>max)throw invalid();
    }
    static long integer(JSONObject o,String key,long min,long max) throws Exception {
        Object v=o.get(key);if(!(v instanceof Number))throw invalid();double d=((Number)v).doubleValue();
        if(!Double.isFinite(d)||d!=Math.rint(d)||d<min||d>max)throw invalid();return ((Number)v).longValue();
    }
    static void bool(JSONObject o,String key) throws Exception {if(!(o.get(key) instanceof Boolean))throw invalid();}
    static IllegalArgumentException invalid(){return new IllegalArgumentException("Malformed or unsupported saved file.");}

    /** JSONObject also accepts non-JSON syntax; check grammar and duplicate keys first. */
    private static final class Parser {
        final String text;int at;
        Parser(String text){this.text=text;}
        void space(){while(at<text.length()&&" \t\r\n".indexOf(text.charAt(at))>=0)at++;}
        boolean take(char c){space();if(at<text.length()&&text.charAt(at)==c){at++;return true;}return false;}
        void need(char c){if(!take(c))throw invalid();}
        void value(int depth) throws Exception {
            if(depth>32)throw invalid();space();if(at>=text.length())throw invalid();char c=text.charAt(at);
            if(c=='{'){at++;Set<String> keys=new HashSet<>();if(take('}'))return;do{space();String key=string();if(!keys.add(key))throw invalid();need(':');value(depth+1);}while(take(','));need('}');}
            else if(c=='['){at++;if(take(']'))return;do{value(depth+1);}while(take(','));need(']');}
            else if(c=='"')string();
            else if(c=='t')literal("true");else if(c=='f')literal("false");else if(c=='n')literal("null");
            else{int start=at;if(c=='-')at++;if(at>=text.length())throw invalid();if(text.charAt(at)=='0')at++;else{if(text.charAt(at)<'1'||text.charAt(at)>'9')throw invalid();digits();}if(at<text.length()&&text.charAt(at)=='.'){at++;int p=at;digits();if(p==at)throw invalid();}if(at<text.length()&&(text.charAt(at)=='e'||text.charAt(at)=='E')){at++;if(at<text.length()&&(text.charAt(at)=='+'||text.charAt(at)=='-'))at++;int p=at;digits();if(p==at)throw invalid();}if(at==start)throw invalid();}
        }
        void digits(){while(at<text.length()&&text.charAt(at)>='0'&&text.charAt(at)<='9')at++;}
        void literal(String word){if(!text.startsWith(word,at))throw invalid();at+=word.length();}
        String string() throws Exception {
            int start=at;if(at>=text.length()||text.charAt(at++)!='"')throw invalid();
            while(at<text.length()){char c=text.charAt(at++);if(c=='"')return (String)new JSONTokener(text.substring(start,at)).nextValue();if(c<32)throw invalid();if(c=='\\'){if(at>=text.length())throw invalid();c=text.charAt(at++);if(c=='u'){for(int i=0;i<4;i++){if(at>=text.length()||Character.digit(text.charAt(at++),16)<0)throw invalid();}}else if("\"\\/bfnrt".indexOf(c)<0)throw invalid();}}
            throw invalid();
        }
    }
}
