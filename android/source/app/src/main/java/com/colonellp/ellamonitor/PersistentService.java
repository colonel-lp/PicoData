package com.colonellp.ellamonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Matches EQ & DSP persistence: a return-to-app notification, no background polling. */
public final class PersistentService extends Service {
    private static final String CHANNEL="ella_persistent";
    private static final int ID=1701;
    @Override public void onCreate(){super.onCreate();NotificationManager manager=getSystemService(NotificationManager.class);NotificationChannel channel=new NotificationChannel(CHANNEL,"Persistent app",NotificationManager.IMPORTANCE_LOW);channel.setDescription("Return to Ella Monitoring");manager.createNotificationChannel(channel);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(!getSharedPreferences("viewer",MODE_PRIVATE).getBoolean("persistent",false)){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return START_NOT_STICKY;}
        boolean visible=intent!=null&&intent.getBooleanExtra("visible",false);
        if(visible){stopForeground(STOP_FOREGROUND_REMOVE);return START_STICKY;}
        Intent launch=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent tap=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,CHANNEL).setSmallIcon(com.colonellp.ellamonitor.R.drawable.app_icon).setContentTitle("Ella Monitoring").setContentIntent(tap).setOngoing(true).setShowWhen(false).build();
        if(android.os.Build.VERSION.SDK_INT>=34)startForeground(ID,notification,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(ID,notification);
        return START_STICKY;
    }
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onDestroy(){stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
}
