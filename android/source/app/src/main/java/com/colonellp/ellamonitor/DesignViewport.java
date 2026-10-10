package com.colonellp.ellamonitor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** EQ & DSP logical coordinates, with Android view transforms for drawing, touch and accessibility. */
final class DesignViewport extends ViewGroup {
    private final boolean fullscreen;
    float scale=1;
    private boolean portrait;
    DesignViewport(Context context,View child,boolean fullscreen){super(context);this.fullscreen=fullscreen;addView(child);setClipChildren(false);}
    @Override protected void onMeasure(int ws,int hs){
        int w=MeasureSpec.getSize(ws),h=MeasureSpec.getSize(hs);int availableW=Math.max(1,w-getPaddingLeft()-getPaddingRight()),availableH=Math.max(1,h-getPaddingTop()-getPaddingBottom());
        // Some recent Android large-screen modes ignore requested orientation; retain the landscape canvas there too.
        portrait=availableW<availableH;
        float landscapeW=portrait?availableH:availableW,landscapeH=portrait?availableW:availableH;scale=landscapeW/1024f;LandscapeLayout layout=new LandscapeLayout(landscapeW,landscapeH,1024,landscapeH/scale);
        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(Math.round(layout.width),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(Math.round(layout.height),MeasureSpec.EXACTLY));setMeasuredDimension(w,h);
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        View child=getChildAt(0);child.layout(getPaddingLeft(),getPaddingTop(),getPaddingLeft()+child.getMeasuredWidth(),getPaddingTop()+child.getMeasuredHeight());
        child.setPivotX(0);child.setPivotY(0);child.setScaleX(scale);child.setScaleY(scale);child.setRotation(portrait?90:0);child.setTranslationX(portrait?getWidth()-getPaddingLeft()-getPaddingRight():0);
    }
}
