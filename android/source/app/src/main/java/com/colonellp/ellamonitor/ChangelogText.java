package com.colonellp.ellamonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Display the maintained changelog file, never replace it with release titles. */
final class ChangelogText {
    private static final Pattern HEADING = Pattern.compile(
            "(?m)^##[ \\t]+v?(\\d+\\.\\d+(?:\\.\\d+)?(?:-beta)?)([^\\n]*)$");

    static boolean valid(String file) {
        LinkedHashMap<String, Entry> entries = entries(file);
        if (entries.isEmpty()) return false;
        for (Entry entry : entries.values()) if (entry.notes.isEmpty()) return false;
        return true;
    }

    static String display(String file, String offered) {
        LinkedHashMap<String, Entry> entries = entries(file);
        if (entries.isEmpty()) return "Changelog is unavailable.";
        List<String> versions = new ArrayList<>(entries.keySet());
        Collections.sort(versions, (a,b) -> ReleaseVersion.compare(b,a));
        if (offered != null && versions.remove(offered)) versions.add(0, offered);
        StringBuilder text = new StringBuilder();
        for (String version : versions) {
            if (text.length() > 0) text.append("\n\n");
            Entry entry = entries.get(version);
            text.append(plain(entry.title));
            if (version.equals(offered)) text.append(" • available update");
            text.append("\n").append(plain(entry.notes));
        }
        return text.toString();
    }

    static String notesForVersion(String file, String version) {
        Entry entry = entries(file).get(version);
        return entry == null ? "" : plain(entry.notes);
    }

    private static LinkedHashMap<String, Entry> entries(String file) {
        LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
        if (file == null) return entries;
        file = file.replace("\r\n", "\n");
        Matcher matcher = HEADING.matcher(file);
        String version = null, title = null;
        int start = 0;
        while (matcher.find()) {
            if (version != null) entries.put(version, new Entry(title, file.substring(start, matcher.start()).trim()));
            version = matcher.group(1);
            title = matcher.group().substring(3).trim();
            start = matcher.end();
        }
        if (version != null) entries.put(version, new Entry(title, file.substring(start).trim()));
        return entries;
    }

    private static String plain(String markdown) {
        return markdown.replaceAll("(?m)^- ", "• ").replace("`", "").replace("**", "")
                .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
    }

    private static final class Entry {
        final String title, notes;
        Entry(String title, String notes) { this.title=title; this.notes=notes; }
    }
}

