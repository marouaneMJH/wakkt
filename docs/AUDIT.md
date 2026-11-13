# wakkt – QA & security audit (13 Nov 2026, v1.0.0)

## Code quality (QA)
| # | Finding | Severity | Status |
|---|---|---|---|
| Q1 | A note closed "forever" could never be shown again; re-enabling didn't clear dismissal (PRD 2.2) | High (functional) | Fixed: editor shows "Closed forever / Hidden until …" with **Show again**; re-enabling clears it |
| Q2 | Starting the foreground service could crash if the system refuses it (Android 12+ background start limits) | Medium | Fixed: `MonitorStarter.start` catches and reports failure |
| Q3 | Status bar height read via internal `status_bar_height` resource (lint: InternalInsetResource, DiscouragedApi) | Low | Fixed: `WindowMetrics` insets |
| Q4 | Hard-coded English "Close" content description | Low (a11y/i18n) | Fixed: string resource |
| Q5 | No launcher icon (lint: MissingApplicationIcon); system icon for notification | Medium (store) | Fixed: adaptive + themed icon, notification icon |
| Q6 | AGP 8.7.3 did not support compileSdk 36 | Medium | Upgraded AGP 8.11.1 / Gradle 8.13; compile & target SDK 36 |
| Q7 | Release build never exercised with R8 | Medium | Added `releaseQa` build type; verified on device: launch, onboarding, Room, editor, FGS start |
| Note | App list isn't refreshed for apps installed while wakkt is open | Low | Accepted for v1 |
| Note | POST_NOTIFICATIONS is never requested, so the "wakkt is active" notification is hidden on Android 13+ unless allowed in settings; the service still runs | Low | Accepted for v1 |

## Security & privacy
| # | Finding | Severity | Status |
|---|---|---|---|
| S1 | `allowBackup="true"`: notes and images could leave the device through backup / device transfer | Medium | Fixed: backup off and `dataExtractionRules` exclude everything |
| S2 | `QUERY_ALL_PACKAGES`: broad package visibility, a Play-restricted permission | Medium (privacy/policy) | Fixed: replaced with a `<queries>` filter for launcher activities |
| S3 | Release signing: no config | – | Upload key (RSA 4096, PKCS12) outside the repo; properties file gitignored (`*.jks`, `keystore.properties`) |
| ✓ | No `INTERNET` permission in the merged manifest: no network attack surface, no data exfiltration path | – | Verified |
| ✓ | Exported components: launcher activity; `BootReceiver` (needed for system broadcasts, checks the action); profile installer guarded by `DUMP` | – | OK |
| ✓ | `PendingIntent` uses `FLAG_IMMUTABLE` | – | OK |
| ✓ | Image import: decoded with bounds and `inSampleSize` (no decompression bombs), re-encoded to private storage with a sanitized filename; deletion is confined to the app's image folder via canonical path check | – | OK |
| ✓ | Monitor service not exported; overlay window `FLAG_NOT_FOCUSABLE` (cannot capture keyboard input) | – | OK |
| ✓ | No logging of user content | – | OK |
| ✓ | Release: R8 minify + resource shrinking, not debuggable, no debug tooling activity in the release manifest | – | Verified |
| ✓ | Chaos visuals capped at 2.5 alternations/s (photosensitive-epilepsy guideline), unit-tested over 5000 random scenes | – | OK |
| Info | Text-to-speech hands note text to the user's installed TTS engine (third-party) | Info | Disclosed in privacy policy |
| Info | Dependencies are about a year old; no network use limits exposure. Update during the next maintenance pass | Low | Open |
