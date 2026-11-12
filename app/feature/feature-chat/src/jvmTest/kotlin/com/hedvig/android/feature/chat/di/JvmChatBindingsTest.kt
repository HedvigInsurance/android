package com.hedvig.android.feature.chat.di

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.benasher44.uuid.uuid4
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
import com.hedvig.android.experiments.ExperimentProvider
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.feature.chat.CbmChatUiState
import com.hedvig.android.feature.chat.CbmChatViewModelFactory
import com.hedvig.android.feature.chat.inbox.InboxUiState
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
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
 * A graph shaped like a desktop host's: [ActivityRetainedScope] collapsed into [AppScope], with every binding taken
 * from the JVM classpath of this module's dependencies (among them `database-jvm` for the Room database and
 * `network-clients` for Apollo) and this module's own contributions.
 *
 * The factory supplies the true host inputs (the data directory and the build config) and the types no library
 * module binds on the JVM ([ExperimentProvider], [EventTrackingClient] and [Clock]).
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface ChatBindingsTestGraph {
  val cbmChatViewModelFactory: CbmChatViewModelFactory
  val inboxViewModel: InboxViewModel

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
      @Provides appBuildConfig: AppBuildConfig,
      @Provides experimentProvider: ExperimentProvider,
      @Provides eventTrackingClient: EventTrackingClient,
      @Provides clock: Clock,
      @Provides extraApolloClientConfiguration: ExtraApolloClientConfiguration,
    ): ChatBindingsTestGraph
  }
}

/**
 * Guarantees that the chat and inbox ViewModels compile and can be constructed in a graph that collapses
 * [ActivityRetainedScope] into [AppScope], using the real JVM bindings for everything they transitively need apart
 * from the inputs listed on [ChatBindingsTestGraph]. The graph is checked at compile time, so a missing JVM binding
 * fails this source set's compilation.
 *
 * It does not run the presenters: the main dispatcher is a test dispatcher that is never advanced, so no message is
 * loaded, no database query runs and no network call is made. It says nothing about the graph of a real host, which
 * may bind more or other things.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JvmChatBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val dataDirectory: File = createTempDirectory("chat-bindings-test").toFile()
  private lateinit var graph: ChatBindingsTestGraph

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    graph = createGraphFactory<ChatBindingsTestGraph.Factory>().create(
      dataDirectory = dataDirectory,
      appBuildConfig = TestAppBuildConfig,
      experimentProvider = FakeExperimentProvider(),
      eventTrackingClient = FakeEventTrackingClient(),
      clock = Clock.System,
      extraApolloClientConfiguration = NoExtraApolloClientConfiguration,
    )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `the chat ViewModel can be built and starts initializing`() {
    val viewModel = graph.cbmChatViewModelFactory.create(uuid4().toString())

    assertThat(viewModel.uiState.value).isEqualTo(CbmChatUiState.Initializing)
  }

  @Test
  fun `the inbox ViewModel can be built and starts loading`() {
    assertThat(graph.inboxViewModel.uiState.value).isEqualTo(InboxUiState.Loading)
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
