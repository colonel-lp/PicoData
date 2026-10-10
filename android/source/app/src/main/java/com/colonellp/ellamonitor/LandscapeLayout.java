package com.colonellp.ellamonitor;

/** Uniform control scale plus the extra logical space available for panels. */
final class LandscapeLayout {
    final float scale, width, height;
    LandscapeLayout(float pixelsWide, float pixelsHigh, float designWidth, float minimumHeight) {
        scale = Math.max(0.0001f, Math.min(Math.max(1f, pixelsWide) / designWidth,
                Math.max(1f, pixelsHigh) / minimumHeight));
        width = pixelsWide / scale;
        height = pixelsHigh / scale;
    }
    float x(float designX, float designWidth) { return designX * width / designWidth; }
    float y(float designY, float designHeight) { return designY * height / designHeight; }
}
