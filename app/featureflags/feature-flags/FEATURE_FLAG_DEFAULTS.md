# Feature flag defaults & the Unleash "never fetched" problem

This doc explains how feature-flag *defaults* work in the app, why we don't use the
SDK's `defaultValue` parameter, and how to reason about a flag's value when Unleash
has never been fetched (offline first launch, fresh install before the first poll
returns, etc.). Read this before adding a new flag.

## TL;DR

- We **only** call `client.isEnabled(name)`. We **never** call the
  `isEnabled(name, defaultValue)` overload — it's broken with the Frontend API.
- We **never** pass `bootstrap` to `client.start()`. It looks like the natural place to seed
  defaults, and it silently breaks readiness and the stored state, see
  [Why we don't use the SDK's `bootstrap` parameter](#why-we-dont-use-the-sdks-bootstrap-parameter).
- An absent toggle reads as `false`. We control the real default through two levers:
  1. **Flag naming polarity** (`enable_x` vs `disable_x`). Each `Feature` enum value is
     named to mirror its underlying Unleash key, and there is no per-flag negation anywhere, so
     a `disable_x` flag reports "is the kill switch on"; the consumer inverts at the read site.
  2. **`neverFetchedDefaults`**, a map in `HedvigUnleashClient`, for the flags where polarity
     alone gives the wrong default.
- `neverFetchedDefaults` holds four entries today. Adding others is usually noise and, for
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

- once the client holds real toggle state (fetched, or stored by an earlier process), it returns
  `client.isEnabled(key)`;
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
includes the stored state being restored and not only a network fetch, so a collector that starts
before the restore lands still sees the value update. The client hands the SDK a `ToggleStateCache` as
its `cacheImpl`, and both "is there real state" and the toggle values are read from that one
`StateFlow`, so a collector can never observe new toggles while `valueOf` still reports the
never-fetched default. The SDK's own `isReady()` is not consulted: it is set once per process, from a
coroutine racing the cache write, and says nothing about which member the toggles were fetched for.

## Toggles belong to the member they were fetched for

Unleash evaluates strategies server-side against the context a fetch carried. A strategy with
`memberId` stickiness or a `memberId` constraint resolves to off for a fetch without the member, even
at a 100% rollout, so toggles fetched for nobody, or for another member, are wrong for this one.

- The client starts only once the first member id value is in its context, so a logged-in launch's
  first fetch already names the member. If the member id cannot be read within two seconds, or the
  read fails, it starts without one rather than not at all, and applies the member when it arrives.
- The context also carries `userId`, set to the member id. It is the field Unleash's `default`
  stickiness resolves, without which a partial rollout using it is re-rolled on every fetch.
- `hasStateForCurrentMember()` tells whether the state in hand was fetched for the member currently in
  effect (or for nobody while logged out), by comparing the `memberId` of the context stored with the
  state. A restored state counts when it names the same member, whatever app version stored it.
- `awaitReady()` waits for exactly that, so a one-shot read after login, such as `OnboardingGate`'s,
  sees the member's own values. It still never completes without such a state, so callers keep their
  timeout.
- `variantOf(flag)` only answers when the state is the member's own, returning null otherwise. Variant
  flags drive A/B tests, where reporting a group fetched for someone else would corrupt the result.
- `valueOf(feature)` does not wait or check the member: right after a login, the previous state is a
  better answer for most flags than every flag falling back to its default.

## Where the last fetched state lives

The SDK is handed a `ToggleStateCache` as its `cacheImpl`. Every state the SDK writes into it after a
successful fetch is kept in memory and saved by `FileToggleStateStore` to
`noBackupFilesDir/unleash_last_fetched_state.json`, together with the `UnleashContext` it was fetched
for. On startup the client restores that file before calling `start()`, so no state fetched in this
process can be replaced by the older stored one.

The SDK's own local backup (`LocalBackup`) is disabled through `localStorageConfig.enabled(false)`.
Verified against `unleash-android:3.3.2`, it cannot serve offline starts reliably:

- It keeps a single slot keyed by `context.hashCode()` and restores it only for an identical
  context. Our context carries `appVersion`, so it misses on the first launch after every app update,
  and on a cold start whose last write was for the logged-out context.
- It writes only when the context changes, so toggle changes later in a session never reach disk.

The stored state is restored whatever context it was fetched for. If it was fetched for a different
member, or for the logged-out context, `valueOf` serves those toggles until the first fetch for the
current context lands. Reads that must be right for the member wait instead, see
[Toggles belong to the member they were fetched for](#toggles-belong-to-the-member-they-were-fetched-for).

## When the "never fetched" default actually matters

Because the last fetched state is stored and restored, the never-fetched default only bites in a
narrow window:

- The very first launch, before the first poll returns, **and**
- Fresh install while fully offline.

After any successful fetch, an offline launch (including the first one after an app update) uses
the last-known remote state, not the never-fetched default.

This holds only because we leave `bootstrap` unset; see the next section.

## Why we don't use the SDK's `bootstrap` parameter

`DefaultUnleash.start(bootstrap = …)` looks like the obvious place to seed never-fetched defaults.
It isn't, because `start()` writes the bootstrap toggles into the toggle cache as an ordinary cache
update, indistinguishable from fetched data (verified against `unleash-android:3.3.2`). With
`ToggleStateCache` as that cache, the consequences are all silent:

- The bootstrapped toggles are written as a state for the current context, so `awaitReady()` returns
  immediately and reports "we have flag values" when we effectively don't. Every flag without a
  bootstrap entry reads `false` until the first poll returns.
- The bootstrapped toggles replace the restored state and are stored as if they had been fetched, so
  offline launches would have only the handful of values compiled into the bootstrap list.

The SDK's own readiness has the same flaw: `readyOnFeaturesReceived()` marks it ready on the first
non-empty cache update, which bootstrap satisfies on its own.

The symptom to recognise: a one-shot flag read (`.first()`) that runs during startup silently gets
the wrong value, while `Flow`-based reads self-correct a second later and look fine. `OnboardingGate`
and `OnboardingSessionStore` are the app's startup-time one-shot reads.

## Never-fetched defaults: when and why

`neverFetchedDefaults` in `HedvigUnleashClient` supplies a flag's value for the window before the
client holds real toggle state. It is consulted only while the cache holds no state; once a fetch or
the stored state lands, the real value takes over. An entry is only needed when the **desired**
never-fetched default differs from the **natural** polarity default.

Today:

```kotlin
private val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_ANALYTICS to true,
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
)
```

All four are kill switches, so their natural absent default is "feature on". Each instead needs to
stay **hidden** until a fetch confirms it should show, which is what a rollout wants. Polarity gives
the wrong default, so each gets an entry of `true` (kill switch on → feature hidden).

`DISABLE_ANALYTICS` is the clearest illustration of why the entry is needed rather than optional: the
step it hides must not appear at all, so letting polarity show it to a member whose first launch is
offline would defeat the flag. Failing closed also fails safe here, since a member who is never asked
leaves their analytics consent undecided, and undecided forwards no product analytics.

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
