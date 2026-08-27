# Datadog Android metric recovery

**Status: partially complete. One scheduled follow-up is blocked on an app release.**

Last updated 2026-08-27.

## What broke

Commit `129d7c34c7` (2026-06-02, "refactor(nav): migrate feature graphs and app shell to Nav3")
deleted `NavigationViewTrackingEffect` from `HedvigAppState.kt` without a Nav3 replacement. That
effect was the only thing naming Datadog RUM views after navigation destinations. The surviving
`ActivityViewTrackingStrategy` in `DatadogInitializer.kt` names views after Activity classes, and the
app has one Activity, so from **14.3.6 onward every RUM view is `com.hedvig.android.app.MainActivity`**.

Measured: app versions up to 14.3.2 emit 42 to 79 distinct `@view.name` values; 14.3.6 and later emit
three or four, all activity-level. All 18 custom `android.*` RUM metrics filter on `@view.name` or
`@view.url`, so all 18 broke. Control: `trace.android.request.hits` was flat across the same window
(1.39M vs 1.41M), so usage never changed. The 13 `ios.*` metrics were unaffected, because iOS names
views differently and its two newest metrics are action-based.

Consumers all reference the metrics **by name**, so the "Apps (Android + iOS)" dashboard, monitor
12054196, and the three Android SLOs need no edits of their own. The SLOs are metric-based
(`sum:android.X.network.count - sum:android.X.network.error` over the count).

The June-to-now data gap is accepted and will not be backfilled. RUM generated metrics are computed at
ingestion and are not retroactive.

## Done

### App code

`Navigation3TrackingEffect` (from `dd-sdk-android-compose`, already pinned) now reports the top of the
back stack as a RUM view, wired in `HedvigApp` off `Backstack.entries`. New view names are the nav key
canonical class names, with no `/{arg}` placeholder suffix:

```
com.hedvig.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey
```

The same change fixed a pre-existing Firebase defect found along the way: `screenName` was
`simpleName.removeSuffix("Key")`, which silently merged four pairs of screens that share a simple name
across features (`FirstVet`, `Forever`, `SubmitSuccess`, `SubmitFailure`).

A Firebase screen name is now the key's fully qualified class name with the shared feature package
prefix removed, so `change.tier.navigation.SubmitSuccessKey`. Prepending
`com.hedvig.android.feature.` (or `com.hedvig.feature.`) gets back to the declaration, which means a
name read off a Firebase report greps straight to its key with no convention to decode. The prefix is
dropped because GA4 truncates parameter values at 100 characters and the longest key name is already
91. `ScreenNameTest` asserts uniqueness, reversibility and that length bound across every key on the
classpath.

### Datadog: 10 filter rewrites (applied 2026-08-27)

Each was verified by re-reading the definition back from Datadog. A whole-org diff confirmed 31 metrics
before and after, exactly these 10 changed, and all 13 `ios.*` metrics byte-identical.

Every rewrite matched the identical event count before and after over the 30-day RUM retention window,
so none of them moved a current number. They only added a branch that begins matching once the fixed
build ships.

## PENDING: remove the legacy `OR` branches

**This is the main reason this document exists.**

The 10 rewrites accept both the old route-pattern names and the new key names, so the metrics stay
continuous across the release. Once the pre-14.3.6 install base is gone, the legacy halves are dead
weight and should be removed. Leaving them is not harmful, but they obscure what the metric actually
measures and they will confuse the next person.

### Trigger condition

Remove them once traffic from app versions at or below 14.3.2 is negligible. Check with:

```
pup rum aggregate \
  --query '@type:view @application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.feature.claim.chat.ClaimOutcomeNewClaimDestination*' \
  --compute count --group-by version --from 30d
```

Any version still listed is still emitting old names. When that returns nothing for a full 30-day
window, the cleanup is safe. Note RUM event retention is only 30 days, so this check cannot look
further back than that.

### Verify the new names arrived first

Before removing anything, confirm the new names are actually flowing. This should list roughly 100
distinct names for the current version rather than four:

```
pup rum aggregate \
  --query '@type:view @application.id:4d7b8355-396d-406e-b543-30a073050e8f @session.type:user' \
  --compute count --group-by @view.name --limit 120 --from 30d
```

### The 8 metrics to edit, with their target filters

Apply with `pup rum metrics update <id> --file payload.json`, where the payload is:

```json
{
  "data": {
    "id": "<id>",
    "type": "rum_metrics",
    "attributes": { "filter": { "query": "<target below>" } }
  }
}
```

#### `android.claim.success`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey
```

#### `android.chat.network.count`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.chat.navigation.ChatKey @connectivity.status:connected
```

#### `android.chat.network.errors`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.chat.navigation.ChatKey @connectivity.status:connected -@error.stack:java.net.ConnectException* -@error.stack:java.net.SocketException* -@error.stack:java.net.SocketTimeoutException* -@error.stack:java.net.UnknownHostException* -@error.stack:java.util.concurrent.CancellationException*
```

#### `android.claimflow.network.count`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.feature.claim.chat.navigation.* @connectivity.status:connected
```

#### `android.claimflow.network.error`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.feature.claim.chat.navigation.* @error.source:network @connectivity.status:connected -@error.stack:java.net.ConnectException* -@error.stack:java.net.SocketException* -@error.stack:java.net.SocketTimeoutException* -@error.stack:java.net.UnknownHostException* -@error.stack:java.util.concurrent.CancellationException*
```

#### `android.login.network.count`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.login.navigation.SwedishLoginKey @connectivity.status:connected
```

#### `android.login.network.error`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.login.navigation.SwedishLoginKey @connectivity.status:connected -@error.stack:java.net.ConnectException* -@error.stack:java.net.SocketException* -@error.stack:java.net.SocketTimeoutException* -@error.stack:java.net.UnknownHostException* -@error.stack:java.util.concurrent.CancellationException* -@error.stack:*CertPathValidatorException* -@error.message:*CertPathValidatorException* -@error.message:*Connection\ reset*
```

#### `android.changeaddress.view.count`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.movingflow.SuccessfulMoveKey
```

### The 2 metrics that need no cleanup

`android.terminateinsurance.network.count` and `android.terminateinsurance.network.error` already use a
package wildcard that covers both eras, because the old and new names share the
`...terminateinsurance.navigation.` package. Verified: old and new patterns matched the same 250
resource events. Leave them alone.

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.terminateinsurance.navigation.*
```

### One trap worth remembering

Do **not** be tempted to simplify chat or login to a package wildcard the way terminate-insurance
does. Measured: `com.hedvig.android.feature.chat.navigation.*` picks up `Inbox` (824 resource events)
on top of `Chat` (8,089), inflating the chat metric and the Chat (Android) SLO denominator by about
10%. Login has the same hazard, where the wildcard would add `LoginKey`, `OtpInputKey` and
`GenericAuthCredentialsInputKey`. Both must keep an explicit single-name filter.

## PENDING: 8 deletions, deliberately held

These metrics read zero and cannot be repaired, because they target screens that no longer exist.
All were confirmed to have **no** dashboards, monitors, SLOs or notebooks attached. `android.claim.failure`
joined this list on 2026-08-27, when the dashboard tile that was its only consumer was replaced (see
the claim-failure section below).

| Metric | Zero since | Why unrepairable |
|---|---|---|
| `android.claim.singleitempayout` | 2026-03 | No payout step in a chat-based flow |
| `android.claim.submitclaim` | 2026-03 | Keyed on the deleted Summary screen's `@view.url` |
| `android.claimsummary.network.count` | 2026-03 | Same deleted Summary screen |
| `android.claimsummary.network.error` | 2026-03 | Same deleted Summary screen |
| `android.resource.claimflow` | 2026-03 | Duplicate of `android.claimflow.network.count` |
| `android.claimflow.errors` | 2026-03 | Duplicate of `android.claimflow.network.error` |
| `android.auth.failure` | 2023-10 | Matches a hand-written `"BankId Error"` view removed in 2023 |
| `android.claim.failure` | 2026-03 | Targets `ClaimFlowDestination.Failure`; the chat flow has no failure screen |

**Why held:** neither the Datadog product docs nor the API reference state whether deleting a
generated metric also purges the already-computed timeseries. Holding costs nothing, since these
metrics already compute zero, so the only correct move was to wait for a definitive answer rather than
risk pre-March history. Resolve by asking Datadog support, then delete.

Their full definitions are recorded in this repo's git history via this document's companion tooling
output; if any is deleted and needs restoring, recreate with `pup rum metrics create`.

## Claim-failure signal: replaced 2026-08-27

`android.claim.failure` targeted `ClaimFlowDestination.Failure`, deleted in March, and there is no
equivalent screen to repoint it at. `ClaimIntentOutcome` is a sealed interface with exactly one case,
`Claim`. Failure surfaces as `ClaimChatUiState.FailedToStart` rendering an error section *inside* the
`ClaimChatKey` view, so it produces no distinct view name and no filter can reach it.

Rather than count failures, the flow is now measured as a completion ratio:

- **`android.claim.started`** created, counting views of the claim chat entry screen, accepting both
  the old and new names exactly like `android.claim.success`.
- The dashboard tile "Failure claim screen viewed" was replaced with **"Claim submissions per chat
  entry"**, computing `android.claim.success / android.claim.started * 100`.

Both halves of the ratio are structurally identical metrics (view count, grouped by `env`, same
`uniqueness`), so any counting semantics apply equally to numerator and denominator.

**Read this number as a trend, not an absolute conversion rate.** The denominator counts chat-screen
views, and a member who backs out and resumes, or returns to a claim later, produces more than one.
On the 30 days before the change the old-name equivalents were 305 chat views against 59 outcome
views, so expect a figure in that region rather than a true per-attempt success rate. What matters is
that it moves when claim submission degrades.

Because that tile was `android.claim.failure`'s only consumer, that metric now has none, and it joins
the held-deletion list above.

### Still worth doing: emit an action for submission failure

The durable form of a failure signal is an action, not a screen or a UI state:

```kotlin
logAction(type = ActionType.CUSTOM, name = "CLAIM_SUBMISSION_FAILED")
```

Action-based metrics do not break when navigation changes, which is why no iOS metric broke in this
incident. Note that the two obvious instrumentation points are both wrong: `failedToStart` and
`errorSubmittingStep` are transient, retryable states that are set and cleared repeatedly, so
instrumenting them counts error *displays*, not failed claims, and a member on a flaky connection
produces several. Emit at a terminal boundary instead, for example when the member abandons the flow
while an error is showing.

The strongest single improvement would be to emit `CLAIM_SUBMITTED` on reaching
`ClaimIntentOutcome.Claim` and rebase `android.claim.success` onto `@action.name`, which would make
the most important claim metric permanently independent of navigation.

## Scope note on `android.claimflow.network.count`

The rewrite widened this metric from "the claim chat screen" to "every claim-chat screen", which now
includes the outcome and deflect screens. Measured on the old equivalents: the chat screen carries 707
resource events, the outcome screens add 10, and the deflect screen adds 0. That is about **+1.4% on
the denominator with a 0% error rate on the added traffic**, which dilutes the measured error rate by
roughly the same 1.4%. This metric is the denominator of the Claims flow (Android) SLO, so the SLO
reads marginally better. Two keys in the new scope, `StartClaimPledgeKey` and `UpdateAppKey`, have no
old equivalent to measure, but neither issues network requests in normal use.

## Guard rail worth adding

Nothing in Datadog would have caught this. A monitor on the share of Android views named
`MainActivity`, alerting above roughly 80%, would have surfaced it within a day of the release instead
of ten weeks later. Equivalently, a monitor on distinct `@view.name` cardinality dropping below, say,
40.
