package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.di.NativeDataStoreMetroProviders
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.ReadFailure
import com.hedvig.android.iosdata.bridge.ReadFailureKind
import com.hedvig.android.iosdata.bridge.component
import com.hedvig.android.iosdata.bridge.iosGraphHolder
import com.hedvig.android.iosdata.bridge.iosReadOrThrow
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.language.Language
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.runBlocking
import octopus.PayingMemberPhoneNumberQuery
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSRunLoop
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/** A feature's reads, written the way a feature module writes them. */
@Inject
class ExampleReads(private val apolloClient: ApolloClient) {
  @NativeCoroutines
  suspend fun memberId(): String = iosReadOrThrow {
    apolloClient.query(PayingMemberPhoneNumberQuery()).safeExecute().bind().currentMember.id
  }
}

@ContributesTo(AppScope::class)
interface ExampleReadsComponent {
  val exampleReads: ExampleReads
}

fun IosGraphHolder.exampleReads(): ExampleReads = component<ExampleReadsComponent>().exampleReads

/** [IosGraph]'s contributions, plus the HTTP clients the tests intercept. */
@DependencyGraph(AppScope::class, excludes = [NativeDataStoreMetroProviders::class])
internal interface TestIosGraph {
  val httpClient: HttpClient

  @BaseHttpClient
  val baseHttpClient: HttpClient

  @PublicApolloClient
  val publicApolloClient: ApolloClient

  val networkCacheManager: NetworkCacheManager

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides appBuildConfig: AppBuildConfig,
      @Provides accessTokenFetcher: AccessTokenFetcher,
      @Provides deviceIdFetcher: DeviceIdFetcher,
      @Provides languageSource: IosLanguageSource,
      @Provides networkLogPolicy: NetworkLogPolicy,
    ): TestIosGraph
  }
}

class IosGraphTest {
  @BeforeTest
  fun installLoggers() {
    IosLogcatLogger.install()
    RumLogger.install(LogcatRumLogger)
  }

  @AfterTest
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `the holder builds the iOS graph on first use`() {
    RumLogger.uninstall()
    val holder = createIosGraphHolder(Flavor.Staging, tokenFetcher, deviceIdFetcher, languageSource, quietPolicy)
    assertThat(RumLogger.isInstalled).isFalse()

    assertThat(holder.graph).isInstanceOf<IosGraph>()
    assertThat(RumLogger.isInstalled).isTrue()
  }

  @Test
  fun `a production build never logs bodies or GraphQL data`() {
    val policy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = true)
      .allowedFor(Flavor.Production)

    assertThat(listOf(policy.logsHttpBodies, policy.logsGraphqlData, policy.logsGraphqlErrors))
      .containsExactly(false, false, true)
  }

  @Test
  fun `a production holder given a permissive policy builds a graph that logs no bodies or GraphQL data`() {
    val permissive = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = true)
    val holder = createIosGraphHolder(Flavor.Production, tokenFetcher, deviceIdFetcher, languageSource, permissive)

    val policy = holder.component<IosGraph>().networkLogPolicy

    assertThat(listOf(policy.logsHttpBodies, policy.logsGraphqlData, policy.logsGraphqlErrors))
      .containsExactly(false, false, true)
  }

  @Test
  fun `clearing the network cache does not throw on iOS`() {
    runBlocking { testGraph().networkCacheManager.clearCache() }
  }

  @Test
  fun `a read delivers its value and sends the token and the device id`() {
    val graph = testGraph()
    val sent = graph.httpClient.answerWith(MEMBER_JSON)

    val delivered = iosGraphHolder { graph }.exampleReads().memberIdNative().await()

    assertThat(delivered.value).isEqualTo("member-1")
    val headers = sent.single()
    assertThat(headers[HttpHeaders.Authorization]).isEqualTo("Bearer $ACCESS_TOKEN")
    assertThat(headers["hedvig-device-id"]).isEqualTo(DEVICE_ID)
  }

  @Test
  fun `a failed read reaches Swift as a ReadFailure in the error`() {
    val graph = testGraph()
    graph.httpClient.answerWith(null)

    val delivered = iosGraphHolder { graph }.exampleReads().memberIdNative().await()

    assertThat(delivered.value).isNull()
    val failure = delivered.error?.userInfo?.get("KotlinException") as? ReadFailure
    assertThat(failure?.kind).isEqualTo(ReadFailureKind.OTHER)
  }

  @Test
  fun `a read without a token that the backend answers unauthenticated fails as UNAUTHENTICATED`() {
    val graph = testGraph(accessTokenFetcher = noTokenFetcher)
    val sent = graph.httpClient.answerWith(UNAUTHENTICATED_JSON)

    val delivered = iosGraphHolder { graph }.exampleReads().memberIdNative().await()

    assertThat(sent.single()[HttpHeaders.Authorization]).isNull()
    val failure = delivered.error?.userInfo?.get("KotlinException") as? ReadFailure
    assertThat(failure?.kind).isEqualTo(ReadFailureKind.UNAUTHENTICATED)
  }

  @Test
  fun `a throwing fetcher fails the read as NETWORK and sends no request`() {
    val graph = testGraph(accessTokenFetcher = throwingFetcher)
    val sent = graph.httpClient.answerWith(MEMBER_JSON)

    val delivered = iosGraphHolder { graph }.exampleReads().memberIdNative().await()

    assertThat(sent).isEmpty()
    val failure = delivered.error?.userInfo?.get("KotlinException") as? ReadFailure
    assertThat(failure?.kind).isEqualTo(ReadFailureKind.NETWORK)
  }

  @Test
  fun `the public client sends no token while the member has one`() {
    val graph = testGraph()
    val sent = graph.baseHttpClient.answerWith(MEMBER_JSON)

    runBlocking { graph.publicApolloClient.query(PayingMemberPhoneNumberQuery()).execute() }

    assertThat(sent.single()[HttpHeaders.Authorization]).isNull()
  }
}

private val tokenFetcher = object : AccessTokenFetcher {
  override suspend fun fetch(): String = ACCESS_TOKEN
}

private val noTokenFetcher = object : AccessTokenFetcher {
  override suspend fun fetch(): String? = null
}

private val throwingFetcher = object : AccessTokenFetcher {
  override suspend fun fetch(): String? = throw IllegalStateException("The token could not be refreshed")
}

private val deviceIdFetcher = object : DeviceIdFetcher {
  override suspend fun fetch(): String = DEVICE_ID
}

private val languageSource = object : IosLanguageSource {
  override fun currentLanguage(): Language = Language.EN_SE
}

private val quietPolicy = NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false, logsGraphqlErrors = false)

private fun testGraph(accessTokenFetcher: AccessTokenFetcher = tokenFetcher): TestIosGraph =
  createGraphFactory<TestIosGraph.Factory>().create(
    appBuildConfig = IosAppBuildConfig(Flavor.Staging),
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageSource = languageSource,
    networkLogPolicy = quietPolicy,
  )

/**
 * Answers every request [this] sends with [json], or HTTP 500 when it is null, instead of sending it, and records the
 * request's headers. The interceptor is the client's last, so it sees every header the client added.
 */
private fun HttpClient.answerWith(json: String?): List<Headers> {
  val sent = mutableListOf<Headers>()
  val backend = HttpClient(
    MockEngine {
      if (json == null) {
        respondError(HttpStatusCode.InternalServerError)
      } else {
        respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      }
    },
  )
  plugin(HttpSend).intercept { request ->
    sent += request.headers.build()
    backend.request(request.url.buildString()).call
  }
  return sent
}

private class Delivered<T>(val value: T?, val error: NSError?)

/** Starts the read as Swift does and runs the main loop until its callback arrives. */
private fun <T> NativeSuspend<T>.await(): Delivered<T> {
  val delivered = AtomicReference<Delivered<T>?>(null)
  this(
    { value, unit ->
      delivered.value = Delivered(value, null)
      unit
    },
    { error, unit ->
      delivered.value = Delivered(null, error)
      unit
    },
    { error, unit ->
      delivered.value = Delivered(null, error)
      unit
    },
  )
  val start = TimeSource.Monotonic.markNow()
  while (delivered.value == null && start.elapsedNow() < 10.seconds) {
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return checkNotNull(delivered.value) { "The read did not finish" }
}

private const val ACCESS_TOKEN = "test-access-token"
private const val DEVICE_ID = "test-device-id"

private val MEMBER_JSON = """
  {"data": {"currentMember": {"__typename": "Member", "id": "member-1", "phoneNumber": null}}}
""".trimIndent()

private val UNAUTHENTICATED_JSON = """
  {"data": null, "errors": [{"message": "Unauthenticated", "extensions": {"errorType": "UNAUTHENTICATED"}}]}
""".trimIndent()
