package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.NetworkLogPolicy
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import octopus.PayingMemberPhoneNumberQuery

/** The real bindings over a canned HTTP engine, sent from a background thread as the iOS app's reads are. */
class IosGraphNetworkTest {
  private val sink = RecordingSink()

  @BeforeTest
  fun installSink() {
    IosLogcatLogger.install(sink, LogPriority.VERBOSE)
    RumLogger.install(LogcatRumLogger)
  }

  @AfterTest
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `a member request carries the token and the iOS headers and calls the inputs from a background thread`() {
    val backend = RecordingBackend(mapOf(OPERATION to MEMBER_JSON))
    val inputs = FakeInputs()
    val graph = inputs.mockedGraph(backend)

    val response = graph.apolloClient.query()

    assertThat(response.exception).isNull()
    val headers = backend.sent().single().headers
    assertThat(headers["Authorization"]).isEqualTo("Bearer $ACCESS_TOKEN")
    assertThat(headers["apollographql-client-name"]!!.startsWith("iOS:")).isTrue()
    assertThat(headers["Accept-Language"]).isEqualTo("en-SE")
    assertThat(headers["X-Platform"]).isEqualTo("ios")
    assertThat(headers["Hedvig-TimeZone"]).isNotNull()
    // Kotlin calls the inputs from any thread; a request made off the main thread calls them off it.
    for (input in listOf(inputs.accessTokenFetcher, inputs.languageSource)) {
      assertThat(input.calledOnMainThread().any { onMainThread -> !onMainThread }).isTrue()
    }
  }

  @Test
  fun `a public request sends no token and never asks for one even while a member is logged in`() {
    val backend = RecordingBackend(mapOf(OPERATION to MEMBER_JSON))
    val inputs = FakeInputs()
    val graph = inputs.mockedGraph(backend)

    graph.apolloClient.query()
    val tokenCallsAfterMemberRequest = inputs.accessTokenFetcher.calls.value
    graph.publicApolloClient.query()

    val sent = backend.sent()
    assertThat(sent.map { it.headers["Authorization"] }).containsExactly("Bearer $ACCESS_TOKEN", null)
    assertThat(sent.last().headers["hedvig-device-id"]).isEqualTo(DEVICE_ID)
    assertThat(inputs.accessTokenFetcher.calls.value).isEqualTo(tokenCallsAfterMemberRequest)
  }

  @Test
  fun `a fetch policy that asks for the cache is answered from the network every time`() {
    val backend = RecordingBackend(mapOf(OPERATION to MEMBER_JSON))
    val graph = FakeInputs().mockedGraph(backend)

    repeat(2) { graph.apolloClient.query(FetchPolicy.CacheFirst) }
    repeat(2) { graph.publicApolloClient.query(FetchPolicy.CacheFirst) }

    assertThat(backend.sent().size).isEqualTo(4)
  }

  @Test
  fun `a throwing token fetcher fails the request without sending it`() {
    val backend = RecordingBackend(mapOf(OPERATION to MEMBER_JSON))
    val graph = FakeInputs(accessTokenFetcher = FakeAccessTokenFetcher(throws = true)).mockedGraph(backend)

    val response = graph.apolloClient.query()

    assertThat(response.exception).isNotNull().isInstanceOf<ApolloNetworkException>()
    assertThat(response.data).isNull()
    assertThat(backend.sent()).isEmpty()
  }

  @Test
  fun `an UNAUTHENTICATED answer is an ordinary error`() {
    val backend = RecordingBackend(mapOf(OPERATION to UNAUTHENTICATED_JSON))
    val graph = FakeInputs().mockedGraph(backend)

    val response = graph.apolloClient.query()

    assertThat(response.errors.orEmpty().map { it.extensions?.get("errorType") }).containsExactly("UNAUTHENTICATED")
  }

  @Test
  fun `with the policy off no line the iOS app's sink receives carries a body or GraphQL data or error text`() {
    val backend = RecordingBackend(mapOf(OPERATION to SENTINEL_DATA_JSON))
    val graph = FakeInputs().mockedGraph(backend)

    graph.apolloClient.query()
    backend.answer(OPERATION, SENTINEL_DATA_AND_ERROR_JSON)
    graph.apolloClient.query()

    assertThat(sink.messages().filter { DATA_SENTINEL in it || EXTENSION_SENTINEL in it || ERROR_TEXT in it })
      .isEmpty()
    val errorLine = "GraphQL errors for $OPERATION: 1 (error types [null])"
    assertThat(sink.messages().filter { "GraphQL" in it }).containsExactly(
      "GraphQL request for $OPERATION START.",
      "GraphQL request for $OPERATION EMISSION.",
      "GraphQL request for $OPERATION END.",
      "GraphQL request for $OPERATION START.",
      "GraphQL request for $OPERATION EMISSION.",
      "$errorLine attributes: {} There was data and errors at the same time!",
      "RUM error from NETWORK: $errorLine attribute keys: []",
      "GraphQL request for $OPERATION END.",
    )
  }

  @Test
  fun `in production the error text reaches the iOS app's sink and the data does not`() {
    val backend = RecordingBackend(mapOf(OPERATION to SENTINEL_DATA_AND_ERROR_JSON))
    val inputs = FakeInputs(
      appFlavor = com.hedvig.android.core.buildconstants.Flavor.Production,
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = true),
    )
    val graph = inputs.mockedGraph(backend)

    graph.apolloClient.query()

    assertThat(sink.messages().filter { DATA_SENTINEL in it }).isEmpty()
    assertThat(sink.messages().any { ERROR_TEXT in it }).isTrue()
  }

  @Test
  fun `with the policy on bodies and GraphQL data reach the iOS app's sink`() {
    val backend = RecordingBackend(mapOf(OPERATION to SENTINEL_DATA_AND_ERROR_JSON))
    val inputs = FakeInputs(
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = true),
    )
    val graph = inputs.mockedGraph(backend)

    graph.apolloClient.query()

    val entries = sink.entries()
    assertThat(entries.any { (priority, line) -> priority == LogPriority.VERBOSE && DATA_SENTINEL in line }).isTrue()
    assertThat(entries.any { (priority, line) -> priority == LogPriority.DEBUG && DATA_SENTINEL in line }).isTrue()
    assertThat(entries.any { (_, line) -> ERROR_TEXT in line }).isTrue()
  }
}

/** Sends the test operation from a background thread, as a read on `Dispatchers.Default` does. */
private fun ApolloClient.query(fetchPolicy: FetchPolicy = FetchPolicy.NetworkOnly) = runBlocking {
  withContext(Dispatchers.Default) {
    query(PayingMemberPhoneNumberQuery()).fetchPolicy(fetchPolicy).execute()
  }
}

private const val OPERATION = "PayingMemberPhoneNumber"
private const val DATA_SENTINEL = "member-sentinel-6d0e"
private const val EXTENSION_SENTINEL = "extension-sentinel-a41b"
private const val ERROR_TEXT = "Something went wrong for member-text-93c1"

private val MEMBER_JSON = """
  {"data": {"currentMember": {"__typename": "Member", "id": "member-1", "phoneNumber": null}}}
""".trimIndent()

private val UNAUTHENTICATED_JSON = """
  {"errors": [{"message": "Unauthenticated", "extensions": {"errorType": "UNAUTHENTICATED"}}]}
""".trimIndent()

private val SENTINEL_DATA_JSON = """
  {"data": {"currentMember": {"__typename": "Member", "id": "$DATA_SENTINEL", "phoneNumber": null}}}
""".trimIndent()

private val SENTINEL_DATA_AND_ERROR_JSON = """
  {
    "data": {"currentMember": {"__typename": "Member", "id": "$DATA_SENTINEL", "phoneNumber": null}},
    "errors": [{"message": "$ERROR_TEXT", "extensions": {"detail": "$EXTENSION_SENTINEL"}}],
    "extensions": {"trace": "$EXTENSION_SENTINEL"}
  }
""".trimIndent()
