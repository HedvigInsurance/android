# Can `onNewIntent` fire on a `standard` launchMode Activity?

**Status:** Answered — **yes**, empirically confirmed.
**Scope:** `MainActivity` in `:app` and its `addOnNewIntentListener` deep-link path.
**Date of investigation:** 2026-06-17.

---

## The question

`MainActivity` registers a listener:

```kotlin
addOnNewIntentListener { newIntent ->
  handleDeepLinkIntent(newIntent)
}
```

`MainActivity` uses the **default `standard` launch mode** (no `launchMode` attribute in the
manifest). The open question was:

> With `standard` launchMode, can the `addOnNewIntentListener` lambda ever actually fire,
> or does every incoming intent just produce a brand-new Activity instance (`onCreate`)
> instead of `onNewIntent`?

Nobody had been able to reproduce the lambda firing in normal app usage. The hypothesis
(credited to Adam Powell) was that `FLAG_ACTIVITY_SINGLE_TOP` set by the *caller* can route
to `onNewIntent` even for a `standard` Activity.

---

## Background: when does `onNewIntent` fire?

`Activity.onNewIntent` is delivered **instead of** a fresh `onCreate` only when the system
decides to *reuse* an existing Activity instance that is already at the top of the target
task, rather than instantiate a new one. For that reuse to happen, one of these must be true:

1. The Activity declares `android:launchMode` = `singleTop`, `singleTask`, or `singleInstance`
   in the manifest, **or**
2. The **caller** sets `Intent.FLAG_ACTIVITY_SINGLE_TOP` (`0x20000000`) on the launching
   intent **and** the target Activity instance is already at the top of the matched task,
   with a matching component.

For a `standard` Activity, condition (1) never applies, so the **only** path is (2): a caller
explicitly setting `FLAG_ACTIVITY_SINGLE_TOP`.

Important non-paths (these do **not** route to `onNewIntent` on a `standard` Activity):

- `FLAG_ACTIVITY_CLEAR_TOP` **alone** → destroys and **recreates** the target (`onCreate`),
  it does not reuse via `onNewIntent`. It only reuses the top instance when combined with
  `FLAG_ACTIVITY_SINGLE_TOP` (`CLEAR_TOP | SINGLE_TOP`).
- `FLAG_ACTIVITY_NEW_TASK` alone → new instance.

---

## Code findings

### 1. `MainActivity` is `standard`

`app/app/src/main/AndroidManifest.xml` — the `<activity>` for
`com.hedvig.android.app.MainActivity` has **no** `android:launchMode`, so it defaults to
`standard`.

### 2. The app's own notification intents set NO reuse flags

`app/app/src/main/kotlin/com/hedvig/android/app/notification/Util.kt`:

```kotlin
fun HedvigBuildConstants.intentForNotification(deepLinkUri: Uri?): Intent = Intent().apply {
  action = Intent.ACTION_VIEW
  data = deepLinkUri
  component = ComponentName(this@intentForNotification.appPackageId, MainActivityFullyQualifiedName)
}
```

No `FLAG_ACTIVITY_SINGLE_TOP`, no `FLAG_ACTIVITY_NEW_TASK`. Every notification sender
(`ChatNotificationSender`, `PaymentNotificationSender`, `CrossSellNotificationSender`, etc.)
wraps this in `PendingIntentCompat.getActivity(...)`. The platform implicitly adds
`FLAG_ACTIVITY_NEW_TASK` for a PendingIntent activity launch, but it does **not** add
`SINGLE_TOP`.

**Consequence:** with the current code, notification taps on a `standard` `MainActivity`
land in `onCreate`, never in the `addOnNewIntentListener` lambda. This is why the lambda
appeared to be dead code and could not be reproduced through normal app usage — **no caller
in the codebase sets `SINGLE_TOP`.**

### 3. External `ACTION_VIEW` deep links (browser/links) also produce fresh `onCreate`

Cold/warm deep links arriving via the manifest `<intent-filter>` for `ACTION_VIEW` are
handled in `onCreate` (`if (savedInstanceState == null) handleDeepLinkIntent(intent)`),
again because nothing sets `SINGLE_TOP`.

---

## The empirical test (no production-code change required to run)

`am start` exposes a convenience flag, `--activity-single-top`, that simulates a caller
setting `FLAG_ACTIVITY_SINGLE_TOP`. Existing log lines in `MainActivity.onCreate` are enough
to tell the two paths apart:

- `onCreate` logs `MainActivity@<identityHashCode> using BackstackController@<hash>`.
  - **New instance** → a new log line with a **different** hashCode.
  - **Reuse via `onNewIntent`** → **no** new `onCreate` log; same instance persists.

### Steps

1. Foreground the app with `MainActivity` at the top of its task.
2. `adb logcat` for the `MainActivity@...` lines.
3. Fire the intent **with** the flag:

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "<some-deeplink-uri>" \
  -n com.hedvig.dev.app/com.hedvig.android.app.MainActivity \
  --activity-single-top
```

Compare against the same command **without** `--activity-single-top` (expected: a fresh
`onCreate` with a new hashCode).

### Caveat

Launching from the shell is a non-Activity context, so the system implicitly adds
`FLAG_ACTIVITY_NEW_TASK`. As long as the app is already foregrounded with `MainActivity` on
top, `NEW_TASK` resolves to the existing task and `SINGLE_TOP` does its job. If the intent
ever lands in a *different* task you'd get a new instance instead — so always foreground the
app first. Do **not** add `--activity-clear-top` for this isolation test (see the non-paths
note above).

---

## Result (confirmed)

A temporary log was added inside the listener purely to observe it:

```kotlin
addOnNewIntentListener { newIntent ->
  logcat { "MainActivity@newIntent:$newIntent" }   // temporary instrumentation
  handleDeepLinkIntent(newIntent)
}
```

Observed logcat:

```
06:09:41.336  MainActivity@129065745 using BackstackController@211175504
06:10:54.649  MainActivity@103329817 using BackstackController@44427708
06:11:41.819  MainActivity@newIntent:Intent { act=android.intent.action.VIEW
              dat=https://link.dev.hedvigit.com/... flg=0x30000000 xflg=0x4
              cmp=com.hedvig.dev.app/com.hedvig.android.app.MainActivity }
```

Decoding the flags on the delivered intent:

- `flg=0x30000000` = `FLAG_ACTIVITY_NEW_TASK (0x10000000)` `|` `FLAG_ACTIVITY_SINGLE_TOP (0x20000000)`.
- The `--activity-single-top` test ran at `06:11:41` with **no preceding `onCreate` log**,
  i.e. the existing instance was reused and the listener fired — exactly the `onNewIntent`
  path.

**Conclusion: yes.** On a `standard` launchMode Activity, `onNewIntent` (and therefore the
`addOnNewIntentListener` lambda) *will* fire when a caller sets `FLAG_ACTIVITY_SINGLE_TOP`
and the Activity is already at the top of the matched task. Adam Powell's recollection was
correct.

---

## Implications for this codebase

- The `addOnNewIntentListener` lambda is **not reachable through the app's own current code
  paths**, because no caller (notification PendingIntents, external deep links) sets
  `FLAG_ACTIVITY_SINGLE_TOP`. It is reachable only by an external caller that sets that flag,
  or if `MainActivity` is ever given a `singleTop`/`singleTask` launchMode.
- The listener is therefore a **defensive safety net**: routing `onNewIntent` through the
  same `handleDeepLinkIntent(...)` handler means that if the launch config ever changes
  (manifest `launchMode`, or we start setting `SINGLE_TOP` on notification intents to avoid
  stacking duplicate Activities), deep links won't be silently dropped.
- If we *want* notification taps to reuse the existing `MainActivity` instead of stacking a
  new one, the change would be to add `FLAG_ACTIVITY_SINGLE_TOP` (likely together with
  `FLAG_ACTIVITY_CLEAR_TOP`) to `intentForNotification` — at which point the listener becomes
  a live, regularly-exercised path and needs corresponding test coverage.

## Open follow-ups (for whoever picks this up)

1. Decide whether the listener should stay as a safety net or whether we should deliberately
   move notification/deep-link entry onto the `SINGLE_TOP` (reuse) model. The current
   "always fresh `onCreate`" behavior means tapping multiple notifications can stack multiple
   `MainActivity` instances — worth verifying whether that actually happens and whether it's
   desirable given the single app-owned back stack.
2. Remove the temporary `logcat { "MainActivity@newIntent:$newIntent" }` instrumentation if
   it was committed — it was added only to observe this experiment.
