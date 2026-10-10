package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

/** EQ & DSP's 52px fullscreen / 38px windowed groups and 48px icon controls. */
final class BottomBar extends ViewGroup {
    private final ThemeConfig theme;
    private final boolean fullscreen;
    private boolean hasTheme;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    BottomBar(Context context,ThemeConfig theme,boolean fullscreen){super(context);this.theme=theme;this.fullscreen=fullscreen;setWillNotDraw(false);}
    void addControl(View view,int position){if(position==7)hasTheme=true;view.setTag(position);addView(view);}
    @Override protected void onMeasure(int ws,int hs){setMeasuredDimension(MeasureSpec.getSize(ws),MeasureSpec.getSize(hs));int height=fullscreen?40:26;for(int i=0;i<getChildCount();i++){View v=getChildAt(i);int position=(Integer)v.getTag();float sx=getMeasuredWidth()/1024f;int width=position==0?Math.round(200*sx):position==7?Math.round(200*sx):position<3?Math.round(100*sx):Math.round(48*sx);v.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));}}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){float sx=getWidth()/1024f;int top=6;int height=fullscreen?40:26;for(int i=0;i<getChildCount();i++){View v=getChildAt(i);int pos=(Integer)v.getTag();int left=pos==0?14:pos==1?220:pos==2?326:pos==7?432:800+(pos-3)*54;int right=pos==0?214:pos==7?632:pos<3?left+100:left+48;if(pos<3||pos==7){left=Math.round(left*sx);right=Math.round(right*sx);}else{left=Math.round((800+(pos-3)*54)*sx);right=Math.round((848+(pos-3)*54)*sx);}v.layout(left,top,right,top+height);}}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float sx=getWidth()/1024f;draw(c,new RectF(8*sx,0,(hasTheme?638:432)*sx,getHeight()-(fullscreen?2:6)));draw(c,new RectF(794*sx,0,1016*sx,getHeight()-(fullscreen?2:6)));}
    private void draw(Canvas c,RectF r){paint.setStyle(Paint.Style.FILL);paint.setColor(theme.panel);c.drawRoundRect(r,6,6,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);paint.setColor(theme.border);r.inset(.6f,.6f);c.drawRoundRect(r,6,6,paint);}
}

