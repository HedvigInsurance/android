package com.hedvig.android.network.clients.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NoopExtraApolloClientConfiguration
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

@DependencyGraph(AppScope::class)
internal interface NetworkTestGraph {
  val accessTokenFetcher: AccessTokenFetcher
  val extraApolloClientConfiguration: ExtraApolloClientConfiguration
  val extraKtorClientConfiguration: ExtraKtorClientConfiguration

  @AuthHttpClientEngine
  val authHttpClientEngine: HttpClientEngine

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides accessTokenProvider: AccessTokenProvider,
    ): NetworkTestGraph
  }
}

class JvmNetworkBindingsTest {
  private val graph = createGraphFactory<NetworkTestGraph.Factory>().create { "token" }

  @Test
  fun `the access token fetcher reads the access token provider`() = runTest {
    assertEquals("token", graph.accessTokenFetcher.fetch())
  }

  @Test
  fun `the extra client configurations are the no-op ones`() {
    assertIs<NoopExtraApolloClientConfiguration>(graph.extraApolloClientConfiguration)
    assertIs<NoopExtraKtorClientConfiguration>(graph.extraKtorClientConfiguration)
  }

  @Test
  fun `the auth engine is OkHttp`() {
    assertIs<OkHttpEngine>(graph.authHttpClientEngine)
  }
}
