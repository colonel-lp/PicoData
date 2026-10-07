package com.microsoft.maui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.core.view.ViewCompat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.microsoft.maui.glide.ShadowBitmapPool;

/* JADX INFO: loaded from: classes2.dex */
public abstract class PlatformWrapperView extends PlatformContentViewGroup {
    private final BitmapPool bitmapPool;
    private float[] bounds;
    private int[] colors;
    private boolean hasClip;
    private float offsetX;
    private float offsetY;
    private int paintType;
    private float[] positions;
    private float radius;
    private Bitmap shadowBitmap;
    private float shadowBitmapX;
    private float shadowBitmapY;
    private Canvas shadowCanvas;
    private boolean shadowInvalidated;
    private Paint shadowPaint;
    private Shader shadowShader;
    private final Rect viewBounds;

    @Deprecated
    protected final void setHasShadow(boolean z) {
    }

    public PlatformWrapperView(Context context) {
        super(context);
        this.shadowInvalidated = true;
        this.hasClip = false;
        this.paintType = 0;
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        this.radius = 0.0f;
        this.colors = new int[0];
        this.positions = new float[0];
        this.bounds = new float[0];
        this.viewBounds = new Rect();
        this.bitmapPool = ShadowBitmapPool.get(context);
        setClipChildren(false);
        setWillNotDraw(true);
    }

    @Override // com.microsoft.maui.PlatformContentViewGroup
    protected void setHasClip(boolean z) {
        super.setHasClip(z);
        this.hasClip = z;
        this.shadowInvalidated = true;
    }

    protected final void updateShadow(int i, float f, float f2, float f3, int[] iArr, float[] fArr, float[] fArr2) {
        this.paintType = i;
        this.radius = f;
        this.offsetX = f2;
        this.offsetY = f3;
        this.colors = iArr;
        this.positions = fArr;
        this.bounds = fArr2;
        if (i == 0) {
            this.shadowPaint = null;
            this.shadowCanvas = null;
            Bitmap bitmap = this.shadowBitmap;
            if (bitmap != null) {
                this.bitmapPool.put(bitmap);
                this.shadowBitmap = null;
            }
        } else {
            this.shadowCanvas = new Canvas();
            Paint paint = new Paint();
            this.shadowPaint = paint;
            paint.setAntiAlias(true);
            this.shadowPaint.setDither(true);
            this.shadowPaint.setFilterBitmap(true);
            this.shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            if (f > 0.0f) {
                this.shadowPaint.setMaskFilter(new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL));
            }
            if (i == 1) {
                this.shadowPaint.setColor(iArr.length > 0 ? iArr[0] : ViewCompat.MEASURED_STATE_MASK);
            }
        }
        this.shadowInvalidated = true;
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.shadowInvalidated = true;
        Bitmap bitmap = this.shadowBitmap;
        if (bitmap != null) {
            this.bitmapPool.put(bitmap);
            this.shadowBitmap = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.shadowInvalidated = true;
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        super.requestLayout();
        this.shadowInvalidated = true;
    }

    @Override // android.view.View
    public void invalidate() {
        super.invalidate();
        this.shadowInvalidated = true;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (getChildCount() == 0) {
            super.onMeasure(i, i2);
            return;
        }
        View childAt = getChildAt(0);
        this.viewBounds.set(0, 0, View.MeasureSpec.getSize(i), View.MeasureSpec.getSize(i2));
        childAt.measure(i, i2);
        setMeasuredDimension(childAt.getMeasuredWidth(), childAt.getMeasuredHeight());
    }

    @Override // com.microsoft.maui.PlatformContentViewGroup, android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        if (this.paintType != 0) {
            int iWidth = this.viewBounds.width();
            int iHeight = this.viewBounds.height();
            if (getChildCount() > 0) {
                View childAt = getChildAt(0);
                if (iWidth == 0) {
                    iWidth = childAt.getMeasuredWidth();
                }
                if (iHeight == 0) {
                    iHeight = childAt.getMeasuredHeight();
                }
            }
            if (iWidth > 0 && iHeight > 0) {
                drawShadow(canvas, iWidth, iHeight);
            }
        }
        super.dispatchDraw(canvas);
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void drawShadow(Canvas canvas, int i, int i2) {
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            Drawable background = childAt.getBackground();
            if (background != 0 && (background instanceof PlatformShadowDrawable)) {
                PlatformShadowDrawable platformShadowDrawable = (PlatformShadowDrawable) background;
                if (platformShadowDrawable.canDrawShadow()) {
                    int left = childAt.getLeft();
                    int top = childAt.getTop();
                    background.setBounds(0, 0, childAt.getRight() - left, childAt.getBottom() - top);
                    drawShadowViaPlatformShadowDrawable(canvas, platformShadowDrawable, i, i2);
                    return;
                }
            }
            drawShadowViaDispatchDraw(canvas, i, i2);
        }
    }

    private void drawShadowViaPlatformShadowDrawable(Canvas canvas, PlatformShadowDrawable platformShadowDrawable, int i, int i2) {
        int radiusSafeSpace = getRadiusSafeSpace();
        updateShadowShader(i + radiusSafeSpace, radiusSafeSpace + i2);
        Path clipPath = this.hasClip ? getClipPath(i, i2) : null;
        canvas.save();
        canvas.translate(this.offsetX, this.offsetY);
        platformShadowDrawable.drawShadow(canvas, this.shadowPaint, clipPath);
        canvas.restore();
    }

    private void drawShadowViaDispatchDraw(Canvas canvas, int i, int i2) {
        if (this.shadowInvalidated) {
            this.shadowInvalidated = false;
            int radiusSafeSpace = getRadiusSafeSpace();
            int iNormalizeForPool = normalizeForPool(i + radiusSafeSpace);
            int iNormalizeForPool2 = normalizeForPool(radiusSafeSpace + i2);
            int i3 = (iNormalizeForPool - i) / 2;
            int i4 = (iNormalizeForPool2 - i2) / 2;
            Bitmap bitmap = this.shadowBitmap;
            if (bitmap != null) {
                if (bitmap.getWidth() == iNormalizeForPool && this.shadowBitmap.getHeight() == iNormalizeForPool2) {
                    this.shadowBitmap.eraseColor(0);
                } else {
                    this.bitmapPool.put(this.shadowBitmap);
                    this.shadowBitmap = this.bitmapPool.get(iNormalizeForPool, iNormalizeForPool2, Bitmap.Config.ARGB_8888);
                }
            } else {
                this.shadowBitmap = this.bitmapPool.get(iNormalizeForPool, iNormalizeForPool2, Bitmap.Config.ARGB_8888);
            }
            this.shadowCanvas.setBitmap(this.shadowBitmap);
            Bitmap bitmap2 = this.bitmapPool.get(normalizeForPool(i), normalizeForPool(i2), Bitmap.Config.ALPHA_8);
            super.dispatchDraw(new Canvas(bitmap2));
            updateShadowShader(iNormalizeForPool, iNormalizeForPool2);
            float f = i3;
            float f2 = i4;
            this.shadowCanvas.drawBitmap(bitmap2, f, f2, this.shadowPaint);
            this.bitmapPool.put(bitmap2);
            this.shadowBitmapX = this.offsetX - f;
            this.shadowBitmapY = this.offsetY - f2;
        }
        canvas.drawBitmap(this.shadowBitmap, this.shadowBitmapX, this.shadowBitmapY, (Paint) null);
    }

    private int getRadiusSafeSpace() {
        return (int) (this.radius * 3.0f);
    }

    private static int normalizeForPool(int i) {
        return (int) (Math.ceil(((double) i) / 48.0d) * 48.0d);
    }

    private void updateShadowShader(int i, int i2) {
        Shader radialGradient;
        int i3 = this.paintType;
        if (i3 == 2) {
            float[] fArr = this.bounds;
            float f = i;
            float f2 = i2;
            radialGradient = new LinearGradient(fArr[0] * f, fArr[1] * f2, fArr[2] * f, fArr[3] * f2, this.colors, this.positions, Shader.TileMode.CLAMP);
        } else if (i3 == 3) {
            float[] fArr2 = this.bounds;
            radialGradient = new RadialGradient(fArr2[0] * i, fArr2[1] * i2, fArr2[2] * Math.max(i, i2), this.colors, this.positions, Shader.TileMode.CLAMP);
        } else {
            radialGradient = null;
        }
        if (radialGradient != null) {
            this.shadowPaint.setShader(radialGradient);
        }
    }
}
