# Datadog Android metric recovery

**Status: three items open.** One is time-boxed to the release carrying the auth instrumentation:
take the SLO window back to 7 days about a week after. See "After the release" below. The
auth-unreachable gap that used to be a fourth item is closed as accepted, also below. The
`OR`-branch cleanup is blocked on the
pre-14.3.6 install base draining. Measured 2026-09-11, versions at or below 14.3.2 were 12.0% of
prod view events over 30 days but only **1.1% over 7 days and 0.8% over one day**, so the 30-day
figure lags badly and the trigger is closer than it looks. The claim-submission-failure action is
unstarted. The guard-rail monitor is also still just a suggestion.

Last updated 2026-09-11.

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
views differently. Three of the 13 are action-based, which is a separate property worth knowing but
not the reason they survived.

Consumers all reference the metrics **by name**, so the "Apps (Android + iOS)" dashboard, monitor
12054196, and the three Android SLOs need no edits of their own. The SLOs are metric-based
(`sum:android.X.network.count - sum:android.X.network.error` over the count).

The June-to-now data gap is accepted and will not be backfilled. RUM generated metrics are computed at
ingestion and are not retroactive.

## Done

### App code

`Navigation3TrackingEffect` now reports the top of the back stack as a RUM view, wired in `HedvigApp`
off `Backstack.entries`, so view names are nav key canonical class names again. The same change fixed
a pre-existing Firebase defect where `screenName` was `simpleName.removeSuffix("Key")` and silently
merged four pairs of screens sharing a simple name, and it consolidated five KMP modules onto the
`com.hedvig.android.*` namespace that the new naming depends on.

Full reasoning, measurements and the `ScreenNameTest` invariants are in PR #3104.

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

### The 6 metrics to edit, with their target filters

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
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey
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
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.claim.chat.navigation.* @connectivity.status:connected
```

#### `android.claimflow.network.error`

```
@application.id:4d7b8355-396d-406e-b543-30a073050e8f @view.name:com.hedvig.android.feature.claim.chat.navigation.* @error.source:network @connectivity.status:connected -@error.stack:java.net.ConnectException* -@error.stack:java.net.SocketException* -@error.stack:java.net.SocketTimeoutException* -@error.stack:java.net.UnknownHostException* -@error.stack:java.util.concurrent.CancellationException*
```

#### The two login metrics are not in this list

**Superseded 2026-09-09. Do not put a `@view.name` filter on `android.login.network.count` or
`android.login.network.error`.**

Both were rebuilt on auth resource events and no longer mention `@view.name` at all. Their live
filters are:

```
count   @application.id:4d7b8355-396d-406e-b543-30a073050e8f @resource.url_host:(auth.prod.hedvigit.com OR auth.dev.hedvigit.com) @resource.url_path:"/member-login" @session.type:user
error   the same, plus @resource.status_code:[500 TO 599]
```

Both are `event_type: resource` grouped by `env`, so the failure count is a subset of the attempt
count by construction. SLO `29588e73473d54f09814173755548b80` moved to a 30-day window and monitor
`93408872` follows it.

Applying a view-name filter here would put the denominator back to counting `apollo-router` calls
that merely coincided with the login screen being open. That is the defect that made this SLO report
534.351% of its error budget with no outage behind it: 96 of its 131 denominator events were GraphQL
traffic, and the numerator counted a different event type entirely.

Note for whoever does the remaining six: `event_type` cannot be changed with `pup rum metrics update`.
The PATCH returns 200, applies the filter and silently discards the event type. A change of event
type needs a delete and recreate under the same name, which does not purge the existing timeseries.

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

## Done: 8 dead metrics deleted 2026-09-10

Eight metrics targeted screens deleted in March 2026, or 2023 in the case of `android.auth.failure`,
and had read zero ever since. They were held back only because nobody knew whether deleting a
generated metric also purges its already-computed timeseries.

It does not. Verified 2026-09-09, when `android.login.network.error` was deleted and recreated under
the same name and kept all 24 of its points. These eight had no data to lose either way.

Deleted: `android.claim.singleitempayout`, `android.claim.submitclaim`,
`android.claimsummary.network.count`, `android.claimsummary.network.error`,
`android.resource.claimflow`, `android.claimflow.errors`, `android.auth.failure` and
`android.claim.failure`. Confirmed first that no dashboard, notebook, monitor or SLO referenced any
of them. Their full definitions are recorded in the message of the commit that deleted them, since
Datadog keeps no history of a generated metric's definition.

## Claim-failure signal: replaced 2026-08-27

`android.claim.failure` targeted `ClaimFlowDestination.Failure`, deleted in March. Failure now
surfaces as `ClaimChatUiState.FailedToStart` rendering inside the `ClaimChatKey` view, so it produces
no distinct view name and no filter can reach it. Rather than count failures, the flow is measured as
a completion ratio: `android.claim.started` counts claim-chat entry views, and the dashboard tile
"Claim submissions per chat entry" computes `android.claim.success / android.claim.started * 100`.
Both halves are structurally identical metrics, so counting semantics apply equally to each.

**Read that tile as a trend, not a conversion rate.** The denominator counts chat-screen views, and a
member who backs out and resumes produces more than one. Over the 30 days before the change the
old-name equivalents were 305 chat views against 59 outcome views, so expect a figure in that region.
What matters is that it moves when claim submission degrades.

### Still worth doing: emit an action for submission failure

The durable form of a failure signal is an action, not a screen or a UI state:

```kotlin
logAction(type = ActionType.CUSTOM, name = "CLAIM_SUBMISSION_FAILED")
```

Action-based metrics do not break when navigation changes.

To be precise about why no iOS metric broke here: the Nav2 to Nav3 migration was Android-only, so
iOS view names never moved. Only 3 of the 13 `ios.*` metrics are actually action-based
(`ios.addonPurchased`, `ios.addonUpgraded`, `ios.claims.end.count`), and only two of them are
genuinely rename-proof. Those two take their names from a Swift enum in `ChangeAddonViewModel.swift`
and go out through `log.addUserAction(...)`, whose sink is `DatadogLogger.swift`. The third is a
cautionary tale: `ios.claims.end.count` filters
`@action.name:ClaimIntentStepContentSummary`, which `ClaimIntentClientOctopus.swift` emits as
`content.__typename`, a GraphQL type name owned by the backend schema. Rename that type and the
metric dies silently, so an action name is only durable if the app owns the constant.

So this is not a novel idea, it is catching up to a pattern already in production on the other
platform, with one example of how to get it wrong.

The counter-example is on iOS too. `ios.login.network.error` excludes user-facing translated strings,
both the English and Swedish wording of the BankID cancellation and the "no existing Hedvig member"
message, plus the entire United States by geolocation. Lokalise is one project shared across Android,
iOS and the backends, so a translator editing that copy silently changes what a reliability metric
counts. That is a worse coupling than view names, and it is a good argument for moving the signal
into code rather than tightening the filter. Note that the two obvious instrumentation points are both wrong: `failedToStart` and
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

## Accepted: this SLO cannot see an unreachable auth service

**Decided 2026-09-11. No action. Do not reopen this without new information.**

The SLI counts `POST /member-login` resource events with a 5xx status. A request that never reaches
the server produces a RUM error and no resource at all, so it lands in neither side of the ratio. If
auth becomes unreachable the denominator collapses toward zero and the SLI reads 100% or no-data.
Nothing fires. The worst outage produces the best number and silence.

An earlier version of this document said to cover that by flipping `notify_no_data` to `true` on
monitor `93408872`. **That does not work.** The monitor is `type: "slo alert"`, and `notify_no_data`
is not honoured on SLO alert monitors: the field reads `false` and carries no `no_data_timeframe`.
It is not a setting someone forgot to turn on. Across the org, all 32 SLO alert monitors have it
`false`, and the only 2 monitors setting it `true` are ordinary metric monitors.

Covering it properly would mean a second monitor watching `sum:android.login.network.count` for an
absence. That is not being built, for two reasons. The gap has existed for as long as the SLO has,
so nothing is getting worse. And an auth service that is actually unreachable is already caught
server-side by `Auth: post auth` and `Auth: get member credentials`, which run on APM traces with
the full population and no client sampling, so someone gets paged either way. What this SLO uniquely
sees is the network path between the member and the server, which is also the part Android cannot
fix.

The thing to remember is the reading rule: **a green `Auth: login (Android)` is not by itself
evidence that login works.** Check that the denominator is non-zero before believing it.

## After the release: take the SLO window back to 7 days

This waits for the release that carries the auth instrumentation, because before it there is
legitimately no prod data on `android.login.network.count`. About a week after it is the right time.

Set the SLO `timeframe` and its `thresholds[].timeframe` back to `7d`, and the monitor query back to
`error_budget("29588e73473d54f09814173755548b80").over("7d")`. All three have to move together or
the monitor asks the SLO for a window it no longer defines.

**Why.** `error_budget(...).over(30d)` is a trailing window, so once the budget is burned the monitor
stays red until the burning events age out, up to a month. Seven days recovers four times faster.

**Check the arithmetic against real traffic before doing it.** The window was widened to 30d to get a
usable budget, and shortening it takes that back. At the expected volume:

| Window and target | Budget |
|---|---|
| 7d at 99% | about 7 failures |
| 30d at 99% | about 30 failures |
| 7d at 97% | about 21 failures |

Based on 3,014 Swedish-login view impressions over the 30 days to 2026-09-11, so roughly 703 attempts
a week, around 100 a day. A 7-day window at 99% means any week with seven 5xx responses breaches.

**Re-measure before acting on this.** An earlier reading of the same query returned 1,547, half the
current figure, because login-view impressions were themselves suppressed by the Nav3 breakage and
are still recovering as the fixed build rolls out. The input is moving, and moving upward, so treat
these numbers as a floor. Retries also mean one member can produce more than one attempt. If that turns out to be normal variance
rather than a real signal, the lever to reach for is the **target**, not the window: 7d at a lower
target buys headroom and keeps fast recovery. Pick it from the first weeks of real data rather than
assuming 99%, which was inherited and never chosen for this signal.

Burn-rate alerting solves both the recovery time and the sensitivity properly, and is the textbook
answer. It was not done here only because it needs a new monitor, but the sizing does not have to be
invented: the org already runs 9 burn-rate monitors, shaped like

```
burn_rate("<slo id>").over("7d").long_window("1h").short_window("5m") > 16.8
```

Revisit if the 7d window turns out to flap.

## What already measures auth, so nobody builds it a third time

Checked 2026-09-11. Two server-side SLOs already cover auth availability, on APM traces rather than
RUM, with the full population and no client sampling:

| SLO | Built on | SLI at the time of checking |
|---|---|---|
| Auth: post auth | `trace.http.request.*`, `service:auth` | 99.958% |
| Auth: get member credentials | `trace.http.request.*`, `service:auth` | 100% |

`Auth: login (Android)` is a client-side view of a question those two already answer better. What it
adds is the network path between the member and the server, which is real, but is also the part
Android cannot fix. Worth knowing before anyone treats it as the primary defence for auth, or
rebuilds backend auth availability inside RUM.

`Auth: login (iOS)` does exist (`9028984bed7351dc92b7e58e5c7db82f`, 7d at 99%, monitor `93102231`)
and reads 99.95%, state OK. That is not reassurance. It divides `ios.login.network.error`
(`event_type: error`) by `ios.login.network.count` (`event_type: resource`), the same numerator and
denominator mismatch that made the Android SLO report 534% of its budget. It looks healthy for the
same reason the Android one looked healthy right up until it did not. Android got paged first
because its view names broke, not because iOS is better instrumented.

Also worth knowing as a precedent: `Purchase: completed signs` is an SLO over
`hedvig.events.signing.completed / hedvig.events.signing.started` with a **70%** target. So the org already has a funnel SLO built on
explicit events rather than HTTP outcomes, and already accepts a target chosen from reality instead
of a round number.

## Guard rail worth adding

Nothing in Datadog would have caught this. A monitor on the share of Android views named
`MainActivity`, alerting above roughly 80%, would have surfaced it within a day of the release instead
of ten weeks later. Equivalently, a monitor on distinct `@view.name` cardinality dropping below, say,
40.

## Rollback: the filters as they were before 2026-08-27

All ten pre-change definitions were recorded in commit `9e5525fc27`, so they are recoverable with:

```
git show 9e5525fc27:docs/plans/2026-08-27-datadog-android-metric-recovery.md
```

They were written into this document because Datadog keeps no history of a generated metric's
definition. Committing it satisfied that, so the values no longer need to sit in the living copy.
Note that the two login entries there are doubly superseded: they were rebuilt again on 2026-09-09.
