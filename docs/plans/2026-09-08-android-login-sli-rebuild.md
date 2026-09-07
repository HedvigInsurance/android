# Rebuilding the Android login SLI on auth resource events

Status: definition agreed 2026-09-08. Blocked on the release that carries the auth instrumentation.

This supersedes an earlier plan to rebuild the SLI as a RUM operation. That plan existed only because
`:authlib` emitted no resource events at all, which made any resource-based SLI impossible. Adding
the Datadog hooks to the auth engine removed that constraint, so the fix is now a filter change with
no app code behind it.

## What was wrong

SLO `Auth: login (Android)` (id `29588e73473d54f09814173755548b80`, monitor `93408872`) fired at
534.351% of its 7-day error budget on 2026-09-07 with no login outage behind it.

Measured over the 7 days to 2026-09-07, `env:prod`:

| Side | Metric | Events | Event type | Where from |
|---|---|---|---|---|
| denominator | `android.login.network.count` | 131 | `resource` | versions 12.11.6 to 14.3.2, Sweden |
| numerator | `android.login.network.error` | 7 | `error` | version 14.4.7, one device in US datacenters |

7/131 = 5.34351%, against a 1% budget, is 534.351%. Four separate defects:

1. The two sides counted different event types, so the numerator could never be a subset of the
   denominator.
2. 96 of the 131 denominator events were `apollo-router` calls that merely happened while the login
   screen was on top. Only 4 were auth calls.
3. Both sides were keyed on `@view.name`, a navigation implementation detail, which changed underneath
   the SLI during the Nav2 to Nav3 migration.
4. Neither filter carried `@session.type:user`.

## The new SLI

Both sides are `@type:resource` on the auth host, so the bad-event set is a subset of the total set by
construction, and `@view.name` disappears entirely.

Total events:

```
@type:resource @application.id:4d7b8355-396d-406e-b543-30a073050e8f @resource.url_host:auth.prod.hedvigit.com @resource.url_path:"/member-login" @session.type:user
```

Bad events: the same query plus `@resource.status_code:[500 TO 599]`.

Both metrics use `event_type: resource` and group by `env`, matching the existing `android.*` metrics,
so the SLO keeps selecting `{env:prod}` and the filter carries no environment of its own.

Proposed names: `android.login.attempt.count` and `android.login.attempt.failure`. New names rather
than reusing the old pair, so the old metrics stay intact for comparison across the transition.

**The quoted exact path is load-bearing.** Verified 2026-09-08 against a real login on staging: the
quoted form matched the single `POST /member-login` and excluded the two `/member-login/{id}` status
polls. Unquoted, the polls would inflate the denominator by however many times BankID happened to be
polled, which varies per attempt.

## Target and window

Use a **30-day** window, not 7.

Login-screen impressions are the closest available proxy for attempt volume: 1,547 over the 30 days
to 2026-09-08 in `env:prod`. At 99% over 30 days that is an error budget of roughly 15 events. The
current 7-day window gives a budget of one to three, which is why a single device on a single
afternoon breached it. Set the target from the first two weeks of real data rather than assuming 99%
carries over.

## Known properties, recorded so nobody rediscovers them

- A request that never reaches the server (no connectivity, refused TLS handshake) is reported by
  `DatadogInterceptor` as `@type:error @error.source:network` and produces no resource, so it lands in
  neither side. This is what the old filters were reaching for with their `-@error.stack:java.net.*`
  exclusion lists, except it now falls out of the data model rather than a list of strings that has to
  be kept current.
- The SLI measures whether login requests that reached the auth service came back non-5xx. It does not
  measure whether the member actually got in. BankID abandonment, a failed status poll and a failed
  token exchange all sit outside it. Answering that needs an operation spanning the whole attempt,
  which is a product question rather than an availability one, and is deliberately out of scope.
- 4xx counts as good. A malformed personal number is not an availability failure.

## Remaining steps

1. Create the two RUM generated metrics on the filters above. Worth doing before the release so they
   begin computing the moment the instrumentation ships, rather than whenever someone remembers.
2. Once they carry real traffic, update SLO `29588e73473d54f09814173755548b80` in place: swap its two
   queries and move the timeframe to 30 days. Updating rather than replacing keeps the id, monitor
   `93408872` and its `@slack-android-dev` routing, and anything else referencing the id.
3. Keep `android.login.network.count` and `android.login.network.error` computing for one full window
   after the swap so the two definitions can be compared, then delete them.
4. Drop the blocker section from `2026-08-27-datadog-android-metric-recovery.md` once those two
   metrics are gone.

Until step 2 lands the monitor keeps firing on the broken definition. It is deliberately not muted.
