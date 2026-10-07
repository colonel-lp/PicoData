package com.microsoft.maui;

import android.content.Context;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;

/* JADX INFO: loaded from: classes2.dex */
public class PlatformAppCompatTextView extends AppCompatTextView {
    private boolean isFormatted;

    protected void onLayoutFormatted(boolean z, int i, int i2, int i3, int i4) {
    }

    public PlatformAppCompatTextView(Context context) {
        super(context);
    }

    @Override // android.widget.TextView
    public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        this.isFormatted = !(charSequence instanceof String);
        super.setText(charSequence, bufferType);
    }

    @Override // androidx.appcompat.widget.AppCompatTextView, android.widget.TextView, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.isFormatted) {
            onLayoutFormatted(z, i, i2, i3, i4);
        }
    }
}
