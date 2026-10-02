---
name: inspect-network-traffic
description: Read the HTTP and GraphQL calls a debug build of the Android app made, with request and response bodies, status codes, timings and errors, over adb. Use when debugging what the app sent to or got back from the backend, why a screen shows the wrong data or an error, whether a call happened at all, or to check a network change on a device or emulator.
---

# Inspecting network traffic

Debug builds record every call made through the app's main Ktor client into
[Chucker](https://github.com/ChuckerTeam/chucker)'s database on the device. Staging and
release get Chucker's no-op artifact and record nothing. `chucker.sh` next to this file
copies the database off the device and queries it with the host's `sqlite3`, so you never
need the Chucker UI.

Needs a **debug** build (`com.hedvig.dev.app`) on the device, because `run-as` only works on
debuggable apps. Set `ANDROID_SERIAL` when more than one device is attached.

## Workflow

Run from the repo root:

1. `.claude/skills/inspect-network-traffic/chucker.sh mark` prints the device clock.
2. Reproduce the behaviour in the app.
3. `.claude/skills/inspect-network-traffic/chucker.sh since <mark>` lists what was called,
   one row per call: id, time, GraphQL operation (or method and path), status code,
   duration, response size, and any problem.
4. `.claude/skills/inspect-network-traffic/chucker.sh show <id>` prints every column of one
   call as JSON, including headers and both bodies.

Also: `recent [N]` for the last N calls, `errors [N]` for calls with an HTTP error, a
transport error or a GraphQL `errors` array, and `sql "<query>"` for anything else.

## Things that will trip you up

- **Cancelled calls are normal.** A call whose coroutine is cancelled, for example when a
  screen leaves composition, shows `java.io.IOException: Canceled` and no status code. On a
  cold start a burst of these is followed by the same operations succeeding. `errors` leaves
  them out.
- **GraphQL errors come back as HTTP 200.** Look at `problem` or the response body, not the
  status code.
- **The operation name comes from the request body.** Every GraphQL call is a POST to the
  same router URL, and Chucker's own `graphQlOperationName` column is always empty. Use
  `json_extract(requestBody, '$.operationName')` in your own queries.
- **What is not recorded.** Login and token refresh go through the auth library's own client.
  Coil's image downloads share the recorded client, so filter on `responseContentType` if
  they get in the way.
- **Bodies.** `Authorization` is redacted. Bodies are stored up to Chucker's 250 KB default,
  and only as far as the app read them. Calls are kept for a day.

Useful `transactions` columns: `requestDate`, `responseDate` (epoch ms), `tookMs`,
`method`, `url`, `path`, `requestHeaders`, `requestBody`, `responseCode`,
`responseHeaders`, `responseBody`, `responseContentType`, `error`.
