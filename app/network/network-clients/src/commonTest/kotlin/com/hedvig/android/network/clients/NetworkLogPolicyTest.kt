package com.hedvig.android.network.clients

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.benasher44.uuid.uuid4
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.apollo.addHedvigInterceptors
import com.hedvig.android.network.clients.di.buildKtorClient
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Runs the HTTP client and the GraphQL logging with values marked by [SENTINELS] and checks which logged line or RUM
 * error carries one.
 */
@OptIn(ApolloExperimental::class)
class NetworkLogPolicyTest {
  private val logs = CapturingLogcatLogger()
  private val rum = CapturingRumLogger()

  @BeforeTest
  fun install() {
    LogcatLogger.install(logs)
    RumLogger.install(rum)
  }

  @AfterTest
  fun uninstall() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `with HTTP bodies off no logged line carries a request or response body`() = runTest {
    exchangeHttpBodies(NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false))

    assertEquals(emptyList(), loggedSentinels())
  }

  @Test
  fun `with HTTP bodies on the request and response bodies are logged`() = runTest {
    exchangeHttpBodies(NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = false))

    assertEquals(listOf(REQUEST_BODY_SENTINEL, RESPONSE_BODY_SENTINEL), loggedSentinels())
  }

  @Test
  fun `with GraphQL data off no logged line or RUM error carries response data and errors are still logged`() =
    runTest {
      exchangeGraphql(NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false))

      assertEquals(listOf(ERROR_EXTENSION_SENTINEL, RESPONSE_EXTENSION_SENTINEL), loggedSentinels())
      assertEquals(listOf(ERROR_EXTENSION_SENTINEL, RESPONSE_EXTENSION_SENTINEL), rumSentinels())
      val messages = logs.entries().map { it.message }
      assertTrue("GraphQL request for Sentinel EMISSION." in messages, "$messages")
      assertTrue(messages.any { it.startsWith("GraphQL errors for Sentinel: [Something failed] attributes: {errors=") })
      assertTrue(messages.any { it.endsWith("There was data and errors at the same time!") })
    }

  @Test
  fun `with GraphQL data on the response data and extensions are logged and reported`() = runTest {
    exchangeGraphql(NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = true))

    assertEquals(
      listOf(DATA_SENTINEL, ERROR_EXTENSION_SENTINEL, RESPONSE_EXTENSION_SENTINEL),
      loggedSentinels(),
    )
    assertEquals(listOf(DATA_SENTINEL, ERROR_EXTENSION_SENTINEL, RESPONSE_EXTENSION_SENTINEL), rumSentinels())
  }

  private suspend fun exchangeHttpBodies(networkLogPolicy: NetworkLogPolicy) {
    val engine = mockEngineFactory {
      respond(
        content = """{"value":"$RESPONSE_BODY_SENTINEL"}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    val client = buildKtorClient(
      engineFactory = engine,
      hedvigBuildConstants = FakeHedvigBuildConstants(isProduction = false),
      languageService = FakeLanguageService,
      deviceIdFetcher = FakeDeviceIdFetcher,
      extraKtorClientConfiguration = NoopExtraKtorClientConfiguration(),
      networkLogPolicy = networkLogPolicy,
    )
    val response = client.post("https://example.test/graphql") {
      contentType(ContentType.Application.Json)
      setBody("""{"value":"$REQUEST_BODY_SENTINEL"}""")
    }
    check(RESPONSE_BODY_SENTINEL in response.bodyAsText())
    client.close()
  }

  private suspend fun exchangeGraphql(networkLogPolicy: NetworkLogPolicy) {
    val apolloClient = ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .addHedvigInterceptors(emptyList(), networkLogPolicy)
      .build()
    apolloClient.enqueueTestResponse(SentinelQuery, SentinelQuery.Data(DATA_SENTINEL))
    apolloClient.enqueueTestResponse(
      ApolloResponse.Builder(SentinelQuery, uuid4())
        .data(SentinelQuery.Data(DATA_SENTINEL))
        .errors(listOf(Error.Builder("Something failed").putExtension("detail", ERROR_EXTENSION_SENTINEL).build()))
        .extensions(mapOf("trace" to RESPONSE_EXTENSION_SENTINEL))
        .build(),
    )
    apolloClient.enqueueTestNetworkError()
    repeat(3) { apolloClient.query(SentinelQuery).execute() }
    apolloClient.close()
  }

  private fun loggedSentinels(): List<String> = SENTINELS.filter { sentinel ->
    logs.entries().any { sentinel in it.message || sentinel in it.tag.orEmpty() }
  }

  private fun rumSentinels(): List<String> = SENTINELS.filter { sentinel ->
    rum.errors().any { sentinel in it.message || sentinel in it.attributes.toString() }
  }
}

private const val REQUEST_BODY_SENTINEL = "request-body-sentinel-91c2"
private const val RESPONSE_BODY_SENTINEL = "response-body-sentinel-4e7a"
private const val DATA_SENTINEL = "data-sentinel-0b5d"
private const val ERROR_EXTENSION_SENTINEL = "error-extension-sentinel-c83f"
private const val RESPONSE_EXTENSION_SENTINEL = "response-extension-sentinel-26e1"
private val SENTINELS = listOf(
  REQUEST_BODY_SENTINEL,
  RESPONSE_BODY_SENTINEL,
  DATA_SENTINEL,
  ERROR_EXTENSION_SENTINEL,
  RESPONSE_EXTENSION_SENTINEL,
)
