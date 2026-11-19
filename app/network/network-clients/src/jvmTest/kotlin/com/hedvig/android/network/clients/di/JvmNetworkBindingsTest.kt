package com.hedvig.android.network.clients.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import com.hedvig.android.network.clients.TlsDiagnostics
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpEngine
import java.io.File
import javax.net.ssl.SSLHandshakeException
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

@DependencyGraph(AppScope::class)
internal interface NetworkTestGraph {
  val accessTokenFetcher: AccessTokenFetcher
  val extraKtorClientConfiguration: ExtraKtorClientConfiguration
  val tlsDiagnostics: TlsDiagnostics

  @AuthHttpClientEngine
  val authHttpClientEngine: HttpClientEngine

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
      @Provides appBuildConfig: AppBuildConfig,
    ): NetworkTestGraph
  }
}

class JvmNetworkBindingsTest {
  private val dataDirectory: File = createTempDirectory("network-bindings-test").toFile()
  private val graph = createGraphFactory<NetworkTestGraph.Factory>().create(dataDirectory, TestAppBuildConfig)

  @AfterTest
  fun tearDown() {
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `the access token fetcher reads the access token provider`() = runTest {
    val accessTokenProvider = AccessTokenProvider { "token" }

    assertEquals("token", object : JvmNetworkMetroProviders {}.provideAccessTokenFetcher(accessTokenProvider).fetch())
  }

  @Test
  fun `the extra Ktor client configuration is the no-op one`() {
    assertIs<NoopExtraKtorClientConfiguration>(graph.extraKtorClientConfiguration)
  }

  @Test
  fun `the auth engine is OkHttp`() {
    assertIs<OkHttpEngine>(graph.authHttpClientEngine)
  }

  @Test
  fun `the TLS diagnostics describe nothing, even for a trust failure`() = runTest {
    assertNull(graph.tlsDiagnostics.describe(SSLHandshakeException("Trust anchor for certification path not found.")))
  }
}

private object TestAppBuildConfig : AppBuildConfig {
  override val debug: Boolean = true
  override val applicationId: String = "com.hedvig.dev.app"
  override val buildType: String = "debug"
  override val versionCode: Int = 1
  override val versionName: String = "1.0.0"
  override val appFlavor: Flavor = Flavor.Develop
  override val osReleaseVersion: String = "1"
  override val osSdkVersion: Int = 1
  override val brand: String = "brand"
  override val model: String = "model"
  override val device: String = "device"
  override val manufacturer: String = "manufacturer"
}
