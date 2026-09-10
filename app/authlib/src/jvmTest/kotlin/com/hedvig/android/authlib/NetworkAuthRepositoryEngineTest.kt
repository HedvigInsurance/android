package com.hedvig.android.authlib

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class NetworkAuthRepositoryEngineTest {
  /**
   * The host application supplies an engine so it can attach its own network observability. Nothing
   * else observes that the engine is honoured, so without this the wiring can be dropped while every
   * other test, and the app itself, keeps passing.
   */
  @Test
  fun `requests are sent through the engine the caller supplied`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = networkAuthRepositoryWithEngine(
      environment = AuthEnvironment.STAGING,
      additionalHttpHeadersProvider = { emptyMap() },
      engine = engine,
    )
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory).hasSize(1)
  }

  @Test
  fun `the environment decides which host the request reaches`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = networkAuthRepositoryWithEngine(
      environment = AuthEnvironment.PRODUCTION,
      additionalHttpHeadersProvider = { emptyMap() },
      engine = engine,
    )
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory.single().url.host).isEqualTo("auth.prod.hedvigit.com")
  }
}
