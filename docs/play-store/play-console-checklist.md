# Play Console checklist – wakkt 1.0.0 (versionCode 1)

## 1. Build
- Upload `app/build/outputs/bundle/release/app-release.aab` (`./gradlew bundleRelease`).
- Signed with the **upload key** `~/keystores/wakkt-upload.jks` (config in gitignored `keystore.properties`).
  **Back up both files and the password somewhere safe.** Enroll in **Play App Signing** (default), so a lost upload key can be reset via Play support.
- Bump `versionCode` in `app/build.gradle.kts` for every upload.
- Pre-upload smoke test of the shrunk build: `./gradlew installReleaseQa` (installs "wakkt QA" next to the real app).

## 2. App content
| Section | Answer |
|---|---|
| Privacy policy | Host `privacy-policy.md` at a public URL (e.g. GitHub Pages) and paste the URL. Fill in the support email. |
| Ads | No ads |
| App access | All functionality available without login. Reviewer note: "Grant 'Display over other apps' and 'Usage access' in onboarding, add a note to any app, then open that app." |
| Content rating | Questionnaire: utility/productivity; no violence, sexual content, gambling, user-generated content shared with others, or location sharing → expected **Everyone / PEGI 3** |
| Target audience | 13+ (not designed for children) |
| News app | No |
| Government app | No |
| Financial features | None |
| Health | None |

## 3. Data safety
- Does the app collect or share user data? **No** (no INTERNET permission, all data stays on the device).
- Data encrypted in transit: not applicable (no transmission).
- Users can request deletion: in-app delete / uninstall.

## 4. Permission declarations
- **Foreground service – special use** (`FOREGROUND_SERVICE_SPECIAL_USE`). Required declaration:
  - *Description:* "wakkt keeps a foreground service running while enabled so it can detect when the user opens an app they chose and immediately draw the user's own note over it. This needs continuous, user-visible operation that no other foreground service type covers."
  - *User impact if deferred:* the note would appear late or not at all, defeating the app's purpose.
  - *Video:* record onboarding → add note → open target app → note appears (screen recording, ≤ 30 s).
- **Usage access (PACKAGE_USAGE_STATS):** no Play form, but needs the in-app prominent disclosure (onboarding screen explains it) and a privacy policy (done).
- **Display over other apps:** no form; explained in onboarding.
- **QUERY_ALL_PACKAGES:** not used (removed in favor of a `<queries>` launcher filter).
- **Accessibility service:** not used.

## 5. Store listing
See `listing.md` and `graphics/`. Still needed: 2–8 phone screenshots.

## 6. Release
- Start with **Internal testing**, then Closed testing.
  New personal developer accounts must run a closed test with **12+ testers for 14 days** before production access.
