package com.hedvig.android.feature.connect.payment.trustly.di

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.feature.connect.payment.trustly.TrustlyUiState
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallbackImpl
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * A graph shaped like a desktop host's: [ActivityRetainedScope] collapsed into [AppScope], with every binding
 * taken from the JVM classpath of this module's dependencies and this module's own contributions. The factory
 * supplies only true host inputs: the data directory, the build config and the Apollo client configuration.
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface TrustlyBindingsTestGraph {
  val trustlyViewModel: TrustlyViewModel
  val trustlyCallback: TrustlyCallback

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
      @Provides appBuildConfig: AppBuildConfig,
      @Provides extraApolloClientConfiguration: ExtraApolloClientConfiguration,
    ): TrustlyBindingsTestGraph
  }
}

/**
 * Guarantees that [TrustlyViewModel] compiles and can be constructed in a graph that collapses
 * [ActivityRetainedScope] into [AppScope], using the real JVM bindings for everything it transitively needs. The
 * graph is checked at compile time, so a missing JVM binding fails this source set's compilation.
 *
 * It does not run the presenter: the ViewModel's main dispatcher is a test dispatcher that is never advanced, so no
 * Trustly session is started and no network call is made. It says nothing about the graph of a real host, which
 * may bind more or other things.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JvmTrustlyBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val dataDirectory: File = createTempDirectory("trustly-bindings-test").toFile()
  private lateinit var graph: TrustlyBindingsTestGraph

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    graph = createGraphFactory<TrustlyBindingsTestGraph.Factory>().create(
      dataDirectory = dataDirectory,
      appBuildConfig = TestAppBuildConfig,
      extraApolloClientConfiguration = NoExtraApolloClientConfiguration,
    )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `the Trustly ViewModel can be built and starts loading`() {
    assertThat(graph.trustlyViewModel.uiState.value).isEqualTo(TrustlyUiState.Loading)
  }

  @Test
  fun `the Trustly callback resolves to the single implementation`() {
    assertThat(graph.trustlyCallback).isInstanceOf<TrustlyCallbackImpl>()
  }
}

/** The host's Apollo interceptors are outside this graph, so its client is built without any. */
private object NoExtraApolloClientConfiguration : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder = builder
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
