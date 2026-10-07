package androidx.window.area;

import kotlin.Metadata;

/* JADX INFO: compiled from: WindowAreaPresentationSessionCallback.kt */
/* JADX INFO: loaded from: classes.dex */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0012\u0010\u0006\u001a\u00020\u00032\b\u0010\u0007\u001a\u0004\u0018\u00010\bH&J\u0010\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH&ø\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001¨\u0006\fÀ\u0006\u0001"}, d2 = {"Landroidx/window/area/WindowAreaPresentationSessionCallback;", "", "onContainerVisibilityChanged", "", "isVisible", "", "onSessionEnded", "t", "", "onSessionStarted", "session", "Landroidx/window/area/WindowAreaSessionPresenter;", "window_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
public interface WindowAreaPresentationSessionCallback {
    void onContainerVisibilityChanged(boolean isVisible);

    void onSessionEnded(Throwable t);

    void onSessionStarted(WindowAreaSessionPresenter session);
}
