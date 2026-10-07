package com.microsoft.maui.glide.font;

import android.graphics.Typeface;
import com.bumptech.glide.signature.ObjectKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public class FontModel {
    private final int color;
    private final String glyph;
    private final float textSize;
    private final Typeface typeface;

    public static String getGlyphHex(String str) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_16BE);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", Byte.valueOf(b)));
        }
        return sb.toString();
    }

    public FontModel(int i, String str, float f, Typeface typeface) {
        this.color = i;
        this.glyph = str;
        this.textSize = f;
        this.typeface = typeface;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FontModel fontModel = (FontModel) obj;
        return this.color == fontModel.color && Objects.equals(this.glyph, fontModel.glyph) && this.textSize == fontModel.textSize && this.typeface == fontModel.typeface;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.color), this.glyph, Float.valueOf(this.textSize), this.typeface);
    }

    public String toString() {
        return "FontModel{color=" + String.format("#%08X", Integer.valueOf(this.color)) + ", glyph='" + getGlyphHex(this.glyph) + "', textSize=" + this.textSize + ", typeface=" + this.typeface + '}';
    }

    public int getColor() {
        return this.color;
    }

    public String getGlyph() {
        return this.glyph;
    }

    public float getTextSize() {
        return this.textSize;
    }

    public Typeface getTypeface() {
        return this.typeface;
    }

    public ObjectKey getCacheKey() {
        return new ObjectKey(Integer.toString(this.color) + this.glyph + String.valueOf(this.textSize) + this.typeface.toString());
    }
}
