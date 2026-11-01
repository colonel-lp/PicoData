package com.colonellp.ellamonitor;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/** Measure full content first; fill the viewport only when it is tall enough. */
final class SettingsColumns extends LinearLayout {
    View right;
    SettingsColumns(Context c){super(c);setOrientation(HORIZONTAL);}
    @Override protected void onMeasure(int width,int height){
        if(right==null){super.onMeasure(width,height);return;}
        LayoutParams p=(LayoutParams)right.getLayoutParams();p.height=LayoutParams.WRAP_CONTENT;
        super.onMeasure(width,MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
        int natural=getMeasuredHeight(),size=MeasureSpec.getSize(height);
        int wanted=MeasureSpec.getMode(height)==MeasureSpec.EXACTLY?Math.max(natural,size):natural;
        p.height=Math.max(0,wanted-getPaddingTop()-getPaddingBottom());
        super.onMeasure(width,MeasureSpec.makeMeasureSpec(wanted,MeasureSpec.EXACTLY));
    }
}
