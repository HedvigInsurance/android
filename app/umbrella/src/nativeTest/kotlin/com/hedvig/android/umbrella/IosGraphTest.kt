package com.hedvig.android.umbrella

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
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
import assertk.assertions.single
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
import com.hedvig.android.feature.payments.ios.PaymentDetailsScreenState
import com.hedvig.android.feature.payments.ios.PaymentHistoryScreenState
import com.hedvig.android.feature.payments.ios.PaymentManualChargeScreenState
import com.hedvig.android.feature.payments.ios.PaymentOverviewScreenState
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModelFactory
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.ios.TravelCertificateChooseContractScreenState
import com.hedvig.android.feature.travelcertificate.ios.TravelCertificateDateInputScreenState
import com.hedvig.android.feature.travelcertificate.ios.TravelCertificateHistoryScreenState
import com.hedvig.android.feature.travelcertificate.ios.TravelCertificateTravellersScreenState
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModelFactory
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModelFactory
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.AtomicInt
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import octopus.PayingMemberPhoneNumberQuery
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
  fun `the holder builds the payments screen models without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()

    val history = holder.paymentHistoryScreenModel()
    val detailsWithChargeId = holder.paymentDetailsScreenModel("charge-1")
    val detailsWithoutChargeId = holder.paymentDetailsScreenModel(null)
    val states = listOf(history.currentState, detailsWithChargeId.currentState, detailsWithoutChargeId.currentState)
    pumpMainLoop(1.seconds)
    history.close()
    detailsWithChargeId.close()
    detailsWithoutChargeId.close()

    assertThat(states).containsExactly(
      PaymentHistoryScreenState.Loading,
      PaymentDetailsScreenState.Loading,
      PaymentDetailsScreenState.Loading,
    )
    assertThat(
      logs.messages().filter { it.endsWith("ViewModel cleared") },
    ).containsExactlyInAnyOrder(
      "PaymentHistoryViewModel cleared",
      "PaymentDetailsViewModel cleared",
      "PaymentDetailsViewModel cleared",
    )
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `the holder builds the overview and manual charge screen models without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()

    val overview = holder.paymentOverviewScreenModel()
    val manualCharge = holder.paymentManualChargeScreenModel()
    val states = listOf(overview.currentState, manualCharge.currentState)
    pumpMainLoop(1.seconds)
    overview.close()
    manualCharge.close()

    assertThat(states).containsExactly(
      PaymentOverviewScreenState.Loading,
      PaymentManualChargeScreenState.Loading,
    )
    assertThat(
      logs.messages().filter { it.endsWith("ViewModel cleared") },
    ).containsExactlyInAnyOrder(
      "PaymentsViewModel cleared",
      "ManualChargeViewModel cleared",
    )
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `the graph resolves the payments ViewModels without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    val store = ViewModelStore()
    val provider = ViewModelProvider.create(store, holder.graph.metroViewModelFactory)
    val paymentsClasses = holder.graph.viewModelProviders.keys.filter {
      it.qualifiedName.orEmpty().startsWith("com.hedvig.android.feature.payments.")
    }

    val viewModels = paymentsClasses.map { provider[it] }
    pumpMainLoop(1.seconds)
    store.clear()

    assertThat(viewModels.map { it::class.simpleName }).containsExactlyInAnyOrder(
      "DiscountsViewModel",
      "ManualChargeViewModel",
      "PaymentHistoryViewModel",
      "PaymentsViewModel",
    )
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  // The ViewModel is internal to its module; this test only needs it as a ViewModel.
  @Suppress("INFERRED_INVISIBLE_RETURN_TYPE_WARNING")
  @Test
  fun `the graph creates the payment details ViewModel with and without a charge id without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    val factory = holder.graph.metroViewModelFactory
      .createManuallyAssistedFactory(PaymentDetailsViewModelFactory::class)
      .invoke()
    val store = ViewModelStore()

    val withChargeId = store.hold("with charge id") { factory.create("charge-1") }
    val withoutChargeId = store.hold("without charge id") { factory.create(null) }
    pumpMainLoop(1.seconds)
    store.clear()

    assertThat(withChargeId::class.simpleName).isEqualTo("PaymentDetailsViewModel")
    assertThat(withoutChargeId::class.simpleName).isEqualTo("PaymentDetailsViewModel")
    assertThat(withChargeId === withoutChargeId).isFalse()
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `the graph resolves the travel certificate ViewModels without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    val store = ViewModelStore()
    val provider = ViewModelProvider.create(store, holder.graph.metroViewModelFactory)
    val travelCertificateClasses = holder.graph.viewModelProviders.keys.filter {
      it.qualifiedName.orEmpty().startsWith("com.hedvig.android.feature.travelcertificate.")
    }

    val viewModels = travelCertificateClasses.map { provider[it] }
    pumpMainLoop(1.seconds)
    store.clear()

    assertThat(viewModels.map { it::class.simpleName }).containsExactlyInAnyOrder(
      "CertificateHistoryViewModel",
      "ChooseContractForCertificateViewModel",
      "TravelCertificateOverviewViewModel",
    )
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  // The ViewModels are internal to their module; this test only needs them as ViewModels.
  @Suppress("INFERRED_INVISIBLE_RETURN_TYPE_WARNING")
  @Test
  fun `the graph creates the assisted travel certificate ViewModels without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    val factory = holder.graph.metroViewModelFactory
    val dateInputFactory = factory
      .createManuallyAssistedFactory(TravelCertificateDateInputViewModelFactory::class)
      .invoke()
    val travellersFactory = factory
      .createManuallyAssistedFactory(TravelCertificateTravellersInputViewModelFactory::class)
      .invoke()
    // The step's constructor is internal to its module, so the test decodes one the way a saved back stack would.
    val step = Json.decodeFromString(
      TravelCertificateTravellersStep.serializer(),
      """{"email":"member@hedvig.com","travelDate":"2026-12-01","contractId":"contract-1"}""",
    )
    val store = ViewModelStore()

    val dateWithContractId = store.hold("date with contract id") { dateInputFactory.create("contract-1") }
    val dateWithoutContractId = store.hold("date without contract id") { dateInputFactory.create(null) }
    val travellers = store.hold("travellers") { travellersFactory.create(step) }
    pumpMainLoop(1.seconds)
    store.clear()

    assertThat(dateWithContractId::class.simpleName).isEqualTo("TravelCertificateDateInputViewModel")
    assertThat(dateWithoutContractId::class.simpleName).isEqualTo("TravelCertificateDateInputViewModel")
    assertThat(dateWithContractId === dateWithoutContractId).isFalse()
    assertThat(travellers::class.simpleName).isEqualTo("TravelCertificateTravellersInputViewModel")
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
    assertThat(inputs.deviceIdFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `the holder builds the travel certificate screen models without a request`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    // The step's constructor is internal to its module, so the test decodes one the way a saved back stack would.
    val step = Json.decodeFromString(
      TravelCertificateTravellersStep.serializer(),
      """{"email":"member@hedvig.com","travelDate":"2026-12-01","contractId":"contract-1"}""",
    )

    val models = listOf(
      holder.travelCertificateHistoryScreenModel(),
      holder.travelCertificateChooseContractScreenModel(),
      holder.travelCertificateDateInputScreenModel("contract-1"),
      holder.travelCertificateDateInputScreenModel(null),
      holder.travelCertificateTravellersScreenModel(step),
    )
    val states = models.map { it.currentState }
    pumpMainLoop(1.seconds)
    models.forEach { it.close() }

    assertThat(states).containsExactly(
      TravelCertificateHistoryScreenState.Loading,
      TravelCertificateChooseContractScreenState.Loading,
      TravelCertificateDateInputScreenState.Loading,
      TravelCertificateDateInputScreenState.Loading,
      TravelCertificateTravellersScreenState.Loading,
    )
    assertThat(
      logs.messages().filter { it.endsWith("ViewModel cleared") },
    ).containsExactlyInAnyOrder(
      "CertificateHistoryViewModel cleared",
      "ChooseContractForCertificateViewModel cleared",
      "TravelCertificateDateInputViewModel cleared",
      "TravelCertificateDateInputViewModel cleared",
      "TravelCertificateTravellersInputViewModel cleared",
    )
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

    val collection = MainScope().launch { model.stateUpdates.collect {} }
    assertThat(pumpMainLoopUntil { inputs.accessTokenFetcher.calls.value == 1 }).isTrue()
    pumpMainLoop(2.seconds)
    collection.cancel()
    model.close()
    val response = runBlocking {
      holder.graph.apolloClient.query(PayingMemberPhoneNumberQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
    }

    assertThat(response.exception).isNotNull().isInstanceOf<ApolloNetworkException>()
    assertThat(response.exception?.message).isEqualTo("token refresh failed")
    assertThat(response.data).isNull()
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(2)
  }

  @Test
  fun `a query on the public Apollo client is sent without an Authorization header and without fetching a token`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    val sent = mutableListOf<Headers>()
    // The graph's Darwin engine cannot be replaced, so the request is recorded just before the engine and aborted
    // there. The client's default headers are already on it by then.
    holder.graph.baseHttpClient.plugin(HttpSend).intercept { request ->
      sent += request.headers.build()
      throw IllegalStateException("Request recorded, not sent")
    }

    val response = runBlocking {
      holder.graph.publicApolloClient
        .query(PayingMemberPhoneNumberQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .execute()
    }

    assertThat(response.exception?.message).isEqualTo("Request recorded, not sent")
    assertThat(sent.size).isEqualTo(1)
    assertThat(sent.single()["Authorization"]).isNull()
    assertThat(sent.single()["Accept-Language"]).isNotNull()
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(0)
  }

  @Test
  fun `an UNAUTHENTICATED response does not log out or change demo mode`() {
    val inputs = FakeInputs()
    val holder = inputs.createHolder()
    holder.seedStore()

    val response = runBlocking {
      holder.respondingWith(unauthenticatedJson)
        .query(PayingMemberPhoneNumberQuery())
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
        .query(PayingMemberPhoneNumberQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .execute()
    }

    assertThat(response.errors.orEmpty().map { it.message }).containsExactly("Something went wrong")
    assertThat(logs.entries().filter { (_, message) -> message.startsWith("RUM error") }).containsExactly(
      LogPriority.WARN to
        "RUM error from NETWORK: GraphQL errors for PayingMemberPhoneNumber: [Something went wrong] attribute keys: []",
    )
  }

  @Test
  fun `with the policy off no logged line carries GraphQL response data or error extensions`() {
    val holder = FakeInputs().createHolder()

    runBlocking {
      for (json in listOf(sentinelDataJson, sentinelDataAndErrorJson)) {
        holder.respondingWith(json).query(PayingMemberPhoneNumberQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
      }
    }

    assertThat(logs.messages().filter { "GraphQL" in it }).containsExactly(
      "GraphQL request for PayingMemberPhoneNumber START.",
      "GraphQL request for PayingMemberPhoneNumber EMISSION.",
      "GraphQL request for PayingMemberPhoneNumber END.",
      "GraphQL request for PayingMemberPhoneNumber START.",
      "GraphQL request for PayingMemberPhoneNumber EMISSION.",
      "GraphQL errors for PayingMemberPhoneNumber: [Something went wrong]",
      "RUM error from NETWORK: GraphQL errors for PayingMemberPhoneNumber: [Something went wrong] attribute keys: []",
      "GraphQL request for PayingMemberPhoneNumber END.",
    )
    assertThat(logs.messages().filter { DATA_SENTINEL in it || EXTENSION_SENTINEL in it }).isEmpty()
  }

  @Test
  fun `with the policy on GraphQL response data and error extensions are logged`() {
    val holder = FakeInputs(networkLogPolicy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true))
      .createHolder()

    runBlocking {
      for (json in listOf(sentinelDataJson, sentinelDataAndErrorJson)) {
        holder.respondingWith(json).query(PayingMemberPhoneNumberQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
      }
    }

    assertThat(logs.messages().any { DATA_SENTINEL in it }).isTrue()
    assertThat(logs.messages().any { EXTENSION_SENTINEL in it }).isTrue()
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

/** Keeps the ViewModel from [create] in this store under [key], so clearing the store clears it. */
private fun ViewModelStore.hold(key: String, create: () -> ViewModel): ViewModel {
  val factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = modelClass.cast(create())
  }
  return ViewModelProvider.create(this, factory)[key, ViewModel::class]
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
  val response = runBlocking { respondingWith(memberJson).query(PayingMemberPhoneNumberQuery()).execute() }
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
  val networkLogPolicy: NetworkLogPolicy = NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false),
) {
  fun createHolder(): IosGraphHolder = createIosGraphHolder(
    appBuildConfig = FakeAppBuildConfig,
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageStorage = FakeLanguageStorage,
    demoMode = demoMode,
    networkLogPolicy = networkLogPolicy,
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

private val memberJson = """
  {
    "data": {
      "currentMember": {
        "__typename": "Member",
        "id": "member-1",
        "phoneNumber": null
      }
    }
  }
""".trimIndent()

private const val DATA_SENTINEL = "member-sentinel-6d0e"
private const val EXTENSION_SENTINEL = "extension-sentinel-a41b"

private val sentinelDataJson = """
  {
    "data": {
      "currentMember": {
        "__typename": "Member",
        "id": "$DATA_SENTINEL",
        "phoneNumber": null
      }
    }
  }
""".trimIndent()

private val sentinelDataAndErrorJson = """
  {
    "data": {
      "currentMember": {
        "__typename": "Member",
        "id": "$DATA_SENTINEL",
        "phoneNumber": null
      }
    },
    "errors": [
      {
        "message": "Something went wrong",
        "extensions": { "detail": "$EXTENSION_SENTINEL" }
      }
    ],
    "extensions": { "trace": "$EXTENSION_SENTINEL" }
  }
""".trimIndent()
