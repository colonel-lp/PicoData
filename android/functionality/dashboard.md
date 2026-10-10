# Live dashboard

Updated in **0.21 / build 3**.

The Node-RED arrangement and EQ logical landscape scaling remain: 1024×600 fullscreen, 1024×510 minimum windowed, with available width/height expanding. Portrait windows retain a rotated landscape canvas when Android ignores sensor-landscape orientation. Fullscreen omits hidden system-bar insets while keeping cutout/keyboard safety. The title is permanently removed; the connection-status line is optional and always absent on Settings.

Every readout is a themed button with background, On/Off text/borders and press/focus highlight. Current rows occupy equal heights with exactly 6px gaps and no blank reference rows. Temperature roles determine a stable semantic order: internal/external ambient, charging equipment, cabinet/internal fridge, water, other channels, SBMS external, Pico battery, SBMS internal. Source binding IDs stay independent of display labels; unknown channels use their stable IDs for tie ordering. Eight flags share a two-row box; CFET/DFET/EOC remain green On, other flags red On, Off hollow and unavailable dashed. Lock suppresses taps/long presses without stopping readings.

Central current/SOC comparisons retain the gauge style with centred faces and corrected padding. Barometer appears above LPG/water then pitch/roll. Inclination is fixed -5 to +5 with a needle and colour only from zero to the clamped needle; the true number remains visible outside that range. Cell voltages have their own lower bounding box, preserving previously observed channels as unavailable when stale. V[P] uses raw selected Pico battery voltage; V[S] uses the raw starter battery voltage. Neither uses the logged secondary voltage or SBMS reference.

Time to full/empty follows the traced owner Node-RED join/calculator chain. With Pico nominal capacity C, remaining Ah R and raw battery current I: round R to the nearest integer; use -(C-round(R))/I when I>0, or -round(R)/I when I<0; round hours to two decimals. Charging retains a minus sign. Absolute hours >=24 display Dd:HHh; shorter values display Hh:MMm. Zero/missing/invalid/stale inputs are unavailable. This is a derived estimate, not a decoded native Simarine time field. The collector's historical capacity.timeRemaining seven-day fallback is ignored by this display; collector/MQTT/database remain unchanged.

Charts and summaries retain their calculations/contents, but every popup now follows the shared current theme and EQ dialog chrome. Existing stable aliases, independent freshness, valid zeros and history/CSV bindings remain.
