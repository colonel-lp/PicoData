package com.colonellp.ellamonitor;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

final class ThemeConfig {
    static final String CHANGED_INDICATOR = "changedIndicator";
    static final String FRONT_EQ = "frontEq";
    static final String REAR_EQ = "rearEq";
    static final String CONTROLS = "controls";
    static final String BACKGROUND = "background";
    static final String PANEL = "panel";
    static final String BORDER = "border";
    static final String GRID = "grid";
    static final String VERTICAL_GRID = "verticalGrid";
    static final String GRAPH_BACKGROUND = "graphBackground";
    static final String TEXT = "text";
    static final String BUTTON_TEXT = "buttonText"; // legacy import key
    static final String BUTTON_TEXT_ON = "buttonTextOn";
    static final String BUTTON_TEXT_OFF = "buttonTextOff";
    static final String FONT_FAMILY = "fontFamily";
    // Legacy single-colour key. New themes use immutable spectrum-style IDs.
    static final String SPECTRUM = "spectrum";
    static final String SPECTRUM_COLOURS = "spectrumColours";
    static final String LINE_1 = "line_1";
    static final String BAR_1 = "bar_1";
    static final String BAR_2 = "bar_2"; // fixed colour; never serialized as a theme colour
    static final String AURORA_1 = "aurora_1";
    static final String AURORA_2 = "aurora_2";
    static final String AURORA_3 = "aurora_3";
    static final String AURORA_4 = "aurora_4";
    static final String AURORA_3_HUE_RANGE = "aurora3HueRangeDeg";
    static final String LINE_GLOW_STRENGTH = "lineGlowStrength";
    static final String LINE_GLOW_STRENGTH_100 = "lineGlowStrength100";
    static final String AURORA_GLOW_STRENGTH = "auroraGlowStrength";
    static final String AURORA_LINE_BRIGHTNESS = "auroraLineBrightness";
    static final String AURORA_LINE_BRIGHTNESS_100 = "auroraLineBrightness100";
    static final String SPECTRUM_SMOOTHING = "spectrumSmoothing";
    // Display order is alphabetical. Persistence uses the stable IDs rather than
    // these array positions, so reordering the menu does not change saved styles.
    static final String[] THEMEABLE_SPECTRUM_IDS = {AURORA_4, BAR_1, AURORA_2, LINE_1};
    static final String[] THEMEABLE_SPECTRUM_LABELS = {"Aurora", "Bar • simple", "Line • glow", "Line • simple"};
    static final String[] ALL_SPECTRUM_IDS = {AURORA_4, BAR_2, BAR_1, AURORA_2, LINE_1};
    static final String[] ALL_SPECTRUM_LABELS = {"Aurora", "Bar • coloured", "Bar • simple", "Line • glow", "Line • simple"};
    static final String SLIDERS = "sliders";
    static final String BUTTON_BACKGROUND = "buttonBackground";
    static final String BUTTON_BORDER = "buttonBorder";
    static final String OFF_BUTTON_BORDER = "offButtonBorder";

    String name = "System default";
    int frontEq = -16729857;
    int rearEq = -65469;
    int controls = -15826385;
    int background = -16777216;
    int panel = -16777216;
    int border = -15267030;
    int grid = -16117999;
    int verticalGrid = -16118514;
    int graphBackground = panel;
    int text = -3616043;
    int buttonTextOn = -4404270;
    int buttonTextOff = -4404270;
    String fontFamily = "sans-serif-medium";
    int line1Spectrum = -16718424;
    int bar1Spectrum = -16718424;
    int aurora1Spectrum = -16718424;
    int aurora2Spectrum = -16718424;
    int aurora3Spectrum = -16718424;
    int aurora4Spectrum = -1436403;
    int aurora3HueRangeDeg = 180;
    int lineGlowStrength = 29;
    int auroraGlowStrength = 50;
    int auroraLineBrightness = 64;
    int lineSimpleSmoothing = 80;
    int lineGlowSmoothing = 80;
    int auroraSmoothing = 80;
    int sliders = -12099221;
    int buttonBackground = -16380912;
    int buttonBorder = controls;
    int offButtonBorder = -14807240;
    int changedIndicator = Color.rgb(255, 194, 87);

    static final String GAUGE_PANEL_1 = "gaugePanel1";
    int gaugePanel1 = buttonBackground;
    static final String GAUGE_OUTLINE_1 = "gaugeOutline1";
    int gaugeOutline1 = controls;
    static final String GAUGE_PANEL_2 = "gaugePanel2";
    int gaugePanel2 = buttonBackground;
    static final String GAUGE_OUTLINE_2 = "gaugeOutline2";
    int gaugeOutline2 = controls;
    static final String GAUGE_HIGHLIGHT = "gaugeHighlight"; // legacy theme import key
    static final String HIGHLIGHT_1 = "highlight1";
    static final String HIGHLIGHT_2 = "highlight2";
    static final String INDICATOR_BACKGROUND = "indicatorBackground";
    static final String VOLTAGES_BACKGROUND = "voltagesBackground";
    int gaugeHighlight = blend(buttonBackground, controls, .2f);
    int highlight2 = blend(buttonBackground, rearEq, .2f);
    int indicatorBackground = panel;
    int voltagesBackground = panel;
    static final String TITLE_TEXT = "titleText";
    int titleText = text;
    static final String TITLE_BACKGROUND = "titleBackground";
    int titleBackground = panel;
    static final String TITLE_OUTLINE = "titleOutline";
    int titleOutline = border;

    static ThemeConfig defaults() {
        return new ThemeConfig();
    }

    // Recognise an unchanged active Default saved by older builds. A customised
    // Default should continue to load exactly as the user left it.
    static ThemeConfig previousDefaults() {
        ThemeConfig t = new ThemeConfig();
        t.frontEq = Color.rgb(0, 184, 255);
        t.rearEq = Color.rgb(166, 105, 255);
        t.controls = Color.rgb(0, 229, 168);
        t.background = Color.rgb(4, 9, 13);
        t.panel = Color.rgb(14, 29, 39);
        t.border = Color.rgb(108, 139, 151);
        t.grid = Color.rgb(60, 90, 103);
        t.verticalGrid = Color.rgb(75, 101, 112);
        t.graphBackground = t.panel;
        t.text = Color.rgb(229, 239, 243);
        t.buttonTextOn = Color.rgb(229, 239, 243);
        t.buttonTextOff = Color.rgb(205, 222, 229);
        t.fontFamily = "sans-serif";
        t.line1Spectrum = t.bar1Spectrum = t.aurora1Spectrum = t.aurora2Spectrum
                = t.aurora3Spectrum = t.aurora4Spectrum = Color.rgb(0, 229, 168);
        t.aurora3HueRangeDeg = 90;
        t.lineGlowStrength = 30;
        t.auroraGlowStrength = 50;
        t.auroraLineBrightness = 100;
        t.lineSimpleSmoothing = 80;
        t.lineGlowSmoothing = 80;
        t.auroraSmoothing = 80;
        t.sliders = Color.rgb(75, 101, 112);
        t.buttonBackground = Color.rgb(18, 36, 47);
        t.offButtonBorder = blend(t.border, t.background, 0.18f);
        t.buttonBorder = t.controls;
        return t;
    }

    ThemeConfig copy() {
        ThemeConfig t = new ThemeConfig();
        t.name = name;
        t.gaugePanel1 = gaugePanel1;
        t.gaugeOutline1 = gaugeOutline1;
        t.gaugePanel2 = gaugePanel2;
        t.gaugeOutline2 = gaugeOutline2;
        t.gaugeHighlight = gaugeHighlight;
        t.highlight2 = highlight2;
        t.indicatorBackground = indicatorBackground;
        t.voltagesBackground = voltagesBackground;
        t.titleText = titleText;
        t.titleBackground = titleBackground;
        t.titleOutline = titleOutline;

        t.frontEq = frontEq;
        t.rearEq = rearEq;
        t.controls = controls;
        t.background = background;
        t.panel = panel;
        t.border = border;
        t.grid = grid;
        t.verticalGrid = verticalGrid;
        t.graphBackground = graphBackground;
        t.text = text;
        t.buttonTextOn = buttonTextOn;
        t.buttonTextOff = buttonTextOff;
        t.fontFamily = fontFamily;
        t.line1Spectrum = line1Spectrum;
        t.bar1Spectrum = bar1Spectrum;
        t.aurora1Spectrum = aurora1Spectrum;
        t.aurora2Spectrum = aurora2Spectrum;
        t.aurora3Spectrum = aurora3Spectrum;
        t.aurora4Spectrum = aurora4Spectrum;
        t.aurora3HueRangeDeg = aurora3HueRangeDeg;
        t.lineGlowStrength = lineGlowStrength;
        t.auroraGlowStrength = auroraGlowStrength;
        t.auroraLineBrightness = auroraLineBrightness;
        t.lineSimpleSmoothing = lineSimpleSmoothing;
        t.lineGlowSmoothing = lineGlowSmoothing;
        t.auroraSmoothing = auroraSmoothing;
        t.sliders = sliders;
        t.buttonBackground = buttonBackground;
        t.buttonBorder = buttonBorder;
        t.offButtonBorder = offButtonBorder;
        t.changedIndicator = changedIndicator;
        return t;
    }

    int panel2() {
        return blend(panel, text, 0.06f);
    }

    int track() {
        return sliders;
    }

    int scaleText() {
        return blend(text, graphBackground, 0.22f);
    }

    int muted() {
        return blend(text, background, 0.48f);
    }

    int offStroke() {
        return offButtonBorder;
    }

    int get(String field) {
        if (BUTTON_BORDER.equals(field)) return buttonBorder;
        if (GAUGE_PANEL_1.equals(field)) return gaugePanel1;
        if (GAUGE_OUTLINE_1.equals(field)) return gaugeOutline1;
        if (GAUGE_PANEL_2.equals(field)) return gaugePanel2;
        if (GAUGE_OUTLINE_2.equals(field)) return gaugeOutline2;
        if (GAUGE_HIGHLIGHT.equals(field) || HIGHLIGHT_1.equals(field)) return gaugeHighlight;
        if (HIGHLIGHT_2.equals(field)) return highlight2;
        if (INDICATOR_BACKGROUND.equals(field)) return indicatorBackground;
        if (VOLTAGES_BACKGROUND.equals(field)) return voltagesBackground;
        if (TITLE_TEXT.equals(field)) return titleText;
        if (TITLE_BACKGROUND.equals(field)) return titleBackground;
        if (TITLE_OUTLINE.equals(field)) return titleOutline;

        if (CHANGED_INDICATOR.equals(field)) return changedIndicator;
        if (FRONT_EQ.equals(field)) return frontEq;
        if (REAR_EQ.equals(field)) return rearEq;
        if (CONTROLS.equals(field)) return controls;
        if (BACKGROUND.equals(field)) return background;
        if (PANEL.equals(field)) return panel;
        if (BORDER.equals(field)) return border;
        if (GRID.equals(field)) return grid;
        if (VERTICAL_GRID.equals(field)) return verticalGrid;
        if (GRAPH_BACKGROUND.equals(field)) return graphBackground;
        if (TEXT.equals(field)) return text;
        if (BUTTON_TEXT_ON.equals(field)) return buttonTextOn;
        if (BUTTON_TEXT_OFF.equals(field) || BUTTON_TEXT.equals(field)) return buttonTextOff;
        if (SPECTRUM.equals(field) || LINE_1.equals(field)) return line1Spectrum;
        if (BAR_1.equals(field)) return bar1Spectrum;
        if (AURORA_1.equals(field)) return aurora1Spectrum;
        if (AURORA_2.equals(field)) return aurora2Spectrum;
        if (AURORA_3.equals(field)) return aurora3Spectrum;
        if (AURORA_4.equals(field)) return aurora4Spectrum;
        if (SLIDERS.equals(field)) return sliders;
        if (BUTTON_BACKGROUND.equals(field)) return buttonBackground;
        if (OFF_BUTTON_BORDER.equals(field)) return offButtonBorder;
        return controls;
    }

    void set(String field, int color) {
        if (BUTTON_BORDER.equals(field)) { buttonBorder = color; return; }
        if (GAUGE_PANEL_1.equals(field)) { gaugePanel1 = color; return; }
        if (GAUGE_OUTLINE_1.equals(field)) { gaugeOutline1 = color; return; }
        if (GAUGE_PANEL_2.equals(field)) { gaugePanel2 = color; return; }
        if (GAUGE_OUTLINE_2.equals(field)) { gaugeOutline2 = color; return; }
        if (GAUGE_HIGHLIGHT.equals(field) || HIGHLIGHT_1.equals(field)) { gaugeHighlight = color; return; }
        if (HIGHLIGHT_2.equals(field)) { highlight2 = color; return; }
        if (INDICATOR_BACKGROUND.equals(field)) { indicatorBackground = color; return; }
        if (VOLTAGES_BACKGROUND.equals(field)) { voltagesBackground = color; return; }
        if (TITLE_TEXT.equals(field)) { titleText = color; return; }
        if (TITLE_BACKGROUND.equals(field)) { titleBackground = color; return; }
        if (TITLE_OUTLINE.equals(field)) { titleOutline = color; return; }

        if (CHANGED_INDICATOR.equals(field)) changedIndicator = color;
        else if (FRONT_EQ.equals(field)) frontEq = color;
        else if (REAR_EQ.equals(field)) rearEq = color;
        else if (CONTROLS.equals(field)) controls = color;
        else if (BACKGROUND.equals(field)) background = color;
        else if (PANEL.equals(field)) panel = color;
        else if (BORDER.equals(field)) border = color;
        else if (GRID.equals(field)) grid = color;
        else if (VERTICAL_GRID.equals(field)) verticalGrid = color;
        else if (GRAPH_BACKGROUND.equals(field)) graphBackground = color;
        else if (TEXT.equals(field)) text = color;
        else if (BUTTON_TEXT_ON.equals(field)) buttonTextOn = color;
        else if (BUTTON_TEXT_OFF.equals(field) || BUTTON_TEXT.equals(field)) buttonTextOff = color;
        else if (SPECTRUM.equals(field) || LINE_1.equals(field)) line1Spectrum = color;
        else if (BAR_1.equals(field)) bar1Spectrum = color;
        else if (AURORA_1.equals(field)) aurora1Spectrum = color;
        else if (AURORA_2.equals(field)) aurora2Spectrum = color;
        else if (AURORA_3.equals(field)) aurora3Spectrum = color;
        else if (AURORA_4.equals(field)) aurora4Spectrum = color;
        else if (SLIDERS.equals(field)) sliders = color;
        else if (BUTTON_BACKGROUND.equals(field)) buttonBackground = color;
        else if (OFF_BUTTON_BORDER.equals(field)) offButtonBorder = color;
    }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("format", 17);
        o.put(TITLE_TEXT, titleText);
        o.put(GAUGE_PANEL_1, gaugePanel1);
        o.put(GAUGE_OUTLINE_1, gaugeOutline1);
        o.put(GAUGE_PANEL_2, gaugePanel2);
        o.put(GAUGE_OUTLINE_2, gaugeOutline2);
        o.put(HIGHLIGHT_1, gaugeHighlight);
        o.put(HIGHLIGHT_2, highlight2);
        o.put(INDICATOR_BACKGROUND, indicatorBackground);
        o.put(VOLTAGES_BACKGROUND, voltagesBackground);
        o.put(TITLE_BACKGROUND, titleBackground);
        o.put(TITLE_OUTLINE, titleOutline);

        o.put("name", name);
        o.put(FRONT_EQ, frontEq);
        o.put(REAR_EQ, rearEq);
        o.put(CONTROLS, controls);
        o.put(BACKGROUND, background);
        o.put(PANEL, panel);
        o.put(BORDER, border);
        o.put(GRID, grid);
        o.put(VERTICAL_GRID, verticalGrid);
        o.put(GRAPH_BACKGROUND, graphBackground);
        o.put(TEXT, text);
        o.put(BUTTON_TEXT_ON, buttonTextOn);
        o.put(BUTTON_TEXT_OFF, buttonTextOff);
        JSONObject spectrumColours = new JSONObject();
        spectrumColours.put(LINE_1, line1Spectrum);
        spectrumColours.put(BAR_1, bar1Spectrum);
        spectrumColours.put(AURORA_1, aurora1Spectrum);
        spectrumColours.put(AURORA_2, aurora2Spectrum);
        spectrumColours.put(AURORA_3, aurora3Spectrum);
        spectrumColours.put(AURORA_4, aurora4Spectrum);
        o.put(SPECTRUM_COLOURS, spectrumColours);
        o.put(SLIDERS, sliders);
        o.put(BUTTON_BACKGROUND, buttonBackground);
        o.put(BUTTON_BORDER, buttonBorder);
        o.put(OFF_BUTTON_BORDER, offButtonBorder);
        o.put(CHANGED_INDICATOR, changedIndicator);
        return o;
    }

    static ThemeConfig fromJson(JSONObject o) throws JSONException {
        ThemeConfig t = defaults();
        t.name = o.optString("name", "Theme");
        t.frontEq = o.optInt(FRONT_EQ, t.frontEq);
        t.rearEq = o.optInt(REAR_EQ, t.rearEq);
        t.controls = o.optInt(CONTROLS, t.controls);
        t.buttonBorder = o.optInt(BUTTON_BORDER, t.controls);
        t.background = o.optInt(BACKGROUND, t.background);
        t.panel = o.optInt(PANEL, t.panel);
        // Older themes used the panel colour for graph and slider surfaces.
        t.graphBackground = o.optInt(GRAPH_BACKGROUND, t.panel);
        t.border = o.optInt(BORDER, t.border);
        t.grid = o.optInt(GRID, t.grid);
        t.verticalGrid = o.optInt(VERTICAL_GRID, t.verticalGrid);
        t.text = o.optInt(TEXT, t.text);
        int legacyButton = o.optInt(BUTTON_TEXT, t.buttonTextOff);
        t.buttonTextOn = o.optInt(BUTTON_TEXT_ON, legacyButton);
        t.buttonTextOff = o.optInt(BUTTON_TEXT_OFF, legacyButton);
        t.fontFamily = o.optString(FONT_FAMILY, t.fontFamily);
        int legacySpectrum = o.optInt(SPECTRUM, t.line1Spectrum);
        JSONObject spectrumColours = o.optJSONObject(SPECTRUM_COLOURS);
        t.line1Spectrum = spectrumColours == null ? legacySpectrum : spectrumColours.optInt(LINE_1, legacySpectrum);
        t.bar1Spectrum = spectrumColours == null ? legacySpectrum : spectrumColours.optInt(BAR_1, legacySpectrum);
        t.aurora1Spectrum = spectrumColours == null ? legacySpectrum : spectrumColours.optInt(AURORA_1, legacySpectrum);
        t.aurora2Spectrum = spectrumColours == null ? legacySpectrum : spectrumColours.optInt(AURORA_2, legacySpectrum);
        t.aurora3Spectrum = spectrumColours == null ? legacySpectrum : spectrumColours.optInt(AURORA_3, legacySpectrum);
        t.aurora4Spectrum = spectrumColours == null ? t.aurora3Spectrum : spectrumColours.optInt(AURORA_4, t.aurora3Spectrum);
        t.aurora3HueRangeDeg = Math.max(0, Math.min(180, o.optInt(AURORA_3_HUE_RANGE, 90)));
        if (o.has(LINE_GLOW_STRENGTH_100)) {
            t.lineGlowStrength = Math.max(0,
                    Math.min(100, o.optInt(LINE_GLOW_STRENGTH_100, 30)));
        } else {
            t.lineGlowStrength = Math.round(Math.max(0,
                    Math.min(20, o.optInt(LINE_GLOW_STRENGTH, 12))) * 2.5f);
        }
        t.auroraGlowStrength = Math.max(0,
                Math.min(100, o.optInt(AURORA_GLOW_STRENGTH, 50)));
        if (o.has(AURORA_LINE_BRIGHTNESS_100)) {
            t.auroraLineBrightness = Math.max(0,
                    Math.min(100, o.optInt(AURORA_LINE_BRIGHTNESS_100, 100)));
        } else {
            // Themes saved by v1.06 and earlier used a 0..20 scale.
            t.auroraLineBrightness = Math.max(0,
                    Math.min(20, o.optInt(AURORA_LINE_BRIGHTNESS, 20))) * 5;
        }
        JSONObject smoothing = o.optJSONObject(SPECTRUM_SMOOTHING);
        if (smoothing != null) {
            t.lineSimpleSmoothing = clamp100(smoothing.optInt(LINE_1, o.optInt("spectrumSmoothingScale",1)<2?60:80));
            t.lineGlowSmoothing = clamp100(smoothing.optInt(AURORA_2, o.optInt("spectrumSmoothingScale",1)<2?60:80));
            t.auroraSmoothing = clamp100(smoothing.optInt(AURORA_4, o.optInt("spectrumSmoothingScale",1)<2?60:80));
            if(o.optInt("spectrumSmoothingScale",1)<2) {
                t.lineSimpleSmoothing=clamp100(t.lineSimpleSmoothing);
                t.lineGlowSmoothing=clamp100(t.lineGlowSmoothing);
                t.auroraSmoothing=clamp100(t.auroraSmoothing);
            }
        }
        t.sliders = o.optInt(SLIDERS, t.sliders);
        t.buttonBackground = o.optInt(BUTTON_BACKGROUND, t.buttonBackground);
        t.offButtonBorder = o.optInt(OFF_BUTTON_BORDER, blend(t.border, t.background, 0.18f));
        t.changedIndicator = o.optInt(CHANGED_INDICATOR, t.changedIndicator);
        t.gaugePanel1 = o.optInt(GAUGE_PANEL_1, t.buttonBackground);
        t.gaugeOutline1 = o.optInt(GAUGE_OUTLINE_1, t.controls);
        t.gaugePanel2 = o.optInt(GAUGE_PANEL_2, t.buttonBackground);
        t.gaugeOutline2 = o.optInt(GAUGE_OUTLINE_2, t.controls);
        t.gaugeHighlight = o.optInt(HIGHLIGHT_1, o.optInt(GAUGE_HIGHLIGHT, blend(t.buttonBackground, t.controls, .2f)));
        t.highlight2 = o.optInt(HIGHLIGHT_2, blend(t.buttonBackground, t.rearEq, .2f));
        t.indicatorBackground = o.optInt(INDICATOR_BACKGROUND, t.panel);
        t.voltagesBackground = o.optInt(VOLTAGES_BACKGROUND, t.panel);
        t.titleText = o.optInt(TITLE_TEXT, t.text);
        t.titleBackground = o.optInt(TITLE_BACKGROUND, t.panel);
        t.titleOutline = o.optInt(TITLE_OUTLINE, t.border);
        if (t.name.equals("Default")) t.name = "System default";
        return t;
    }

    boolean sameColours(ThemeConfig other) {
        return other != null
                && gaugePanel1 == other.gaugePanel1
                && gaugeOutline1 == other.gaugeOutline1
                && gaugePanel2 == other.gaugePanel2
                && gaugeOutline2 == other.gaugeOutline2
                && gaugeHighlight == other.gaugeHighlight
                && highlight2 == other.highlight2
                && indicatorBackground == other.indicatorBackground
                && voltagesBackground == other.voltagesBackground
                && titleText == other.titleText
                && titleBackground == other.titleBackground
                && titleOutline == other.titleOutline
                && frontEq == other.frontEq
                && rearEq == other.rearEq
                && controls == other.controls
                && background == other.background
                && panel == other.panel
                && border == other.border
                && grid == other.grid
                && verticalGrid == other.verticalGrid
                && graphBackground == other.graphBackground
                && text == other.text
                && buttonTextOn == other.buttonTextOn
                && buttonTextOff == other.buttonTextOff
                && line1Spectrum == other.line1Spectrum
                && bar1Spectrum == other.bar1Spectrum
                && aurora1Spectrum == other.aurora1Spectrum
                && aurora2Spectrum == other.aurora2Spectrum
                && aurora3Spectrum == other.aurora3Spectrum
                && aurora4Spectrum == other.aurora4Spectrum
                && sliders == other.sliders
                && buttonBackground == other.buttonBackground
                && buttonBorder == other.buttonBorder
                && offButtonBorder == other.offButtonBorder
                && changedIndicator == other.changedIndicator;
    }

    int getSpectrumColour(String styleId) {
        return get(styleId == null ? LINE_1 : styleId);
    }

    void setSpectrumColour(String styleId, int color) {
        if (!BAR_2.equals(styleId)) set(styleId, color);
    }

    int getSpectrumSmoothing(String styleId) {
        if (AURORA_2.equals(styleId)) return lineGlowSmoothing;
        if (AURORA_4.equals(styleId)) return auroraSmoothing;
        return lineSimpleSmoothing;
    }

    void setSpectrumSmoothing(String styleId, int value) {
        value = clamp100(value);
        if (AURORA_2.equals(styleId)) lineGlowSmoothing = value;
        else if (AURORA_4.equals(styleId)) auroraSmoothing = value;
        else if (LINE_1.equals(styleId)) lineSimpleSmoothing = value;
    }

    private static int clamp100(int value) {
        return Math.max(0, Math.min(100, value));
    }

    static int spectrumIndexForId(String id) {
        if (AURORA_1.equals(id)) id = AURORA_2;
        else if (AURORA_3.equals(id)) id = AURORA_4;
        for (int i = 0; i < ALL_SPECTRUM_IDS.length; i++) if (ALL_SPECTRUM_IDS[i].equals(id)) return i;
        for (int i = 0; i < ALL_SPECTRUM_IDS.length; i++) if (LINE_1.equals(ALL_SPECTRUM_IDS[i])) return i;
        return 0;
    }

    static String spectrumIdForIndex(int index) {
        return index >= 0 && index < ALL_SPECTRUM_IDS.length ? ALL_SPECTRUM_IDS[index] : LINE_1;
    }

    static int blend(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(Color.red(a) * (1f - t) + Color.red(b) * t);
        int g = Math.round(Color.green(a) * (1f - t) + Color.green(b) * t);
        int bl = Math.round(Color.blue(a) * (1f - t) + Color.blue(b) * t);
        return Color.rgb(r, g, bl);
    }
}
