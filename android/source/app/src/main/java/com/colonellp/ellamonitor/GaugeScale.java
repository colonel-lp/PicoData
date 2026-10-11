package com.colonellp.ellamonitor;

/** Dashboard ranges; clamp the drawing, never the measured value. */
final class GaugeScale {
    enum Kind { DEFAULT, LOAD, PV, BATTERY, SOC }
    static Kind kind(String slot, String title) {
        if (!slot.equals("gauges")) return Kind.DEFAULT;
        if (title.startsWith("Load")) return Kind.LOAD;
        if (title.startsWith("PV")) return Kind.PV;
        if (title.startsWith("Battery")) return Kind.BATTERY;
        if (title.startsWith("SOC")) return Kind.SOC;
        return Kind.DEFAULT;
    }
    static double minimum(Kind kind, String unit, Double value) {
        if (kind == Kind.BATTERY) return -10;
        if (kind != Kind.DEFAULT || unit.equals("%")) return 0;
        if (unit.equals("hPa")) return 950;
        return -Math.max(20, Math.ceil(Math.abs(value == null ? 0 : value) / 10) * 10);
    }
    static double maximum(Kind kind, String unit, Double value) {
        if (kind == Kind.BATTERY || kind == Kind.LOAD) return 10;
        if (kind == Kind.PV) return 20;
        if (kind == Kind.SOC || unit.equals("%")) return 100;
        return unit.equals("hPa") ? 1050 : -minimum(kind, unit, value);
    }
    static float sweep(Kind kind, String unit, double value) {
        if (kind == Kind.BATTERY) return (float)Math.max(-125, Math.min(125, value * 12.5));
        double min = minimum(kind, unit, value), max = maximum(kind, unit, value);
        float fraction = (float)Math.max(0, Math.min(1, (value - min) / (max - min)));
        return fraction * (kind == Kind.SOC ? -360 : 250);
    }
    static float socBandSweep(double value, int band) {
        double[] ends = {0, 25, 50, 100};
        return (float)(-3.6 * Math.max(0, Math.min(ends[band + 1] - ends[band], value - ends[band])));
    }
}
