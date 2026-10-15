package com.hedvig.android.authlib

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
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

  /**
   * A 5xx carries the framework's default error body, which has nothing to do with the typed login response. The
   * status code is the only signal the member-facing UI can key a "try again in a few minutes" state on, so it has to
   * survive instead of being lost in a deserialization failure.
   */
  @Test
  fun `a 5xx response to a login attempt is reported as a server error carrying the status code`() = runTest {
    val engine = MockEngine {
      respond(
        content = """
          |{"timestamp":"2026-10-09T17:01:21.475Z","status":500,"error":"Internal Server Error","path":"/member-login"}
        """.trimMargin(),
        status = HttpStatusCode.InternalServerError,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    val repository = networkAuthRepositoryWithEngine(AuthEnvironment.STAGING, { emptyMap() }, engine)

    val result = repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(result)
      .isInstanceOf<AuthAttemptResult.Error.ServerError>()
      .prop(AuthAttemptResult.Error.ServerError::statusCode)
      .isEqualTo(500)
  }

  @Test
  fun `a typed error body on a non-5xx status still reaches the member as the backend's own reason`() = runTest {
    val engine = MockEngine {
      respond(
        content = """{"result":"error","reason":"Too many attempts"}""",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    val repository = networkAuthRepositoryWithEngine(AuthEnvironment.STAGING, { emptyMap() }, engine)

    val result = repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(result).isEqualTo(AuthAttemptResult.Error.Localised("Too many attempts"))
  }
}
