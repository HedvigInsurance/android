package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.TestLogcatLoggingRule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule

class JvmUnleashClientTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  @AfterTest
  fun tearDown() {
    scope.cancel()
  }

  @Test
  fun `a toggle present and enabled reads true, fetched with the context Android sends`() = runBlocking {
    val engine = MockEngine { respondToggles("enable_claim_intent_resume") }
    val client = client(engine, memberIds = MutableStateFlow("member-1"))

    withTimeout(5.seconds) { client.awaitReady() }

    assertTrue(client.valueOf(Feature.ENABLE_CLAIM_INTENT_RESUME))
    val request = engine.requestHistory.single()
    assertEquals("https://eu.app.unleash-hosted.com/eubb1047/api/frontend", request.url.toString().substringBefore('?'))
    assertEquals("client-key", request.headers[HttpHeaders.Authorization])
    assertEquals("android", request.url.parameters["appName"])
    assertEquals("android", request.url.parameters["properties[appName]"])
    assertEquals("1.2.3", request.url.parameters["properties[appVersion]"])
    assertEquals("SE", request.url.parameters["properties[market]"])
    assertEquals("member-1", request.url.parameters["properties[memberId]"])
    assertEquals("member-1", request.url.parameters["userId"])
  }

  @Test
  fun `before any fetch every flag reads its never-fetched default`() {
    val client = client(MockEngine { respondToggles() })

    assertTrue(client.valueOf(Feature.DISABLE_ANALYTICS))
    assertFalse(client.valueOf(Feature.UPDATE_NECESSARY))
  }

  @Test
  fun `once fetched, an absent toggle reads false, whatever its never-fetched default`() = runBlocking {
    val client = client(MockEngine { respondToggles("enable_claim_intent_resume") })

    withTimeout(5.seconds) { client.awaitReady() }

    assertFalse(client.valueOf(Feature.UPDATE_NECESSARY))
    assertFalse(client.valueOf(Feature.DISABLE_ANALYTICS))
  }

  @Test
  fun `a failed request keeps the never-fetched defaults without throwing`() = runBlocking {
    val failingEngines = listOf(
      MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) },
      MockEngine { throw IOException("offline") },
    )
    for (engine in failingEngines) {
      val client = client(engine)

      assertNull(withTimeoutOrNull(1.seconds) { client.awaitReady() })

      assertFalse(client.hasStateForCurrentMember())
      assertTrue(client.valueOf(Feature.DISABLE_ANALYTICS))
      assertFalse(client.valueOf(Feature.UPDATE_NECESSARY))
    }
  }

  @Test
  fun `a failed request after a successful one keeps the fetched state`() = runBlocking {
    val fail = AtomicBoolean(false)
    val engine = MockEngine {
      if (fail.get()) respond(content = "", status = HttpStatusCode.InternalServerError) else respondToggles()
    }
    val client = client(engine, pollInterval = 50.milliseconds)
    val observing = client.featureUpdatedFlow.launchIn(this)
    withTimeout(5.seconds) { client.awaitReady() }

    fail.set(true)
    withTimeout(5.seconds) { while (engine.responseHistory.none { it.statusCode.value == 500 }) delay(10) }
    observing.cancel()

    assertTrue(client.hasStateForCurrentMember())
    assertFalse(client.valueOf(Feature.DISABLE_ANALYTICS))
  }

  @Test
  fun `a member change fetches again, for the new member`() = runBlocking {
    val memberIds = MutableStateFlow<String?>(null)
    val engine = MockEngine { request ->
      if (request.url.parameters["userId"] == "member-2") {
        respondToggles("enable_new_conversation_from_inbox")
      } else {
        respondToggles()
      }
    }
    val client = client(engine, memberIds = memberIds)
    val observing = client.featureUpdatedFlow.launchIn(this)
    withTimeout(5.seconds) { client.awaitReady() }
    assertFalse(client.valueOf(Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX))

    memberIds.value = "member-2"
    withTimeout(5.seconds) { while (!client.valueOf(Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX)) delay(10) }
    withTimeout(5.seconds) { client.awaitReady() }
    observing.cancel()

    assertEquals(listOf(null, "member-2"), engine.requestHistory.map { it.url.parameters["userId"] })
  }

  @Test
  fun `a member change while nothing observes is not ready on the previous member's toggles`() = runBlocking {
    val memberIds = MutableStateFlow<String?>("member-1")
    val engine = MockEngine { request ->
      if (request.url.parameters["userId"] == "member-2") awaitCancellation()
      respondToggles("enable_claim_intent_resume")
    }
    // Like MemberIdService, which maps over the token store, the member id arrives after subscription, not during it.
    val client = client(engine, memberIds = memberIds.onEach { delay(50.milliseconds) })
    val observing = client.featureUpdatedFlow.launchIn(this)
    withTimeout(5.seconds) { client.awaitReady() }
    observing.cancel()

    memberIds.value = "member-2"
    delay(300)

    assertNull(withTimeoutOrNull(1.seconds) { client.awaitReady() })
    assertFalse(client.hasStateForCurrentMember())
    // As on Android, a flag read does not wait for the member: the previous member's toggles answer until the new
    // member's fetch lands.
    assertTrue(client.valueOf(Feature.ENABLE_CLAIM_INTENT_RESUME))
  }

  @Test
  fun `nothing is fetched while nothing observes the flags`() = runBlocking {
    val engine = MockEngine { respondToggles() }
    val client = client(engine, pollInterval = 50.milliseconds)

    delay(300)
    assertEquals(0, engine.requestHistory.size)

    withTimeout(5.seconds) { client.awaitReady() }
    delay(100)
    val requestsWhenObservationStopped = engine.requestHistory.size
    delay(300)
    assertEquals(requestsWhenObservationStopped, engine.requestHistory.size)
  }

  private fun client(
    engine: MockEngine,
    memberIds: Flow<String?> = MutableStateFlow(null),
    pollInterval: Duration = 30.seconds,
  ) = JvmUnleashClient(
    httpClient = HttpClient(engine),
    clientKey = "client-key",
    appVersionName = "1.2.3",
    memberIds = memberIds,
    coroutineScope = scope,
    pollInterval = pollInterval,
  )
}

/** The frontend API's response shape: only enabled toggles are listed. */
private fun MockRequestHandleScope.respondToggles(vararg enabledToggleNames: String): HttpResponseData {
  val toggles = enabledToggleNames.joinToString(",") { name ->
    """{"name":"$name","enabled":true,"variant":{"name":"disabled","enabled":false},"impressionData":false}"""
  }
  return respond(
    content = """{"toggles":[$toggles]}""",
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
  )
}
