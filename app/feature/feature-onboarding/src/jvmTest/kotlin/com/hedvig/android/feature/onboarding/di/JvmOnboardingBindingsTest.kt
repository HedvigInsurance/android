package com.hedvig.android.feature.onboarding.di

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameInstanceAs
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.experiments.ExperimentProvider
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.feature.onboarding.data.ResetOnboardingSeenUseCase
import com.hedvig.android.feature.onboarding.data.ResetOnboardingSeenUseCaseImpl
import com.hedvig.android.feature.onboarding.gate.OnboardingGate
import com.hedvig.android.feature.onboarding.gate.OnboardingGateImpl
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
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
 * taken from the JVM classpath of this module's dependencies (`core-datastore-public`, `feature-flags`,
 * `network-clients`, `language-core`, `core-build-constants` and this module's own contributions).
 *
 * The factory supplies only true host inputs (the data directory, the build config and the Apollo client
 * configuration, which the host's shell binds) and fakes for the two types no library module binds on the JVM
 * ([ExperimentProvider] and [Backstack]). `EventTrackingClient`, the
 * third such type, is needed only by a ViewModel, which these accessors do not reach.
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface OnboardingBindingsTestGraph {
  val onboardingGate: OnboardingGate
  val resetOnboardingSeenUseCase: ResetOnboardingSeenUseCase

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
      @Provides appBuildConfig: AppBuildConfig,
      @Provides experimentProvider: ExperimentProvider,
      @Provides backstack: Backstack,
    ): OnboardingBindingsTestGraph
  }
}

/**
 * Guarantees that [OnboardingGate] and [ResetOnboardingSeenUseCase] compile and resolve to this module's
 * implementations in a graph that collapses [ActivityRetainedScope] into [AppScope], using the real JVM
 * bindings for everything they transitively need apart from the inputs listed on [OnboardingBindingsTestGraph].
 * The graph is checked at compile time, so a missing JVM binding fails this source set's compilation.
 *
 * It does not run either implementation: no flag fetch, no network call and no DataStore read happen here,
 * and it says nothing about the graph of a real host, which may bind more or other things. Some bindings
 * launch work on the application scope while they are built; the main dispatcher is a test dispatcher that
 * is never advanced, so that work stays queued and never runs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JvmOnboardingBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private lateinit var graph: OnboardingBindingsTestGraph

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    graph = createGraphFactory<OnboardingBindingsTestGraph.Factory>().create(
      dataDirectory = createTempDirectory("onboarding-bindings-test").toFile(),
      appBuildConfig = TestAppBuildConfig,
      experimentProvider = FakeExperimentProvider(),
      backstack = TestBackstack(),
    )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `the onboarding gate resolves to the single implementation, once per graph`() {
    assertThat(graph.onboardingGate).isInstanceOf<OnboardingGateImpl>()
    assertThat(graph.onboardingGate).isSameInstanceAs(graph.onboardingGate)
  }

  @Test
  fun `the reset onboarding use case resolves to the single implementation`() {
    assertThat(graph.resetOnboardingSeenUseCase).isInstanceOf<ResetOnboardingSeenUseCaseImpl>()
  }
}

private class TestBackstack : Backstack {
  override val entries: MutableList<HedvigNavKey> = mutableListOf()
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
