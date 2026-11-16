package com.hedvig.android.network.clients.di

import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlin.test.Test
import kotlin.test.assertEquals

class NativeApolloClientTest {
  @Test
  fun `the provided Apollo client only logs`() {
    val httpClient = HttpClient(Darwin)
    val apolloClient = object : NativeNetworkMetroProviders {}.provideApolloClient(
      normalizedCacheFactory = MemoryCacheFactory(),
      httpClient = httpClient,
      hedvigBuildConstants = FakeHedvigBuildConstants,
    )

    assertEquals(listOf("LoggingInterceptor"), apolloClient.interceptors.map { it::class.simpleName })
    apolloClient.close()
    httpClient.close()
  }
}

private object FakeHedvigBuildConstants : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = "https://example.test"
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = emptyList()
  override val appVersionName: String = ""
  override val appVersionCode: String = ""
  override val appPackageId: String = ""
  override val isDebug: Boolean = true
  override val isProduction: Boolean = false
  override val buildApiVersion: Int = 0
  override val platformName: String = "ios"
  override val model: String = ""
  override val userAgent: String = ""
}
