package com.microsoft.maui.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.microsoft.maui.ImageLoaderCallback;
import com.microsoft.maui.PlatformLogger;
import microsoft.maui.R;

/* JADX INFO: loaded from: classes2.dex */
public class MauiCustomViewTarget extends CustomViewTarget<ImageView, Drawable> implements MauiTarget {
    private final ImageLoaderCallback callback;
    private boolean completed;
    private final String resourceLogIdentifier;
    private static final PlatformLogger logger = new PlatformLogger("MauiCustomViewTarget");
    private static final int RUNNING_CALLBACKS_TAG = R.id.maui_custom_view_target_running_callbacks_tag;

    public MauiCustomViewTarget(ImageView imageView, ImageLoaderCallback imageLoaderCallback, Object obj) {
        super(imageView);
        this.completed = false;
        this.callback = imageLoaderCallback;
        if (logger.isVerboseLoggable && obj != null) {
            this.resourceLogIdentifier = obj.toString();
        } else {
            this.resourceLogIdentifier = null;
        }
    }

    @Override // com.microsoft.maui.glide.MauiTarget
    /* JADX INFO: renamed from: load, reason: merged with bridge method [inline-methods] */
    public void m285lambda$load$0$commicrosoftmauiglideMauiCustomViewTarget(final RequestBuilder<Drawable> requestBuilder) {
        if (getIsInvokingCallbacks()) {
            PlatformLogger platformLogger = logger;
            if (platformLogger.isVerboseLoggable) {
                platformLogger.v("defer load: " + this.resourceLogIdentifier);
            }
            ((ImageView) this.view).post(new Runnable() { // from class: com.microsoft.maui.glide.MauiCustomViewTarget$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m285lambda$load$0$commicrosoftmauiglideMauiCustomViewTarget(requestBuilder);
                }
            });
            return;
        }
        requestBuilder.into(this);
    }

    @Override // com.bumptech.glide.request.target.CustomViewTarget
    protected void onResourceCleared(Drawable drawable) {
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v("onResourceCleared: " + this.resourceLogIdentifier);
        }
        ((ImageView) this.view).setImageDrawable(drawable);
    }

    @Override // com.bumptech.glide.request.target.Target
    public void onLoadFailed(Drawable drawable) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        PlatformLogger platformLogger = logger;
        if (platformLogger.isVerboseLoggable) {
            platformLogger.v("onLoadFailed: " + this.resourceLogIdentifier);
        }
        try {
            setIsInvokingCallbacks(true);
            this.callback.onComplete(false, drawable, null);
        } finally {
            setIsInvokingCallbacks(false);
        }
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
        ((ImageView) this.view).setImageDrawable(drawable);
        try {
            setIsInvokingCallbacks(true);
            this.callback.onComplete(true, drawable, null);
        } finally {
            setIsInvokingCallbacks(false);
        }
    }

    private void setIsInvokingCallbacks(boolean z) {
        ((ImageView) this.view).setTag(RUNNING_CALLBACKS_TAG, Boolean.valueOf(z));
    }

    private boolean getIsInvokingCallbacks() {
        Object tag = ((ImageView) this.view).getTag(RUNNING_CALLBACKS_TAG);
        return tag != null && (tag instanceof Boolean) && ((Boolean) tag).booleanValue();
    }
}
