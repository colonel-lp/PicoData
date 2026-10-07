package com.google.crypto.tink.internal;

/* JADX INFO: loaded from: classes2.dex */
public final class MonitoringUtil {
    public static final MonitoringClient.Logger DO_NOTHING_LOGGER = new DoNothingLogger();

    private static class DoNothingLogger implements MonitoringClient.Logger {
        @Override // com.google.crypto.tink.internal.MonitoringClient.Logger
        public void log(int keyId, long numBytesAsInput) {
        }

        @Override // com.google.crypto.tink.internal.MonitoringClient.Logger
        public void logFailure() {
        }

        private DoNothingLogger() {
        }
    }

    private MonitoringUtil() {
    }
}
