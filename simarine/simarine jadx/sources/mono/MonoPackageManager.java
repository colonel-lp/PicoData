package mono;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import java.time.OffsetDateTime;
import java.util.Locale;
import mono.android.BuildConfig;
import mono.android.DebugRuntime;
import mono.android.Runtime;
import mono.android.app.ApplicationRegistration;
import mono.android.app.NotifyTimeZoneChanges;

/* JADX INFO: loaded from: classes2.dex */
public class MonoPackageManager {
    static Context Context;
    static boolean initialized;
    static Object lock = new Object();

    public static void setContext(Context context) {
    }

    public static void LoadApplication(Context context, ApplicationInfo applicationInfo, String[] strArr) {
        synchronized (lock) {
            if (context instanceof Application) {
                Context = context;
            }
            if (!initialized) {
                context.registerReceiver(new NotifyTimeZoneChanges(), new IntentFilter("android.intent.action.TIMEZONE_CHANGED"));
                Locale locale = Locale.getDefault();
                String str = locale.getLanguage() + "-" + locale.getCountry();
                String absolutePath = context.getFilesDir().getAbsolutePath();
                String absolutePath2 = context.getCacheDir().getAbsolutePath();
                String nativeLibraryPath = getNativeLibraryPath(context);
                ClassLoader classLoader = context.getClassLoader();
                String nativeLibraryPath2 = getNativeLibraryPath(applicationInfo);
                int totalSeconds = OffsetDateTime.now().getOffset().getTotalSeconds();
                String[] strArr2 = {absolutePath, absolutePath2, nativeLibraryPath};
                boolean z = false;
                if (applicationInfo.splitSourceDirs != null && applicationInfo.splitSourceDirs.length > 1) {
                    z = true;
                }
                boolean z2 = z;
                if (BuildConfig.Debug) {
                    System.loadLibrary("xamarin-debug-app-helper");
                    DebugRuntime.init(strArr, nativeLibraryPath2, strArr2, z2);
                } else {
                    System.loadLibrary("monosgen-2.0");
                }
                System.loadLibrary("xamarin-app");
                if (!BuildConfig.DotNetRuntime) {
                    System.loadLibrary("mono-native");
                } else {
                    System.loadLibrary("System.Security.Cryptography.Native.Android");
                }
                System.loadLibrary("monodroid");
                Runtime.initInternal(str, strArr, nativeLibraryPath2, strArr2, totalSeconds, classLoader, MonoPackageManager_Resources.Assemblies, isEmulator(), z2);
                ApplicationRegistration.registerApplications();
                initialized = true;
            }
        }
    }

    static boolean isEmulator() {
        String str = Build.HARDWARE;
        return str.contains("ranchu") || str.contains("goldfish");
    }

    static String getNativeLibraryPath(Context context) {
        return getNativeLibraryPath(context.getApplicationInfo());
    }

    static String getNativeLibraryPath(ApplicationInfo applicationInfo) {
        return applicationInfo.nativeLibraryDir;
    }

    public static String[] getAssemblies() {
        return MonoPackageManager_Resources.Assemblies;
    }

    public static String[] getDependencies() {
        return MonoPackageManager_Resources.Dependencies;
    }
}
