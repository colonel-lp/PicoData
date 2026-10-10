package com.colonellp.ellamonitor;

final class Palette {
    final String fontFamily;
    final int background, panel, border, text, muted, accent, positive, negative;
    Palette(ThemeConfig t) {
        fontFamily=t.fontFamily; background=t.background;panel=t.panel;border=t.border;text=t.text;
        muted=t.buttonTextOff;accent=t.controls;positive=t.frontEq;negative=t.rearEq;
    }
    Palette(boolean light) {
        fontFamily="sans-serif-medium";
        background = light ? 0xffedf3f7 : 0xff0c1520; panel = light ? 0xfffbfdff : 0xff142232;
        border = light ? 0xffa5bacb : 0xff355169; text = light ? 0xff152c40 : 0xffe9f3fc;
        muted = light ? 0xff52687b : 0xff91a9bc; accent = light ? 0xff007aa4 : 0xff58c6f2;
        positive = light ? 0xff13784b : 0xff80e2ae; negative = light ? 0xffac4820 : 0xffffb06d;
    }
}
