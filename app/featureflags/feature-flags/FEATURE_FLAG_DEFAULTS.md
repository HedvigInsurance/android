# Feature flag defaults & the Unleash "never fetched" problem

This doc explains how feature-flag *defaults* work in the app, why we don't use the
SDK's `defaultValue` parameter, and how to reason about a flag's value when Unleash
has never been fetched (offline first launch, fresh install before the first poll
returns, etc.). Read this before adding a new flag.

## TL;DR

- We **only** call `client.isEnabled(name)`. We **never** call the
  `isEnabled(name, defaultValue)` overload — it's broken with the Frontend API.
- We **never** pass `bootstrap` to `client.start()`. It looks like the natural place to seed
  defaults, and it silently breaks readiness and the local backup — see
  [Why we don't use the SDK's `bootstrap` parameter](#why-we-dont-use-the-sdks-bootstrap-parameter).
- An absent toggle reads as `false`. We control the real default through two levers:
  1. **Flag naming polarity** (`enable_x` vs `disable_x`). Each `Feature` enum value is
     named to mirror its underlying Unleash key, and there is no per-flag negation anywhere, so
     a `disable_x` flag reports "is the kill switch on"; the consumer inverts at the read site.
  2. **`neverFetchedDefaults`**, a map in `HedvigUnleashClient`, for the flags where polarity
     alone gives the wrong default.
- `neverFetchedDefaults` holds three entries today. Adding others is usually noise and, for
  app-gating flags like `UPDATE_NECESSARY`, actively dangerous.

## The bug: Unleash Android SDK issue #141

The Frontend API (`/api/frontend`) **only returns toggles that are enabled**. Disabled
or unknown toggles simply aren't in the response. The SDK's
`isEnabled(name, defaultValue = true)` overload is supposed to fall back to
`defaultValue` when a toggle is missing, but it doesn't — it returns `false` regardless.
See https://github.com/Unleash/unleash-android-sdk/issues/141.

The important takeaway: **the bug lives entirely in that one deprecated overload.** The
plain `isEnabled(name)` call is well-defined — it returns `false` for any toggle the SDK
hasn't seen. As long as we never pass a `defaultValue`, we're not exposed to #141.

## How a flag resolves to a value

`UnleashFeatureFlagProvider` resolves every read through `HedvigUnleashClient.valueOf(feature)`:

- once the client holds real toggle state (`isReady()`), it returns `client.isEnabled(key)`;
- before that, it returns the flag's `neverFetchedDefaults` entry, or `false` if it has none.

`isEnabled(name)` returns `false` for an absent toggle, and a `Feature`'s name mirrors its key's
polarity, so the toggle value *is* the flag value. The polarity convention then determines the
default:

- **Positive flags** (`enable_x`, `update_necessary`…) read `isEnabled(key)`. Absent →
  `false` → feature **off**. Good default for new features: they stay off until we
  explicitly turn them on remotely.

- **Kill switches** (`disable_x`) also read `isEnabled(key)`, which reports "is the kill
  switch on". Absent → `false` → switch **off** → feature **on**. The consumer inverts at
  the read site (`if (!disableX)`), so the feature is normally available and the remote
  toggle is a switch we flip to turn it *off*. When offline we can't fetch the switch, so
  it stays off and the feature stays on — an inherent and acceptable property of a kill
  switch.

Reads are exposed as a `Flow`. `featureUpdatedFlow` emits on any toggle-state change, which
includes the local backup being restored and not only a network fetch, so a collector that starts
before the backup lands still sees the value update. It also emits when the client becomes ready,
which matters because the SDK sets `isReady()` from a coroutine independent of the one that
notifies state listeners: the two race on the first cache write, and a collector that wins the race
would read a `neverFetchedDefaults` value out of a cache that already holds real toggles. There is
no second chance to correct that on its own, since an unchanged toggle set answers `304` and
`doFetchToggles` writes the cache only on a successful fetch.

## When the "never fetched" default actually matters

Thanks to `LocalBackup`, the SDK persists the last successfully-fetched toggle state and
reloads it on subsequent launches. So the never-fetched default only bites in a narrow
window:

- The very first launch, before the first poll returns, **and**
- Fresh install while fully offline.

After any successful fetch, an offline launch uses the last-known remote state, not the
never-fetched default.

This holds only because we leave `bootstrap` unset. Seeding it stops the backup from ever loading,
which widens that narrow window to every cold start.

## Why we don't use the SDK's `bootstrap` parameter

`DefaultUnleash.start(bootstrap = …)` looks like the obvious place to seed never-fetched defaults.
It isn't, because of how the SDK defines readiness (verified against `unleash-android:3.3.1`):

- `start()` writes the bootstrap toggles into the toggle cache as an ordinary cache update,
  indistinguishable from fetched data.
- `readyOnFeaturesReceived()` marks the client ready on the first **non-empty** cache update.
  Bootstrap satisfies that on its own, so `isReady()` becomes true before any real data exists.
- `initializeLocalBackup()` only attempts to load the on-disk backup *while the client is not
  ready*. Once bootstrap has made it ready, the backup is never loaded at all.

The consequences are all silent:

- `awaitReady()` returns immediately with only the bootstrapped toggles in cache, so it reports
  "we have flag values" when we effectively don't. Every flag without a bootstrap entry reads
  `false` until the first poll returns.
- Offline launches lose last-known-good state entirely, since the backup can no longer load. The
  app has only the handful of values compiled into the bootstrap list.

The symptom to recognise: a one-shot flag read (`.first()`) that runs during startup silently gets
the wrong value, while `Flow`-based reads self-correct a second later and look fine. `OnboardingGate`
is currently the app's only startup-time one-shot read.

## Never-fetched defaults: when and why

`neverFetchedDefaults` in `HedvigUnleashClient` supplies a flag's value for the window before the
client holds real toggle state. It is consulted only while `!client.isReady()`; once a fetch or the
local backup lands, the real value takes over. An entry is only needed when the **desired**
never-fetched default differs from the **natural** polarity default.

Today:

```kotlin
private val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
)
```

All three are kill switches, so their natural absent default is "feature on". Each instead needs to
stay **hidden** until a fetch confirms it should show, which is what a rollout wants. Polarity gives
the wrong default, so each gets an entry of `true` (kill switch on → feature hidden).

### Do NOT default app-gating flags to the "blocking" state

`UPDATE_NECESSARY` is the cautionary example. `update_necessary` is positive, so absent →
`false` → the app does **not** force an update → offline users can still use the app.
That's the safe direction. Giving it an entry of `true` would brick the app for anyone who
is offline on first launch. Leave it alone.

## Adding a new flag — checklist

1. Add the enum value to `Feature` (commonMain), named to mirror its Unleash key polarity
   (`ENABLE_X` for `enable_x`, `DISABLE_X` for `disable_x`), with a short explanation.
2. Add its raw Unleash key to `Feature.unleashKey` (androidMain).
3. `UnleashFeatureFlagProvider` needs no change — every flag resolves through
   `HedvigUnleashClient.valueOf(feature)`. At the read site, use the value directly for a
   positive flag, or invert it (`if (!disableX)`) for a kill switch.
4. Ask: **what should this be when never fetched / offline on first launch?**
   - If the natural polarity default is acceptable → done, no entry needed.
   - If you need the opposite default during rollout → add an entry to `neverFetchedDefaults`.
     Double-check you're not gating the whole app into a blocked state.
