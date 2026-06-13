# Problem: browser fallback fires for the wrong deep-link sources

> **Handoff doc.** This describes a behavioural bug and the constraints around fixing it. It is *not*
> a finished plan — the picture is here so a fresh agent can brainstorm the right shape and implement
> it. Read `docs/architecture/navigation-and-di.md` for the navigation model first.

## One-line summary

When a deep link doesn't match any in-app destination we fall back to opening it in the browser.
That fallback is correct for a link the user **tapped inside our app**, but wrong for a link that
**launched the app from outside** (a notification tap, or a URL opened from another app like Notes).
Today all three sources share one code path, so they all get the browser fallback.

## How the three sources flow today

All three converge on the same resolver — `DeepLinkFirstUriHandler.openUri(uri)`
(`app/app/src/main/kotlin/com/hedvig/android/app/urihandler/DeepLinkFirstUriHandler.kt:18`):

```kotlin
override fun openUri(uri: String) {
  val destination = matcher.match(uri)
  if (destination != null) {
    backstackController.navigateToDeepLink(destination)   // resolved → navigate in-app
  } else {
    delegate.openUri(uri)                                 // unmatched → BROWSER fallback
  }
}
```

They differ only in how they *arrive* at that method:

| Source | Arrival path | Should unmatched → browser? |
|---|---|---|
| **In-app link tap** | Compose `LocalUriHandler` *is* `DeepLinkFirstUriHandler`; `openUri` runs in-process directly (wired in `HedvigApp.kt`, provided via `LocalUriHandler`) | **Yes** — keep current behaviour |
| **External URL** (Notes app, browser, SMS) | OS matches the manifest `intent-filter` (`autoVerify` https on the Hedvig hosts) → `ACTION_VIEW` to `MainActivity` → `MainActivity.handleDeepLinkIntent` (`MainActivity.kt:246`) → `deepLinkChannel.trySend(uri)` → `HedvigApp.kt:152-156` collects → `deepLinkFirstUriHandler.openUri(uri)` | **No** |
| **Notification tap** | `intentForNotification(uri)` (`app/app/src/main/kotlin/com/hedvig/android/app/notification/Util.kt:15`) builds an *explicit* `ACTION_VIEW` intent (`component = MainActivity`, `data = uri`) → same `handleDeepLinkIntent` → same channel → same `openUri` | **No** |

So the external and notification sources both reach the resolver through
`MainActivity.handleDeepLinkIntent` → `deepLinkChannel` → `HedvigApp` → `deepLinkFirstUriHandler.openUri`.
The in-app source reaches it directly as the Compose `LocalUriHandler`.

## Why the shared path is the problem

`DeepLinkFirstUriHandler.openUri` has **no idea where the URI came from**. A URL the user tapped
inside a screen and a URL that just cold-launched the app from a notification are byte-identical by
the time they hit `openUri`. So the "unmatched → browser" branch can't currently be made
source-dependent — it's one decision for everyone.

Concrete bad outcomes for the external/notification sources:

- A notification carries a URI we no longer have a matcher for (matchers are per-path; the manifest
  host filter is broader, and notification intents are explicit so they skip the filter entirely).
  Tapping it bounces the user straight out to the browser instead of landing somewhere sane in-app.
- An https link opened from another app that clears the host filter but not any specific
  `DeepLinkMatcher` does the same — we launch, then immediately kick the user to the browser, which
  can look like a flicker/loop.

## Desired behaviour

- **In-app tap, unmatched** → browser fallback (unchanged).
- **External URL / notification tap, unmatched** → do **not** open the browser. Decide the right
  in-app fallback (most likely: land on Home, or no-op + log). This is a product call — confirm with
  the repo owner which fallback they want before implementing.

## Where the fix probably lives (for the implementer to evaluate, not prescriptive)

The core need is to make the unmatched decision **origin-aware**. Options to weigh:

1. Give the channel-driven path (external + notification) a different entry point than the Compose
   `LocalUriHandler` path — e.g. a method that resolves-or-lands-on-Home and never touches the
   browser delegate, called from `HedvigApp.kt:152-156`, while `LocalUriHandler` keeps using the
   browser-falling-back `DeepLinkFirstUriHandler`.
2. Thread an origin flag through the `deepLinkChannel` (carry `(uri, origin)` instead of a bare
   `String`) and branch inside `openUri`.

Option 1 keeps the two behaviours in two clearly-named places and avoids a flag that callers can get
wrong; option 2 keeps a single entry point. Lean option 1 unless there's a reason not to.

## Files to read

- `app/app/src/main/kotlin/com/hedvig/android/app/urihandler/DeepLinkFirstUriHandler.kt`
- `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt` (`handleDeepLinkIntent`, `deepLinkChannel`)
- `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt` (the `deepLinkChannel.receiveAsFlow().collect` block, ~line 152)
- `app/app/src/main/kotlin/com/hedvig/android/app/notification/Util.kt` (`intentForNotification`)
- `app/app/src/main/AndroidManifest.xml` (the three `autoVerify` `VIEW` intent-filters)
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` (`navigateToDeepLink`)

## Out of scope

This is independent of the navigation-bar / chrome work for lone deep links (tracked separately in
`docs/architecture/deep-link-chrome-fix-plan.md`). They touch adjacent code but don't depend on each
other.
