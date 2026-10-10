package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

/** Companion controls: fixed 54px bar and 40px buttons, with 6px top padding. */
final class BottomBar extends ViewGroup {
    private final ThemeConfig theme;
    private final boolean fullscreen;
    private boolean hasTheme;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    BottomBar(Context context,ThemeConfig theme,boolean fullscreen){super(context);this.theme=theme;this.fullscreen=fullscreen;setWillNotDraw(false);}
    void addControl(View view,int position){if(position==7)hasTheme=true;view.setTag(position);addView(view);}
    @Override protected void onMeasure(int ws,int hs){setMeasuredDimension(MeasureSpec.getSize(ws),MeasureSpec.getSize(hs));int height=40;for(int i=0;i<getChildCount();i++){View v=getChildAt(i);int position=(Integer)v.getTag();float sx=getMeasuredWidth()/1024f;int width=position==0?Math.round(200*sx):position==7?Math.round(200*sx):position<3?Math.round(100*sx):Math.round(48*sx);v.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));}}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){float sx=getWidth()/1024f;int top=6;int height=40;for(int i=0;i<getChildCount();i++){View v=getChildAt(i);int pos=(Integer)v.getTag();int left=pos==0?14:pos==1?426:pos==2?532:pos==7?220:800+(pos-3)*54;int right=pos==0?214:pos==7?420:pos<3?left+100:left+48;if(pos<3||pos==7){left=Math.round(left*sx);right=Math.round(right*sx);}else{left=Math.round((800+(pos-3)*54)*sx);right=Math.round((848+(pos-3)*54)*sx);}v.layout(left,top,right,top+height);}}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float sx=getWidth()/1024f;draw(c,new RectF(8*sx,0,638*sx,getHeight()-2));draw(c,new RectF(794*sx,0,1016*sx,getHeight()-2));}
    private void draw(Canvas c,RectF r){paint.setStyle(Paint.Style.FILL);paint.setColor(theme.panel);c.drawRoundRect(r,6,6,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);paint.setColor(theme.border);r.inset(.6f,.6f);c.drawRoundRect(r,6,6,paint);}
}

