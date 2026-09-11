package com.hedvig.android.auth.di

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.hedvig.android.authlib.LoginMethod
import com.hedvig.android.authlib.OtpMarket
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AuthMetroProvidersTest {
  private val providers = object : AuthMetroProviders {}

  /**
   * The repository is only instrumented because this provider threads the supplied engine through.
   * Reverting it to the two-argument `NetworkAuthRepository` constructor still compiles, and no
   * other test would notice, so this asserts the request actually leaves through the given engine.
   */
  @Test
  fun `the provided repository sends its requests through the supplied engine`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = providers.provideAuthRepository(buildConstants(isProduction = false), engine)
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory).hasSize(1)
  }

  @Test
  fun `a production build reaches the production auth host`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = providers.provideAuthRepository(buildConstants(isProduction = true), engine)
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory.single().url.host).isEqualTo("auth.prod.hedvigit.com")
  }
}

private fun buildConstants(isProduction: Boolean) = object : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = ""
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = listOf("")
  override val appVersionName: String = ""
  override val appVersionCode: String = ""
  override val appPackageId: String = ""
  override val isDebug: Boolean = false
  override val isProduction: Boolean = isProduction
  override val buildApiVersion: Int = Int.MAX_VALUE
  override val platformName: String = ""
  override val model: String = ""
  override val userAgent: String = ""
}
