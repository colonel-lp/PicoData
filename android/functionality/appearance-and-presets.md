# Appearance and viewer presets

Updated in **0.23 / build 5**.

Themes contain colour roles only, with name/format metadata. Named files remain in Downloads/ella-monitoring/themes; older themes are read compatibly. Font belongs to viewer settings/presets and is selected on Settings, not in the theme colours box. Choosing/editing a theme preserves the font and affects only the Theme modification indication. Preset save/load/update does not include or restore colours/theme selection.

Title text, Title background and Title outline independently style Currents:, Ella Monitoring and Temps:. Missing title text in an older theme inherits general Text. Central/environmental Gauge panel 1/2, their outlines and per-gauge highlight colour retain the 0.22 grouping. All colour roles save/load and compare independently. The colour-editor list groups related options; fixed green/red flag semantics remain distinct.

The shared picker accepts exactly six RGB hex digits, with optional # and whitespace trimming. Incomplete or invalid typing does not change the colour; invalid OK gives feedback and keeps the picker open. Wheel changes update hex input. OK accepts and Cancel/outside/Back restores the original. The editor stays alive but hidden while a picker is open, then becomes visible after the picker dismissal callback, without extra dimming. Return tests check actual visibility and repeated picker use after queued callbacks.

Presets contain labels and editable app/chart/screen settings, gauge highlights, font, update interval and private connection data. Tokens are AES-GCM encrypted with the existing app-private Android Keystore key; snapshots contain ciphertext/IV, never plaintext. Address/token comparisons use address and decrypted token values, not randomized ciphertext. Loading validates the snapshot before committing settings and uses the normal reconnect/cancellation lifecycle. View values/Copy hides token/address. System default resets viewer settings while retaining the active connection and its comparison baseline.

Older presets migrate embedded font into the settings field, discard theme from preset comparison and retain their labels/settings. Presets that previously had no connection snapshot acquire the existing active connection during migration, preserving connectivity. Migration leaves older named theme files readable; writing a theme produces colour-only format 15.

PRESET • name and THEME • name are available on all pages; modifications show UNSAVED and the changed dot. One preset difference map controls selector state and changed-item colouring for Settings fields, chart setting controls, screen/lock icons and editable dashboard labels/headings. Save/update or restoring matching values clears differences. Automatic chart fallback is read-only and live readings/freshness never count as preset settings. Chart edits update indication immediately.

Appearance code adapts the owner's EQ reference without DSP/audio/vendor operations. Popups inherit the active app theme; summaries, charts, source bindings and calculations are retained.
