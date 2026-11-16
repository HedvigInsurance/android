package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.apolloStore
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.demomode.IosDemoMode
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.AccessTokenFetcher
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.AtomicInt
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.runBlocking
import octopus.ClaimsHistoryQuery
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/**
 * Builds the real graph from Kotlin fakes of the iOS app's five inputs. The graph's HTTP engine is Darwin and cannot be
 * replaced from outside, so no test lets the graph's own client reach a server. Tests that need a response use
 * [respondingWith], the graph's Apollo client (its interceptors and store) over a mock engine, which leaves out the
 * graph's token plugin.
 */
class IosGraphTest {
  private val logs = CapturingLogger()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `the graph builds and resolves the claim history screen model without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()

    val model = holder.claimHistoryScreenModel()
    pumpMainLoop(1.seconds)
    model.close()

    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `the auth listeners include the Apollo cache listener`() {
    val holder = FakeInputs().createHolder()

    assertThat(holder.graph.authEventListeners.map { it::class.simpleName }).containsExactlyInAnyOrder(
      "ApolloNormalizedCacheAuthEventListener",
      "DemoAuthEventListener",
    )
  }

  @Test
  fun `the graph's Apollo client only logs`() {
    val holder = FakeInputs().createHolder()

    assertThat(holder.graph.apolloClient.interceptors.map { it::class.simpleName })
      .containsExactly("LoggingInterceptor")
  }

  @Test
  fun `sessionChanged false clears the Apollo store and turns demo mode off on the main thread`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    holder.seedStore()

    holder.sessionChanged(false)

    assertThat(pumpMainLoopUntil { holder.storeIsEmpty() && inputs.demoMode.writes().isNotEmpty() }).isTrue()
    assertThat(inputs.demoMode.writes()).containsExactly(DemoModeWrite(demoMode = false, onMainThread = true))
  }

  @Test
  fun `sessionChanged twice in a row is harmless`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    holder.seedStore()

    holder.sessionChanged(true)
    holder.sessionChanged(true)
    assertThat(pumpMainLoopUntil { holder.storeIsEmpty() }).isTrue()
    pumpMainLoop(1.seconds)
    assertThat(inputs.demoMode.writes()).isEmpty()

    holder.seedStore()
    holder.sessionChanged(false)
    holder.sessionChanged(false)
    assertThat(pumpMainLoopUntil { holder.storeIsEmpty() && inputs.demoMode.writes().size == 2 }).isTrue()
    pumpMainLoop(1.seconds)
    assertThat(inputs.demoMode.writes()).containsExactly(
      DemoModeWrite(demoMode = false, onMainThread = true),
      DemoModeWrite(demoMode = false, onMainThread = true),
    )
  }

  @Test
  fun `a throwing token fetcher fails the request and the process survives`() {
    val inputs = FakeInputs(accessTokenFetcher = FakeAccessTokenFetcher(throws = true))
    val holder = inputs.createHolder()
    val model = holder.claimHistoryScreenModel()

    model.observe {}
    assertThat(pumpMainLoopUntil { inputs.accessTokenFetcher.calls.value == 1 }).isTrue()
    pumpMainLoop(2.seconds)
    model.close()
    val response = runBlocking {
      holder.graph.apolloClient.query(ClaimsHistoryQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
    }

    assertThat(response.exception).isNotNull().isInstanceOf<ApolloNetworkException>()
    assertThat(response.exception?.message).isEqualTo("token refresh failed")
    assertThat(response.data).isNull()
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(2)
  }

  @Test
  fun `an UNAUTHENTICATED response does not log out or change demo mode`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    holder.seedStore()

    val response = runBlocking {
      holder.respondingWith(unauthenticatedJson)
        .query(ClaimsHistoryQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .execute()
    }
    pumpMainLoop(2.seconds)

    assertThat(response.errors.orEmpty().map { it.extensions?.get("errorType") }).containsExactly("UNAUTHENTICATED")
    assertThat(holder.storeIsEmpty()).isFalse()
    assertThat(inputs.demoMode.writes()).isEmpty()
    assertThat(logs.messages().filter { "dispatching event" in it }).isEmpty()
  }

  @Test
  fun `a GraphQL error response does not throw and is logged once as a RUM error without attribute values`() {
    val holder = FakeInputs().createHolder()

    val response = runBlocking {
      holder.respondingWith(graphQlErrorJson)
        .query(ClaimsHistoryQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .execute()
    }

    assertThat(response.errors.orEmpty().map { it.message }).containsExactly("Something went wrong")
    assertThat(logs.entries().filter { (_, message) -> message.startsWith("RUM error") }).containsExactly(
      LogPriority.WARN to
        "RUM error from NETWORK: GraphQL errors for ClaimsHistory: [Something went wrong] attribute keys: [data, errors]",
    )
  }

  @Test
  fun `a RumLogger installed before the graph is kept`() {
    val installed = RecordingRumLogger()
    RumLogger.install(installed)

    FakeInputs().createHolder()
    logError("an error", ErrorSource.SOURCE)

    assertThat(installed.errors).containsExactly("an error")
  }
}

private class RecordingRumLogger : RumLogger {
  val errors = mutableListOf<String>()

  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {}

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    errors += message
  }
}

private fun IosGraphHolder.respondingWith(json: String): ApolloClient {
  val engine = MockEngine {
    respond(
      content = json,
      status = HttpStatusCode.OK,
      headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
  }
  return graph.apolloClient.newBuilder().ktorClient(HttpClient(engine)).build()
}

private fun IosGraphHolder.seedStore() {
  val response = runBlocking { respondingWith(claimsJson).query(ClaimsHistoryQuery()).execute() }
  check(response.data != null) { "Seeding failed: ${response.exception} ${response.errors}" }
  check(!storeIsEmpty()) { "Seeding wrote nothing" }
}

private fun IosGraphHolder.storeIsEmpty(): Boolean = runBlocking {
  graph.apolloClient.apolloStore.dump().values.all { it.isEmpty() }
}

private fun pumpMainLoop(duration: Duration) {
  pumpMainLoopUntil(duration) { false }
}

/** Drives the main run loop in 10 ms slices until [condition] holds or [timeout] passes. */
private fun pumpMainLoopUntil(timeout: Duration = 10.seconds, condition: () -> Boolean): Boolean {
  val start = TimeSource.Monotonic.markNow()
  while (!condition()) {
    if (start.elapsedNow() > timeout) return false
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return true
}

private class FakeInputs(
  val accessTokenFetcher: FakeAccessTokenFetcher = FakeAccessTokenFetcher(),
  val deviceIdFetcher: FakeDeviceIdFetcher = FakeDeviceIdFetcher(),
  val demoMode: FakeIosDemoMode = FakeIosDemoMode(),
) {
  fun createHolder(): IosGraphHolder = createIosGraphHolder(
    appBuildConfig = FakeAppBuildConfig,
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageStorage = FakeLanguageStorage,
    demoMode = demoMode,
  )
}

private class FakeAccessTokenFetcher(private val throws: Boolean = false) : AccessTokenFetcher {
  val calls = AtomicInt(0)

  override suspend fun fetch(): String? {
    calls.incrementAndGet()
    if (throws) throw IllegalStateException("token refresh failed")
    return "test-access-token-5d1c"
  }
}

private class FakeDeviceIdFetcher : DeviceIdFetcher {
  val calls = AtomicInt(0)

  override suspend fun fetch(): String? {
    calls.incrementAndGet()
    return "test-device-id"
  }
}

private data class DemoModeWrite(val demoMode: Boolean, val onMainThread: Boolean)

private class FakeIosDemoMode : IosDemoMode {
  private val lock = NSLock()
  private val recorded = mutableListOf<DemoModeWrite>()

  fun writes(): List<DemoModeWrite> = locked { recorded.toList() }

  override fun isDemoMode(): Boolean = false

  override fun setDemoMode(demoMode: Boolean) {
    locked { recorded += DemoModeWrite(demoMode, NSThread.isMainThread) }
  }

  private fun <T> locked(block: () -> T): T {
    lock.lock()
    try {
      return block()
    } finally {
      lock.unlock()
    }
  }
}

private object FakeLanguageStorage : LanguageStorage {
  override fun getCurrentLanguageTag(): String = "en-SE"

  override fun getSelectedLanguageTag(): String? = null

  override fun setLanguageTag(tag: String) {}
}

private object FakeAppBuildConfig : AppBuildConfig {
  override val debug: Boolean = true
  override val applicationId: String = "com.hedvig.test"
  override val buildType: String = "debug"
  override val versionCode: Int = 1
  override val versionName: String = "1.0.0"
  override val appFlavor: Flavor = Flavor.Staging
  override val osReleaseVersion: String = "26.0"
  override val osSdkVersion: Int = 26
  override val brand: String = "Apple"
  override val model: String = "iPhone"
  override val device: String = "iPhone"
  override val manufacturer: String = "Apple"
}

/** Logs arrive from the main thread and from background threads. */
private class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> {
    lock.lock()
    try {
      return captured.toList()
    } finally {
      lock.unlock()
    }
  }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    lock.lock()
    try {
      captured += priority to text
    } finally {
      lock.unlock()
    }
  }
}

private val unauthenticatedJson = """
  {
    "errors": [
      {
        "message": "Unauthenticated",
        "extensions": { "errorType": "UNAUTHENTICATED" }
      }
    ]
  }
""".trimIndent()

private val graphQlErrorJson = """
  {
    "errors": [
      {
        "message": "Something went wrong",
        "extensions": { "detail": "attribute-value-7f3a" }
      }
    ]
  }
""".trimIndent()

private val claimsJson = """
  {
    "data": {
      "currentMember": {
        "__typename": "Member",
        "id": "member-1",
        "claimsHistory": [],
        "partnerClaimsHistory": []
      }
    }
  }
""".trimIndent()
