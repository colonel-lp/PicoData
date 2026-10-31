package com.colonellp.ellamonitor;

import com.jcraft.jsch.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Fixed operations only; bounded output/time, pinned host identity before authentication. */
final class PiSshClient {
    interface Sockets {Socket connect(String host,int port) throws Exception;}
    static final class Credentials {
        final String user,password,key,passphrase,fingerprint;final int port;
        Credentials(String user,int port,String password,String key,String passphrase,String fingerprint){this.user=user;this.port=port;this.password=password;this.key=key;this.passphrase=passphrase;this.fingerprint=fingerprint;}
    }
    static final class UntrustedKey extends IOException {
        final String fingerprint;final boolean changed;
        UntrustedKey(String value,boolean changed){super(changed?"Pi SSH host key changed. Check the Pi identity in SSH setup.":"Verify the Pi SSH host key.");fingerprint=value;this.changed=changed;}
    }
    // No owner-specific network names, users, addresses, values or configuration paths.
    static final String READ=
        "export LC_ALL=C PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin; printf 'protocol\\tella-pi-control-1\\n'; "+
        "printf 'boot\\t'; cat /proc/sys/kernel/random/boot_id; "+
        "printf 'ssid\\t'; iw dev wlan1 link 2>/dev/null | sed -n 's/^[[:space:]]*SSID: //p'; printf '\\n'; "+
        "printf 'profile\\t'; nmcli -g GENERAL.CONNECTION device show wlan1 2>/dev/null; "+
        "printf 'ip\\t'; ip -o -4 addr show dev wlan1 2>/dev/null | awk 'NR==1 {print $4}'; printf '\\n'; "+
        "printf 'mode\\t'; if [ -r /etc/pi-wifi-mode ]; then cat /etc/pi-wifi-mode; elif [ -e /etc/pi-wifi-mode ]; then printf 'unknown\\n'; else printf 'auto\\n'; fi; "+
        "awk '/^MemTotal:/ {t=$2} /^MemAvailable:/ {a=$2} END {printf \"memory\\t%d %d\\n\",t,a}' /proc/meminfo; "+
        "awk 'NR==1 {printf \"cpu\\t\"; for(i=2;i<=9;i++)printf \"%s \",$i; print \"\"}' /proc/stat; "+
        "printf 'load\\t'; cut -d ' ' -f 1-3 /proc/loadavg; "+
        "printf 'temperature\\t'; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null; printf '\\n'; true";
    private final Sockets sockets;
    private volatile Session session;private volatile ChannelExec channel;private volatile Socket socket;private volatile boolean cancelled;
    private volatile String observed="";
    PiSshClient(Sockets sockets){this.sockets=sockets;}
    static String fingerprint(byte[] key) throws Exception {return "SHA256:"+Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(key));}
    String run(String host,Credentials c,String action) throws Exception {
        boolean read=action.equals("status");
        if(!read&&!action.equals("auto")&&!action.equals("home")&&!action.equals("m1"))throw new IllegalArgumentException("Unsupported Pi operation.");
        if(cancelled)throw new IOException("Cancelled.");
        JSch ssh=new JSch();
        ssh.setHostKeyRepository(new HostKeyRepository(){
            public int check(String name,byte[] key){try{observed=fingerprint(key);return !c.fingerprint.isEmpty()&&MessageDigest.isEqual(observed.getBytes(StandardCharsets.US_ASCII),c.fingerprint.getBytes(StandardCharsets.US_ASCII))?OK:c.fingerprint.isEmpty()?NOT_INCLUDED:CHANGED;}catch(Exception e){return CHANGED;}}
            public void add(HostKey h,UserInfo u){}public void remove(String h,String t){}public void remove(String h,String t,byte[] k){}
            public String getKnownHostsRepositoryID(){return "Ella Monitoring pinned Pi";}public HostKey[] getHostKey(){return new HostKey[0];}public HostKey[] getHostKey(String h,String t){return new HostKey[0];}
        });
        try {
            if(!c.key.isEmpty())ssh.addIdentity("Pi key",c.key.getBytes(StandardCharsets.UTF_8),null,c.passphrase.isEmpty()?null:c.passphrase.getBytes(StandardCharsets.UTF_8));
            session=ssh.getSession(c.user,PiNetworkConfig.host(host),c.port);
            session.setConfig("StrictHostKeyChecking","yes");session.setConfig("PreferredAuthentications",c.key.isEmpty()?"password":"publickey");
            // Platform Java 8 crypto on Android 8.1; do not enable legacy SHA-1 host keys.
            session.setConfig("server_host_key","ecdsa-sha2-nistp256,rsa-sha2-512,rsa-sha2-256");
            if(c.key.isEmpty())session.setPassword(c.password);
            session.setTimeout(6000);session.setServerAliveInterval(4000);session.setServerAliveCountMax(1);
            session.setSocketFactory(new com.jcraft.jsch.SocketFactory(){
                public Socket createSocket(String host,int port)throws IOException{try{socket=sockets.connect(host,port);if(cancelled){socket.close();throw new IOException("Cancelled.");}return socket;}catch(Exception e){throw new IOException("Pi SSH connection failed.");}}
                public InputStream getInputStream(Socket s)throws IOException{return s.getInputStream();}public OutputStream getOutputStream(Socket s)throws IOException{return s.getOutputStream();}
            });
            session.connect(8000);if(cancelled)throw new IOException("Cancelled.");
            channel=(ChannelExec)session.openChannel("exec");
            channel.setCommand(read?READ:"sudo -S -p '' -- /usr/local/sbin/pi-wifi "+action);
            channel.setPty(false);channel.setInputStream(new ByteArrayInputStream(read?new byte[0]:(c.password+"\n").getBytes(StandardCharsets.UTF_8)));
            // Discard remote stderr; it may contain installation details or command diagnostics.
            channel.setErrStream(new OutputStream(){public void write(int b){}});
            InputStream input=channel.getInputStream();channel.connect(3000);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[1024];long deadline=System.nanoTime()+(read?12_000_000_000L:45_000_000_000L);
            for(;;){
                if(cancelled||Thread.currentThread().isInterrupted())throw new IOException("Cancelled.");
                while(input.available()>0){int n=input.read(buffer,0,Math.min(buffer.length,input.available()));if(n<0)break;if(bytes.size()+n>16384)throw new IOException("Pi response exceeds the limit.");bytes.write(buffer,0,n);}
                if(channel.isClosed()&&input.available()==0)break;
                if(System.nanoTime()>deadline)throw new IOException("Pi operation timed out.");Thread.sleep(20);
            }
            if(channel.getExitStatus()!=0)throw new IOException("Pi command failed. Check SSH/sudo credentials and pi-wifi installation.");
            return bytes.toString("UTF-8");
        }catch(Exception e){
            if(!observed.isEmpty()&&!observed.equals(c.fingerprint))throw new UntrustedKey(observed,!c.fingerprint.isEmpty());
            if(e instanceof IOException)throw e;
            throw new IOException("Pi SSH connection failed. Check address, credentials and supported key format.");
        }finally{close();ssh.removeAllIdentity();}
    }
    void close(){ChannelExec ch=channel;if(ch!=null)ch.disconnect();Session s=session;if(s!=null)s.disconnect();Socket sock=socket;if(sock!=null)try{sock.close();}catch(IOException ignored){}}
    void cancel(){cancelled=true;close();}
}
