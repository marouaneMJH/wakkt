# wakkt

Shows a per-app label (sticky note) when you open a chosen app.

## Run
Open this folder in Android Studio, or run `./gradlew assembleDebug`.

## Stack
Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Coroutines/Flow.

## Release
- `./gradlew bundleRelease` builds the signed AAB for Google Play (needs `keystore.properties`, see `docs/play-store/play-console-checklist.md`).
- `./gradlew installReleaseQa` installs the shrunk release build as "wakkt QA" next to the dev app for smoke testing.
- Store listing, privacy policy and Play Console answers: `docs/play-store/`. Audit report: `docs/AUDIT.md`.

## Website
The project website (landing page, presentation, privacy policy, terms, open-source info) lives in `web/`, see `web/README.md`.

## License
wakkt is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License v3.0. See [LICENSE](LICENSE). Copyright © 2026 marouaneMJH.
