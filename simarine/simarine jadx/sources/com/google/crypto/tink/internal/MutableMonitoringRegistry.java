package com.google.crypto.tink.internal;

import java.util.concurrent.atomic.AtomicReference;

/* JADX INFO: loaded from: classes2.dex */
public final class MutableMonitoringRegistry {
    private final AtomicReference<MonitoringClient> monitoringClient = new AtomicReference<>();
    private static final MutableMonitoringRegistry GLOBAL_INSTANCE = new MutableMonitoringRegistry();
    private static final DoNothingClient DO_NOTHING_CLIENT = new DoNothingClient();

    public static MutableMonitoringRegistry globalInstance() {
        return GLOBAL_INSTANCE;
    }

    private static class DoNothingClient implements MonitoringClient {
        private DoNothingClient() {
        }

        @Override // com.google.crypto.tink.internal.MonitoringClient
        public MonitoringClient.Logger createLogger(KeysetHandleInterface keysetInfo, MonitoringAnnotations annotations, String primitive, String api) {
            return MonitoringUtil.DO_NOTHING_LOGGER;
        }
    }

    public synchronized void clear() {
        this.monitoringClient.set(null);
    }

    public synchronized void registerMonitoringClient(MonitoringClient client) {
        if (this.monitoringClient.get() != null) {
            throw new IllegalStateException("a monitoring client has already been registered");
        }
        this.monitoringClient.set(client);
    }

    public MonitoringClient getMonitoringClient() {
        MonitoringClient monitoringClient = this.monitoringClient.get();
        return monitoringClient == null ? DO_NOTHING_CLIENT : monitoringClient;
    }
}
