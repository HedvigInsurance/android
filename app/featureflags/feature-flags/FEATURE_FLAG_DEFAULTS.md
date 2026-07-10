# Feature flag defaults & the Unleash "never fetched" problem

This doc explains how feature-flag *defaults* work in the app, why we don't use the
SDK's `defaultValue` parameter, and how to reason about a flag's value when Unleash
has never been fetched (offline first launch, fresh install before the first poll
returns, etc.). Read this before adding a new flag.

## TL;DR

- We **only** call `client.isEnabled(name)`. We **never** call the
  `isEnabled(name, defaultValue)` overload — it's broken with the Frontend API.
- An absent toggle reads as `false`. We control the real default through two levers:
  1. **Flag naming polarity** (`enable_x` vs `disable_x`). Each `Feature` enum value is
     named to mirror its underlying Unleash key, and `UnleashFeatureFlagProvider` returns
     the raw `isEnabled(key)` value with no per-flag negation. A `disable_x` flag therefore
     reports "is the kill switch on"; the consumer inverts at the read site.
  2. **Bootstrap** — only for the one flag where polarity alone gives the wrong default.
- Only `DISABLE_PUPPY_GUIDE` is bootstrapped today. Adding others is usually noise and, for
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

`isEnabled(name)` returns `false` for an absent toggle. `UnleashFeatureFlagProvider`
returns that raw value unchanged — the `Feature` name mirrors the key's polarity, so the
toggle value *is* the flag value. The polarity convention then determines the default:

- **Positive flags** (`enable_x`, `update_necessary`…) read `isEnabled(key)`. Absent →
  `false` → feature **off**. Good default for new features: they stay off until we
  explicitly turn them on remotely.

- **Kill switches** (`disable_x`) also read `isEnabled(key)`, which reports "is the kill
  switch on". Absent → `false` → switch **off** → feature **on**. The consumer inverts at
  the read site (`if (!disableX)`), so the feature is normally available and the remote
  toggle is a switch we flip to turn it *off*. When offline we can't fetch the switch, so
  it stays off and the feature stays on — an inherent and acceptable property of a kill
  switch.

## When the "never fetched" default actually matters

Thanks to `LocalBackup`, the SDK persists the last successfully-fetched toggle state and
reloads it on subsequent launches. So the never-fetched default only bites in a narrow
window:

- The very first launch, before the first poll returns, **and**
- Fresh install while fully offline.

After any successful fetch, an offline launch uses the last-known remote state, not the
bootstrap/absent default.

## Bootstrap: when and why

`HedvigUnleashClient.start(bootstrap = …)` seeds toggle state before the first fetch.
Bootstrap is only needed when the **desired** never-fetched default differs from the
**natural** polarity default.

Today the only entry is:

```kotlin
client.start(bootstrap = listOf(Toggle(name = Feature.DISABLE_PUPPY_GUIDE.unleashKey, enabled = true)))
```

`disable_puppy_guide` is a kill switch, so its natural absent default is "feature on".
But during rollout we want the puppy guide **hidden** until the first fetch confirms it
should show. Polarity gives the wrong default, so we bootstrap `enabled = true` (kill
switch on → feature hidden). Once toggles are fetched, the remote value takes over —
the bootstrap is discarded wholesale on the first successful fetch.

### Do NOT bootstrap app-gating flags to the "blocking" state

`UPDATE_NECESSARY` is the cautionary example. `update_necessary` is positive, so absent →
`false` → the app does **not** force an update → offline users can still use the app.
That's the safe direction. Bootstrapping it to `true` would brick the app for anyone who
is offline on first launch. Leave it alone.

## Adding a new flag — checklist

1. Add the enum value to `Feature` (commonMain), named to mirror its Unleash key polarity
   (`ENABLE_X` for `enable_x`, `DISABLE_X` for `disable_x`), with a short explanation.
2. Add its raw Unleash key to `Feature.unleashKey` (androidMain). `UnleashFeatureFlagProvider`
   needs no change — it returns `isEnabled(key)` for every flag.
3. At the read site, use the value directly for a positive flag, or invert it
   (`if (!disableX)`) for a kill switch.
4. Ask: **what should this be when never fetched / offline on first launch?**
   - If the natural polarity default is acceptable → done, no bootstrap.
   - If you need the opposite default during rollout → add a `Toggle(...)` to the
     bootstrap list. Double-check you're not gating the whole app into a blocked state.
