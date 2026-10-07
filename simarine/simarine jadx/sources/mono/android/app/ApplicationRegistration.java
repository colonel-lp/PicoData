package mono.android.app;

import crc6488302ad6e9e4df1a.MauiApplication;
import crc64a057c3230d7882d7.MainApplication;
import mono.android.Runtime;

/* JADX INFO: loaded from: classes2.dex */
public class ApplicationRegistration {
    public static void registerApplications() {
        Runtime.register("Microsoft.Maui.MauiApplication, Microsoft.Maui, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null", MauiApplication.class, MauiApplication.__md_methods);
        Runtime.register("PicoM.MainApplication, PicoM, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null", MainApplication.class, MainApplication.__md_methods);
    }
}
