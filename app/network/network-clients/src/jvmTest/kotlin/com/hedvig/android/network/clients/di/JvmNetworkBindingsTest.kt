package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import com.hedvig.android.network.clients.TlsDiagnostics
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.Headers
import java.io.File
import javax.net.ssl.SSLHandshakeException
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule

@DependencyGraph(AppScope::class)
internal interface NetworkTestGraph {
  val accessTokenFetcher: AccessTokenFetcher
  val extraKtorClientConfiguration: ExtraKtorClientConfiguration
  val tlsDiagnostics: TlsDiagnostics
  val networkLogPolicy: NetworkLogPolicy

  @PublicApolloClient
  val publicApolloClient: ApolloClient

  @BaseHttpClient
  val baseHttpClient: HttpClient

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
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

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
  fun `a query on the public Apollo client is sent without an Authorization header even when a token is available`() =
    runTest {
      val sent = mutableListOf<Headers>()
      graph.baseHttpClient.plugin(HttpSend).intercept { request ->
        sent += request.headers.build()
        throw RequestRecorded()
      }

      graph.publicApolloClient.query(TestQuery).fetchPolicy(FetchPolicy.NetworkOnly).execute()

      assertEquals(1, sent.size)
      assertNull(sent.single()["Authorization"])
      assertNotNull(sent.single()["Accept-Language"])
    }

  @Test
  fun `the TLS diagnostics describe nothing, even for a trust failure`() = runTest {
    assertNull(graph.tlsDiagnostics.describe(SSLHandshakeException("Trust anchor for certification path not found.")))
  }
}

private object TestQuery : Query<TestQuery.Data> {
  object Data : Query.Data

  override fun document(): String = "query Test { __typename }"

  override fun name(): String = "Test"

  override fun id(): String = "Test"

  override fun adapter(): Adapter<Data> = error("The request is aborted before a response exists")

  override fun serializeVariables(
    writer: JsonWriter,
    customScalarAdapters: CustomScalarAdapters,
    withDefaultValues: Boolean,
  ) {}

  override fun rootField(): CompiledField = error("The request is aborted before the cache is read")
}

/** Thrown by the test's last-stop interceptor so the request never leaves the machine. */
private class RequestRecorded : RuntimeException()

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
