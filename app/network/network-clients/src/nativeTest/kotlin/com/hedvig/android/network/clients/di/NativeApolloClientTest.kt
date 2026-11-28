package com.hedvig.android.network.clients.di

import com.apollographql.cache.normalized.apolloStore
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.network.clients.NetworkLogPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NativeApolloClientTest {
  @Test
  fun `the provided Apollo client only logs and has no normalized cache`() {
    val httpClient = HttpClient(Darwin)
    val apolloClient = object : NativeNetworkMetroProviders {}.provideApolloClient(
      httpClient = httpClient,
      hedvigBuildConstants = FakeHedvigBuildConstants,
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false, logsGraphqlErrors = true),
    )

    assertEquals(listOf("LoggingInterceptor"), apolloClient.interceptors.map { it::class.simpleName })
    assertFailsWith<IllegalStateException> { apolloClient.apolloStore }
    apolloClient.close()
    httpClient.close()
  }

  @Test
  fun `the provided public Apollo client only logs and has no normalized cache`() {
    val httpClient = HttpClient(Darwin)
    val apolloClient = object : NativeNetworkMetroProviders {}.providePublicApolloClient(
      httpClient = httpClient,
      hedvigBuildConstants = FakeHedvigBuildConstants,
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false, logsGraphqlErrors = true),
    )

    assertEquals(listOf("LoggingInterceptor"), apolloClient.interceptors.map { it::class.simpleName })
    assertFailsWith<IllegalStateException> { apolloClient.apolloStore }
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
