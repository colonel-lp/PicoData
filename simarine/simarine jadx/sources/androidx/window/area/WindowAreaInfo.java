package androidx.window.area;

import android.os.Binder;
import androidx.window.extensions.area.ExtensionWindowAreaPresentation;
import androidx.window.extensions.area.WindowAreaComponent;
import androidx.window.layout.WindowMetrics;
import java.util.HashMap;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: WindowAreaInfo.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001&B'\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t¢\u0006\u0002\u0010\nJ\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\rH\u0002J\u0013\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\u0010\u0010 \u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u001c\u001a\u00020\rJ\u000e\u0010!\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\rJ\b\u0010\"\u001a\u00020#H\u0016J\b\u0010$\u001a\u00020%H\u0016R0\u0010\u000b\u001a\u001e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u000e0\fj\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u000e`\u000fX\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006'"}, d2 = {"Landroidx/window/area/WindowAreaInfo;", "", "metrics", "Landroidx/window/layout/WindowMetrics;", "type", "Landroidx/window/area/WindowAreaInfo$Type;", "token", "Landroid/os/Binder;", "windowAreaComponent", "Landroidx/window/extensions/area/WindowAreaComponent;", "(Landroidx/window/layout/WindowMetrics;Landroidx/window/area/WindowAreaInfo$Type;Landroid/os/Binder;Landroidx/window/extensions/area/WindowAreaComponent;)V", "capabilityMap", "Ljava/util/HashMap;", "Landroidx/window/area/WindowAreaCapability$Operation;", "Landroidx/window/area/WindowAreaCapability;", "Lkotlin/collections/HashMap;", "getCapabilityMap$window_release", "()Ljava/util/HashMap;", "getMetrics", "()Landroidx/window/layout/WindowMetrics;", "setMetrics", "(Landroidx/window/layout/WindowMetrics;)V", "getToken", "()Landroid/os/Binder;", "getType", "()Landroidx/window/area/WindowAreaInfo$Type;", "createRearFacingSession", "Landroidx/window/area/WindowAreaSession;", "operation", "equals", "", "other", "getActiveSession", "getCapability", "hashCode", "", "toString", "", "Type", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public final class WindowAreaInfo {
    private final HashMap<WindowAreaCapability.Operation, WindowAreaCapability> capabilityMap;
    private WindowMetrics metrics;
    private final Binder token;
    private final Type type;
    private final WindowAreaComponent windowAreaComponent;

    public WindowAreaInfo(WindowMetrics metrics, Type type, Binder token, WindowAreaComponent windowAreaComponent) {
        Intrinsics.checkNotNullParameter(metrics, "metrics");
        Intrinsics.checkNotNullParameter(type, "type");
        Intrinsics.checkNotNullParameter(token, "token");
        Intrinsics.checkNotNullParameter(windowAreaComponent, "windowAreaComponent");
        this.metrics = metrics;
        this.type = type;
        this.token = token;
        this.windowAreaComponent = windowAreaComponent;
        this.capabilityMap = new HashMap<>();
    }

    public final WindowMetrics getMetrics() {
        return this.metrics;
    }

    public final void setMetrics(WindowMetrics windowMetrics) {
        Intrinsics.checkNotNullParameter(windowMetrics, "<set-?>");
        this.metrics = windowMetrics;
    }

    public final Type getType() {
        return this.type;
    }

    public final Binder getToken() {
        return this.token;
    }

    public final HashMap<WindowAreaCapability.Operation, WindowAreaCapability> getCapabilityMap$window_release() {
        return this.capabilityMap;
    }

    public final WindowAreaCapability getCapability(WindowAreaCapability.Operation operation) {
        Intrinsics.checkNotNullParameter(operation, "operation");
        WindowAreaCapability windowAreaCapability = this.capabilityMap.get(operation);
        return windowAreaCapability == null ? new WindowAreaCapability(operation, WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED) : windowAreaCapability;
    }

    public final WindowAreaSession getActiveSession(WindowAreaCapability.Operation operation) {
        Intrinsics.checkNotNullParameter(operation, "operation");
        if (!Intrinsics.areEqual(getCapability(operation).getStatus(), WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE)) {
            throw new IllegalStateException("No session is currently active");
        }
        if (Intrinsics.areEqual(this.type, Type.TYPE_REAR_FACING)) {
            return createRearFacingSession(operation);
        }
        return null;
    }

    private final WindowAreaSession createRearFacingSession(WindowAreaCapability.Operation operation) {
        if (Intrinsics.areEqual(operation, WindowAreaCapability.Operation.OPERATION_TRANSFER_ACTIVITY_TO_AREA)) {
            return new RearDisplaySessionImpl(this.windowAreaComponent);
        }
        if (Intrinsics.areEqual(operation, WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA)) {
            WindowAreaComponent windowAreaComponent = this.windowAreaComponent;
            ExtensionWindowAreaPresentation rearDisplayPresentation = windowAreaComponent.getRearDisplayPresentation();
            Intrinsics.checkNotNull(rearDisplayPresentation);
            return new RearDisplayPresentationSessionPresenterImpl(windowAreaComponent, rearDisplayPresentation);
        }
        throw new IllegalArgumentException("Invalid operation provided");
    }

    /* JADX INFO: compiled from: WindowAreaInfo.kt */
    @Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0003H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0007"}, d2 = {"Landroidx/window/area/WindowAreaInfo$Type;", "", "description", "", "(Ljava/lang/String;)V", "toString", "Companion", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    public static final class Type {
        public static final Type TYPE_REAR_FACING = new Type("REAR FACING");
        private final String description;

        private Type(String str) {
            this.description = str;
        }

        /* JADX INFO: renamed from: toString, reason: from getter */
        public String getDescription() {
            return this.description;
        }
    }

    public boolean equals(Object other) {
        if (other instanceof WindowAreaInfo) {
            WindowAreaInfo windowAreaInfo = (WindowAreaInfo) other;
            if (Intrinsics.areEqual(this.metrics, windowAreaInfo.metrics) && Intrinsics.areEqual(this.type, windowAreaInfo.type) && Intrinsics.areEqual(this.capabilityMap.entrySet(), windowAreaInfo.capabilityMap.entrySet())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return (((this.metrics.hashCode() * 31) + this.type.hashCode()) * 31) + this.capabilityMap.entrySet().hashCode();
    }

    public String toString() {
        return "WindowAreaInfo{ Metrics: " + this.metrics + ", type: " + this.type + ", Capabilities: " + this.capabilityMap.entrySet() + " }";
    }
}
