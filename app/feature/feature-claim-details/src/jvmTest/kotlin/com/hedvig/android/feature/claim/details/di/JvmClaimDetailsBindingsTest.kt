package com.hedvig.android.feature.claim.details.di

import assertk.assertThat
import assertk.assertions.isNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsDeepLinkMatcherProvider
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModelFactory
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModelFactory
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.assertTrue
import kotlin.time.Clock
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
 * supplies only host inputs: the data directory, the build config, the clock and the Apollo client configuration,
 * which the host's shell binds.
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface ClaimDetailsBindingsTestGraph {
  val claimDetailsViewModelFactory: ClaimDetailsViewModelFactory
  val addFilesViewModelFactory: AddFilesViewModelFactory
  val deepLinkMatcherProviders: Set<DeepLinkMatcherProvider>

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
      @Provides appBuildConfig: AppBuildConfig,
      @Provides clock: Clock,
    ): ClaimDetailsBindingsTestGraph
  }
}

/**
 * Guarantees that the factories of both ViewModels and the deep link matcher provider of this module resolve
 * in a graph that collapses [ActivityRetainedScope] into [AppScope], using the real JVM bindings for everything
 * they need apart from the inputs listed on [ClaimDetailsBindingsTestGraph]. The graph is checked at compile
 * time, so a missing JVM binding fails this source set's compilation.
 *
 * It does not create either ViewModel or run any query, upload or download, and it says nothing about the
 * graph of a real host, which may bind more or other things. Some bindings launch work on the application
 * scope while they are built; the main dispatcher is a test dispatcher that is never advanced, so that work
 * stays queued and never runs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JvmClaimDetailsBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private lateinit var graph: ClaimDetailsBindingsTestGraph

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    graph = createGraphFactory<ClaimDetailsBindingsTestGraph.Factory>().create(
      dataDirectory = createTempDirectory("claim-details-bindings-test").toFile(),
      appBuildConfig = TestAppBuildConfig,
      clock = Clock.System,
    )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `both ViewModel factories resolve`() {
    assertThat(graph.claimDetailsViewModelFactory).isNotNull()
    assertThat(graph.addFilesViewModelFactory).isNotNull()
  }

  @Test
  fun `the claim details deep link matcher provider is contributed`() {
    assertTrue(graph.deepLinkMatcherProviders.any { it is ClaimDetailsDeepLinkMatcherProvider })
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
