package com.colonellp.ellamonitor;

/** Keep the platform crash handler; never swallow or restart a crashing app. */
final class CrashRecorder implements Thread.UncaughtExceptionHandler {
    private final ConnectionDiagnostics diagnostics;
    private final Thread.UncaughtExceptionHandler next;
    private CrashRecorder(ConnectionDiagnostics d,Thread.UncaughtExceptionHandler n){diagnostics=d;next=n;}
    static CrashRecorder install(ConnectionDiagnostics diagnostics){
        Thread.UncaughtExceptionHandler original=Thread.getDefaultUncaughtExceptionHandler();
        if(original instanceof CrashRecorder)original=((CrashRecorder)original).next;
        if(original==null)return null;
        CrashRecorder recorder=new CrashRecorder(diagnostics,original);Thread.setDefaultUncaughtExceptionHandler(recorder);return recorder;
    }
    void restore(){if(Thread.getDefaultUncaughtExceptionHandler()==this)Thread.setDefaultUncaughtExceptionHandler(next);}
    @Override public void uncaughtException(Thread thread,Throwable error){try{diagnostics.crash(error);}finally{next.uncaughtException(thread,error);}}
}
