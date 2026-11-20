package com.hedvig.android.network.clients.di

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

class NetworkMetroProvidersTest {
  @Test
  fun `the authenticated client sends the device id header`() = runTest {
    val engine = MockEngine { respondOk() }

    authenticatedClient(engine, AccessTokenFetcher { "token" }).get("https://example.com")

    assertThat(engine.requestHistory.single().headers["hedvig-device-id"]).isEqualTo("device-id")
  }

  @Test
  fun `every request sends the device's time zone`() = runTest {
    val engine = MockEngine { respondOk() }
    val original = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"))
    try {
      authenticatedClient(engine, AccessTokenFetcher { null }).get("https://example.com")
    } finally {
      TimeZone.setDefault(original)
    }

    assertThat(engine.requestHistory.single().headers["Hedvig-TimeZone"]).isEqualTo("America/Sao_Paulo")
  }

  @Test
  fun `each request reads the access token once and sends no authorization header without one`() = runTest {
    val engine = MockEngine { respondOk() }
    val tokens = ArrayDeque(listOf("token", null))
    var fetchCount = 0
    val client = authenticatedClient(
      engine,
      AccessTokenFetcher {
        fetchCount++
        tokens.removeFirstOrNull()
      },
    )

    client.get("https://example.com")
    client.get("https://example.com")

    assertThat(engine.requestHistory.map { it.headers[HttpHeaders.Authorization] })
      .containsExactly("Bearer token", null)
    assertThat(fetchCount).isEqualTo(2)
  }

  private fun authenticatedClient(engine: MockEngine, accessTokenFetcher: AccessTokenFetcher): HttpClient {
    val baseHttpClient = buildKtorClient(
      hedvigBuildConstants = FakeHedvigBuildConstants,
      languageService = FakeLanguageService,
      deviceIdFetcher = DeviceIdFetcher { "device-id" },
      extraKtorClientConfiguration = NoopExtraKtorClientConfiguration(),
      engineFactory = object : HttpClientEngineFactory<HttpClientEngineConfig> {
        override fun create(block: HttpClientEngineConfig.() -> Unit): HttpClientEngine = engine
      },
    )
    return object : NetworkMetroProviders {}.provideHttpClient(baseHttpClient, accessTokenFetcher)
  }
}

private fun DeviceIdFetcher(block: suspend () -> String?): DeviceIdFetcher = object : DeviceIdFetcher {
  override suspend fun fetch(): String? = block()
}

private fun AccessTokenFetcher(block: suspend () -> String?): AccessTokenFetcher = object : AccessTokenFetcher {
  override suspend fun fetch(): String? = block()
}

private object FakeLanguageService : LanguageService {
  override val language: StateFlow<Language> = MutableStateFlow(Language.EN_SE)

  override fun setLanguage(language: Language) {}

  override fun getLocale(): Locale = Locale.ROOT
}

private object FakeHedvigBuildConstants : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = "https://example.com"
  override val urlBaseWeb: String = "https://example.com"
  override val urlOdyssey: String = "https://example.com"
  override val urlHedvigGateway: String = "https://example.com"
  override val urlAuthService: String = "https://example.com"
  override val urlBotService: String = "https://example.com"
  override val urlClaimsService: String = "https://example.com"
  override val deepLinkHosts: List<String> = emptyList()
  override val appVersionName: String = "1.0.0"
  override val appVersionCode: String = "1"
  override val appPackageId: String = "com.hedvig.test"
  override val isDebug: Boolean = false
  override val isProduction: Boolean = true
  override val buildApiVersion: Int = 1
  override val platformName: String = "ANDROID"
  override val model: String = "model"
  override val userAgent: String = "user-agent"
}
