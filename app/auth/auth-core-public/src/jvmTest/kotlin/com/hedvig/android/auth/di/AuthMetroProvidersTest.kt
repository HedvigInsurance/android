package com.hedvig.android.auth.di

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.auth.BankIdSandboxState
import com.hedvig.android.authlib.LoginMethod
import com.hedvig.android.authlib.OtpMarket
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AuthMetroProvidersTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  private val providers = object : AuthMetroProviders {}

  /**
   * The repository is only instrumented because this provider threads the supplied engine through.
   * Reverting it to the two-argument `NetworkAuthRepository` constructor still compiles, and no
   * other test would notice, so this asserts the request actually leaves through the given engine.
   */
  @Test
  fun `the provided repository sends its requests through the supplied engine`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = providers.provideAuthRepository(buildConstants(isProduction = false), engine, sandboxState())
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory).hasSize(1)
  }

  @Test
  fun `a production build reaches the production auth host`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = providers.provideAuthRepository(buildConstants(isProduction = true), engine, sandboxState())
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory.single().url.host).isEqualTo("auth.prod.hedvigit.com")
  }

  /**
   * The auth host is decided twice, from two hand-maintained tables that nothing ties together.
   * `AuthEnvironment` in :authlib picks the URL the client actually calls, and
   * [HedvigBuildConstants.urlAuthService] is what :datadog-android hands to Datadog as the host to
   * instrument. If those drift apart, login keeps working and Datadog quietly stops recording it,
   * which is the silence #3140 existed to end.
   *
   * The rows below mirror `AndroidBuildConfig.appFlavor` and `AppConfigUrlHolder`, both private to
   * their own modules, so this cannot catch an edit made on that side alone. It does catch the
   * `AuthEnvironment` table, or the provider, drifting away from them.
   */
  @Test
  fun `every build flavour calls the same auth host it tells Datadog to instrument`() = runTest {
    val flavours = listOf(
      Flavour("Production", isProduction = true, urlAuthService = "https://auth.prod.hedvigit.com"),
      Flavour("Staging", isProduction = false, urlAuthService = "https://auth.dev.hedvigit.com"),
      Flavour("Develop", isProduction = false, urlAuthService = "https://auth.dev.hedvigit.com"),
    )

    for (flavour in flavours) {
      val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
      val buildConstants = buildConstants(flavour.isProduction, flavour.urlAuthService)

      providers
        .provideAuthRepository(buildConstants, engine, sandboxState())
        .startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

      assertThat(engine.requestHistory.single().url.host, name = flavour.name)
        .isEqualTo(Url(buildConstants.urlAuthService).host)
    }
  }

  @Test
  fun `a non-production build reaches the staging auth host`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }

    val repository = providers.provideAuthRepository(buildConstants(isProduction = false), engine, sandboxState())
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory.single().url.host).isEqualTo("auth.dev.hedvigit.com")
  }

  @Test
  fun `the BankID sandbox header is sent once the sandbox is turned on`() = runTest {
    val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
    val sandboxState = sandboxState()
    val repository = providers.provideAuthRepository(buildConstants(isProduction = false), engine, sandboxState)

    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)
    sandboxState.setEnabled(true)
    runCurrent()
    repository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)

    assertThat(engine.requestHistory[0].headers["Hedvig-Bankid-Sandbox"]).isNull()
    assertThat(engine.requestHistory[1].headers["Hedvig-Bankid-Sandbox"]).isEqualTo("true")
  }

  private fun TestScope.sandboxState() = BankIdSandboxState(
    TestPreferencesDataStore(testFolder.newFolder(), backgroundScope),
    buildConstants(isProduction = false),
    ApplicationScope(backgroundScope),
  )
}

internal fun buildConstants(isProduction: Boolean, urlAuthService: String = "") = object : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = ""
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = urlAuthService
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

private data class Flavour(val name: String, val isProduction: Boolean, val urlAuthService: String)
