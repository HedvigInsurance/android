# Datadog Android metric recovery

**Status: the dated item is done; two items still open.** The window switch landed on **2026-09-21**,
two days early. SLO `29588e73473d54f09814173755548b80` and monitor `93408872` are both on a 7-day
window at a 99% target. See "Done 2026-09-21: switched to a 7-day window" below. Downtime
`b72d5680-5084-45e5-b4c7-20169460ecec` still runs to 2026-09-23T08:00Z and needs nothing from
anyone; it expires on its own. The auth-unreachable gap that used to be a separate item is closed as
accepted, also below, though 2026-09-15 taught us its reading rule was only half written, twice.

**New and not acted on:** `POST /member-login` returned short-duration 504s twice, on 2026-09-15 and
again on 2026-09-20, and no server-side SLO saw either. See "Recurring gateway 504s" below.

The `OR`-branch cleanup is blocked on the pre-14.3.6 install base draining. Measured 2026-09-11,
versions at or below 14.3.2 were about 11.5% of prod view events over 30 days but only **1.1% over 7
days and 0.7% over one day**, so the 30-day figure lags badly and the trigger is closer than it
looks. Every number in this document is re-runnable; the commands are inline next to each one, and
they should be re-run rather than trusted, because most of these are still moving.

The claim-submission-failure action is unstarted, and the guard-rail monitor is still just a
suggestion.

Last updated 2026-09-21.

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

Remove them once traffic from app versions at or below 14.3.2 is negligible.

To get the share directly, over any window (this is where the header's 30d/7d/1d figures come from):

```bash
pup rum aggregate \
  --query '@type:view @application.id:4d7b8355-396d-406e-b543-30a073050e8f @session.type:user' \
  --compute count --group-by version --limit 200 --from 7d
```

Sum the buckets whose version is at or below 14.3.2 and divide by the total. Or check a single
known-old view name, which returns nothing once the old builds are gone:

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
count by construction. SLO `29588e73473d54f09814173755548b80` is on a 7-day window since 2026-09-21
and monitor `93408872` follows it.

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

Action-based metrics do not break when navigation changes. An earlier version of this document
added "which is why no iOS metric broke in this incident". That was wrong: the Nav2 to Nav3
migration was Android-only, so iOS view names never moved at all. One thing is worth keeping from
that check, because it constrains how to do this here: an action name is only durable if the app
owns the constant. Of the three action-based `ios.*` metrics, one keys off a GraphQL type name owned
by the backend schema and would die silently on a rename.

Note that the two obvious instrumentation points are both wrong: `failedToStart` and
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

```bash
pup api "/api/v1/monitor/93408872" | jq '.data | {type, notify_no_data: .options.notify_no_data}'
```

It is not a setting someone forgot to turn on. Across the org, all 32 SLO alert monitors have it
`false`, and the only 2 monitors setting it `true` are ordinary metric monitors, both of the
"no signs on the website" absence-alarm kind:

```bash
pup api "/api/v1/monitor?page_size=1000" \
  | jq -r '.data[] | [.type, (.options.notify_no_data|tostring), .name] | @tsv' \
  | sort | uniq -c -f1
```

Covering it properly would mean a second monitor watching `sum:android.login.network.count` for an
absence. That is not being built, for two reasons. The gap has existed for as long as the SLO has,
so nothing is getting worse. And a genuinely unreachable auth service is already caught server-side
by the `Auth: post auth` and `Auth: get member credentials` SLOs, which run on APM traces with the
full population and no client sampling, so someone gets paged either way. What this SLO uniquely
sees is the network path between the member and the server, which is also the part Android cannot
fix.

The thing to remember is the reading rule: **a green `Auth: login (Android)` is not by itself
evidence that login works.** Check that the denominator is non-zero before believing it.

### The corollary, learned the hard way on 2026-09-15

The rule above only covered the falsely-healthy direction. The opposite happened first.

At 10:37 CEST on 2026-09-15 the monitor fired at **110.339% of the 30-day budget**. There was no
outage.

**Corrected 2026-09-15, second pass.** An earlier revision of this section blamed a 12-event
denominator. That number came from a RUM event search, and this SLO is metric-based, so it was the
wrong quantity entirely. The real figures, read from the metrics the SLO actually divides:

```bash
pup api "/api/v1/query?from=<t-30d>&to=<now>&query=sum:android.login.network.count%7Benv:prod%7D.as_count()"
pup api "/api/v1/query?from=<t-30d>&to=<now>&query=sum:android.login.network.error%7Benv:prod%7D.as_count()"
```

30-day totals were **2,451 denominator and 27 errors**, giving SLI 98.899% and 110.2% of budget,
which reproduces the alert exactly. Add `.rollup(sum,86400)` to either query to get it per day, which
is what makes the cause visible:

| Period | Denominator | Errors | What it is |
|---|---|---|---|
| Aug 17 to Sep 02 | 22 to 90 per day | 13 | the **old, broken** metric definition |
| Sep 03 to Sep 06 | 2 to 6 per day | 0 | collapsing |
| Sep 07 to Sep 09 | 225, 692, 512 | 11 | the rewrite window |
| Sep 10 to Sep 14 | no data | 0 | gap |
| Sep 15 | 25 | 3 | the first real measurement |

The 25 above is the partial day as it stood at 10:37 CEST, which is what the alert divided. Sep 15
closed at **104** denominator with the same 3 errors. The Sep 10 to Sep 14 gap is not a fault: 14.4.8
is the first version that instruments `/member-login` at all, so the recreated metric had nothing to
count until that build reached users on the 15th.

**Only 3 of the 27 errors and 25 of the 2,451 denominator events come from the current metric
definition.** The monitor fired because its 30-day trailing window still contains data from the
metric deleted and recreated on 2026-09-09. This document already warned that deleting a generated
metric does not purge its timeseries; the alert is that warning coming true.

So the low-volume rule still stands, and gains a second half:

**This ratio is meaningless at low volume, whichever way it reads**, and **a trailing window that
straddles a metric rewrite is measuring two different definitions at once.** Before believing any
reading, check both: what the denominator is, and whether the window spans a definition change.

Note which tool answers which question. A RUM event search shows retained sessions only and will
under-report; the metric query above is what the SLO sees.

Two things that event also settled, both worth keeping:

**The instrumentation works.** Splitting the same auth host by path and app version on the day
14.4.8 reached the internal track: `/member-authorization-codes` (which already went through
`:app`'s instrumented OkHttp client) read 23 on 14.4.6 and 94 on 14.4.7, while `/member-login` and
`/member-login/<uuid>` read **zero on both** and 12 and 74 on 14.4.8. That is the whole original
diagnosis confirmed in production: `:authlib`'s Ktor client was invisible, and now it is not.

**The budget table below is sized off the wrong quantity.** It uses login *view impressions* as the
input. The SLI actually counts `POST /member-login`, which is a different and much smaller number,
and it excludes the `/member-login/<uuid>` polling calls entirely (74 of them on the same day as the
12 initiations). Rebuild that table from the live metric once adoption is real, rather than from the
view-impression proxy.

## Muted until 2026-09-23: downtime on monitor 93408872

Set 2026-09-15 after the alert above. Datadog downtime
`b72d5680-5084-45e5-b4c7-20169460ecec`, scoped to monitor `93408872` only, running
2026-09-15T09:40Z to **2026-09-23T08:00Z**. It expires on its own and Datadog notifies when it does.

**Why time-boxed rather than open-ended.** Training the team to ignore this monitor is how the
original breakage went unnoticed for ten weeks, so the mute is deliberately short. It was set to end
on the day the window change was due; the change landed on the 21st instead, so the last two days of
the mute are simply slack.

**The end date is 23 September, not 22, and the one-day difference matters.** The 15 September
failures age out of a 7-day window exactly on the 22nd. Unmuting on the 23rd starts the new window
clean.

**Corrected 2026-09-21. The "cannot clear before October" claim below was wrong.** This section used
to say the monitor was latched in `Alert` and could not clear on its own before October, so unmuting
without the window switch would page immediately and stay red for three weeks. It cleared to `OK` by
itself on **2026-09-16 at 16:45:32Z**, three weeks early.

The reasoning missed that the denominator was growing much faster than assumed. The prediction used a
run rate near 15 a day; actual daily volume from 09-16 onward was 320, 347, 400, 317, 385. Errors
stayed flat at 26 to 27 while the 30-day denominator climbed past 4,100, so `26 / (0.01 × 4141)` fell
to 62.8% and back under the 100 threshold. **The stale data did not have to age out. It only had to
be diluted.** Worth remembering the next time this document predicts a latch duration: on a ratio
monitor, denominator growth clears a budget breach as effectively as the errors expiring.

The downtime needs no action. It expires on its own at 2026-09-23T08:00Z and both the window switch
and the monitor recovery already happened underneath it.

## Done 2026-09-21: switched to a 7-day window

Applied two days ahead of the planned date, because the precondition the date was waiting for had
already been met. Two edits, both verified by reading the objects back:

1. SLO `timeframe` and `thresholds[0].timeframe`, `30d` to `7d` (13:08:12Z).
2. Monitor query, to `error_budget("29588e73473d54f09814173755548b80").over("7d") > 100` (13:10:58Z).

The target was left at **99%**, not dropped to 95%. Reasoning below. Query, tags and name were
unchanged; the alert message uses the `{{timeframe}}` template variable, so it picked up the new
window with no edit.

**Why earlier than the 23rd was safe.** The date was chosen so the window would contain no
pre-rewrite data. That became true on **2026-09-16 at about 15:00Z**, not the 23rd: the last point
from the deleted definition sits in the 09-09 14:00Z hourly bucket, and a 7-day trailing window
stopped reaching it seven days later. Confirmed before switching, by checking the earliest point in
the window rather than assuming:

```bash
pup api "/api/v1/query?from=<t-7d>&to=<now>&query=sum:android.login.network.count%7Benv:prod%7D.as_count()" \
  | jq -r '.data.series[0].pointlist[0][0]/1000|floor|strftime("%Y-%m-%d %H:%MZ")'
```

It read 2026-09-15 08:00Z, so the window was entirely current-definition data.

**Staying on 30d was actively costing detection**, which is the real argument for not waiting. At a
4,141 denominator it took about 41 errors in a month to reach 100% budget; at 2,165 over 7 days it
takes about 21. The wider window was the less sensitive of the two, while an unexplained 504
recurrence was in progress.

**The ordering hazard is real but was covered.** Between the two edits the monitor asks the SLO for a
`30d` window it no longer defines, which makes it go blind rather than noisy. Two things contained
that: `notify_no_data` is `false` on this monitor, and the downtime was still muting it. Doing the
switch while the downtime was live was therefore the safest moment, not merely the earliest. Verified
afterwards that `state.groups["*"].last_nodata_ts` was still `null`, so it never actually lapsed into
No Data.

### Correction: the 95% recommendation was sized off a 25-event week

The table this section used to carry recommended starting at **95%** and tightening later. It was
computed when the current-definition denominator was 25, and projected a 7-day denominator "near
105" from a 15-a-day run rate. **Both inputs were an order of magnitude low.** Measured 2026-09-21,
the 7-day denominator is **2,165** and daily volume is 320 to 400.

An older table in this section was sized off Swedish-login *view impressions* (3,361 over 30 days),
which this document already flagged as the wrong quantity, since the SLI counts `POST /member-login`
and excludes the `/member-login/<uuid>` polling calls. Both tables are deleted rather than corrected,
because the live metric now answers the question directly:

```bash
NOW=$(date +%s)
for M in android.login.network.count android.login.network.error; do
  pup api "/api/v1/query?from=$((NOW-604800))&to=$NOW&query=sum:${M}%7Benv:prod%7D.as_count()" \
    | jq '[.data.series[0].pointlist[]? | select(.[1]!=null) | .[1]] | add'
done
```

At a 2,165 denominator, 99% allows **21.6 failures a week**. For scale, the 09-15 burst cost 13.9% of
that and the 09-20 burst 4.6%, so it takes five to seven such bursts in one week to breach. 95% would
allow 108 a week, which this failure mode would never reach. 99% is the defensible choice at current
volume and it also matches the three sibling auth SLOs (`Auth: login (iOS)`, `Auth: post auth`,
`Auth: get member credentials`), all of which are 7d at 99%.

Treat 99% as chosen for this signal now, rather than inherited. Revisit only if volume falls back
toward three digits a week.

### Correction: burn-rate alerting is not the right next step here

This section used to say burn-rate alerting was the textbook answer, not done only because it needed
a new monitor, and that the org's existing shape could be copied:

```
burn_rate("<slo id>").over("7d").long_window("1h").short_window("5m") > 16.8
```

**Do not copy that shape onto this SLO.** Burn rate is `observed error rate ÷ allowed error rate`, so
at a 99% target the `> 16.8` threshold fires at a 16.8% error rate, which is the speed that would
consume a week's budget in about ten hours. That threshold is only meaningful when the hourly
denominator is large. Measured over the 7 days to 2026-09-21, Android login volume per hour was
**min 1, median 16, p90 29, max 49** across 140 hours with data. Working backwards from 16.8%:

| Errors in the hour | Fires if the hour had at most |
|---|---|
| 1 | 5 requests |
| 2 | 11 requests |
| 3 | 17 requests, and the median hour is 16 |

So one failed login in a quiet overnight hour pages, and three in a median hour pages. The monitor
would be measuring how quiet the night was. The pattern works on iOS because that SLO runs 146,111
requests a week, roughly 870 an hour, where one failure is 0.11%.

It would also have missed the incident it is meant to catch. Applied to the two bursts:

| Hour | Requests | Errors | Error rate | Burn rate | Fires at 16.8? |
|---|---|---|---|---|---|
| 2026-09-15 08:00Z | 11 | 3 | 27.3% | 27.3 | yes |
| 2026-09-20 07:00Z | 37 | 1 | 2.7% | 2.7 | no |

There is a deeper reason it would miss, and it is not the monitor's fault: the metric only counted
**1** of the 3 failures on 09-20, because the path filter is an exact match on `/member-login` and the
two polling failures on `/member-login/<uuid>` are excluded. No monitor tuning on this metric can see
an event it only observes a third of.

**So the order of work is: widen the path filter first, then reconsider burn rate.** The filter
widening was deferred on the grounds that the window was dirty, and that blocker cleared on
2026-09-16. If a burn-rate monitor is added later, use longer windows (something like 6h long and 30m
short) so the denominator is large enough for a ratio to mean anything.

## Recurring gateway 504s

**Open. Not acted on. Raised 2026-09-21.**

`POST /member-login` has returned short-duration 504s twice:

| Burst | Spread | Events | Durations |
|---|---|---|---|
| 2026-09-15 08:21:58 to 08:22:41Z | 43s | 5 | 3.8, 4.2, 23.4, 29.2, 127.0 ms |
| 2026-09-20 07:52:31 to 07:57:48Z | 5m17s | 3 | 4.6, 18.4, 200.7 ms |

All on 14.4.8. **These are not timeouts, and should not be described as such.** Grouping
`/member-login` by status code over the same 7 days: 1,630 responses at 200 averaging **527.6 ms**,
against 4 at 504 averaging **95.1 ms**. A 504 returned five times faster than a success is an instant
rejection at the gateway: no healthy upstream, a config reload, a rolling restart, or a circuit
breaker.

Nothing server-side saw either burst. `Auth: post auth` and `Auth: get member credentials` both read
100.0% over the 7 days covering both, and `Auth: login (iOS)` read 99.973% on 09-20 with no matching
spread, so this is specific to the Android client's path to the gateway.

Two bursts six days apart, both in the 07:50 to 08:25Z band, is a pattern rather than a one-off, which
is the threshold this document previously set for escalating. Worth raising with whoever owns the auth
gateway.

What could not be established: whether either burst correlates with an auth deploy. The Datadog event
stream returns zero events for the whole of 09-15 and 09-20, so it carries no deploy markers in this
org and its silence is uninformative.

```bash
Q='@type:resource @application.id:4d7b8355-396d-406e-b543-30a073050e8f env:prod
   @resource.url_host:auth.prod.hedvigit.com @session.type:user'
pup rum aggregate --query "$Q @resource.url_path:/member-login" \
  --compute 'count,avg(@resource.duration)' --group-by @resource.status_code --from 7d
```

Note the budget monitor structurally cannot page on these: 09-15 consumed 13.9% of the weekly budget
and 09-20 consumed 4.6%, against a 100% threshold. Detecting them needs the path filter widened
first, then a different instrument. See the burn-rate correction above.

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
