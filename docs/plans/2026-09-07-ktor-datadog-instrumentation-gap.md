# Datadog network instrumentation after the move off OkHttp

Status: gap 1 and gap 2 implemented 2026-09-07. Gap 3 partially resolved as a side effect, with a
recorded decision not to pursue the rest. Written 2026-09-07.

## What shipped

`:authlib` now accepts a caller-supplied engine, and `:datadog-android` supplies one carrying
`DatadogInterceptor`, `TracingInterceptor` and `DatadogEventListener`. The auth host was added to
`setFirstPartyHosts` and to the engine's traced hosts.

Verified:

- `:app:compileDebugKotlin` passes, so the Metro graph resolves the new `@AuthHttpClientEngine`
  binding.
- `:authlib:compileKotlinIosSimulatorArm64` and `:umbrella:linkDebugFrameworkIosSimulatorArm64` pass.
- **The exported Obj-C API changes by four added lines**, diffed against a clean-tree relink of the
  framework: a new `AuthEnvironment` extension exposing a read-only `baseUrl`. Nothing removed,
  nothing changed. `NetworkAuthRepository` keeps exactly one initializer,
  `init(environment:additionalHttpHeadersProvider:)`, so Ugglan's call site compiles untouched. The
  JVM-only `networkAuthRepositoryWithEngine` does not appear in the header at all.

The remaining sections describe why this shape was chosen and what was rejected.

Found while diagnosing the `Auth: login (Android)` SLO breach. See
`2026-09-08-android-login-sli-rebuild.md`.

## What OkHttp used to give us

`7d14888e81` ("Start removing okhttp related configuration", 2025-12-17) deleted
`OkHttpDatadogUtil.kt`, which applied three separate Datadog pieces to the one shared
`OkHttpClient`:

| Piece | What it provided |
|---|---|
| `DatadogInterceptor` | a RUM `resource` event per request, plus automatic RUM error reporting on failures |
| `TracingInterceptor` (network interceptor) | APM span creation and trace-header propagation |
| `DatadogEventListener.Factory()` | the per-request timing breakdown: DNS, connect, SSL, first byte, download, size |

`libs.datadog.sdk.okhttp` was dropped in the same commit. No OkHttp remains in Kotlin source today;
it survives only as the Ktor engine (`ktor-client-okhttp`, and `OkHttp` as `:authlib`'s engine
factory).

## What we have now

Two Ktor `HttpClient` construction sites, and only one is instrumented.

**`:network-clients`** — `NetworkMetroProviders.kt` builds the shared base client and installs
`datadogKtorPlugin` via a `mobileMain` expect/actual (`dd-sdk-kotlin-multiplatform-ktor3` 1.7.0).
Serves Apollo GraphQL, file uploads, PDF downloads, Coil image fetching, and
`GetMemberAuthorizationCodeUseCase`.

**`:authlib`** — `KtorConfiguration.kt` builds its own client with `ContentNegotiation`, Ktor
`Logging` and `defaultRequest`, and **no Datadog plugin**. The module has no Datadog dependency at
all. Serves `POST /member-login`, the `/member-login/{id}` status polling, `/oauth/token`, and the
OTP endpoints.

### This is not a Ktor regression

Worth stating plainly, because it changes what there is to fix: `:authlib` arrived with its own Ktor
client in `f918732be5` ("Bring in authlib") and has never had a Datadog dependency. It builds its own
engine via `httpClientEngineFactory()`, so it never received the Datadog-configured `OkHttpClient`
either. Auth calls were never instrumented. The Ktor migration did not break this; it made it harder
to notice, because now both clients are Ktor and look interchangeable.

The Ktor migration did cost us one thing, and it is the timing breakdown. See gap 3.

## The three gaps, with evidence

Measured 7 days to 2026-09-07, `env:prod`, RUM app `4d7b8355-396d-406e-b543-30a073050e8f`.

### Gap 1: `:authlib` traffic is invisible to RUM

Resource events on `auth.prod.hedvigit.com`, by path:

| Path | Events | Which client |
|---|---|---|
| `/member-authorization-codes` | 634 | shared base client |
| `/member-login/{id}` | 2 | `:authlib` |
| `/member-login` | 1 | `:authlib` |
| `/oauth/token` | 1 | `:authlib` |

The 4 `:authlib` events all come from 12.11.6, an ancient build. Every login, OTP and token-exchange
call on every current version is absent. Auth failures surface only as `@error.source:logger` text
from `logcat(LogPriority.ERROR)` calls in the presenter, with no status code, no duration, no
correlation.

That is why today's real incident, five `504 Gateway Timeout` responses from
`auth.prod.hedvigit.com/member-login`, was only visible as prose inside an error message.

#### Why the obvious fix does not fit

Installing `datadogKtorPlugin` in `:authlib`, mirroring `:network-clients`, does not work without a
structural change. Verified:

- `:authlib` is `hedvig.multiplatform.library` and declares only `jvm()` and iOS targets. There is
  no `androidTarget()` and no `androidMain`. The Android app therefore consumes authlib's **JVM**
  variant, and its engine comes from `jvmMain`, which is `io.ktor.client.engine.okhttp.OkHttp`.
- `:auth-core-public`, which constructs `NetworkAuthRepository`, is `hedvig.jvm.library`. Also not
  Android.
- The Datadog Ktor plugin publishes android and iOS variants only. The cached 1.7.0 artifacts are
  `-android`, `-iosarm64` and `-iossimulatorarm64`, with no JVM variant. `dd-sdk-android-okhttp` is
  an AAR, so it cannot go into a JVM source set either.

So neither Datadog artifact can be added to either module as they are structured. Giving `:authlib`
an Android target would change which variant the app resolves, which is exactly the kind of change
we do not want for an observability hook.

#### The fix that is actually small

Inject a preconfigured engine from a module that *is* Android, and leave `:authlib` Datadog-free.
OkHttp is already the engine underneath on Android, so the Datadog OkHttp instrumentation can be
attached to it directly.

1. `:authlib` — `NetworkAuthRepository`'s primary constructor becomes `internal` and takes
   `engine: HttpClientEngine?`, with a `public` secondary constructor keeping the original two
   parameters and delegating with `null`. `buildKtorClient` uses the engine when given and falls back
   to `httpClientEngineFactory()` otherwise.

   **A default parameter will not do here.** Kotlin/Native does not carry default values into the
   Obj-C API, so `engine: HttpClientEngine? = null` on the existing constructor exports
   `initWithEnvironment:additionalHttpHeadersProvider:engine:` and breaks
   `AuthenticationClientAuthLib.swift` in Ugglan at the next umbrella bump. `internal` declarations
   are not exported, and a separate `public` secondary constructor keeps the original selector
   byte-identical.

   A JVM-only `networkAuthRepositoryWithEngine` in `jvmMain` reaches the internal constructor from
   inside the module. Living in `jvmMain` keeps it, and `HttpClientEngine`, off the iOS surface
   entirely. `jvmMain` exposes `api(libs.ktor.client.core)` so JVM consumers can name the type.
2. `:auth-core-public` — `provideAuthRepository` takes a qualified `HttpClientEngine` and calls
   `networkAuthRepositoryWithEngine`. It needs `implementation(libs.ktor.client.core)` added. The
   `@AuthHttpClientEngine` qualifier goes in `:core-common-public` next to the existing
   `@BaseHttpClient`.
3. `:datadog-android` — provide that engine, restoring what `7d14888e81` deleted:

```kotlin
OkHttp.create {
  config {
    eventListenerFactory(DatadogEventListener.Factory())
    addInterceptor(DatadogInterceptor.Builder(tracedHosts).build())
    addNetworkInterceptor(TracingInterceptor.Builder(tracedHosts).build())
  }
}
```

   `:datadog-android` is where `OkHttpDatadogUtil.kt` lived before deletion, so the Datadog
   knowledge stays in the Datadog module. Re-add `dd-sdk-android-okhttp` to its dependencies and to
   the version catalog.

No dependency cycle: `:auth-core-public` needs only the `HttpClientEngine` type and the qualifier,
not `:datadog-android`. Metro merges the binding in `:app`.

This is strictly better than the plugin route, for about the same effort:

- **Gap 1 is fixed.** Auth calls become RUM resources with status codes and durations.
- **Gap 3 is fixed for auth.** `DatadogInterceptor` and `DatadogEventListener` sit at the same
  OkHttp layer and share its key space, which is precisely what the Ktor plugin cannot do, so the
  DNS, connect, SSL, first byte and download phases land.
- **Gap 2 is fixed for auth** in the same change, by putting the auth host in `tracedHosts` here and
  in `setFirstPartyHosts` in `DatadogInitializer`, same module.
- **`:network-clients` is not touched at all.** It keeps the Ktor plugin, so the ~329k resource
  events a week that work today carry no risk, and there is no double counting anywhere, because
  authlib has no Ktor plugin to conflict with.

iOS is deliberately out of scope: Ugglan ships its own tracking, so authlib on iOS keeps passing
null and behaves exactly as it does now.

Effort: small and additive. Requires a release.

### Gap 2: only the GraphQL host is treated as first-party

Two independent allowlists both name only `urlGraphqlOctopus`:

- `DatadogInitializer.kt`: `.setFirstPartyHosts(listOf(urlGraphqlOctopus.removePrefix("https://")))`
- `NetworkMetroProviders.mobile.kt`: `tracedHosts = mapOf(urlGraphqlOctopus... to DATADOG)`

Consequence, observable today: an `apollo-router.prod.hedvigit.com` resource carries
`provider.type: "first party"`, while `/member-authorization-codes` on `auth.prod.hedvigit.com`
carries `provider.type: "unknown"` even though it goes through the instrumented client. No trace
headers are sent to the auth host, so client spans do not join the auth service's existing traces.

**Fix:** add the auth host to both lists. This is client-side configuration only and adds no code to
any backend, so it stays inside the decided scope. It makes the auth service's existing APM traces
correlatable from the app side, which is free.

Effort: two lines. Requires a release.

### Gap 3: resource timing phases are gone, permanently

A current resource event from 14.4.6 against the instrumented client:

```
duration     212556539
status_code  200
dns          null
connect      null
ssl          null
first_byte   null
download     null
size         null
```

`duration` and `status_code` survive. Every phase field is null, on both clients, for all traffic.
This was `DatadogEventListener.Factory()`'s contribution, and it worked by hooking OkHttp's
low-level `EventListener` socket callbacks. Ktor exposes no equivalent hook, and the Datadog Ktor
plugin does not provide one.

Two corrections to the obvious first guesses, both checked rather than assumed.

**The SDK does not have this, in any version.** Read the sources of the pinned
`dd-sdk-kotlin-multiplatform-ktor3` 1.7.0 out of the Gradle cache: `grep -ri timing` across the
whole plugin returns nothing. The latest published version is 1.8.0 (2026-08-10) and its changelog,
and every changelog before it, mentions trace propagation, RUM session id, resource attribute
providers and error handling, but never timings, phases, or an event listener. Bumping the plugin
does not get this.

**Adding `DatadogEventListener` alongside the plugin cannot work.** This is the important one,
because it is the natural thing to try. From
`com/datadog/kmp/ktor/internal/plugin/DatadogKtorPlugin.kt`, `onSend`:

```kotlin
val requestId = uuid4().toString()
request.attributes.put(DD_REQUEST_ID_ATTR, requestId)
rumMonitorProvider.invoke().startResource(key = requestId, ...)
```

The plugin keys every RUM resource with a freshly generated UUID that lives only in Ktor's attribute
bag. `DatadogEventListener` runs down at the OkHttp layer and has no access to that bag, so whatever
key it reports timings against, it can never be the plugin's UUID. The timings would be attached to a
resource RUM has never heard of and silently dropped. The two mechanisms have disjoint key spaces.

### The path that does work on Android

Not supplement the plugin, **replace** it, on Android only.

Ktor's `OkHttpConfig` exposes `config(block: OkHttpClient.Builder.() -> Unit)` plus `addInterceptor`
/ `addNetworkInterceptor` and a `preconfigured: OkHttpClient?`. Its own default config only sets
`followRedirects`, `followSslRedirects` and `retryOnConnectionFailure`, so it does not install an
event listener and will not fight one. That means the exact trio from the deleted
`OkHttpDatadogUtil.kt` can be reattached to the engine, and `DatadogInterceptor` and
`DatadogEventListener` then share OkHttp's own key space, which is what makes the timings land.

Android would go back to OkHttp-level instrumentation; iOS keeps the Ktor plugin on the Darwin
engine. The `mobileMain` / `iosMain` expect/actual seam for `installDatadogKtorPlugin` already exists
precisely for this kind of split, and `NetworkMetroProviders.jvm.kt` already shows the no-op shape.

Mechanically, the cleanest seam is the engine factory rather than the plugin installer.
`installDatadogKtorPlugin` is typed `HttpClientConfig<*>`, and `HttpClient(httpClientEngineFactory())`
star-projects the engine config, so `engine { }` cannot reach `OkHttpConfig` from `commonMain`
without a cast. Instead, have the Android actual of `httpClientEngineFactory()` return a factory
that bakes the interceptors and the event listener in, and make `installDatadogKtorPlugin` a no-op
there. This needs `hedvigBuildConstants` threaded into the factory, which it does not take today.

Costs and risks, all of which need a spike before committing:

- Reintroduces the `dd-sdk-android-okhttp` dependency, dropped in `7d14888e81`.
- **Double counting is the main hazard.** Exactly one of the two mechanisms may be active per
  platform. Both means two RUM resource events per request and a doubled denominator on anything
  counting resources.
- Network error messages change shape. The plugin emits `"Ktor request error $method $url"`, which
  is observable in RUM today; `DatadogInterceptor` does not. Any Datadog filter matching that string
  breaks.
- Verify websockets and SSE still work through the engine, and that Ktor-level retries
  (`HttpSend` interceptors, the auth retry) do not produce surprising resource counts.
- Android and iOS would report resources through different code paths, so subtle attribute
  differences between platforms become possible.

### Recommendation

Split the two clients, because the answer differs.

**For `:authlib`, timings come for free.** The gap 1 fix above already puts `DatadogInterceptor` and
`DatadogEventListener` on the auth engine together, so auth traffic gets full phase breakdown as a
side effect. Nothing extra to decide.

**For `:network-clients`, leave it alone.** Recovering timings on the Apollo traffic means replacing
a working Ktor plugin with engine-level instrumentation on the client that carries ~329k events a
week, and that is where every risk in the list above lives: double counting, changed error message
shape, websockets and SSE to re-verify. `duration` plus `status_code` covers ordinary triage, and
missing DNS-versus-TLS attribution on GraphQL has not actually blocked anything.

Revisit only if a real latency question needs phase attribution on GraphQL specifically. The path is
viable and now written down, so that decision can be made on evidence rather than rediscovered.

Also worth noting while here: `size` is not an unconditional loss. The plugin does report it, as
`size = response.contentLength()` with a `TODO RUM-6382` for the missing-header case, so the nulls
we see mean our responses lack `Content-Length` (chunked or compressed), not that the field is
unsupported.

And a small free win found in the same source: `traceHeaderTypesForHost` does suffix matching and
honours a `"*"` key, so gap 2 can be fixed with a wildcard instead of enumerating hosts, if that is
preferred.

## Order of work

Gap 1 and gap 2 are one small PR and one release. They are worth doing on their own merits: auth
currently has no network observability whatsoever, which is the reason the login SLO investigation
needed a full afternoon.

They also turned out to be the whole fix for the login SLI. Because auth calls now produce resource
events, that SLI can be rebuilt as a filter change with no further app code, which is what
`2026-09-08-android-login-sli-rebuild.md` records. The earlier plan to rebuild it as a RUM operation
existed only because these calls were invisible.

Gap 3 needs a decision recorded, not work.
