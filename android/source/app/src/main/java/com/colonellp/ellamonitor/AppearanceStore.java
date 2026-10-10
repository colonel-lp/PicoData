package com.colonellp.ellamonitor;

import android.content.SharedPreferences;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Viewer appearance only: never copies connection credentials into presets. */
final class AppearanceStore {
    final SharedPreferences prefs;
    AppearanceStore(SharedPreferences prefs) { this.prefs = prefs; }
    ThemeConfig active() { if (!prefs.contains("appearance.active")) return ThemeConfig.defaults(); try { return ThemeConfig.fromJson(new JSONObject(prefs.getString("appearance.active", "{}"))); } catch (Exception e) { return ThemeConfig.defaults(); } }
    void active(ThemeConfig t) { try { prefs.edit().putString("appearance.active", t.toJson().toString()).apply(); } catch (Exception ignored) { } }
    List<String> names() { List<String> names = new ArrayList<>(); for (String key : prefs.getAll().keySet()) if (key.startsWith("appearance.saved:")) names.add(key.substring(17)); Collections.sort(names, String.CASE_INSENSITIVE_ORDER); return names; }
    ThemeConfig load(String name) { if (!prefs.contains("appearance.saved:" + name)) return null; try { return ThemeConfig.fromJson(new JSONObject(prefs.getString("appearance.saved:" + name, "{}"))); } catch (Exception e) { return null; } }
    void save(String name, ThemeConfig t) throws Exception { if (name == null || name.trim().isEmpty() || name.length() > 80) throw new IllegalArgumentException("Use 1–80 characters."); ThemeConfig copy = t.copy(); copy.name = name.trim(); prefs.edit().putString("appearance.saved:" + copy.name, copy.toJson().toString()).apply(); }
    void delete(String name) { prefs.edit().remove("appearance.saved:" + name).apply(); }
    void rename(String old, String name) throws Exception { if (names().contains(name.trim())) throw new IllegalArgumentException("That name already exists."); ThemeConfig t = load(old); if (t == null) throw new IllegalArgumentException("Preset unavailable."); save(name, t); delete(old); }
}
