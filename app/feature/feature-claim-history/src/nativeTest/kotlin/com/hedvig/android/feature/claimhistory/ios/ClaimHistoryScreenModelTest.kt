@file:OptIn(NativeRuntimeApi::class)

package com.hedvig.android.feature.claimhistory.ios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.cash.molecule.DisplayLinkClock
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.claimhistory.ClaimHistoryViewModel
import com.hedvig.android.feature.claimhistory.GetClaimsHistoryUseCase
import com.hedvig.android.feature.claimhistory.graphQlErrorJson
import com.hedvig.android.feature.claimhistory.regularAndPartnerClaimsJson
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.di.NativeNetworkMetroProviders
import com.hedvig.android.network.clients.di.NetworkMetroProviders
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.AtomicInt
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/**
 * Runs on the test binary's main thread. `Dispatchers.Main` is the main queue, which only drains while a test drives
 * the main run loop ([pumpMainLoop], [pumpMainLoopUntil]).
 *
 * `DisplayLinkClock` produces no frame in the test process, so the presenter composes once and never recomposes: a
 * subscriber only ever receives the initial `Loading`, though the presenter's query is sent. These tests assert what is
 * observable without a second state: callbacks, threads, requests, headers and logs.
 */
class ClaimHistoryScreenModelTest {
  private val logs = CapturingLogger()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `the display link produces no frame in the test process`() {
    var frameTime: Long? = null
    val scope = MainScope()
    scope.launch { frameTime = DisplayLinkClock.withFrameNanos { it } }

    pumpMainLoop(3.seconds)

    assertThat(frameTime).isNull()
    scope.cancel()
  }

  @Test
  fun `the presenter sends its query but delivers only Loading`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    val states = mutableListOf<ClaimHistoryScreenState>()

    model.observe { states += it }
    pumpMainLoop(3.seconds)

    assertThat(server.requests.value).isEqualTo(1)
    assertThat(states).containsExactly(ClaimHistoryScreenState.Loading)
    model.close()
  }

  @Test
  fun `state is delivered on the main thread`() {
    val model = ClaimHistoryScreenModel(factoryOver(FakeServer(regularAndPartnerClaimsJson).apolloClient()))
    val onMainThread = mutableListOf<Boolean>()

    model.observe { onMainThread += NSThread.isMainThread }
    pumpMainLoopUntil { onMainThread.isNotEmpty() }

    assertThat(onMainThread).containsExactly(true)
    model.close()
  }

  @Test
  fun `the first callback arrives after observe returns and repeats currentState`() {
    val model = ClaimHistoryScreenModel(factoryOver(FakeServer(regularAndPartnerClaimsJson).apolloClient()))
    val seeded = model.currentState
    var observeReturned = false
    var calledDuringObserve = false
    val states = mutableListOf<ClaimHistoryScreenState>()

    model.observe {
      if (!observeReturned) calledDuringObserve = true
      states += it
    }
    observeReturned = true
    assertThat(states).isEmpty()
    pumpMainLoopUntil { states.isNotEmpty() }

    assertThat(calledDuringObserve).isFalse()
    assertThat(seeded).isEqualTo(ClaimHistoryScreenState.Loading)
    assertThat(states.first()).isEqualTo(seeded)
    model.close()
  }

  @Test
  fun `no callback arrives after cancel returns`() {
    val model = ClaimHistoryScreenModel(factoryOver(FakeServer(regularAndPartnerClaimsJson).apolloClient()))
    val cancelled = mutableListOf<ClaimHistoryScreenState>()
    val control = mutableListOf<ClaimHistoryScreenState>()

    val subscription = model.observe { cancelled += it }
    model.observe { control += it }
    subscription.cancel()
    pumpMainLoop(1.seconds)

    assertThat(control).containsExactly(ClaimHistoryScreenState.Loading)
    assertThat(cancelled).isEmpty()
    model.close()
  }

  @Test
  fun `no callback arrives after close returns`() {
    val model = ClaimHistoryScreenModel(factoryOver(FakeServer(regularAndPartnerClaimsJson).apolloClient()))
    val states = mutableListOf<ClaimHistoryScreenState>()

    model.observe { states += it }
    model.observe { states += it }
    model.close()
    pumpMainLoop(1.seconds)

    assertThat(states).isEmpty()
  }

  @Test
  fun `observe after close never fires and sends nothing`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    model.close()
    val states = mutableListOf<ClaimHistoryScreenState>()

    model.observe { states += it }
    model.reload()
    pumpMainLoop(1.seconds)

    assertThat(states).isEmpty()
    assertThat(server.requests.value).isEqualTo(0)
  }

  @Test
  fun `close clears the ViewModel once and stops requests`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    model.close()
    model.close()
    model.observe {}
    model.reload()
    pumpMainLoop(6.seconds)

    assertThat(logs.messages().count { it == "ClaimHistoryViewModel cleared" }).isEqualTo(1)
    assertThat(server.requests.value).isEqualTo(1)
  }

  @Test
  fun `with no subscription for more than 5 seconds the presenter stops and a new observe queries again`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    val subscription = model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    subscription.cancel()
    pumpMainLoop(6.seconds)
    assertThat(server.requests.value).isEqualTo(1)
    model.observe {}

    assertThat(pumpMainLoopUntil(5.seconds) { server.requests.value == 2 }).isTrue()
    model.close()
  }

  @Test
  fun `a new observe within 5 seconds reuses the running presenter`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    val subscription = model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    subscription.cancel()
    pumpMainLoop(1.seconds)
    model.observe {}
    pumpMainLoop(2.seconds)

    assertThat(server.requests.value).isEqualTo(1)
    model.close()
  }

  @Test
  fun `a dropped subscription keeps the presenter running until close`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val model = ClaimHistoryScreenModel(factoryOver(server.apolloClient()))
    var droppedCallbacks = 0
    model.observe { droppedCallbacks++ }
    pumpMainLoopUntil { server.requests.value == 1 }

    GC.collect()
    pumpMainLoop(6.seconds)
    model.observe {}
    pumpMainLoop(2.seconds)
    assertThat(server.requests.value).isEqualTo(1)
    assertThat(droppedCallbacks).isEqualTo(1)

    model.close()
    assertThat(logs.messages().count { it == "ClaimHistoryViewModel cleared" }).isEqualTo(1)
  }

  @Test
  fun `many reloads before observing do not throw and the overflow is logged`() {
    val model = ClaimHistoryScreenModel(factoryOver(FakeServer(regularAndPartnerClaimsJson).apolloClient()))

    repeat(25) { model.reload() }

    val dropped = logs.entries().count { (priority, message) ->
      priority == LogPriority.WARN && message.startsWith("ClaimHistoryViewModel dropped a reload")
    }
    assertThat(dropped).isEqualTo(5)
    model.close()
  }

  @Test
  fun `a non-null token is sent as a Bearer header`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val fetcher = FakeAccessTokenFetcher(listOf(TOKEN, TOKEN))
    val model = ClaimHistoryScreenModel(factoryOver(server.nativeApolloClient(fetcher)))

    model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    assertThat(server.authorizationHeaders()).containsExactly("Bearer $TOKEN")
    assertThat(fetcher.calls.value).isEqualTo(2)
    assertThat(logs.messages().filter { TOKEN in it }).isEmpty()
    model.close()
  }

  @Test
  fun `a null token sends no Authorization header`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val fetcher = FakeAccessTokenFetcher(listOf(null))
    val model = ClaimHistoryScreenModel(factoryOver(server.nativeApolloClient(fetcher)))

    model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    assertThat(server.authorizationHeaders()).containsExactly(null)
    assertThat(fetcher.calls.value).isEqualTo(1)
    model.close()
  }

  @Test
  fun `a token that is null on its second read is sent as Bearer null`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val fetcher = FakeAccessTokenFetcher(listOf(TOKEN, null))
    val model = ClaimHistoryScreenModel(factoryOver(server.nativeApolloClient(fetcher)))

    model.observe {}
    pumpMainLoopUntil { server.requests.value == 1 }

    assertThat(server.authorizationHeaders()).containsExactly("Bearer null")
    assertThat(fetcher.calls.value).isEqualTo(2)
    model.close()
  }

  @Test
  fun `a throwing token fetcher sends nothing and the process survives`() {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val fetcher = FakeAccessTokenFetcher(throws = true)
    val model = ClaimHistoryScreenModel(factoryOver(server.nativeApolloClient(fetcher)))

    model.observe {}
    pumpMainLoopUntil { fetcher.calls.value == 1 }
    pumpMainLoop(2.seconds)

    assertThat(server.requests.value).isEqualTo(0)
    assertThat(fetcher.calls.value).isEqualTo(1)
    model.close()
  }

  @Test
  fun `a throwing token fetcher is a Left for the use case`() = runTest {
    val server = FakeServer(regularAndPartnerClaimsJson)
    val useCase = GetClaimsHistoryUseCase(server.nativeApolloClient(FakeAccessTokenFetcher(throws = true)))

    val result = useCase.invoke().first()

    assertThat(result).isInstanceOf<Either.Left<*>>()
    assertThat(server.requests.value).isEqualTo(0)
  }

  @Test
  fun `an UNAUTHENTICATED response is a Left for the use case`() = runTest {
    val server = FakeServer(unauthenticatedJson)
    val useCase = GetClaimsHistoryUseCase(server.nativeApolloClient(FakeAccessTokenFetcher(listOf(TOKEN, TOKEN))))

    val result = useCase.invoke().first()

    assertThat(result).isInstanceOf<Either.Left<*>>()
    assertThat(logs.messages().filter { TOKEN in it }).isEmpty()
  }

  @Test
  fun `a GraphQL error with no RumLogger installed throws out of the native Apollo client`() = runTest {
    val server = FakeServer(graphQlErrorJson)
    val useCase = GetClaimsHistoryUseCase(server.nativeApolloClient(FakeAccessTokenFetcher(listOf(TOKEN, TOKEN))))

    val result = runCatching { useCase.invoke().first() }

    assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<IllegalStateException>()
    assertThat(result.exceptionOrNull()?.message).isEqualTo("Should never receive any error")
  }
}

private const val TOKEN = "test-access-token-5d1c"

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

/** Logs arrive from the main thread and from request threads. */
private class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> = locked { captured.toList() }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    locked { captured += priority to text }
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

private class FakeAccessTokenFetcher(
  private val tokens: List<String?> = emptyList(),
  private val throws: Boolean = false,
) : AccessTokenFetcher {
  val calls = AtomicInt(0)

  override suspend fun fetch(): String? {
    val call = calls.incrementAndGet()
    if (throws) throw IllegalStateException("token refresh failed")
    return tokens[call - 1]
  }
}

private class FakeServer(private val responseJson: String) {
  val requests = AtomicInt(0)
  private val lock = NSLock()
  private val headers = mutableListOf<String?>()

  fun authorizationHeaders(): List<String?> {
    lock.lock()
    try {
      return headers.toList()
    } finally {
      lock.unlock()
    }
  }

  private val engine = MockEngine { request ->
    lock.lock()
    try {
      headers += request.headers[HttpHeaders.Authorization]
    } finally {
      lock.unlock()
    }
    requests.incrementAndGet()
    respond(
      content = responseJson,
      status = HttpStatusCode.OK,
      headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
  }

  /** No normalized cache, so every query reaches [engine]. */
  fun apolloClient(): ApolloClient = ApolloClient.Builder()
    .serverUrl("https://example.test/graphql")
    .ktorClient(HttpClient(engine))
    .build()

  /** The iOS app's Apollo client and token plugin, over [engine] instead of Darwin. */
  fun nativeApolloClient(accessTokenFetcher: AccessTokenFetcher): ApolloClient {
    val httpClient = object : NetworkMetroProviders {}.provideHttpClient(HttpClient(engine), accessTokenFetcher)
    return object : NativeNetworkMetroProviders {}.provideApolloClient(
      normalizedCacheFactory = MemoryCacheFactory(),
      httpClient = httpClient,
      hedvigBuildConstants = FakeHedvigBuildConstants,
    )
  }
}

private fun factoryOver(apolloClient: ApolloClient): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST")
    return ClaimHistoryViewModel(GetClaimsHistoryUseCase(apolloClient)) as T
  }
}

private object FakeHedvigBuildConstants : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = "https://example.test/graphql"
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = emptyList()
  override val appVersionName: String = ""
  override val appVersionCode: String = ""
  override val appPackageId: String = ""
  override val isDebug: Boolean = true
  override val isProduction: Boolean = false
  override val buildApiVersion: Int = 0
  override val platformName: String = "ios"
  override val model: String = ""
  override val userAgent: String = ""
}
