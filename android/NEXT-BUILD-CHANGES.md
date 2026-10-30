## Pending next changes — collector clock-step status recovery — 2026-10-30

Recorded at the owner's request for the next authorized collector update; not implemented.

- [ ] Return API `logging.state` from `clock-step` to `started` once normal clock progression has resumed and any backward-time hold has ended. Advancing `savedAt` alone must not clear the warning while integration is still paused.
- [ ] Preserve the persistent `clockSteps` counter, existing history, clock-adjustment detection, skipped-interval/backward-hold safeguards, calculations, coverage and retention. Do not mask an active logging failure or unrelated diagnostic state.
- [ ] Add focused forward/backward clock-step recovery tests, including no premature recovery during a backward hold and no reset of the accumulated counter.

This is collector/API status reporting only; it does not request an Android build or authorize implementation now.

## Completed in 0.29 — 2026-10-13

The owner authorized “next build please”. Prepared source 0.29 / versionCode 11 for owner-built Ella-monitoring-v0.29.apk. Completed the two pending request sections below: interval Ah/Wh and matching cumulative totals; compact boxed controls/taller graph/two-line headers; bar annotations and temperature ranges; refresh scheduling; distinct month failures; graph axis theme role and independent global Values state.

Current-draw presentation now adds magnitudes of each recorded interval's net Ah/Wh, consistently in grouped points and the header total. Battery flow stays signed; source derivations, logged integrals/directional values and CSV are untouched. This does not reconstruct gross within-interval consumption. Weekly bar grouping is twelve hours; line grouping stays six. Values affects bar annotations only. Barometer 900–1100 is the explicit zero-origin exception.

Collector remains 0.7.3; package/signing identity, SDK/Gradle/JDK settings and logging/retention remain unchanged. No APK is assembled. See BUILD-VALIDATION.md for actual checks and remaining device/Android Studio validation.

## Next-build requests — popup chart layout, refresh, extrema and bar labels — 2026-10-13

Implemented after the owner authorized the next source build: 0.29 / build 11. Preserve the pending interval Ah/Wh correction below and unrelated app/collector behavior.

### Owner-confirmed clarifications

- [x] The quantity selector is Ah / Wh, not the ambiguous W / Ah wording. The owner selected Ah / Wh after being reminded that interval/cumulative quantities must agree with integrated totals. Ordinary points are interval amounts; Sum is cumulative.
- [x] Values toggles only the numeric annotations above/below bars. The owner answered “the values above or below the bars”; keep header readings/totals, Samples, period captions and axis labels visible. Do not interpret this as hiding all chart values or header contents.
- [x] SOC, voltage and barometer headers show the latest recorded reading within the selected window on the left. Samples and the selected period sit centrally; Values is on the right. Temperature headers instead show Min and Max. Do not add Ah/Wh selection or enable Sum for non-additive readings.

### Layout and theme

- [x] Reduce popup button height without reducing text size. Proposed compact height is 28 logical pixels instead of 38, retaining 11.5px text and 6px padding/gaps where applicable; the two-line header buttons are the specified exception. Keep Close and the other compact buttons consistent, correctly scaled on supported screens.
- [x] Put Highlight 1 / Highlight 2 / Hide / Chart / Close in a themed bounding box. Put both time-period rows in their own themed bounding box. Preserve mutually exclusive highlights, swatches, Hide behavior, existing order and global 6px gaps/edge padding. Do not change unrelated approved dialog padding.
- [x] Remove Rolling data / Fixed data labels and shift the four time buttons in each row left. Put Line/Bar above Sum in the freed right column, inside a separate themed/padded bounding box. Align that box with the two-row period box; retain unavailable Sum handling for non-current charts.
- [x] Move Current:/Energy: to the left, aligned with the Y-axis label area, preserving the requested colon alignment. Samples occupies the centre of the Current row; the period caption occupies the centre of the Energy row. Do not leave the old separate Samples/period rows below them.
- [x] Move Ah/Wh into the right side of that two-line header. Its height matches the combined two text lines. Put Values immediately to its left, suitably aligned. The Ah/Wh button's right inset from the popup edge must equal the left inset of the Current text. Use measured/shared header-axis layout rather than mismatched fixed margins.
- [x] Use the freed vertical space for a taller graph. Remove the old in-graph top-right controls and their reserved space; preserve the approved popup width, scaling, source/calculation behavior and safe small-screen fallback. Apply matching compact footer/period/mode boxes and header layout to verified chart-enabled non-current popups. Unlogged title/footer popups gain the compact footer box only; no new history is implied.
- [x] Use hour-only X-axis labels on day charts, without :00. Keep the full local-calendar day and accurate elapsed-time axis, including DST.
- [x] Add a logically placed Graph axes & markers colour picker with a shown swatch/hex value. Apply it to popup axis lines/ticks/labels and point markers, with compatible old-theme defaults, preview/save/restore/validation. Preserve independent grid/data-line/bar colours and bottom-bar roles. This section concerns popup charts; the separate Charts page is not otherwise requested to change.
- [x] Persist per-popup Values state alongside period/quantity/Line-Bar/Sum in Downloads/ella-monitoring/display-settings.json, outside presets. Read older files compatibly; retain strict bounds/field validation and existing theme/preset ownership.

### Refresh timing — proposed corrected calendar rules

The current popup loads on opening/selection changes but has no periodic history refresh. The owner wants changing data to redraw at its relevant resolution. All times below use the selected device-local calendar; requests remain UTC. Refresh in place without clearing the graph or rebuilding the popup, and guard/cancel callbacks on close, background, reconnect or selection change.

- [x] This hour: refresh each minute when minute history exists, and roll to the new hour at its boundary. Environmental minute history remains unavailable.
- [x] Last hour: load the closed selected hour, then advance/refetch at each new hour. No minute polling of a finalized unchanged past hour is necessary; allow a bounded retry if its final checkpoint is not yet published.
- [x] Today: refresh hourly, and restart the selected day at midnight. This week: refresh hourly so current six-/twelve-hour groups gain newly logged hourly data, and roll at Monday 00:00. Do not wait until Sunday/Monday to show the current week's changing data.
- [x] This month: refresh as new daily history becomes available, and roll at 00:00 on the first. Do not wait until month end to add the current month's daily points. Account for existing UTC daily buckets versus local-calendar cutoffs without inventing/prorating data; source checkpoint availability may require retry after a boundary.
- [x] Yesterday: change/refetch at local midnight. Last week: change/refetch at Monday 00:00, meaning the end of Sunday, not the start of Sunday.
- [x] Explicitly selected older months are fixed: no continuous data polling after finalized data loads. Update month availability when a new month begins or after an explicit retry/reopen; preserve a user-selected older month rather than silently changing it. Default previous-month selection, if still relative, advances at the first-of-month boundary. Respect the saved selection.
- [x] Calculate next real calendar boundary (DST and month length), not repeated fixed 24-hour sleeps. Keep source freshness/logging intervals/retention untouched. Refresh only while the relevant popup and foreground activity are active, with connection/checkpoint-failure retry handling.

### Bars, temperature extrema and pressure range

- [x] Today/Yesterday bars span one actual logged hour. This week/Last week bars group actual hourly records into local 00:00–12:00 / 12:00–00:00 intervals; use summed valid Ah/Wh rather than a mean rate. This supersedes the earlier six-hour grouping for weekly BAR mode only; retain six-hour weekly LINE points and daily month points unless separately requested. Keep missing hours/coverage visible rather than treating the full bar width as complete recording.
- [x] Draw the value above a positive bar or below a negative bar, rounded to zero decimal places. Values turns these annotations on/off. Actual plot positions, integrals/totals, exports and stored data retain their precision. Rounded annotation sums need not equal the unrounded total; do not quantize calculation inputs to make them appear equal. Signed battery bars remain signed; current-draw presentation retains the approved sign rules and common chart/summary arithmetic.
- [x] Temperature line mode plots recorded High and Low series instead of the single last-reading series. Temperature bar mode shows a low-to-high range bar, with high/low annotations above/below it controlled by Values. Use one-hour day bars, twelve-hour week bars and corresponding retained month/day data. Keep negative Celsius, missing ranges and unavailable minute options honest. Use distinguishable existing theme colours unless further roles are requested.
- [x] Replace temperature Current/Energy header content with Min:/Max: for the selected window. Use min/max already recorded in the hour/day environmental records; combine extrema using min of lows and max of highs, never averages, sums or invented extrema from only last values. No new logging is requested.
- [x] Barometer Y-axis is fixed at 900–1100 hPa, an explicit exception to the earlier universal zero-origin rule. Preserve recorded pressure precision and existing periods/freshness.

### Grey month button — source finding and next-build correction

Source reviewed at PicoData e6de554daec37fff6a4d5021318941cc811ff31e. The Fixed month dropdown is enabled only for a supported day-history metric with earlier recorded months. loadMonths filters out the current month, so current-month data alone correctly gives no previous-month menu. However, a failed/cancelled history request is caught and converted to an empty months list, disabling the same button with no distinct feedback. Thus grey alone cannot prove that no data exists. The Rolling current-month button does not depend on that list; its disabled state instead reflects a missing binding or unsupported resolution. No current owner API/database contents were inspected.

- [x] Distinguish successful empty availability from loading, unsupported/unlogged source and request failure. Keep genuine no-earlier-daily-data handling, but provide themed, clear failure feedback and an accessible retry instead of silently presenting a network/API failure as no data.
- [x] Verify the correct selected field/sensor and actual compatible daily records for derived sources. Do not reconstruct old daily curves from monthly totals or fabricate earlier months. Keep the current month available through Rolling where its exact source/day resolution exists.

Implemented in source 0.29 / build 11. Collector/database/retention are unchanged; the owner builds the APK. See BUILD-VALIDATION.md for focused checks.

## Next-build correction — popup chart interval Ah/Wh — 2026-10-13

Owner clarifies this concerns the element popup charts: current/energy points should represent Ah/Wh for their plotted interval regardless of the selected capture period, so the ordinary chart relates directly to the shown Ah/Wh total.

Read-only finding at PicoData 30f331d44397e19124e9867653e0b934a4eed2ce: PopupHistory.series currently returns coverage-weighted mean A/W when Sum is off, and integrated cumulative Ah/Wh only when Sum is on. MainActivity labels the selector A/W. Current:/Energy: totals already integrate netAh/netWh, or mean times valid recorded coverage divided by 3600. Adding ordinary A/W ordinates therefore does not reproduce Ah/Wh totals. The conversion exists only in the cumulative branch; this is the requested ordinary-view quantity correction, not evidence that collector integrals are wrong.

- [x] Change current/energy popup charts to Ah/Wh per plotted interval with Sum off. Use recorded netAh/netWh where available, falling back to mean A/W multiplied by actual corresponding recorded coverage seconds / 3600. Do not multiply by the nominal minute/hour/day duration when coverage is missing or partial.
- [x] Group integrated quantities by addition, including the six-hour weekly groups; do not use the current coverage-weighted-rate mean as an interval Ah/Wh amount. Keep existing minute/hour/six-hour/day point spacing, selected full calendar axes, gaps and Samples semantics.
- [x] Keep Sum as cumulative Ah/Wh. Use the same valid intervals, source definitions and sign/usage presentation for points and totals so the final cumulative point agrees with its shown total. Review sign reversals: summing displayed absolute interval magnitudes and taking the absolute signed-net total are not equivalent. Do not hide a discrepancy through a rate/unit label change, fabricate coverage or silently alter source signs.
- [x] Label the quantity selector and Y-axis Ah/Wh in both ordinary and cumulative views. Apply this only to current/energy popup charts; SOC/voltage/pressure/temperatures retain their physical units and unavailable Sum handling. No changes to the separate Charts page are requested here.
- [x] Focused checks: a 2 A / 24 W reading with 15 minutes of valid coverage contributes 0.5 Ah / 6 Wh, independent of nominal bucket size; grouped interval amounts and cumulative endpoint match the displayed integrated total using common coverage/sign rules. Cover partial recording, missing current/power, missing buckets, signed batteries and positive draw presentation.
- [x] Leave collector 0.7.3, database, retention, raw logged signs/integrals and CSV untouched. Implemented in authorized source 0.29 / build 11; no APK is assembled.

## Completed same-version correction — unit-test JSONException — 2026-10-12

- [x] Fix the five checked JSONException compilation errors in PopupHistoryTest: construct the synthetic JSON response in the test method before the IOException-only connection-factory lambda. Keep the response, assertions and production code unchanged.
- [x] Retain source 0.28 / versionCode 10 and Ella-monitoring-v0.28.apk. The Gradle minor-version and Java-8 deprecation messages are warnings; no toolchain/version change is required. UpdateTest.java was listed without a separate error detail; its checked JSON calls are already in methods declaring Exception.
- [x] Correct the validation gap: compile the focused sources with actual Android checked-JSONException declarations ahead of test JSON classes. See BUILD-VALIDATION.md for the before/after check and focused test results.

## Completed in 0.28 — 2026-10-12

The owner authorized “Next build”. Prepared source 0.28 / versionCode 10 for the owner to compile Ella-monitoring-v0.28.apk; no APK assembled. Implemented the pending popup graphs/two-row periods/captions/month menu/global choices, default titles/Pitch-Roll pointer and Load [BMS] correction. Preserve collector 0.7.3, current logs and its already-authorized 15-day hours/indefinite days.

- [x] Load [BMS] is PV1 minus signed Battery [BMS], with the existing gauge settings identity, consistent derived summaries/charts/CSV and distinct untouched raw externalLoad history.
- [x] Current/energy choices, positive usage presentation, signed batteries/negative temperatures, exact logged-source availability, calendar graph scales and independent samples/plot-group counts are implemented. Week/hour and day/hour plots use retained hourly records; older/missing detail stays gaps.
- [x] Each popup remembers its view in Downloads/ella-monitoring/display-settings.json, outside presets. Strict validation and private pending-cache/write-failure behavior are covered by focused checks. Main-element Highlight/Hide retains preset ownership.
- [x] Version/changelog/user guides/functionality/validation updated. Read BUILD-VALIDATION.md for performed source checks and remaining Android Studio/device checks.

The completed request records below preserve their original decision history. Later owner clarifications supersede earlier retention suggestions and title-only SOC behavior: collector 0.7.3 already provides 15-day electrical hours and indefinite days; source 0.28 gives verified logged non-current popups graph/period controls. No new logging is added.

## Next-build correction — Load [BMS] calculation/source binding — 2026-10-12

Owner reports Load [BMS] falling to zero as PV1 rises and requests the difference between Battery [BMS] and PV1. Record for the next authorized Android build; implemented in authorized Android source 0.28 / build 10, with no collector change.

- [x] Replace the Load [BMS] gauge's current direct externalLoad binding with the requested derived current: PV1 current minus Battery [BMS] current, in amps. Positive battery current means charging; negative means discharge. Use PV1 only as requested, preserving the signed inputs and their mA-to-A conversion.
- [x] Follow the verified Node-RED path: raw battery/PV1 currentMA values join, subtract battery minus PV1, then divide by -1000. This is PV1_A - battery_A, not battery_A - PV1_A.
- [x] Example checks: PV1 0 A / battery -3 A gives load 3 A; PV1 2 A / battery -1 A gives load 3 A; PV1 5 A / battery +2 A gives load 3 A. Cover valid zero, charging/discharging, missing/stale input and fresh same-source snapshot cases. Do not force unavailable inputs to zero or introduce a clamp that hides sign/binding errors.
- [x] Make the derived Load [BMS] popup summaries, A/W/Sum views, charts and CSV use the same PV1-minus-battery logged components, signed Ah/Wh arithmetic and compatible coverage/gap handling. Both inputs are already logged; no new aggregate logging is needed.
- [x] Preserve existing display aliases, Highlight/Hide choices and future per-popup global selections when changing the gauge's binding. Keep the raw externalLoad channel's identity, recorded history and documented meaning distinct; do not relabel or overwrite it as the derived total.
- [x] Leave MQTT/Node-RED, SBMS decoding, collector 0.7.3, logging fields, source signs, database and retention unchanged. Apply the pending positive current-draw presentation at display level without altering battery +/- or source arithmetic.

Source review at PicoData d309ea3866445f2753c08a6b41d8cd54d3e6f6f6: LiveDashboard currently selects externalLoad for Load Σ [BMS]; MonitorData passes through the matching API current, and the decoder exposes currentMA.extLoad / 1000. The owner Node-RED flow instead uses its joined battery/PV1 subtraction and -1000 divider. Existing init-logging documents positive battery current as charge. This finding concerns the gauge's source selection; it does not establish a fault in saved raw externalLoad records.

## Corrected Pi update instructions — 2026-10-12

- [x] Use the owner's existing ~/update-pico.sh for collector updates. The user supplied its script: temporary sparse clone, node.js copy into ~/PicoData/node.js, npm ci --omit=dev and pico-mqtt.service restart. The parent configuration/history files are outside the copy target. Correct the README and future-work instructions instead of assuming the installed folder is a Git checkout.
- [x] Retain collector 0.7.3 and Android 0.27 / build 9. This documentation correction does not modify the updater, application/collector source or logging policy.

## Implemented collector change — extended retention — 2026-10-12

Owner subsequently authorizes an exception to the earlier keep-retention instruction: electrical hours 15 days and daily history indefinite, preserving current logs. Implemented immediately in collector source 0.7.3; Android remains 0.27 / build 9. This does not authorize new logged fields or minute/environmental sampling changes.

- [x] Change expiry for the existing combined battery and separate electrical/selected-voltage history in place. Keep schema 2 and existing payloads/bindings/calculations, 24-hour minutes, indefinite months and the longer environmental hourly window.
- [x] Update API catalogue retention metadata and collector package/lock root version; dependencies and default query windows are unchanged.
- [x] Verify database preservation/restart and exact expiry in synthetic temporary databases; 36 logger/combined/API tests pass. Current retained logs survive; longer retained detail accumulates going forward, while previously expired rows are not recreated. The owner applies the source on the Pi with its existing configuration/path.
- [x] Next Android build: use actual server history/retention availability for full previous-week six-hour plots and daily past-month curves as the longer history accumulates. Unlogged/raw-voltage/Ah/minute-environmental options remain unavailable without verified existing history; no extra logging is approved.

## Next-build clarifications — chart units, signs, global view state and unchanged logging — 2026-10-12

Owner answered the popup-chart questions. These decisions supersede the earlier suggestions/questions below; implemented in authorized Android source 0.28 / build 10.

- [x] Use the agreed A/W selector for current charts. Sum shows total Ah and total energy Wh over valid recorded coverage; power W and energy Wh remain distinct. Current-draw totals are presented as usage; preserve supply/net-flow meanings for PV and signed battery channels.
- [x] Offer Sum only for current/energy charts, not SOC, voltage, remaining capacity, pressure or temperatures. Do not sum non-additive physical readings.
- [x] Present current-draw values as positive display magnitudes; retain +/- for Battery [BMS] and Battery [Pico]. This is a presentation request, not a change to recorded signs, component selection or existing signed calculations. PV charging values retain their supply meaning. Temperature charts also need negative Celsius values where recorded, despite the positive-only current-draw rule.
- [x] Save period/month/quantity/Line-Bar/Sum and equivalent future chart display selections in the single global Downloads/ella-monitoring file, individually keyed per popup/chart. Exclude these frequently changed view selections from presets and preset-modified indication. Existing main-element Highlight/Hide choices retain their preset ownership.
- [x] Do not change logging, selected fields, database schema, retention, MQTT or collector behavior for these chart requests. Adjust the availability of display options to actual recorded fields, resolutions, retention and source bindings. Preserve available summaries/live displays; disable or omit unavailable graphs/options rather than fabricate detail or substitute another source.
- [x] Existing retention stays: electrical minute 24 hours, hour 7 days, day one calendar month, month indefinite; pressure/logged environmental hourly one calendar month and their daily records indefinite. Six-hour week plots must use retained hourly data; older monthly totals cannot recreate daily curves. Pressure/temperature minute plots, remaining-Ah history and raw Pico-voltage graphs need unavailable-option handling unless their exact required history is already demonstrably recorded.
- [x] Correct Last week Samples x/8 and explain actual recorded-entry availability independently of plotted six-hour groups. Do not imply a complete past week's hourly curve when part has expired. A full previous calendar week can begin almost 14 days before the current date; the earlier 15-day suggestion was a buffer for providing that whole week, not a requirement for current operation, and was rejected.
- [x] Keep global settings and plotting resilient to missing records and mismatched source coverage; display-only positive magnitudes must not alter battery +/- or turn unavailable readings into zero.

Retention-size explanation requested: current electrical daily history is bounded to approximately 28–31 completed entries per separately logged metric/combined battery group plus the current checkpoint. Keeping daily electrical history indefinitely would instead add roughly 365 entries per year per metric/group, with bytes dependent on actual JSON/index/storage overhead. Extending hour retention from 7 to 15 days would increase that bounded hourly portion from 168 to 360 entries per metric/group, then level off. These are generic row-count illustrations, not measured database-size estimates. The database already retains monthly electrical and daily environmental summaries indefinitely. No retention/logging change is authorized.

## Next-build requests — popup charts, captions, titles and global view state — 2026-10-12

Implemented in authorized Android source 0.28 / build 10; no collector change. This extends the pending two-row period controls above the 0.27 footer. Chart-enabled SOC/voltage/capacity/environmental popups now need period controls, superseding their earlier title/footer-only treatment.

### Layout and titles

- [x] Shift the Current: line left so its colon aligns with Energy:; retain the requested text sizes and values.
- [x] Change the summary label to Samples:.
- [x] Correct misleading Last week Samples x/8. Source review confirms SummaryPeriods.expected counts intersecting UTC day buckets for a device-local calendar week: a BST Monday-to-Monday week intersects eight UTC days, while included omits closed crossing buckets. Align sampling/count semantics with the selected calendar period and actual retained data; do not label eight UTC intersections as eight local days or invent missing readings.
- [x] Put the popup graph above the buttons, the same width as the button rows. Retain existing requested button sizes, theme and spacing; no unrelated padding changes.
- [x] Supply the requested popup titles: SBMS0 Voltage; Remaining Battery Capacity; Time Remaining; Voltage Pico for V[P]; Voltage Starter Battery for V[S]; Cell [1], Cell [2], etc. Add temperature after the temperature popup label. Keep source bindings/calculations distinct from display captions.
- [x] Shorten Pitch/Roll needles so their tips end a couple of logical pixels inside the coloured band's inner edge, with existing scaling; preserve the measured angle, scale and coloured arc.

### Graph periods and controls

- [x] This hour / Last hour: show the full selected hour, 10-minute x-axis divisions and minute readings.
- [x] Today / Yesterday: show the full selected day, 2-hour x-axis divisions and hourly readings.
- [x] This week / Last week: show the whole Monday–Sunday week, daily x-axis divisions and 6-hour readings. Six-hour plot groups may be derived from recorded hourly data with valid coverage; there is currently no stored/API 6-hour resolution.
- [x] Month: show the full selected calendar month, weekly major divisions plus smaller day divisions and daily readings. Preserve the pending scrollable Fixed month dropdown with two-digit years for previous-year months.
- [x] Scale the y-axis to the plotted data with whole-number tick labels; its origin is always zero. Preserve measured precision instead of rounding data to integer ticks. The owner's sign clarification above applies: positive current-draw presentation, signed batteries and negative temperatures.
- [x] Add themed graph controls at top right: Line/Bar and Sum. Sum switches to cumulative values and rescales the y-axis. The owner's answer above confirms the A/W selector and cumulative Ah/Wh for current/energy only.
- [x] Add graphs/time-period controls for current popups and the requested SOC, Pico/SBMS voltage, remaining Ah, barometer and logged temperatures. Do not invent history for live-only fields. Starter/cell/time popup title requests alone do not establish their chart data sources.
- [x] Keep the agreed Samples captured/expected recorded-entry semantics, distinguishing underlying logged hourly entries from derived 6-hour plot groups. Handle gaps, partial elapsed periods and retention explicitly; a plot point is not a newly recorded sample.
- [x] Remember every popup's display selections and save them in a single global file under Downloads/ella-monitoring, keyed to stable element identity. This includes the requested period/chart view choices. Only popup/chart view selections move to that file; existing main-element Highlight/Hide choices remain preset-owned.

### Data prerequisites and questions to settle

- [x] Current graph quantity: establish whether it displays current A, power W, per-period Ah/Wh or a selectable quantity. For A/W, meaningful cumulative output integrates recorded duration/coverage into Ah/Wh. Clarify whether Sum should be unavailable for SOC/voltage/capacity/pressure/temperature, rather than blindly adding non-additive readings.
- [x] Negative values: establish whether zero is a baseline with negative currents/temperatures below it; do not clip valid negatives or silently change their signs.
- [x] History retention: electrical hourly data currently lasts 7 days, so it cannot supply a full preceding calendar week of 6-hour points throughout the current week. Electrical daily data lasts one calendar month, so older selectable months cannot supply daily curves from monthly totals. The owner declines retention changes: keep current retention and adjust unavailable display options. Old expired detail cannot be recovered.
- [x] Pressure/logged temperatures currently have hour/day history only. No new minute recording is authorized; adjust option availability rather than fabricate minute values from an hourly last reading.
- [x] Remaining battery capacity Ah is live-only in MonitorData and absent from the combined battery history fields. Raw Pico battery voltage V[P] has no attached history metric in the app; Pico electrical history's documented voltage/power reference is SBMS, while secondaryVoltage is a separately selected raw Pico voltage. Verify exact sensor identity before using a historical voltage; no new fields/logging are authorized, so leave historical display options unavailable where the required exact source has no recorded history. Do not alter the power reference or substitute SBMS voltage.
- [x] Global file ownership: decide whether it stores only period/Line-Bar/Sum view selections or also per-element Highlight/Hide choices already owned by presets. Keep saved colours in themes and credentials out of this view-state file.

Source reviewed at PicoData b9603a828ccebe5e7f68f025e5ec10bcc889e8c9: SummaryPeriods.java, MonitorData.java, GaugeView.java, node.js/LOGGING.md, history-reader.js, battery-history.js and history-config.js. These are source findings, not new hardware/database measurements.

## Next-build requests — popup period rows and recorded periods — 2026-10-12

Owner requests these for the next build. Implemented in authorized Android source 0.28 / build 10. The two-row design supersedes the 0.27 Rolling/Fixed toggle.

- [x] Remember the Rolling/Fixed selection when a popup is closed and reopened, showing the restored selection in the new row layout.
- [x] Remove the Rolling/Fixed toggle button. Show the Rolling period buttons in the upper row and the Fixed period buttons directly below.
- [x] Move the period buttons to the right, with Rolling data and Fixed data labels in front of their respective rows. Keep the existing button sizes; the new labels use the same existing text size. Retain the agreed 6px button/bounding-box spacing.
- [x] Show the sampled time period directly below Samples. For Rolling hour/day/week/month, display the period in minutes/hours/days/days respectively.
- [x] For Fixed hour, show the actual hour range, for example 6am to 7am. For yesterday, show weekday, ordinal date and month, for example Tuesday 5th September. For last week, show the date range, for example 5th September to 11th September. For a fixed month, show the recorded month.
- [x] Replace the Fixed month button with a dropdown containing scrollable previous months. Selecting a month displays its recorded data. When a month belongs to a previous year, show its two-digit year, for example September '25.
- [x] Preserve the current calendar definitions, signed Current/Energy calculations, recorded-entry Samples x/y, honest missing-data handling, source bindings and the title-only treatment of non-current popups. Month navigation must use recorded history rather than inventing values for absent data.

## Completed same-version correction — test compilation — 2026-10-12

Owner reported cannot find symbol TextView in CompanionLayoutTest.java when compiling unit-test sources.

- [x] Add the missing android.widget.TextView import for the highlight-button test, introduced during the 0.27 checkbox-to-button conversion. Retain versionName 0.27 / versionCode 9 and Ella-monitoring-v0.27.apk.
- [x] Leave Gradle/JDK/Java compatibility configuration unchanged: the supplied newer-Gradle and Java-8 deprecation messages are warnings, not this missing-symbol error. UpdateTest.java is named in the supplied output, but no separate error detail was provided for it.

## Completed in 0.27 — addition — historical Load [Pico] derived from logged currents — 2026-10-11

Owner confirms that all currents used to calculate the live Load [Pico] gauge are logged.

- [x] Provide derived historical values for Load [Pico] from those existing logged current components. Its live derivation must not cause the popup to call it live-only or claim no historical data is available.
- [x] Use the same component selection and verified signed polarity as the live calculation: Pico electrical metrics with the load role, summed as current times their configured polarity. Preserve actual signs; do not use absolute values or substitute the Pico battery current.
- [x] Make derived history available to the requested current/energy summary periods and chart navigation. Derive net charge from the component histories; derive energy from their valid logged/integrated power or voltage/current data with the existing coverage rules. Preserve source attribution, units, period boundaries and historical gaps. This uses existing logged data; do not introduce a new logged aggregate or alter the database schema just to expose the derived history.
- [x] Align component history periods and retain missing/partial coverage. Do not treat an absent required component as zero or claim a complete total from incomplete inputs. Samples x/y for the derived gauge must reflect the component data needed for the total, not an inflated sum of independent component counts. The build-time clarification below supersedes the earlier raw-sample interpretation.

Read-only source review: PicoData d8796c79d8a8648637813d926cd6655d352be190, MonitorData.display. The live sum uses catalogue-backed Pico load currents and verified polarity, then created pico:load-sum with a null metric in that reviewed source; its popup consequently displayed the live-only message. The owner's confirmation establishes that the constituent currents have retained history. Implemented after the owner authorized this source build: 0.27 / versionCode 9; no APK assembled.

## Completed in 0.27 — requests — element popups, summaries, font dropdown and text themes — 2026-10-11

Owner authorized the source build and clarified Samples during implementation: contributing recorded history entries versus expected minute/hour/day entries for the selected period. Implemented in 0.27 / build 9.

### Popup layout and controls

- [x] Replace Highlight 1/2 checkboxes with selectable buttons, retaining mutually exclusive selection, shown highlight colours and saved per-element choices. Keep Hide contents in the bottom control row.
- [x] Put Chart immediately to the left of Close in the bottom row, alongside the highlight/hide controls; preserve existing chart navigation and hide/highlight behavior.
- [x] Make every popup button's text the same size as Close. Period/time buttons must also match Close's height.
- [x] Apply 6px gaps on every side between buttons and between buttons and their bounding box. This explicitly authorizes these popup/button spacing locations; do not change unrelated dashboard spacing.
- [x] Centre the element label/title at the top in larger text. Remove the existing snapshot/receipt/source/original-name/help/coverage prose and other unrequested text.
- [x] Use Button on/off theming for all actual buttons, including selectable highlights, periods and the Rolling/Fixed control.

### Summary periods and contents

- [x] Add a period row directly above the bottom highlight/control row, with both rows the same total width. Put the Rolling/Fixed mode button on the right. Period buttons change with the mode: This hour / Last hour, Today / Yesterday, This week / Last week, This month / Last month, showing the relevant month name. Use Close's text size throughout.
- [x] Rolling means the current calendar period so far, confirmed by the owner on 2026-10-11: This hour starts at the current hour's :00, even when only one minute has elapsed; Today starts at midnight; This week starts at Monday 00:00; This month starts at 00:00 on the 1st. Each ends at now. Do not use trailing 1-hour/24-hour/7-day/30-day windows. Weeks run Monday through Sunday. Calendar starts use the device timezone; the API and chart buckets remain UTC. This assumption was stated during implementation.
- [x] For Rolling sample counts, the expected-samples denominator covers only the elapsed calendar-start-to-now window, not the future remainder of the full hour/day/week/month.
- [x] For Pico currents and all current gauges, show only the centred title plus Current: using the previous signed net-charge summary (Ah), and Energy: using the previous signed net-energy summary (Wh), with the requested period/control rows. Preserve their existing source meanings and arithmetic; do not substitute instantaneous amps/watts or invent unlogged metrics.
- [x] SOC popups have the title and common bottom controls, with no period buttons or extra summary text.
- [x] Voltage, cell delta, Ah, time remaining, barometer, LPG, water, Pitch/Roll and temperature popups need only the title and bottom control row; no summary-period controls or additional body text.
- [x] On relevant current/energy summary pages, place Samples x/y between the summary values and buttons: captured samples / samples that should have been captured for the selected period. Owner clarified during the build that these are recorded history entries against expected minute/hour/day entries for the selected period, rather than raw publisher sample counts. Count contributing entries and intersecting elapsed/full slots; retention, missing inputs and excluded boundary buckets remain gaps. Counts do not assert full duration/energy coverage.

### Font menu and text-colour roles

- [x] Fix the Font dropdown appearing behind Edit theme and remaining visible after the editor closes. Attach it to the owning theme-dialog window, keep it above that editor, and dismiss/clear it on editor close/dismissal and lifecycle changes. Preserve font choice as preset state.
- [x] All main-page labels and values, other than the three titles and bottom-bar controls, use the Labels theme colour. Include gauge labels/values/units/limits and unavailable dashes; do not select a readout text colour from Button on/off based on data availability.
- [x] Remove Button off text from the theme colour picker/active role and replace remaining uses with Button on text. Apply the Labels rule above to main-page content; use Button on text for actual button text that previously used the removed role. Preserve other button on/off theme roles and compatible loading of older saved themes. Titles retain their Title text role; bottom controls retain their own button roles apart from the explicitly removed off-text role.

Read-only source review: PicoData 418d5d5d373a9932b77ca40ada3b465c9a5e95fe. LiveDashboard.Readout.update selects Button on/off text for available/unavailable readings, while GaugeView uses Palette.text (Labels) and Palette.muted (Button off text), explaining the inconsistent roles. MainActivity.showElementDetail/addElementOptions/loadSummary contain the existing verbose summaries, four rolling periods and checkbox footer. Its font menu uses anchoredMenu.showAtLocation(viewport, ...) and a method-local PopupWindow; the theme-dialog dismissal handler does not dismiss that popup. These are source findings, not device tests.

## Completed same-version correction — SOC depletion direction — 2026-10-11

Owner reported that both SOC gauges still operated clockwise and explicitly requested an immediate fix. Retain 0.26 / build 8.

- [x] Fix the moving edge's direction when SOC falls. A negative sweep starting at a fixed 12 o'clock made its endpoint move clockwise during depletion. Anchor the arc's end at 12 o'clock instead and move its start anticlockwise: falling from full, 75% places the moving edge at 9, 50% at 6 and 25% at 3 o'clock.
- [x] Retain one whole remaining arc, unrounded green/amber/red thresholds, numerical readings and all other gauge appearance. Update the existing drawing assertion to describe the corrected geometry.

Source parent: PicoData b6da1b3b5dc82d39b55b7e80df487e3fb86d7012. Focused source review only; no tests, lint, compilation or device checks run under the simple-UI fast-edit rule.

## Completed same-version correction — voltage text and equal internal spacing — 2026-10-11

Owner explicitly requested the file changes immediately and instructed that no tests be performed. Implemented in 0.26 / build 8; no new build/version increment.

- [x] Draw V[P], V[S] and cell-voltage labels/values at the same 14px logical text size as the SBMS voltage readout, bypassing combined-string auto-shrinking. Preserve three-decimal values, units, themed colours, hidden contents and screen scaling; shorten long custom labels rather than shrinking voltage text.
- [x] Measure and draw label/value fragments separately with equal left inset, label-to-value gap and right inset derived from each cell's full width. This replaces the visible fixed text insets for these cells; other padding and bounding-box geometry are unchanged.

Source parent: PicoData 5c8f065ad40fcd8cb58765e143df71f5bac3c6b5. No tests, compilation or device checks were run for this correction.

## Authorized source build 0.26 — 2026-10-11

Owner requested the next source build from the pending list. Android versionName **0.26** / versionCode **8**, with **Ella-monitoring-v0.26.apk** configured as the owner-built APK filename. Implemented the completed export, gauge and preset-file items below; validation is recorded in BUILD-VALIDATION.md. No app APK assembly/signing/publication here. Pi collector remains 0.7.2; the SBMS investigation below remains open because no controlled device/network result has been supplied.

Preset migration preserves both files on conflicts with a migrated suffix and retains private originals on failure. Preset/theme files are validated before loading; tokens remain installation-bound ciphertext. CSV fallback saves the same loaded-range bytes in Downloads/ella-monitoring/exports only when the picker fails to launch, not when the user cancels it. No broker/router/Wi-Fi reconfiguration or inferred SBMS transport fix is included.

## Next-build requests — Settings export and file-picker failure — 2026-10-11

- [x] Remove **Export current chart range** and its otherwise empty EXPORT group from the Settings page. Retain the chart-page export action; the owner's removal request applies to Settings.
- [x] Investigate/fix the reported **No file picker is available.** failure for CSV export. Current MainActivity.exportHistory opens Android ACTION_CREATE_DOCUMENT and displays this message for any exception when launching it; it does not establish that no picker is installed. Distinguish an unavailable handler from other launch failures, provide useful redacted error feedback and a supported save fallback when a document picker is unavailable or fails to launch. Preserve user cancellation, loaded-range CSV contents and Android 8.1/current-version storage compatibility. Owner confirms this occurs on the Joying: a file picker is installed, but the system does not successfully use it, and similar picker warnings have occurred for other operations. Record this as a launch/compatibility failure, not a demonstrated missing picker; the exact exception and the other affected operations remain unknown. When the picker cannot launch, provide an export save fallback to Downloads, using the existing supported storage paths and permission handling. Preserve ordinary picker cancellation without silently exporting. Theme/preset saves to their agreed Downloads folders must not depend on successfully launching a picker.

Originally recorded at 81ade8e2bf7f45a82b0dad9fa0248e48057e7c62; implemented in the authorized source 0.26 / build 8. Physical Joying picker behavior and the underlying launch exception still require device verification.

## Next-build requests — gauge appearance and text positions — 2026-10-11

Implemented in source 0.26 / build 8 after the owner requested the next build.

- [x] Remove the pointers from both Battery current gauges (Pico and SBMS). Retain their existing zero-centred negative/positive arcs and scales.
- [x] Both SOC gauges must operate anticlockwise. Colour the entire remaining-SOC arc with one colour selected from the actual SOC: **green at 50–100%**, **amber at 25% to below 50%**, **red below 25% down to 0%**. This supersedes the separate red/amber/green arc segments implemented in 0.25. Keep the existing full-circle scale and change the remaining arc's whole colour only when a threshold is crossed; do not round SOC before deciding the colour. Check 50%, 49.9%, 25%, 24.9%, zero and full.
- [x] Make the live values on Pitch and Roll a little smaller. Preserve their pointers, zero-origin shading and −5 to +5 scales.
- [x] Move the displayed limit values on gauges, including Pitch and Roll, **down 6px**.
- [x] On all six current gauges (two Load, PV1, PV2, two Battery), replace the unit **A** with **amps**, centred on the same baseline as the repositioned limit values.
- [x] Move the central live value on all six current gauges **up 6px**.
- [x] Move the Barometer live reading **up 6px**.
- [x] Move the LPG and Water live values **up 6px**.

Apply these text offsets in the established reference layout with existing screen scaling; check fitting/alignment without moving the gauge boards or changing unrelated padding. Current GitHub GaugeView/GaugeScale at e44ae68629b7462ea5d85d6f23025def33cf077d already draw SOC anticlockwise but use multiple fixed colour bands; the requested single threshold-selected remaining-arc colour is implemented in 0.26. Preserve source readings, calculations, unavailable dashes and unrelated theming.

## Next-build requests — preset files and validation — 2026-10-11

Originally recorded after source 0.25; implemented after the owner authorized source 0.26 / build 8.

- [x] Save named presets as JSON files in **Downloads/ella-monitoring/presets**, alongside the existing Downloads/ella-monitoring/themes folder. Adapt the existing Android-version-compatible Downloads storage implementation. Preserve save/update, load, rename/delete, view-values, selected-preset and changed-item behavior. Migrate existing app-private presets without losing settings, deleting originals before successful migration, or overwriting existing files silently. Keep the agreed separation: themes contain colours; presets contain editable settings, labels and connection data; Keep screen awake, Full screen and Lock remain excluded.
- [x] Strengthen validation for preset and theme files before applying them: bounded file reads, valid JSON object/document, supported compatible format, appropriate field types and allowed setting values/ranges, and safe file/name handling. Preserve compatible older files and missing newer fields through explicit migration/defaults; distinguish them from malformed supplied fields rather than silently accepting arbitrary objects or coercing invalid values. Reject invalid files with clear feedback and retain the current active settings/theme. Add synthetic malformed-file and migration checks when implementation is authorized.
- [x] Retain encrypted token storage and redacted value summaries; never write plaintext tokens to Downloads, logs, copied summaries or source. Current AES/GCM encryption uses an Android Keystore key tied to the installation/device: moving a saved encrypted preset to another installation/device does not make its connection token portable. Handle an undecryptable saved connection explicitly without partially applying the preset or replacing a working connection; do not invent a plaintext export or change this privacy contract.

Current-source investigation at f776e78595aee116a9154e16fc53b5e14e6790f2:

- Presets are JSON strings in app-private SharedPreferences (viewer.preset:), not Downloads files. Load catches JSON/type/decryption failures and stages connection/setting edits before committing. It validates the connection address and decrypted token, but does not strictly validate all setting ranges, enums or field types; normalization accepts supplied values for recognized keys.
- Named theme files already use Downloads/ella-monitoring/themes. File reading rejects parse/read failures and files larger than 65,536 bytes; save validates filename length and unsafe characters. ThemeConfig.fromJson uses permissive defaults/coercions for colour fields, so a syntactically parseable but malformed theme can be accepted as defaults. There is no complete strict file schema/format validation.
- The colour picker's six-digit RGB input checks and damaged encrypted-connection regression test exist. They do not establish comprehensive malformed preset/theme file validation.

## Authorized source build 0.25 — 2026-10-11

Completed the agreed source changes for 0.25 / build 7 and the accompanying collector 0.7.2 display-policy correction. Owner builds Ella-monitoring-v0.25.apk in Android Studio; no APK assembly/signing/publication here. Physical-device checks and the demonstrated device/network dropout investigation remain open.

- [x] Restore the barometer value when SBMS is connected. Source review at b14ac1879cc6bd9dbf00b5b35b0f1e2cbd906979 identifies a raw-to-catalogue transition bug: MonitorData suppresses raw barometer/temperature entries once their sensor is catalogued, but updateLive only recognizes replacement IDs with the same raw prefix. It can therefore append an obsolete unavailable raw copy after the valid catalogue entry; LiveDashboard's last-pressure selection can then bind the barometer to that unavailable copy. Resolve replacement by physical source/sensor identity and quantity, preserving actual stale handling, history bindings and saved element settings. SBMS connectivity is the owner's observed condition; a device-specific transport cause is not demonstrated.
- [x] Correct the live Inverter calculation to **Battery [Pico] + Load [Pico]**. This supersedes the subtraction requested and implemented in 0.24. Preserve the displayed inputs' signs and unavailable-input rules; no new logging/history.
- [x] Remove the duplicate unavailable Outside row introduced by retained raw metadata when the same sensor obtains a catalogue-backed binding. Deduplicate by physical source/sensor identity, not display label; genuinely distinct sensors may share labels. Check initial catalogue arrival, periodic catalogue refresh, stale/recovery and reconnect without retaining orphan readouts or losing saved aliases/highlights/hide choices.
- [x] Preserve the previously working temperature order and saved labels while removing the duplicate unavailable binding. Owner reports barometer and probably temperatures worked in the previous build; the supplied 0.24 photo shows one live Outside row plus an extra unavailable Outside, with the other visible temperature roles following the intended order. Source comparison with 0.23 at 351e3859a12db457c47f07e0961a2fd8eece692d confirms temperatureOrder is unchanged. Fix the new retention regression first; do not replace the working sorter or rename/reorder unrelated readings based only on possible abbreviation edge cases. Check the corrected result against the existing Node-RED/photo reference.
- [x] Place Highlight 1, Highlight 2, Hide contents and Close together on the bottom row of element popups. Keep the highlight colour swatches, mutually exclusive highlight selection, hide behavior and current app theme. Preserve existing summary/chart actions and content; verify the bottom row fits without clipping or oversized controls on phones and Joying. The owner's later padding restriction supersedes the earlier broad 6px normalization request: retain existing popup padding unless the owner specifically requests a change for that location.
- [x] Add an independent theme colour picker named **Button border**, with its colour shown, for main-page buttons/elements whose borders currently use the button on/off colours. Replace those on/off border-colour bindings with this role throughout the main page, including normal and pressed outlines where applicable. Keep the bottom bar and all its existing theming unchanged. Retain the separately requested gauge-panel outlines and title outline roles. Save/load the new colour with themes, include it in live preview and theme-change detection, and preserve older-theme compatibility.
- [x] Move the Font picker from the Settings page into the **Edit theme** popup. Its bottom row must contain two controls: the Font dropdown on the left and **Back** on the right. Open the font choices above the dropdown. Match the app's themed button style and retain the existing Edit theme popup padding, which the owner says is correct. This changes the control's location only; preserve the agreed storage contract that themes contain colours and font selection remains a preset setting.
- [x] Set both Load gauges (SBMS and Pico) to a fixed **0–10 A** scale and colour their value arcs with the existing **Gauge negative** theme colour. Preserve the source values/signs and calculation; this is a display-scale/colour change.
- [x] Set **PV1 and PV2** gauges to fixed **0–20 A** scales.
- [x] Set both Battery current gauges (SBMS and Pico) to fixed **−10 to +10 A** scales with zero in the middle. Colour only the arc between zero and the current indicator: Gauge negative for negative current and Gauge positive for positive current, matching the Pitch/Roll behavior. Preserve zero and unavailable-value handling.
- [x] Make both SOC gauges full-circle **0–100%** scales, with red for **0–25%**, amber for **25–50%**, and green for **50–100%**. Owner clarified through the direction choice that SOC increases **anticlockwise** from 0% at 12 o'clock, through 25% at 9 o'clock and 50% at 6 o'clock, to 100% back at 12 o'clock. This direction clarification supersedes the initial clockwise clock-position interpretation; retain the source SOC and the displayed percentage.
- [x] Correct padding and button alignment in **Save changes to preset** and **Save changes to theme** dialogues. Owner extended the prior single-dialogue exception to these two: they must match each other and the existing approved theme/preset dialogues in padding, button sizing/alignment and themed appearance. Use those existing dialogues as the reference rather than introducing a new spacing scheme. Withdraw the previous blanket dialogue/popup padding changes: preserve Edit theme and all other existing padding unless the owner specifically requests that location. Preserve each save action and the separately requested element-popup and Edit theme footer layouts; do not use those layout moves to normalize unrelated padding.
- [x] Add synthetic regression checks for a raw pressure/outside reading becoming catalogue-backed while SBMS stays connected: one Outside row, the valid barometer value retained, stable temperature order, correct new Inverter sum and unchanged independent source freshness.
- [x] Increase V[P]/V[S] labels and voltages to match the cell-voltage text size, retaining their current 10px side padding (owner addition during the authorized build).

## SBMS investigation remains open after 0.25

- [ ] Owner reports no SBMS dropped values overnight while the Joying was off; drops began after the Joying was turned on in the morning (2026-10-11). This is a timing correlation to investigate, not proof that the app, Pi or head unit causes the loss. Correlate accepted MQTT samples/Node-RED, router reachability and Pi API freshness with controlled Joying/app states.
- [ ] Separate head-unit power/Wi-Fi from viewer activity: with the Joying powered on and Wi-Fi connected, force-stop Ella Monitoring and observe Node-RED/router SBMS stability; then open Ella Monitoring and compare over a similar interval. Repeat if necessary because failures are intermittent. If loss also occurs with the app stopped, test the Joying Wi-Fi state separately while retaining power. Do not assume the overnight interval or a short successful test rules out the Pi/broker or other changing conditions. No runtime reconfiguration or source changes are authorized by this observation.

- [x] Correct the demonstrated API display cutoff separately from logging integration gaps. Use receiver-owned live deadlines; reject isolated invalid JSON without renewing accepted data or instantly clearing an unexpired display. Logging stops coverage at rejection and retains maxGapSeconds. Synthetic API/receiver/history checks do not establish a real network cause.
- [ ] Verify the owner's deployed timeout configuration, live receive/acceptance ages and slow/failed API requests against a contemporaneous dropout. The app still has its separate three-second cached HTTP bound.
- [ ] Correlate device/router reachability, active broker logging and direct Node-RED/MQTT reception during the reported drops. The owner confirms the SBMS entry disappears from the router, and has changed automatic Wi-Fi channel selection to a manual channel to test stability. These observations point to real device/network connection loss, but the responsible component and reason remain unproven. No fresh diagnostic evidence or outcome of that channel test has been supplied; the owner cannot establish whether the device is restarting. Keep actual network/account/device inventories and raw diagnostics private. No broker/router reconfiguration or downgrade is included in this source build.
- [ ] Owner reports SBMS reception was consistently stable before these changes. Preserve that regression report and investigate the exact runtime cause rather than treating connection losses as expected behavior. Historical broker evidence shows real transport/protocol disconnects, separate from app/API clearing; verify fresh contemporaneous events before attributing current drops.

## Completed authorized source build 0.24 — 2026-10-10

Implemented in source 0.24 / build 6. Owner compiles Ella-monitoring-v0.24.apk in Android Studio; no APK assembly/signing/publication here. App-side freshness/render fixes are covered by synthetic checks; actual head-unit dropout/picker timing remains a device check.

- [x] Exclude Keep screen awake, Full screen and Lock states from presets, including save/load/difference checks and older presets. Preserve current app-wide button states when loading any preset or System default.
- [x] Correct the gap after V[P]/V[S], keeping 6px between elements and inside their bounding box.
- [x] Rename Gauge highlight to Highlight 1; add Highlight 2. Show each colour in the theme editor and element popup. Offer both for every element, with at most one selected per element, preserving old highlight selections.
- [x] Reduce the cell/delta text side padding from 20px to 10px and use the same 10px side padding in V[P]/V[S].
- [x] Add Hide contents to every element popup. Keep its bounding box/layout and popup access while hiding its label/value/gauge contents. Save hide/highlight choices in presets; themes store their colours only.
- [x] Add a live Inverter row at the bottom of Currents:, calculated exactly as Battery [Pico] minus Load [Pico], without altering source signs. Treat either unavailable input as unavailable; do not add database logging.
- [x] Investigate/fix SBMS display dropouts while Pico remains visible. Preserve actual independent stale/disconnected handling. Source review found app-only hard-coded source-age cutoffs in addition to collector freshness; Node-RED reads MQTT directly. Avoid treating a display cutoff as a demonstrated MQTT transport failure.
- [x] Prevent periodic clearing/reconstruction of all gauges when live metadata/readings are transiently absent; update stable views in place and show dashes only for genuinely unavailable/stale values. Preserve binding identity and actual reconnect recovery.
- [x] Move Changed indicator to the blank colour-editor slot beside Title outline.
- [x] Add Indicator background for the CFET/DFET/etc bounding box and Voltages background for both lower voltage/summary boxes. Preserve fixed green/red flag meaning.

## Completed picker corrections in 0.24 — 2026-10-10

Owner device report supersedes the completed picker claims below. Implemented after the owner authorized source build 0.24. Preserve this report as the reason the 0.23 behavior was revisited.

- [x] Restore live colour preview while moving the wheel/brightness control or entering a valid RGB value. Apply the current colour to the visible app and relevant themed elements immediately, matching the EQ app. Preserve current preset/theme separation, input validation and Cancel/Back/outside rollback. Source review at 70125b4a82a2ff103437e2849c51d89d95c823d9: the picker listener changes the ThemeConfig value but calls only refreshAppearance(), which invalidates the hidden colour-list view; it does not refresh existing app views/backgrounds for live preview. Verify the visible result, not only the stored colour.
- [x] Remove the owner's reported approximately two-second delay between OK/Cancel and the theme-colour list returning. Match the EQ app's immediate return and keep the existing list usable, undimmed and correctly themed. Source review shows the dismiss callback writes the active theme and posts a full build() before showThemeEditor(); investigate that unnecessary full-page rebuild and persistence/difference work before returning the editor. This identifies work on the return path, not a measured cause of the exact delay. Preview/rollback and editor return should avoid full page reconstruction on each picker interaction. Verify repeated wheel/hex edits, OK/Cancel/Back/outside return and visible live preview on supported device layouts; framework callback tests alone did not establish timing or actual live rendering in 0.23.

- [x] Follow the owner's reuse instruction: start from the working EQ code when its behavior is requested, remove irrelevant EQ/DSP logic and adapt monitoring-specific state/views/bindings without changing the requested behavior. For these picker issues, the owner permits the best appropriate solution after inspecting the working reference. Read-only reference reviewed: Joying-EQ-DSP 2ce2fc16a02e05692960675a9913d687b556a8a0, MainActivity.editThemeColour / returnAfterThemeEditor / showThemeAppearanceDialog and ColorWheelView. Its wheel/valid-hex callback invalidates the dashboard directly; OK/Cancel return through the dialog-specific path rather than rebuilding the monitoring page. Adapt live repaint and immediate return while preserving the existing validation, rollback and theme/preset contract; do not import spectrum, DSP or audio state.

## Authorized source build 0.23 — 2026-10-10

Completed the agreed picker, title-colour, preset/theme separation, private connection and change-indication list. Owner compiles Ella-monitoring-v0.23.apk in Android Studio; no APK assembly/signing/publication here.

## Completed theme/preset scope in 0.23 — 2026-10-10

Owner clarification supersedes earlier requirements that included a theme in a preset or excluded connection data. Implemented after the owner requested the build; source 0.23 / versionCode 5, owner compiles the APK.

- [x] Themes store colour-role values only. Keep their saved name as identity metadata; move non-colour appearance settings such as font into the preset/settings state. Theme colour edits, selection, save or rename affect only the Theme changed indication, not the Preset comparison.
- [x] Presets store display labels and all items the owner can set/change, including connection data. Include display/app/chart settings, gauge-highlight choices, screen/lock/persistence options, update frequency and non-colour appearance settings. Exclude theme colour values/selection from preset save/load/update and dirty comparison. Live readings and automatic display fallbacks are not editable preset settings.
- [x] Include the connection/API address and private authentication token in preset save/load/update and difference detection. Retain encrypted app-private credential storage and existing privacy protections; do not place token plaintext in preset JSON, copied value summaries, exports, logs or source. Loading the preset must apply its connection data through the existing connection lifecycle. Keep actual addresses/tokens private.
- [x] Use one consistent per-item preset comparison for the selector's UNSAVED/dot and Changed indicator colouring of editable labels/settings, including connection settings. Save/update or restoration to matching values clears both; theme-only changes do not mark preset items.
- [x] Preserve existing saved themes/presets with explicit compatible migration: separate previously bundled theme/font fields and handle older presets without connection fields without clearing the active connection. Current app-private settings/credentials remain intact.

## Completed preset difference and changed-item corrections in 0.23 — 2026-10-10

Read-only review of PicoData 3041cdc6051c4e604a1cf8183a0f38959e37f581 against Joying-EQ-DSP e1b54f3f58efb92215d99e2e3f2941530fbcbc15. The review was read-only; the owner subsequently authorized implementation in source 0.23.

- [x] Resolve the owner's report that Preset intermittently shows UNSAVED after theme-only changes. The pre-0.23 presetState included the entire theme JSON (including its name), so colour/font/theme-name changes differ from the saved preset. EQ compares audio preset state separately from theme state. Resolved by the owner: theme stores colours only; preset stores labels/adjustable items including connection data. Apply the confirmed scope above and preserve saved-theme/preset compatibility.
- [x] Remove unintended preset differences from automatic chart fallback: The pre-0.23 selected() mutated selectedMetric when choosing a default/missing metric. Since metric is included in presetState, merely opening/loading Charts can change the compared preset state. Keep a stable distinction between the saved/user-selected setting and its available display fallback; live readings/freshness must never count as edited settings.
- [x] Refresh preset/changed-item indication after every included setting edit, load/save/restore and relevant label/theme action. Some chart handlers mutate included range/quantity/defined/metric fields through buildHistory without refreshing the bottom selectors; build() computes the selector state before buildHistory may choose its fallback. This can delay the visible UNSAVED state until a later rebuild. Verify the exact state change instead of inferring an intermittent device cause from source alone.
- [x] Implement the missing per-item change colouring using the theme's Changed indicator colour, following the EQ difference/render pattern. The pre-0.23 ChangedButton used that role for selector text/dots, while individual dashboard/Settings controls lacked a saved-preset difference mapping. Compare each editable label/setting against the selected preset, highlight only differing items and clear highlights on save/restore. Preserve normal fixed CFET/DFET/EOC green and fault red semantics, availability styling and actual measurement values; changing sensor readings is not a preset edit. Review gauge Highlight background as a saved setting, not a live reading. Include correct default/older-preset handling and consistent indicator/render refresh in regression checks.

## Completed theme picker corrections in 0.23 — 2026-10-10

Recorded from the owner's latest report; implemented in the authorized 0.23 source update.

- [x] Fix the reported regression where OK or Cancel in a colour picker leaves the theme colours box closed. Both must return to a visible, usable colour-editor list; OK retains the chosen colour, Cancel restores the original, and no extra screen dimming is introduced. Preserve the same return path for outside/Back cancellation. Inspect dismissal callbacks and page rebuilding; the pre-0.23 source already called showThemeEditor on these paths, so source presence alone does not establish correct runtime behaviour. Regression checks must assert the returned dialog is actually showing, can open another picker, and survives queued dismissal callbacks, in framework tests; actual phone/head-unit verification remains pending.
- [x] Match the EQ app's colour-entry checks: trim whitespace, accept exactly six hexadecimal RGB digits with an optional leading #, reject incomplete/invalid input without changing the colour, and keep the picker open with clear feedback on invalid OK. Do not expand three-digit shorthand while editing. Use the same validation for typed preview and confirmation; retain wheel/hex synchronization. Read-only reference: Joying-EQ-DSP e1b54f3f58efb92215d99e2e3f2941530fbcbc15, MainActivity.parseHexColor/editThemeColour. The pre-0.23 monitoring code required a literal # followed by six hex digits and has different feedback/return handling.
- [x] Provide a separate Title text colour picker for the main-screen Currents:, Ella Monitoring and Temps: headings, alongside their Title background and Title outline pickers. Apply/save/load all title roles with the theme and retain compatibility with saved themes. Title background/outline already exist in source; heading text previously followed general Text. Ensure the requested title text/background controls are visible in the colour list and affect all three heading boxes.

## Completed same-version corrections — 2026-10-10

Explicitly authorized for immediate implementation; retain **0.22 / versionCode 4**. The owner compiles the APK.

- [x] Reduce the central gauge bounding box to restore 6px gaps to the current column and environmental group; keep 6px inner gauge padding.
- [x] Align the barometer/LPG/water/pitch/roll bounding box with the cell-voltage box below, using the same left/right boundaries and width.
- [x] Add 6px padding between the indicator buttons and both ends of their bounding box; retain 6px between indicator buttons.
- [x] Open the Updates frequency dropdown above its button.
- [x] Retain the Theme dropdown beside Preset on the Settings bottom bar.
- [x] Show loaded names in the Preset/Theme buttons using the EQ format: PRESET • name / THEME • name. Modified values show UNSAVED with the existing modification dot; preserve loaded identity for accessibility. Apply on Live data, Charts and Settings. Explicit immediate correction; keep 0.22 / versionCode 4.

## Completed authorized source update 0.22 — 2026-10-10

- [x] Persistent notification: show one app-authored line saying **Ella Monitoring**, with no body/subtitle text. Remove the existing tap-to-return text; preserve the notification's tap action and ongoing behavior.
- [x] Configure the actual Android Studio/Gradle APK output filename as **Ella-monitoring-vX.XX.apk**, using the versionName and advancing by 0.01 per authorized version. The current project still outputs `app-debug.apk`; renaming only the delivered APK did not satisfy this requirement. The next authorized version after 0.21 is 0.22.
- The owner will compile the APK in Android Studio. Prepare and push source changes on an explicit implementation request; do not assemble, sign, deliver or publish an APK unless explicitly requested. Record validation accurately and distinguish source checks from Android compilation/device tests.

Implemented in source 0.22 / versionCode 4 after the owner explicitly requested the next build and source commit. The owner compiles the APK; no APK is assembled or published here.

### Dashboard and theme corrections — 2026-10-10

These owner corrections supersede conflicting earlier checked-off items. Implemented in source 0.22; the owner compiles the APK.

- [x] Restore the **Ella Monitoring** dashboard header directly above the indicator box, as in the supplied Node-RED reference. This is a dashboard group title, distinct from the removed app-wide title. Source inspection: the current flags group has an empty title.
- [x] Gauge-border theming is implemented with the later requested independent **Gauge panel 1 outline** and **Gauge panel 2 outline** controls, superseding the single shared picker.
- [x] Centre the lower voltage, amp-hours and time-remaining values within their cells.
- [x] Change the text padding before and after the values in the cell-voltage and delta-voltage boxes from 6px to **20px**. Retain the global 6px gaps between elements.
- [x] Remove the user-facing **LIVE** entry identified in the themes list and add a **System default** option. Check the existing theme/preset menu placement rather than silently relabelling an unrelated setting.
- [x] Reformat the Settings block currently titled **SYSTEM** to match the EQ app's bottom-right block. Remove the SYSTEM heading, arrange controls in two rows with space reserved for a future button, and put the app version at the bottom. Match EQ button proportions and spacing. Retain Changelog/Update and Back.
- [x] Remove the separate **Check for updates** text and include **Updates:** within the update-frequency control. Retain Off / 1hr / 3hr / 6hr / 12hr / 24hr choices.
- [x] Keep the bottom bar and its buttons at the same height when fullscreen is toggled, matching the requested EQ style. Fullscreen should expand the available content without changing bottom-control heights. Source inspection: MainActivity uses 44/54 logical pixels for the bar; BottomBar uses 26/40 for its controls, and DesignViewport changes its logical base height. Account for all three when fixing the visible height.
- [x] Add an option in each gauge's popup to highlight its background colour, with a corresponding colour picker in Themes.
- [x] The bottom preset and theme boxes must say **Preset** and **Theme** and show a changed-state dot like the EQ app. Replace the current asterisk/whole-label colour treatment with the reference dot behavior.
- [x] Rename **Current Draw** to **Currents:**.
- [x] Give **Currents:**, **Temps:** and **Ella Monitoring** title boxes themed outlines and backgrounds. Add theme colour roles named **Title outline** and **Title background**.
- [x] Both OK and Cancel in a colour picker must return to the theme colour-editor box. Fix the reported unwanted screen dimming; inspect nested-dialog/window dim behavior and match the EQ app.
- [x] Remove the top explanatory/title text from the rename-display-label popup and resize it to fit. Match comparable EQ popup dimensions and button sizes; the current buttons are too large.

Voltage clarification: the owner confirms that the voltage delivered with the other Pico battery values is the intended source. Preserve the current V[P] raw Pico battery-voltage binding; no voltage-source change is requested.

### Further dashboard and preset clarification — 2026-10-10

- [x] Put the complete gauge array in its own bounding box, using global 6px padding between elements and around the group.
- [x] The preset changed indicator must show that current settings differ from the currently saved preset. Include display-label changes and other settings belonging to the preset in this comparison. Unsaved differences trigger the dot/highlight; saving or restoring matching values clears it. Keep theme-specific modification indication consistent with the saved theme.
- [x] When adding the **Ella Monitoring** header above the indicators, make space by reducing only the height of the gauges. Preserve their widths and the dimensions of the remaining dashboard elements.
- [x] Increase text size in the **amp-hours** and **time remaining** boxes to use their available space. Preserve centred alignment and fit longer valid values without clipping.
- The requested centring of voltage, amp-hours and time remaining, and **20px** text padding in cell-voltage/delta-voltage boxes, are implemented as listed above. Only the accidental unfinished removal phrase was discarded.

- [x] Move the Theme dropdown directly beside the Preset dropdown in the bottom-left control box. Order the controls **Preset → Theme → Live data → Charts**, retaining global 6px padding and the separate right-hand icon control box.

### Separate gauge background theme roles — 2026-10-10

- [x] Add two independently selectable background colour roles to the theme editor: **Gauge panel 1** for the eight central gauges, and **Gauge panel 2** for the barometer, LPG, water, pitch and roll gauges. Apply each role to the individual gauge panels in its respective group. Keep these two group background colours separate from general panels, gauge borders and the optional background-highlight colour requested above; save/load them with the theme.

### Gauge outlines and colour-picker ordering — 2026-10-10

- [x] Make the two gauge groups' outlines independently themeable: **Gauge panel 1 outline** for the eight central gauges and **Gauge panel 2 outline** for barometer, LPG, water, pitch and roll. This supersedes the earlier single shared Gauge borders colour request. Save/load both roles with the theme.
- [x] Order all theme colour pickers logically by the elements they control. Keep related background, text, outline and state colours together; place each gauge panel's background and outline next to each other. Group general app colours, titles, panels/gauges, buttons and their on/off/changed states, then chart-specific options where applicable. Preserve existing functionality while reorganising the editor.

- [x] Display unavailable measurement values as a single dash **—**, without the word **Unavailable**. Apply consistently wherever measurement values are shown; preserve missing/stale handling and keep valid zero values as zero.

- [x] Fix Settings checkboxes appearing oversized or incorrectly laid out on the Samsung while displaying correctly on the Joying. Match the EQ companion proportions across phone/head-unit display densities, keeping the checkbox, label, spacing and touch area aligned and unclipped. Source inspection points to native CheckBox controls using default drawable/minimum sizing alongside explicit pixel text and a fixed 38px row within the scaled design viewport; verify this likely sizing mismatch before implementing the correction. Validate both device layouts, retaining correct toggle behavior and theming.

- [x] Match Ella Monitoring's visible app-icon size to the EQ app in both the launcher/desktop and the Android status bar on the Joying. Compare each app's actual icon resources, artwork bounds and padding, including notification small icons; adjust the applicable Ella resources while preserving its icon design and required Android formats. Verify both placements rather than assuming one asset change covers both.

## Completed authorized build 0.21 — 2026-10-10

- Use Pico's reported nominal and remaining capacity with the verified Node-RED time-to-full/empty chain and raw battery current. Zero/missing/stale inputs are unavailable.
- Remove the blank gaps from the reference current list; distribute all rows evenly with 6px global element padding.
- Completed the pending dashboard, theme, Settings, changelog and update corrections below. Updates use PicoData releases; source stays in android/source. Retain the installed preview package and signing key.

# Next build changes

## Owner device-review corrections — implemented in 0.21, 2026-10-10

### Runtime source correction and owner preference — 2026-10-10

- Collector formula review: with `C` as decoded nominal Ah and `f = SOC/100`, its calculation is approximately **`3600 × C / (currentA × f + 0.001)` seconds**. Discharge current is negative, so normal discharging selects the fixed seven-day substitute. It also puts SOC in the denominator instead of using remaining capacity in the numerator, and does not use the missing capacity for charging. This is not a correct time-to-empty/time-to-full estimate.
- The owner's Node-RED arithmetic is the better fit: discharge uses `remainingAh / abs(currentA)` hours; charge uses `(fullCapacityAh − remainingAh) / currentA` hours and the flow prepends the established negative sign. It assumes the present net current continues; it is not necessarily identical to Simarine's own estimate.
- Recommendation for a future authorized implementation: keep the Node-RED calculation/formatting but use the battery's valid decoded nominal Ah for full capacity rather than a fixed installation capacity. Treat zero/missing/stale current as unavailable; do not keep the old fabricated seven-day result. This recommendation does not authorize altering collector compatibility or application code now.
- [x] **Prefer Simarine/Pico's own displayed time estimate if it is available in the received protocol data**, as requested by the owner. Investigate the actual packet/configuration field and its units, sign, unavailable sentinels and freshness before using it. The native estimate has not yet been identified in the existing decoder; the Node-RED calculation below is the confirmed alternative, not evidence that Pico transmits its own estimate.
- [x] Correct the misleading **Runtime · Pico estimate** description. The currently consumed `capacity.timeRemaining` is computed in `node.js/lib/readings.js`, inherited from `python/pico-mqtt.py` and the archived upstream code; it is **not a decoded native time value**. It explicitly changes negative calculated results to **604800 seconds (seven days)**, which the Android formatter renders as `7d:00h`. Do not present that fabricated fallback as Simarine's estimate.
- [x] Remove that seven-day substitute from the app's runtime source selection. Preserve the collector's existing MQTT compatibility contract during the investigation; any collector/protocol change must be explicitly included in an authorized build/release. If a valid native estimate cannot be obtained, use the verified owner Node-RED calculation below with correct unavailable handling.
- The owner reports that Simarine displays an estimate while the app remains at seven days. Source inspection explains the app's fallback; agreement with Simarine's displayed estimate has not been tested on hardware.


These corrections supersede conflicting earlier implementation claims and exclusions. The owner has supplied photos comparing the actual app with the Node-RED display. The previous checked-off layout/theme items do not establish acceptance. This records the next-build requirements; no new build is authorized by this review.

### Dashboard elements, layout and readings

- [x] Each displayed element is a button: apply the EQ app's actual themed button background, text and highlight/on/off borders to individual readouts and gauges, rather than plain text rows or generic panel borders. Preserve the lock behavior for popup interactions.
- [x] Match the reference photo's arrangement and temperature ordering using stable source bindings; do not use API arrival order or alphabetical order. Preserve the reference's intended grouping/spacing, without publishing the private photo, inventory or readings.
- [x] Remove the excessive unused space below gauge faces; fit the dashboard elements and lower readouts to the available screen while retaining the requested layout and 6px spacing.
- [x] Pitch and roll: fixed **−5 to +5** scale, zero in the centre, a needle, and colour **only between zero and the needle**. At zero there is no coloured sweep. Keep the actual numeric reading even when it exceeds the visible scale; do not substitute the current generic gauge appearance.
- [x] Correct days/time remaining using the now-supplied owner export **flows(1).json** (2026-10-10), following **Pico → Delay → Total → Divide → Battery [Pico] and Set message topic → Join 2 → Set message payload → Divide → Split into time → Time to Full 2**; and **Pico → Delay → SOC AH → Nearest integer → SOC Ah and Set message topic → the same Join 2**. This source requirement is now resolved; do not use raw Pico `capacity.timeRemaining`.
  - Total supplies raw signed Pico total/battery current. The first Divide node divides by 1 and rounds to one decimal, but its output field is **topic**, so **payload remains the raw current**; the next Change node then assigns topic `batteryPico`. Do not mistakenly use the rounded topic value as the calculation input.
  - SOC AH supplies battery remaining capacity in Ah, rounded with nearest-integer/JavaScript Math.round behavior, and is assigned topic `SOC`. Join 2 builds a payload object keyed by topic and emits after two messages.
  - The payload expression selects `[fullCapacityAh − roundedRemainingAh, currentA]` when current is positive; otherwise `[roundedRemainingAh, currentA]`. Use the configured full-capacity constant from the owner's private export; preserve it without publishing the installation's capacity/inventory.
  - The second Divide reduces that array and the appended constant **−1**, then rounds to **two decimal places**: `hours = −selectedAh / currentA`. Negative current therefore gives positive discharge runtime; positive current gives a negative charge-to-full time. Retain that sign behavior instead of silently taking an absolute value for the calculation.
  - Split into time takes the absolute rounded hours for formatting and prepends `-` for negative hours. At 24 hours or more, display `Dd:HHh`; below 24 hours, display `Hh:MMm`. Days, leftover hours and minutes are **floored**, and HH/MM are zero-padded. Do not convert the input from seconds or always format days/hours.
  - Time to Full 2 centres the resulting string vertically/horizontally and uses bold text. Apply the app's current theme/button styling while preserving the calculation/formatting.
  - The calculator rejects a zero denominator and emits no updated result; it does not return zero runtime. The Android display must treat zero-current, missing or stale inputs as an unavailable estimate rather than a fresh fabricated result. Preserve the independently fresh Ah/current readings.
- [x] **Remove the app title altogether**, including the title currently above the indicators. This supersedes the earlier optional hide-title feature; do not add another Ella Monitoring heading.
- [x] Put cell voltages in **their own bounding box**, separate from the other battery summary readouts.
- [x] Show both **V[P]** and **V[S]**, as live readings: V[P] is the actual raw Pico Ella battery voltage; V[S] is the actual raw Pico starter-battery voltage. The reviewed Node-RED change nodes bind these to their respective battery voltage fields. The current secondary logged voltage selected for V[P] is not a verified equivalent; do not replace either raw live source with SBMS voltage or a derived/reference voltage.
- [x] Ensure **6px padding above the buttons inside the bottom panels**, alongside the agreed reference spacing.
- [x] Full screen must let the app **expand into the area freed by hiding Android system bars**, rather than leaving the old inset/empty space. Recalculate the usable viewport and scale on both entry and exit, preserving the reference layout.

### EQ theme engine, menus and every popup

- [x] Replace the approximate theme/menu/dialog implementation with the actual EQ app's behavior and styling, adapted only for monitoring-specific elements. Inspect its current DashboardView, MainActivity menu/dialog helpers, ThemeAppearanceView, ThemeConfig and ThemeStore; reuse the established interaction/formatting rather than generic Android menus recoloured afterward.
- [x] Provide the **theme dropdown on the main page**, with the EQ options: Save theme as, conditional Update for a modified named theme, Manage themes, Edit theme, Default and all saved themes. Match the EQ anchored dropdown, selection/modified indication and popup behavior. Restore the reference's relevant missing main-page preset options as well.
- [x] Restore saved themes and relevant options throughout the theme workflows; manage/load/save/update/rename/delete must work as in EQ, with its colour picker and theme roles.
- [x] **Every popup/dialog must use the current app theme and look like the EQ app's popups**, including its panels, borders, text, controls, sizing, spacing and selected states. This explicitly supersedes the previous exclusion of summary-popup styling. Chart/summary content redesign is not otherwise inferred from this styling correction.
- [x] **No theme controls/options on the Settings page.** Theme editing belongs in the main-page theme dropdown, as in the current EQ app.
- [x] Move **Edit display labels** into the theme dropdown.
- [x] Save named theme files in **Downloads/ella-monitoring/themes**, with EQ-equivalent file-based save/load/manage behavior. Preserve existing saved themes during migration; adapt storage access for the supported Android versions.

### Settings page

- [x] Do not display connection-status text on the Settings page.
- [x] Change **Hide app title** to **Hide connection status**; the title is removed permanently. Apply the connection-status preference to the monitoring display.
- [x] Add an explicit **Back** button that returns from Settings to the dashboard.
- [x] Group the **app version, Changelog/Update and Back button in one bounding box**, following the EQ Settings page's System grouping and themed appearance.
- [x] Retain the already-recorded update-check dropdown: **Off, 1hr, 3hr, 6hr, 12hr, 24hr**, alongside the previously requested update download/install and full changelog functionality.

### Review evidence (prior to this authorized build)

- EQ source reviewed at `e1b54f3f58efb92215d99e2e3f2941530fbcbc15`: the dashboard has separate preset/theme selectors; themes use anchored menus and files in Downloads; Settings explicitly places theme editing on the dashboard and groups Back/Changelog/Update/version in System.
- Ella source reviewed at `22dff908431f97356a07c54af198e506e3701d5f`: readout rows lack individual button backgrounds/borders, temperature rows follow incoming data order, the raw starter voltage is absent from LiveDashboard, and generic menu/theme handling differs from EQ.
- Runtime source resolved from the owner's supplied `flows(1).json`: it contains Time to Full 2 and the additional topic/join/calculation nodes absent from the older GitHub copies. The export remains private; only the functional calculation contract is recorded here. That review changed no application source. The later authorized 0.21 build implements the confirmed Node-RED alternative; a native protocol estimate remains unidentified.

## Missing EQ companion functionality — implemented in 0.21, 2026-10-10

- [x] Carry over the Joying EQ & DSP **Changelog/Update** button, its themed dialog and complete changelog (newest entries first), adapting project references for Ella Monitoring. The 0.2.0-preview settings page currently has no such button; the repository changelog alone does not fulfil this requirement.
- [x] Carry over the EQ app's update checking, **Download & Install** action, validated APK download, Android installer handoff and confirmed-install cleanup. Inspect the current EQ implementation before adapting it; use Ella's own package, version, signing identity and release source rather than EQ's.
- [x] Add a themed **update-check frequency dropdown in Settings**, with options in this order: **Off, 1hr, 3hr, 6hr, 12hr, 24hr**. Save the selection and use it for automatic update-check frequency; **Off** disables automatic checks. Keep manual checking available through Changelog/Update. This selection supersedes a fixed automatic-check interval copied from EQ.
- [x] Resolve the still-unsettled Ella release/package/signing configuration needed to make updates functional. This does not block displaying the complete in-app changelog. Updates were requested in APP-PLAN.md but remain unimplemented in 0.2.0-preview; do not describe the companion functionality as complete while these are missing.
- Originally recorded without source changes; implemented after the owner authorized 0.21.

## Future APK naming and versioning — 2026-10-10

- [x] On every future authorized Android build, deliver `Ella-monitoring-vX.XX.apk`, preserving this exact capitalization and two decimal places, with the version increasing by **0.01 per build**. Keep versionName consistent with the filename and increment versionCode. Do not append preview/build suffixes to the filename.
- This is a standing requirement for future builds, also recorded in `agents.md`; it does not request a new build or change the current delivered version.

## Owner-requested next-build list — 2026-10-10

The owner explicitly authorized this build with **"Ok. Do that build"**. Implemented in **0.2.0-preview / build 2**. Later additions/corrections take precedence. The requirements below supersede conflicting 0.1.0 layout/theme defaults. Device review remains pending; compilation/synthetic rendering does not establish exact physical-device fit.

### Companion-app reference and theming

- [x] Treat the owner's **Joying EQ & DSP app as the companion-app reference** for screen size, appearance and the specified functionality. Inspect its current source and actual controls when implementing; do not approximate it with a generic Android dashboard.
- [x] Match the EQ & DSP button styles, icons, theme engine and colour picker, with the same ability to theme most elements. Respect **6px padding between elements where relevant**, using the same coordinate/scaling approach as the reference.
- [x] All dialogue/popup windows must follow the application's current theme and the reference app's styling. **For this next build, do not touch the existing summary popups or charts:** they require drastic changes separately. Apply global theming to the in-scope surfaces while respecting this exclusion; do not redesign, alter or restyle the excluded summary/chart surfaces as part of this list.

### Screen size and orientation

- [x] Build pages around the **same reference screen resolution and scaling as EQ & DSP** (the owner's Joying is 1024×600); use the reference source to establish its actual layout/scaling behavior for other screen sizes.
- [x] **Force landscape on portrait screens.** This supersedes the preview's portrait/stacked dashboard behavior; do not use the old 800dp threshold to replace the reference layout with a portrait arrangement.

### Bottom bar

- [x] Match the EQ & DSP bottom bar's **height, style, button/icon sizes, spacing and theme behavior**.
- [x] In the first bounding box, left to right: **preset dropdown button → Live data → Charts**. The dropdown must have the same look and options as the reference app; preserve the owner's term "preset dropdown" rather than silently choosing different functionality. Live data opens the main monitoring page; Charts opens the charts page. Adding its navigation button does not authorize changes to chart contents.
- [x] In another bounding box, use the same reference controls, left to right: **Keep screen on → Full screen → Lock → Settings**, matching their icons, size and style.
- [x] **Lock** disables gauge-click popups. Preserve current collection/display behavior while locked.
- [x] Theme these controls using the same type of colour picker/theme engine as EQ & DSP.
- [x] **Settings opens a settings page, not a popup.** Use the EQ & DSP settings page as the reference for separation, grouping and display of settings.

### Main monitoring layout

- [x] Use the owner's supplied **Node-RED dashboard image** as the layout reference. Keep its arrangement and formatting; do not publish the private image or its readings/inventory.
- [x] **Current list at left:** each label and its value share one line. Remove the extra "current" text added to the displayed titles; preserve editable user labels.
- [x] **CFET, DFET and all other On/Off indicators:** put them together in **one bounding box**, each as a single-line indicator arranged as in the reference image. **CFET, DFET and EOC are green when On; all the others are red when On.** Preserve the distinction between Off and unavailable.
- [x] **Central gauges below the flags:** retain their current drawing for now; correct their padding and positioning to fit the reference arrangement.
- [x] **Barometer/LPG/water/pitch/roll box:** correct its layout to the reference image: barometer above, LPG/water paired below, pitch/roll paired below those.
- [x] **Temperature box at far right:** each label/value shares one line, following the same compact layout principle as the current list at left.
- [x] Fit and format the remaining **battery voltages, delta, SOC, remaining capacity and time remaining** according to the image/previous Node-RED display, preserving the requested central/bottom arrangement, units and source identities.

### Settings-page additions

- [x] Add an option **not to display the app title**.
- [x] Add an option for the app to be **persistent**, matching the EQ & DSP app's existing persistence option and behavior.

### Build outcome

0.2.0-preview implements this authorized list; the existing summary popups/chart contents and all Pi collector/database behaviour remain unchanged. Viewer presets adapt the EQ Save/Update/Manage options to display settings; audio factory presets are project-specific to EQ. The first preview signing key is unavailable, so this APK requires reinstalling the preview and re-entering its settings. See [CHANGELOG.md](CHANGELOG.md) and [BUILD-VALIDATION.md](BUILD-VALIDATION.md).

- [ ] Owner device review: compare the new screen with the reference image, try themes/labels/presets/lock, test forced landscape/fullscreen/keep-screen, notification permission and persistence/background/resume on a phone and the Joying head unit.

## Earlier clarification — context only, 2026-10-09

The owner clarified that the earlier questions were to establish the data, not requests to record application changes. Do not treat those questions or assistant assumptions as extra next-build tasks.

- Owner reports phone API connectivity: Pico readings arrive and SBMS readings eventually arrive. This is not full device/recovery validation.
- The owner confirms the SC302T shunt is assigned to the Pico battery. The complete Pico block under the exact `Ella  ` label supplies the battery information; preserve exact source bindings, units and signs.
- The combined battery history contains Pico current, SC302T voltage and Pico SOC, alongside SBMS battery current, PV1/PV2 current, voltage, SOC and external-load current. Sources remain distinct within the same battery record. Pico derived battery power uses the assigned Pico battery current multiplied by fresh SBMS voltage.
- The preview's Load Σ · Pico display currently sums individual shunts. The owner did not request a calculated total or new aggregate summaries/graphs; those were assistant assumptions. No new logging/schema or popup/chart work is authorized by this clarification.

## Authorized first Android preview — 0.1.0-preview, 2026-10-09

- [x] Owner explicitly requests starting implementation and will monitor allowance warnings.
- [x] Add native Java source under `android/source/`, minimum API 27 / target API 37, with pinned AGP/Gradle tooling. Keep Pi collector 0.7.1 and schema 2 unchanged.
- [x] Add bounded GET-only local API transport, Wi-Fi/Ethernet routing, Android 17 LAN permission, encrypted private token storage and regular TLS validation. Independent source expiry clears values rather than substituting zero; polling stops in the background.
- [x] Add the reference dashboard grouping and both source comparisons, live flags/environment/temperature/cell/capacity details, themes, bottom controls and detected FYT foreground screen handling. Use verified per-load signs for a selected-load consumption sum; leave it unavailable on unknown signs/values.
- [x] Add persistent element/group label overrides and restoration, source-referenced details/retained summaries, paginated rolling/defined charts, explicit coverage/gaps/net direction, temperature extrema and selected-range CSV export.
- [x] Record preview defaults for review: landscape grouping at 800dp or wider, stacked narrower screens; rolling Month 30 days; defined midnight-aligned UTC 6/12-hour blocks, calendar days, Monday weeks and calendar months; dark/light themes; manual Pi origin/token; development-only preview package/signing. Production identity/releases/updater remain unsettled.
- [ ] Test actual phone/head-unit installation, Pi LAN/HTTPS, Android 17 permission grant/denial, source/API/Wi-Fi restart, rotation/visual fit/long labels, background/resume, FYT timeout/wake handling and interaction with the EQ app, chart values and CSV file-picker output.
- [ ] Agree production app identity and persistent private signing/release location, then implement validated GitHub update checking/download/installer handoff.
- [ ] Agree alert rules/delivery and implement them with background support in a subsequent authorized stage; no foreground-only alerts are introduced here.
- [ ] Future remote outbound relay, offline cache, direct Pico/MQTT modes, database backups and Simarine settings remain separate stages.

See [README.md](README.md), [CHANGELOG.md](CHANGELOG.md) and [BUILD-VALIDATION.md](BUILD-VALIDATION.md). This preview is a concrete first review/test build; compilation/synthetic tests do not establish hardware connectivity or production signing.

## Current planning: Android viewer requirements — 2026-10-09

- [x] Owner targets both phones and the Joying/FYT head unit, supporting Android 8.1 (API 27) through current Android versions.
- [x] Record the owner's requested EQ/DSP visual grouping, Node-RED dashboard reference, bottom bar, full-screen/keep-screen/theme controls, relevant summary popups, line/period bar graphs, data export and GitHub updates in [APP-PLAN.md](APP-PLAN.md).
- [x] Inspect the separate EQ app's current screen/window/update source as a read-only reference; preserve FYT Main module 0 command 13 handling with Android foreground lifecycle and timeout restoration. Detect supported capability before head-unit-specific operations.
- [x] Owner confirms live flag changes working; short local API load test completed without failed requests and collection/logging remained healthy. Publish only functional outcomes, excluding system/network details and numerical resource/timing snapshots.
- [x] Owner selects rolling Last 6 hours, Last 12 hours, Last 24 hours, Week and Month, with a Rolling / Defined period toggle.
- [x] Owner provides the existing dashboard photo and asks to preserve its layout/grouping and both monitor comparisons. Record the arrangement without publishing the photo, readings or inventory.
- [x] If alerts are included, they must work while the Android UI is backgrounded; implementation, thresholds/rules and delivery policy are not yet selected.
- [x] Owner requests stylish design informed by other manufacturers while staying consistent with the EQ app and supplied dashboard arrangement. Record Victron GX and Mastervolt display guidance as design references; visual styling/wireframes remain to review.
- [x] Make each monitoring-element label/group heading renameable, with persistent display aliases and original/default restoration. Keep stable source/metric bindings, units, calculations, history and monitor settings unchanged; apply labels consistently to related graphs/popups/exports.
- [ ] Settle phone adaptation, indicator/summary fields, defined-period boundaries/rolling Month convention, graph interaction/edge coverage, offline/background alert delivery, export formats and package/signing/release location before an app build.
- [ ] Future option: internet-relayed alerts and remote data display using Pi-initiated outbound uploads and an authenticated phone read-only relay. No exposed Pi API/ports, incoming commands or remote monitor/settings changes. Investigate notification/provider choices later; no cloud service or private-data upload is enabled.
- [ ] Investigate Simarine settings backup/restore/editing later; vendor-app capability does not prove our collector has a complete restorable backup or settings write path.
- [x] Owner subsequently authorizes starting implementation; the first Android preview is recorded above. This older planning entry is retained as history, not an outstanding implementation block.

## Previous authorized correction: live SBMS states in collector 0.7.1

- [x] Expose the complete accepted SBMS broadcast through the live API; preserve existing normalized battery values and original raw field names/units.
- [x] Provide live boolean CFET, DFET, OVLK, UVLK, EOC, IOT, LVC and CELF states for On/Off display. Missing/invalid flags and stale/disconnected readings are unavailable, never silently Off.
- [x] Deliver flag/auxiliary-only changes within a source-clock second while continuing to ignore retained and complete repeated broadcasts, including reordered object keys.
- [x] Keep flags/auxiliary values out of database history; preserve schema, selected measurements, calculations, retention and Pico MQTT output.
- [x] Owner updated the Pi and confirms the live flag changes work. Android display remains pending.

## Previous authorized step: local API in collector 0.7.0

- [x] Owner requests the API before Android implementation. Add authenticated, read-only status, measurement catalogue, live Pico/SBMS freshness and bounded combined/per-metric history.
- [x] Reuse the existing service and schema 2 without changing collection, selected statistics, retention, source references, units, signs or MQTT output. Add independent private credentials, optional HTTPS, pagination and explicit partial/gap semantics.
- [x] Generate/check private parent API settings, document setup/routes/limits, test denied access, safe errors, read-only database access and simultaneous collector/API/MQTT operation.
- [x] Inspect the owner's live schema-2 database and migration backup privately: integrity passes, completed history/metadata preserved, active buckets extended consistently. Other shunts/environmental history and primary/secondary voltage references are present.
- [x] Owner enabled the API and confirmed local status/live/history requests; supplied responses pass private freshness/calculation/source-reference review.
- [x] Owner completed a short local API load check with no request failures and healthy collection/logging; raw timing/resource data remains private.
- [ ] Test LAN/TLS requests and restarts. Finish long-run retention/midnight and charge-polarity checks.
- [x] Agree Android target range/device categories; draft screen/chart requirements are recorded in APP-PLAN.md.
- [ ] Finalise layout, discovery, credential storage and offline/background behaviour before the app build.
- [ ] Keep history on its current persistent storage for now. Plan USB migration with consistent backups; SD/Android/Google Drive destinations, backup schedule and retention remain undecided. No automatic backup/cloud access is included in this API build.

## Previous authorized step: combined battery history in collector 0.6.0

- [x] Owner requests one combined battery record per interval with field-level source references, instead of separate battery/SBMS history rows.
- [x] Include primary/secondary voltage, distinct Pico/SBMS current and SOC, PV1/PV2 and external load, retaining independent coverage and energy/statistics.
- [x] Back up and migrate existing history atomically without recalculating/discarding saved values; keep other shunts, pressure and outside temperature in their current records and periods.
- [x] Add combined inspection and preserve existing per-metric commands as views; support existing private configuration without regeneration.
- [x] Owner updated the Pi and supplied live history plus pre-upgrade backup; read-only review confirms integrity, preserved completed records, source bindings, valid zeros and consistent rollups. Long-run hardware checks remain pending.
- [x] History API is implemented in 0.7.0 above; Android/browser display remains a subsequent stage.

## Previous authorized step: SQLite logging in collector 0.5.0

- [x] Implement optional SQLite history and private sensor selection alongside unchanged Pico MQTT publishing and SBMS reception.
- [x] Log the main battery through its selected battery instance; exclude the duplicate physical main-shunt alias. Keep selected secondary voltage raw and exclude Pico internal voltage from history.
- [x] Use fresh SBMS voltage for derived Pico battery/load watts; retain raw signed current, source/display names, relevant-device SOC and stable database IDs. Check configuration fingerprints before using a selected sensor.
- [x] Implement UTC duration-aware electrical summaries, additive energy/directional totals, coverage, environmental summaries and requested retention. Use fresh SBMS pack voltage for PV1/PV2 battery-side power; leave unverified directional classifications unavailable.
- [x] Save partial summaries every minute, flush graceful stops, resume saved sums without extending over downtime, prevent concurrent writers and report storage failures/clock steps.
- [x] Add configuration generation, read-only inspection and private offline replay. Keep capture/configuration/database/system details out of published source.
- [ ] Install private configuration on the Pi; confirm saved rows, live value/energy comparisons, sign/voltage settings, reboot/network/broker recovery, storage use and long unattended operation.
- [ ] Implement the local history API after live logging checks, then build the Android/browser history display. Neither is included in 0.5.0.

## Previous authorized step: ElectroDacus reception in collector 0.4.0

- [x] Receive `/Ella/sbms` on the existing MQTT connection independently of Pico availability; resubscribe after reconnection and retry denied subscription without stopping publishing.
- [x] Decode all four mA current fields to amps, SOC and pack voltage from a private configured cell map; include PV2 and preserve valid zeros/current signs.
- [x] Use Pi UTC receipt time and keep monitor time separately. Reject invalid payloads, ignore retained/exact repeated samples, and clear current readings on stale timeout or broker disconnect.
- [x] Preserve original Pico MQTT output and quiet service operation; add `--sbms-stdout`, `--no-sbms` and local SBMS capture records for diagnostics.
- [x] Owner supplied a short real Pi run showing simultaneous Pico/SBMS connection, approximately one SBMS sample per second, valid derived pack voltage and normal timed shutdown. Keep raw readings/timestamps/runtime details private.
- [ ] Check actual broker restart, stale/repeated/retained behaviour and long unattended SBMS collection. Confirm charging polarity under known conditions before directional integration.
- [x] Selected SQLite history, retention and restart recovery are added in 0.5.0 above. No database/API was part of 0.4.0; the API remains pending.

## Previous Pico base and service work

- [x] Start with updated Node.js discovery/configuration and TCP request methods.
- [x] Carry over the owner's sensor mappings, pitch/roll and Ella JSON calculations/formatting from Python.
- [x] Exclude SignalK integration. The initial MQTT exclusion was superseded by the owner's 2026-10-08 request for publishing to the existing dashboard.
- [x] Add fragmented TCP reads, error/retry handling, sender checks and clean shutdown.
- [x] Compare normal readings with the original Python functions and test simulated connection failures/recovery.
- [x] Provide recording, verification and replay commands for a live Pi test.
- [x] Support installation directly in `~/PicoData/node.js`, including a bundled Python comparison reference so deleted original folders are not needed.
- [x] Receive configurations and live readings from the owner's actual Pico; 1,096 captured outputs match the original Python, with zero differences (owner-reported verification).
- [x] Confirm real receive frame lengths/checksums for the reported capture: 108 TCP responses and 1,096 UDP packets all pass.
- [ ] Confirm automatic discovery mode; retain device identity/runtime inventory privately unless the owner requests publication.
- [x] Owner reports successful Pico reboot/Wi-Fi recovery and correct-looking MQTT output in the existing Node-RED webpage.
- [ ] Audit expected values against the Pico display, test changed IP and actual broker restart, and run a long unattended stability check.

## MQTT addition — 2026-10-08

- [x] Add optional publishing using the existing five-key Python `mqtt` configuration file.
- [x] Preserve the exact configured topic, parsed JSON structure/values/labels/units, QoS 0 and non-retained messages.
- [x] Restore original raw-65535 value handling in MQTT compatibility mode; reject incomplete output snapshots.
- [x] Add independent reconnect, no offline backlog/resync, bounded pending writes and clean shutdown.
- [x] Test actual MQTT wire settings and received CLI payloads against the Python source; verify Pico output continues with broker access denied.
- [x] Owner reports successful publishing/display through the existing Mosquitto/Node-RED setup.

## Service changes — 2026-10-08

- [x] Use the parent `PicoData/mqtt` configuration by default for files installed directly in `PicoData/node.js`, independent of the working directory.
- [x] Remove normal readings from terminal/journal output by default; retain connection/error status. `--stdout` enables diagnostic JSON and `--no-mqtt` enables reader-only testing.
- [x] Add a generic systemd service template with the correct `bin/pico.js` path, network/broker ordering and a 30-second process restart interval.
- [x] Confirm default config resolution and silent MQTT payload parity in an isolated installation; missing config fails visibly.
- [x] Owner reports clean shutdown/restart and successful MQTT/Pico live connections with the updated service.
- Publish functional test outcomes only; exclude owner runtime/process snapshots, device inventory, addresses, credentials and account details.

## Repository layout — 2026-10-08

- [x] Move the standalone collector to the repository root `node.js/` and remove its former folder.
- [x] Preserve the original SignalK reference under `_old/pico2signalk/` to avoid mixing it with the collector.
- [x] Update documentation, links, code provenance and checkout/update examples. The parent MQTT config and deployed service path are unchanged.

## Proposed Pi logging direction

- Keep collection/logging on the always-on, headless Pi Zero 2 W; the head unit is not continuously powered.
- Prefer one focused service over an expanding dashboard/database stack. Measure actual memory/CPU before replacing working services.
- SQLite plus a small history API is the preferred proposal; the owner's selected logging/retention rules are in `node.js/DATABASE-PLAN.md`, with calculation details and final display/app architecture still to be agreed. No logging/API/UI implementation is included in this MQTT addition.
- Keep history on persistent storage outside zram-managed directories (proposed `/var/lib/ella/`), with batched durable commits; reserve `/var/log` for diagnostics.
- ElectroDacus MQTT decoding is now implemented in 0.4.0, preserving the existing Mosquitto destination during validation.

## Later Android work

- Start the Android port after reviewing the live Pi evidence; confirm target hardware, Android range, layout and background requirements first.
- Review Android's role as a viewer/client of the Pi logger; direct Pico/embedded-broker operation can remain a separate future option. ElectroDacus reception is implemented on the Pi; Android source is not yet implemented.
- Keep SignalK functionality outside the Android scope.

## Database planning — 2026-10-08

- [x] Review existing Pico output and dashboard SBMS mappings; record the discussion draft in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md).
- [ ] Agree logged measurements, power/energy inputs and signs, sampling/retention, daily timezone and acceptable uncommitted-data window.
- [x] Confirm a live SBMS payload and active cell channels privately; solar current fields are present. Solar voltage/measurement-point verification remains pending.
- [ ] Implement and benchmark the agreed persistent SQLite history only after implementation is requested. Keep legacy MQTT output unchanged, separate sources, and preserve gaps/validity in summaries and energy totals.

## Owner logging specification — 2026-10-08

- [x] Record the owner's selected measurements and retention in [`node.js/DATABASE-PLAN.md`](../node.js/DATABASE-PLAN.md), superseding the initial broad candidates and retention tiers.
- [ ] Barometer: hourly for 1 month; last valid daily reading indefinitely.
- [ ] All Pico current shunts: `[W, A, V]` minute/hour/day/month summaries retained for 1 day / 1 week / 1 month / indefinitely; add main-battery SOC.
- [ ] Outside temperature: hourly for 1 month; daily minimum/maximum indefinitely.
- [x] Add independent ElectroDacus MQTT subscription/decoding for voltage, total/battery current, PV1/PV2 current and SOC. Pi operation and solar voltage/current associations remain to verify.
- [ ] Agree instantaneous-power averaging, duration-aware rollups and directional energy/charge totals so short-history deletion does not remove information needed for later calculations.
- [ ] Confirm SOC/hourly environmental aggregation, calendar timezone/cutoffs, ElectroDacus retention and durable in-progress bucket recovery.
- This records planning requirements only; no logging implementation or version change is authorized by the calculation question.

## Current polarity requirement — 2026-10-08

- [x] Owner defines all non-battery Pico shunts as load/current-draw channels; battery channels are net balance and ElectroDacus PV channels are charging supply.
- [ ] Verify voltage association and reported sign separately for every Pico shunt and ElectroDacus current field using known charging/load conditions; current draw is not assumed negative on every channel.
- [ ] Preserve source readings/MQTT output and use explicitly verified channel-specific direction mappings for logging calculations. Unverified channels remain unclassified for directional totals.
- [ ] Keep net main-battery inflow/outflow separate from gross supply/load totals; PV1 is a charging-only supply channel as described by the owner, with its reported polarity still to verify.

## Proposed implementation sequence and history API — 2026-10-08

- [ ] Confirm live SBMS fields/reporting cadence, per-channel voltage/sign mapping and remaining aggregation/timezone choices.
- [x] Add ElectroDacus subscription using the existing Mosquitto destination without altering Pico MQTT output.
- [ ] Implement persistent selected history/rollups/retention after Pi reception validation.
- [ ] Add an authenticated local JSON API: metric catalogue, live readings/freshness, bounded per-channel history queries and retained Wh/Ah totals. Only the Pi opens SQLite; Android/browser clients request data through the API.
- [ ] Define stable API IDs/units/array field meanings, credentials/transport/port/address discovery, partial/gap responses, backups/export and schema migration.
- [ ] Test restart recovery, power interruption, clock/calendar boundaries, pruning, denied MQTT/API access and long unattended collection; privately measure Pi performance.
- Reception is now authorized and implemented; the remaining sequence is planned Pi logging/API work, not an Android build.

## Received ElectroDacus example — 2026-10-08

- [x] Owner supplied a live MQTT JSON payload containing SOC, cell-voltage slots and battery/PV1/PV2/external-load current fields. Publish the field contract only, not the sample readings or device inventory.
- [x] Confirm configured active cells and the example's discharge condition privately. Implement Pi UTC receipt time and retained/stale handling.
- [ ] Verify remaining channel polarity, actual reporting cadence and retained/repeated behaviour on the Pi before integration.
- [ ] Decide whether external-load current is required in the logged channel set; keep it distinct from battery balance and overlapping load-shunt sums.

## Owner-confirmed acquisition details — 2026-10-08

- [x] Confirm Pi acquisition/receipt timestamps for both Pico and ElectroDacus logging; preserve existing MQTT output.
- [x] Confirm the active cells in the supplied example and that its negative battery current is discharge. Keep the actual cell map and present installation inventory private.
- [x] Convert PV1/PV2 mA to A with division by 1000 in reception, preserving valid zero readings.
- [ ] Include both solar channels in electrical history; log valid zeros distinctly from unavailable readings.
- [x] Include PV2 for future use, with its directional verification performed when connected. This confirmation does not establish its charging polarity from the current zero sample.

## Agreed logging choices and voltage review — 2026-10-09

- [x] Select UTC timestamps and minute/hour/day/month aggregation boundaries; optional Europe/London chart display does not change UTC daily totals.
- [x] Select the more accurate duration-aware average of instantaneous paired A × V measurements, with coverage and additive rollup integrals.
- [x] Store last valid SOC from each relevant device and use identical electrical retention periods for Pico and SBMS0.
- [x] Record owner-described SBMS battery/external-load consumption distinction without adding overlapping currents to totals.
- [x] Inspect current sensor mappings: voltage sensors/battery voltage are decoded separately; extra current-record fields have no verified voltage interpretation, and bracketed sensor names are filtered from legacy public JSON.
- [x] Owner confirms all Pico load shunts are on the main battery supply; use SBMS pack voltage as their preferred reference. Preserve original Pico MQTT output and label derived voltage provenance. Other battery/PV measurement points remain separate.
- [ ] Pair only fresh valid current/voltage measurements; retain current/SOC during voltage gaps and avoid an automatic fallback to untrusted voltage.
- [ ] Implement persistent logging and the authenticated read-only API when requested; this decision/source-inspection update changes no application version or source.

## Secondary voltage and stable names — 2026-10-09

- [x] Record selected secondary shunt-module voltage for comparison and exclude Pico internal voltage from regular history/reference selection; keep private sensor selection outside published source/documents.
- [x] Choose database identity separate from name, packet position and dashboard instance: permanent metric ID with source/device, sensor ID and measurement kind; preserve exact source name plus editable display name and time-effective mapping/name history.
- [x] Independently inspect the owner's supplied raw Pico/SBMS capture, validate framing and map actual configured IDs/names privately. Secondary voltage is present; main virtual-battery voltage/current duplicate configured source measurements.
- [x] Identify load-dependent primary/secondary voltage differences; avoid a fixed correction. Extra current fields appear counter-like rather than voltage, with meaning/units still unverified.
- [ ] Verify sensor-ID persistence through rename/reboot/reconfiguration and handle ID reuse/physical channel changes explicitly.
- [ ] Add an internal timestamped, ID-keyed decoded Pico snapshot for the future logger, before legacy filtering/name collisions; preserve all current MQTT output.
- [ ] Log the selected secondary voltage with original readings preserved; evaluate paired voltage/current differences before approving any correction or fallback.
- [ ] Implement the agreed SQLite history/API when requested. This planning/source review changes no application version or runtime code.

## Capture findings for the logger — 2026-10-09

- [x] Pass length/CRC checks for all recorded complete TCP replies and live packets; reconstruct the recorded sensor metadata from configuration. Raw capture/statistics/actual ID-to-name map remain private.
- [ ] Bind the private confirmed sensor map to permanent database metric IDs and editable names; retain versioned mapping metadata and check identity across later configuration changes.
- [ ] Treat duplicate main-battery current/voltage paths as aliases of one measurement; attach the relevant battery SOC without double counting currents/energy.
- [ ] Record the selected secondary voltage unchanged. Any later calibration/fallback requires explicit validation under different operating conditions, not a fixed offset inferred from one sample.
- [ ] Keep extra current-field counter candidates diagnostic-only until scale, deadband, signs, rollover and resets are established. Continue with sample/time-based integration for primary history.
- [ ] Consume raw decoded ID-keyed Pico snapshots before public filtering/throttling, while preserving the original MQTT contract and keeping cross-source timing/freshness explicit.


## PV voltage confirmation — 2026-10-09

- [x] Owner confirms both PV currents are charge supplied to the battery; use fresh SBMS pack voltage for their watts/Wh, not an assumed panel voltage.
- [x] Log current, battery-side power/energy, coverage and valid zeros for both PV channels with the agreed electrical periods/retention. Preserve raw signs; polarity verification remains separate.

## Owner-reported initial history check — 2026-10-09

- [x] Owner inspected an actual saved minute electrical record and could identify voltage/SOC. Keep the posted values/timestamps private. This verifies initial live history visibility, not all channels, energy accuracy, retention or long-run recovery.


