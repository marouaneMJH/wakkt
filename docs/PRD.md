# wakkt – Product Requirements Document

*Working name: wakkt · Status: Draft v0.1 · Platform: Android (Kotlin)*

## 1. Overview

wakkt is a mobile app that shows a short label, like a sticky note, every time the user opens a chosen app. The user decides which apps get a label, what each label says, and how it behaves once it appears.

The goal is a small, calm tool: a gentle reminder at the exact moment it matters, with no setup friction and no clutter.

## 2. How the app is used

This section explains the usage of wakkt from the user's point of view.

### 2.1 Core flow

1. The user opens **wakkt**.
2. They see the list of apps installed on their phone.
3. They choose an app (for example, Instagram).
4. They write the **label text** for that app (for example, "Do you really need this right now?").
5. They choose the **label behavior** (see 2.2).
6. They save. From now on, whenever that app is opened, wakkt shows the label on top of it.

Each app has its own label and its own behavior. The user can edit, disable, or delete a label at any time.

### 2.2 Label behaviors

The user picks one behavior per app:

| Behavior | What the user sees | What happens when closed |
| --- | --- | --- |
| **Sticky** | The label stays pinned on the front of the app screen for the whole session. | It cannot be closed. It disappears only when the user leaves the app. |
| **Closable for a period** | The label appears with a close button. | It stays hidden for a chosen period (for example 15 min, 1 hour, until tomorrow), then shows again on the next open. |
| **Closable forever** | The label appears with a close button. | It never shows again for that app, unless the user turns it back on inside wakkt. |

### 2.3 Example scenarios

- **Focus reminder (Sticky):** a note on a social media app that says "10 minutes max" and stays visible the whole time.
- **Soft nudge (Closable for a period):** a note on a food delivery app saying "Check your budget first", hidden for 1 hour once dismissed.
- **One-time tip (Closable forever):** a note on a banking app saying "Never share your code", dismissed once and gone.

### 2.4 Screens (first draft)

1. **Onboarding and permissions:** explains why wakkt needs "Display over other apps" and "Usage access", and guides the user to grant them.
2. **App list:** searchable list of installed apps, showing which ones already have a label.
3. **Label editor:** text field, behavior selector, period picker (when relevant), live preview of the note.
4. **Overlay note:** the floating sticky note shown over the target app.
5. **Settings:** global on/off switch, battery optimization help, about.

## 3. Technical direction

- **Language and UI:** Kotlin with Jetpack Compose and Material 3, for fast iteration and a modern look.
- **Architecture:** single-activity, MVVM with unidirectional data flow, Kotlin Coroutines and Flow.
- **Dependency injection:** Hilt.
- **Storage:** Room for labels and behavior rules, DataStore for app settings.
- **App detection:** `UsageStatsManager` first; `AccessibilityService` as an option if faster detection is needed.
- **Overlay:** `WindowManager` with the `SYSTEM_ALERT_WINDOW` permission, hosted by a foreground service.
- **Platform scope:** Android first. iOS is out of scope for v1 because the OS does not allow this behavior.

## 4. UI and design direction

- Modern and minimalist: lots of white space, one accent color, rounded shapes, clear typography.
- Light and dark themes, following the system setting.
- Few screens, few taps: adding a label should take under 30 seconds.
- The sticky note itself should feel light and unobtrusive: small, soft shadow, draggable.
- The visual design will be produced with Claude's design tool as a separate step, once the screens above are agreed.

## 5. Open questions

1. Should the "period" be a fixed list of options or fully custom?
2. Can the user have more than one label per app?
3. Should the sticky note be draggable, or fixed in a corner?
4. Should there be a global "pause all labels" option, for example for one hour?
