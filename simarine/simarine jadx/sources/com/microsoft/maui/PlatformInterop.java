package com.microsoft.maui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.R;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TintTypedArray;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.window.layout.WindowMetricsCalculator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.microsoft.maui.glide.MauiCustomTarget;
import com.microsoft.maui.glide.MauiCustomViewTarget;
import com.microsoft.maui.glide.MauiTarget;
import com.microsoft.maui.glide.font.FontModel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformInterop {
    public static void requestLayoutIfNeeded(final View view) {
        if (!view.isInLayout()) {
            view.requestLayout();
        } else {
            view.post(new Runnable() { // from class: com.microsoft.maui.PlatformInterop$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    PlatformInterop.lambda$requestLayoutIfNeeded$0(view);
                }
            });
        }
    }

    static /* synthetic */ void lambda$requestLayoutIfNeeded$0(View view) {
        if (view.isInLayout()) {
            return;
        }
        view.requestLayout();
    }

    public static void removeFromParent(View view) {
        ViewParent parent = view.getParent();
        if (parent == null) {
            return;
        }
        ((ViewGroup) parent).removeView(view);
    }

    public static void setPivotXIfNeeded(View view, float f) {
        if (view.getPivotX() != f) {
            view.setPivotX(f);
        }
    }

    public static void setPivotYIfNeeded(View view, float f) {
        if (view.getPivotY() != f) {
            view.setPivotY(f);
        }
    }

    public static void setContentDescriptionForAutomationId(View view, String str) {
        View semanticPlatformElement = getSemanticPlatformElement(view);
        int importantForAccessibility = semanticPlatformElement.getImportantForAccessibility();
        semanticPlatformElement.setContentDescription(str);
        if (importantForAccessibility == 0) {
            semanticPlatformElement.setImportantForAccessibility(0);
        }
    }

    public static View getSemanticPlatformElement(View view) {
        return view instanceof SearchView ? view.findViewById(R.id.search_src_text) : view;
    }

    public static void set(View view, int i, int i2, int i3, int i4, boolean z, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        requestLayoutIfNeeded(view);
        view.setVisibility(i);
        view.setLayoutDirection(i2);
        view.setMinimumHeight(i3);
        view.setMinimumWidth(i4);
        view.setEnabled(z);
        view.setAlpha(f);
        view.setTranslationX(f2);
        view.setTranslationY(f3);
        view.setScaleX(f4);
        view.setScaleY(f5);
        view.setRotation(f6);
        view.setRotationX(f7);
        view.setRotationY(f8);
        setPivotXIfNeeded(view, f9);
        setPivotYIfNeeded(view, f10);
    }

    public static LinearLayout createNavigationBarOuterLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        return linearLayout;
    }

    public static FrameLayout createNavigationBarArea(Context context, LinearLayout linearLayout) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setId(View.generateViewId());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0);
        layoutParams.gravity = 119;
        layoutParams.weight = 1.0f;
        frameLayout.setLayoutParams(layoutParams);
        linearLayout.addView(frameLayout);
        return frameLayout;
    }

    public static BottomNavigationView createNavigationBar(Context context, int i, LinearLayout linearLayout, NavigationBarView.OnItemSelectedListener onItemSelectedListener) {
        BottomNavigationView bottomNavigationView = new BottomNavigationView(context, null, i);
        bottomNavigationView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        bottomNavigationView.setBackgroundColor(-1);
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);
        linearLayout.addView(bottomNavigationView);
        return bottomNavigationView;
    }

    public static MaterialToolbar createToolbar(Context context, int i, int i2) {
        MaterialToolbar materialToolbar = new MaterialToolbar(context);
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(-1, i);
        layoutParams.setScrollFlags(0);
        materialToolbar.setLayoutParams(layoutParams);
        if (i2 > 0) {
            materialToolbar.setPopupTheme(i2);
        }
        return materialToolbar;
    }

    public static CoordinatorLayout createShellCoordinatorLayout(Context context) {
        CoordinatorLayout coordinatorLayout = new CoordinatorLayout(context);
        coordinatorLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        return coordinatorLayout;
    }

    public static AppBarLayout createShellAppBar(Context context, int i, CoordinatorLayout coordinatorLayout) {
        AppBarLayout appBarLayout = new AppBarLayout(context, null, i);
        appBarLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        coordinatorLayout.addView(appBarLayout);
        return appBarLayout;
    }

    public static TabLayout createShellTabLayout(Context context, AppBarLayout appBarLayout, int i) {
        TabLayout tabLayout = new TabLayout(context);
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(-1, i);
        layoutParams.gravity = 80;
        tabLayout.setLayoutParams(layoutParams);
        tabLayout.setTabMode(0);
        appBarLayout.addView(tabLayout);
        return tabLayout;
    }

    public static ViewPager2 createShellViewPager(Context context, CoordinatorLayout coordinatorLayout, TabLayout tabLayout, TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy, FragmentStateAdapter fragmentStateAdapter, ViewPager2.OnPageChangeCallback onPageChangeCallback) {
        ViewPager2 viewPager2 = new ViewPager2(context);
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(-1, -1);
        layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        viewPager2.setOverScrollMode(2);
        viewPager2.setId(View.generateViewId());
        viewPager2.setLayoutParams(layoutParams);
        viewPager2.setAdapter(fragmentStateAdapter);
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);
        coordinatorLayout.addView(viewPager2);
        new TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy).attach();
        return viewPager2;
    }

    public static void setColorFilter(Drawable drawable, int i, int i2) {
        drawable.setColorFilter(new BlendModeColorFilter(i, getBlendMode(i2)));
    }

    static BlendMode getBlendMode(int i) {
        if (i == 0) {
            return BlendMode.SRC_IN;
        }
        if (i == 1) {
            return BlendMode.MULTIPLY;
        }
        if (i == 2) {
            return BlendMode.SRC_ATOP;
        }
        throw new RuntimeException("Invalid Mode");
    }

    static PorterDuff.Mode getPorterMode(int i) {
        if (i == 0) {
            return PorterDuff.Mode.SRC_IN;
        }
        if (i == 1) {
            return PorterDuff.Mode.MULTIPLY;
        }
        if (i == 2) {
            return PorterDuff.Mode.SRC_ATOP;
        }
        throw new RuntimeException("Invalid Mode");
    }

    private static void prepare(RequestBuilder<Drawable> requestBuilder, MauiTarget mauiTarget, boolean z, ImageLoaderCallback imageLoaderCallback) {
        RequestBuilder<Drawable> requestBuilderError = requestBuilder.error(imageLoaderCallback);
        if (!z) {
            requestBuilderError = (RequestBuilder) requestBuilderError.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true);
        }
        mauiTarget.m285lambda$load$0$commicrosoftmauiglideMauiCustomViewTarget(requestBuilderError);
    }

    public static String getGlyphHex(String str) {
        return FontModel.getGlyphHex(str);
    }

    private static void loadInto(RequestBuilder<Drawable> requestBuilder, ImageView imageView, boolean z, ImageLoaderCallback imageLoaderCallback, Object obj) {
        prepare(requestBuilder, new MauiCustomViewTarget(imageView, imageLoaderCallback, obj), z, imageLoaderCallback);
    }

    private static void load(RequestBuilder<Drawable> requestBuilder, Context context, boolean z, ImageLoaderCallback imageLoaderCallback, Object obj) {
        prepare(requestBuilder, new MauiCustomTarget(context, imageLoaderCallback, obj), z, imageLoaderCallback);
    }

    public static void loadImageFromFile(ImageView imageView, String str, ImageLoaderCallback imageLoaderCallback) {
        loadInto(Glide.with(imageView).load(str), imageView, true, imageLoaderCallback, str);
    }

    public static void loadImageFromUri(ImageView imageView, String str, boolean z, ImageLoaderCallback imageLoaderCallback) {
        Uri uri = Uri.parse(str);
        if (uri == null) {
            imageLoaderCallback.onComplete(false, null, null);
        } else {
            loadInto(Glide.with(imageView).load(uri), imageView, z, imageLoaderCallback, uri);
        }
    }

    public static void loadImageFromStream(ImageView imageView, InputStream inputStream, ImageLoaderCallback imageLoaderCallback) {
        loadInto(Glide.with(imageView).load((Object) inputStream), imageView, false, imageLoaderCallback, inputStream);
    }

    public static void loadImageFromFont(ImageView imageView, int i, String str, Typeface typeface, float f, ImageLoaderCallback imageLoaderCallback) {
        FontModel fontModel = new FontModel(i, str, f, typeface);
        loadInto(Glide.with(imageView).load((Object) fontModel).override(Integer.MIN_VALUE, Integer.MIN_VALUE), imageView, true, imageLoaderCallback, fontModel);
    }

    public static void loadImageFromFile(Context context, String str, ImageLoaderCallback imageLoaderCallback) {
        load(Glide.with(context).load(str), context, true, imageLoaderCallback, str);
    }

    public static void loadImageFromUri(Context context, String str, boolean z, ImageLoaderCallback imageLoaderCallback) {
        Uri uri = Uri.parse(str);
        if (uri == null) {
            imageLoaderCallback.onComplete(false, null, null);
        } else {
            load(Glide.with(context).load(uri), context, z, imageLoaderCallback, uri);
        }
    }

    public static void loadImageFromStream(Context context, InputStream inputStream, ImageLoaderCallback imageLoaderCallback) {
        load(Glide.with(context).load((Object) inputStream), context, false, imageLoaderCallback, inputStream);
    }

    public static void loadImageFromFont(Context context, int i, String str, Typeface typeface, float f, ImageLoaderCallback imageLoaderCallback) {
        FontModel fontModel = new FontModel(i, str, f, typeface);
        load(Glide.with(context).load((Object) fontModel).override(Integer.MIN_VALUE, Integer.MIN_VALUE), context, true, imageLoaderCallback, fontModel);
    }

    public static ColorStateList getColorStateListForToolbarStyleableAttribute(Context context, int i, int i2) {
        TintTypedArray tintTypedArrayObtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, null, R.styleable.Toolbar, i, 0);
        try {
            return tintTypedArrayObtainStyledAttributes.getColorStateList(i2);
        } finally {
            tintTypedArrayObtainStyledAttributes.recycle();
        }
    }

    public static long measureAndGetWidthAndHeight(View view, int i, int i2) {
        view.measure(i, i2);
        return (((long) view.getMeasuredWidth()) << 32) | (((long) view.getMeasuredHeight()) & 4294967295L);
    }

    public static ColorStateList getDefaultColorStateList(int i) {
        return new ColorStateList(ColorStates.DEFAULT, new int[]{i});
    }

    public static ColorStateList getEditTextColorStateList(int i, int i2) {
        return new ColorStateList(ColorStates.getEditTextState(), new int[]{i, i2});
    }

    public static ColorStateList getCheckBoxColorStateList(int i, int i2, int i3, int i4) {
        return new ColorStateList(ColorStates.getCheckBoxState(), new int[]{i, i2, i3, i4});
    }

    public static ColorStateList getSwitchColorStateList(int i, int i2, int i3) {
        return new ColorStateList(ColorStates.getSwitchState(), new int[]{i, i2, i3});
    }

    public static ColorStateList getButtonColorStateList(int i, int i2, int i3, int i4) {
        return new ColorStateList(ColorStates.getButtonState(), new int[]{i, i2, i3, i4});
    }

    public static ColorStateList createEditTextColorStateList(ColorStateList colorStateList, int i) {
        if (colorStateList == null) {
            return getEditTextColorStateList(i, i);
        }
        for (int[] iArr : ColorStates.getEditTextState()) {
            if (colorStateList.getColorForState(iArr, i) != i) {
                return getEditTextColorStateList(i, i);
            }
        }
        return null;
    }

    public static void setPaintValues(Paint paint, float f, Paint.Join join, Paint.Cap cap, float f2, PathEffect pathEffect) {
        paint.setStrokeWidth(f);
        paint.setStrokeJoin(join);
        paint.setStrokeCap(cap);
        paint.setStrokeMiter(f2);
        if (pathEffect != null) {
            paint.setPathEffect(pathEffect);
        }
    }

    public static void drawMauiDrawablePath(PaintDrawable paintDrawable, Canvas canvas, int i, int i2, Path path, Paint paint) {
        Paint paint2 = paintDrawable.getPaint();
        if (paint2 != null) {
            canvas.drawPath(path, paint2);
        }
        if (paint != null) {
            canvas.drawPath(path, paint);
        }
    }

    public static int getWindowBackgroundColor(Context context) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true) || !isColorType(typedValue)) {
            return -1;
        }
        return typedValue.data;
    }

    private static boolean isColorType(TypedValue typedValue) {
        return typedValue.isColorType();
    }

    public static void updateMaxLength(EditText editText, int i) {
        setLengthFilter(editText, i);
        if (i < 0) {
            return;
        }
        Editable text = editText.getText();
        if (text.length() > i) {
            editText.setText(text.subSequence(0, i));
        }
    }

    public static void setLengthFilter(EditText editText, int i) {
        boolean z;
        if (i == -1) {
            i = Integer.MAX_VALUE;
        }
        ArrayList arrayList = new ArrayList(Arrays.asList(editText.getFilters()));
        boolean z2 = false;
        int i2 = 0;
        while (true) {
            z = true;
            if (i2 >= arrayList.size()) {
                break;
            }
            if (((InputFilter) arrayList.get(i2)) instanceof InputFilter.LengthFilter) {
                arrayList.remove(i2);
                z2 = true;
                break;
            }
            i2++;
        }
        if (i >= 0) {
            arrayList.add(new InputFilter.LengthFilter(i));
        } else {
            z = z2;
        }
        if (z) {
            editText.setFilters((InputFilter[]) arrayList.toArray(new InputFilter[arrayList.size()]));
        }
    }

    public static Rect getCurrentWindowMetrics(Activity activity) {
        return WindowMetricsCalculator.INSTANCE.getOrCreate().computeCurrentWindowMetrics(activity).getBounds();
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.microsoft.maui.PlatformInterop$1] */
    public static Paint.FontMetrics getFontMetrics(Context context, float f) {
        DisplayMetrics displayMetrics;
        if (context == null || (displayMetrics = context.getResources().getDisplayMetrics()) == null) {
            return null;
        }
        return new Paint(f, displayMetrics) { // from class: com.microsoft.maui.PlatformInterop.1
            final /* synthetic */ float val$defaultFontSize;
            final /* synthetic */ DisplayMetrics val$metrics;

            {
                this.val$defaultFontSize = f;
                this.val$metrics = displayMetrics;
                setTextSize(TypedValue.applyDimension(2, f, displayMetrics));
            }
        }.getFontMetrics();
    }

    private static class ColorStates {
        static final int[][] DEFAULT;
        static final int[] EMPTY;
        private static int[][] buttonState;
        private static int[][] checkBoxState;
        private static int[][] editTextState;
        private static int[][] switchState;

        private ColorStates() {
        }

        static {
            int[] iArr = new int[0];
            EMPTY = iArr;
            DEFAULT = new int[][]{iArr};
        }

        static int[][] getEditTextState() {
            if (editTextState == null) {
                editTextState = new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{-16842910}};
            }
            return editTextState;
        }

        static int[][] getCheckBoxState() {
            if (checkBoxState == null) {
                checkBoxState = new int[][]{new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}, new int[]{android.R.attr.state_enabled, -16842912}, new int[]{-16842910, android.R.attr.state_checked}, new int[]{-16842910, -16842919}};
            }
            return checkBoxState;
        }

        static int[][] getSwitchState() {
            if (switchState == null) {
                switchState = new int[][]{new int[]{-16842910}, new int[]{android.R.attr.state_checked}, EMPTY};
            }
            return switchState;
        }

        static int[][] getButtonState() {
            if (buttonState == null) {
                buttonState = new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{-16842910}, new int[]{-16842912}, new int[]{android.R.attr.state_pressed}};
            }
            return buttonState;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static Animatable getAnimatable(Drawable drawable) {
        if (drawable instanceof Animatable) {
            return (Animatable) drawable;
        }
        return null;
    }

    public static boolean isImageViewCenterCrop(ImageView imageView) {
        return imageView.getScaleType() == ImageView.ScaleType.CENTER_CROP;
    }

    public static void setClipBounds(View view, int i, int i2, int i3, int i4) {
        view.setClipBounds(new Rect(i, i2, i3, i4));
    }
}
