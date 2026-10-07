package com.microsoft.maui.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.microsoft.maui.ImageLoaderCallback;
import com.microsoft.maui.PlatformLogger;

/* JADX INFO: loaded from: classes2.dex */
public class MauiCustomTarget extends CustomTarget<Drawable> implements MauiTarget {
    private static final PlatformLogger logger = new PlatformLogger("MauiCustomTarget");
    private final ImageLoaderCallback callback;
    private boolean completed = false;
    private final Context context;
    private final String resourceLogIdentifier;

    public MauiCustomTarget(Context context, ImageLoaderCallback imageLoaderCallback, Object obj) {
        this.context = context;
        this.callback = imageLoaderCallback;
        if (logger.isVerboseLoggable && obj != null) {
            this.resourceLogIdentifier = obj.toString();
        } else {
            this.resourceLogIdentifier = null;
        }
    }

    @Override // com.microsoft.maui.glide.MauiTarget
    /* JADX INFO: renamed from: load */
    public void m285lambda$load$0$commicrosoftmauiglideMauiCustomViewTarget(RequestBuilder<Drawable> requestBuilder) {
        requestBuilder.into(this);
    }

    @Override // com.bumptech.glide.request.target.CustomTarget, com.bumptech.glide.request.target.Target
    public void onLoadFailed(Drawable drawable) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v("onLoadFailed: " + this.resourceLogIdentifier);
        }
        this.callback.onComplete(false, drawable, null);
    }

    @Override // com.bumptech.glide.request.target.Target
    public void onResourceReady(Drawable drawable, Transition<? super Drawable> transition) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v("onResourceReady: " + this.resourceLogIdentifier);
        }
        this.callback.onComplete(true, drawable, new Runnable() { // from class: com.microsoft.maui.glide.MauiCustomTarget$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.clear();
            }
        });
    }

    @Override // com.bumptech.glide.request.target.Target
    public void onLoadCleared(Drawable drawable) {
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v("onLoadCleared: " + this.resourceLogIdentifier);
        }
    }

    private void post(Runnable runnable) {
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper.isCurrentThread()) {
            runnable.run();
        } else {
            new Handler(mainLooper).post(runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clear() {
        post(new Runnable() { // from class: com.microsoft.maui.glide.MauiCustomTarget$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m284lambda$clear$0$commicrosoftmauiglideMauiCustomTarget();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$clear$0$com-microsoft-maui-glide-MauiCustomTarget, reason: not valid java name */
    /* synthetic */ void m284lambda$clear$0$commicrosoftmauiglideMauiCustomTarget() {
        Glide.with(this.context).clear(this);
    }
}
