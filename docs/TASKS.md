# wakkt MVP – task breakdown

Source: `docs/PRD.md`. The foundation (wrapper, contracts, Hilt, Room) is on `master`.
Each task runs in parallel on its own branch and owns ONLY the files listed.
Shared contracts (do not change their signatures): `data/LabelRepository.kt`, `data/LabelRules.kt`,
`settings/SettingsRepository.kt`, `service/ForegroundAppDetector.kt`, `overlay/OverlayController.kt`,
`ui/navigation/Routes.kt`.

MVP decisions for the PRD's open questions: preset periods (15 min, 1 h, 4 h, until tomorrow);
one label per app; draggable note; no "pause all" in v1 (global on/off only).

| # | Task | Owns | Done when |
|---|------|------|-----------|
| T1 | Data & settings | `data/RoomLabelRepository.kt`, `settings/DataStoreSettingsRepository.kt`, `di/RepositoryModule.kt`, `app/src/test/**` | Hilt binds both interfaces; unit tests for `LabelRules` incl. until-tomorrow resolution pass |
| T2 | Monitoring service | `service/**` (except the contract), `di/ServiceModule.kt`, manifest `<service>`/`<receiver>` entries | Foreground service polls UsageStats, shows/hides overlay via contracts, starts/stops with global switch, restarts on boot |
| T3 | Overlay note | `overlay/**` (except the contract), `di/OverlayModule.kt` | Draggable Compose note in WindowManager; close button hidden for STICKY; soft shadow, light/dark |
| T4 | App UI | `MainActivity.kt`, `ui/**` (except Routes), `res/**` | Onboarding+permissions, searchable app list, editor with live preview, settings; add label in <30 s |
