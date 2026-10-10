package com.colonellp.ellamonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.Button;

/** EQ-style modification dot, independent of the dropdown's text and border colours. */
final class ChangedButton extends Button {
    private final ThemeConfig theme;
    private final String label;
    private boolean changed;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    ChangedButton(Context context,ThemeConfig theme,String label,boolean changed,Runnable action){
        super(context);this.theme=theme;this.label=label;this.changed=changed;
        setText(label+"  ▾");setAllCaps(false);setTextSize(TypedValue.COMPLEX_UNIT_PX,11.5f);setTextColor(theme.buttonTextOn);setTypeface(Typeface.create(theme.fontFamily,Typeface.NORMAL));
        setMinWidth(0);setMinimumWidth(0);setMinHeight(0);setMinimumHeight(0);setPadding(6,0,6,0);
        GradientDrawable background=new GradientDrawable();background.setColor(theme.buttonBackground);background.setCornerRadius(5);background.setStroke(1,theme.controls);setBackground(background);
        setOnClickListener(v->action.run());changed(changed);
    }
    void changed(boolean value){changed=value;setContentDescription(label+(changed?", modified":""));invalidate();}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(changed){paint.setColor(theme.changedIndicator);canvas.drawCircle(getWidth()-18,getHeight()/2f,8,paint);}}
}
