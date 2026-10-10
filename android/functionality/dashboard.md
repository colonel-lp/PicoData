# Live dashboard

Updated in **0.2.0-preview / build 2**.

LiveDashboard uses the owner's Node-RED arrangement and EQ & DSP logical scale: 1024×600 fullscreen, 1024×510 minimum windowed, with logical width/height expanding to available space. DesignViewport applies Android view transforms to scale drawing/touch. Sensor landscape is requested; a portrait window rotates the canvas if the system ignores the request. Safe system/keyboard insets are respected. Native Back callbacks on API 33+ and the legacy path on API 27–32 return Settings/Charts to Live data.

Current/temperature boxes show one-line label/value pairs. Eight flags share one two-row box; CFET/DFET/EOC use green On, other flags red On, Off hollow circles and unavailable dashes. Gauge drawing remains the preview design with theme colours/font and corrected 6px gaps. Environment uses barometer above LPG percentage/water litres and pitch/roll. The bottom strip contains SBMS voltage, delta, capacity, runtime, recorded secondary SC302T voltage labelled Pico, and dynamically available cell channels. Previously observed cell positions stay as unavailable when stale; no private cell map is embedded.

Source IDs, signs, calculations, freshness, valid zero, aliases and database bindings are unchanged. ChartView, history processing and existing summary popup methods retain their original implementation/palette; range navigation is relocated to the Charts header. Lock prevents readout tap/long-press popups without stopping display or collection. Pi 0.7.1 remains the logger; no database changes occur in this build.
