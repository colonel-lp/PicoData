package com.colonellp.ellamonitor;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Release naming helpers kept independent of Android for regression testing. */
final class ReleaseVersion {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+){1,3})");
    private static final Pattern BUILD_PATTERN = Pattern.compile(
            "(?:build|version\\s*code|versioncode)[^0-9]{0,8}(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private ReleaseVersion() { }

    static String extractVersion(String text) {
        String source = text == null ? "" : text;
        Matcher matcher = VERSION_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1)
                + (source.toLowerCase(Locale.US).contains("beta") ? "-beta" : "") : "";
    }

    static String fromRelease(String tag, String title, String apkName) {
        String version = extractVersion(tag + " " + title);
        return version.length() > 0 ? version : extractVersion(apkName);
    }

    static boolean isNewer(String version, int build, String installed, int installedBuild) {
        return build > 0 ? build > installedBuild : compare(version, installed) > 0;
    }

    static int extractBuildCode(String text) {
        Matcher matcher = BUILD_PATTERN.matcher(text == null ? "" : text);
        if (!matcher.find()) return 0;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (Exception ignored) { return 0; }
    }

    static int compare(String left, String right) {
        int[] a = parts(left);
        int[] b = parts(right);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int av = i < a.length ? a[i] : 0;
            int bv = i < b.length ? b[i] : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private static int[] parts(String value) {
        Matcher matcher = VERSION_PATTERN.matcher(value == null ? "" : value);
        if (!matcher.find()) return new int[0];
        String[] pieces = matcher.group(1).split("\\.");
        int[] result = new int[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            try { result[i] = Integer.parseInt(pieces[i]); }
            catch (Exception ignored) { result[i] = 0; }
        }
        return result;
    }
}

