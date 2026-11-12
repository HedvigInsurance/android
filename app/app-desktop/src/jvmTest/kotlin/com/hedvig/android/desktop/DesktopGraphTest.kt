package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.logger.TestLogcatLoggingRule
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

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopGraphTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val dataDirectory: File = createTempDirectory("desktop-graph-test").toFile()

  @Before
  fun setUp() {
    // ViewModels launch their presenters on Dispatchers.Main. This dispatcher is never advanced, so constructing them
    // runs no presenter and makes no network call.
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `every feature the shared entry provider registers has its ViewModels in the graph`() {
    val graph = createTestDesktopGraph(dataDirectory)
    val registeredViewModels = (
      graph.viewModelProviders.keys +
        graph.assistedFactoryProviders.keys +
        graph.manualAssistedFactoryProviders.keys
    ).mapNotNull { it.qualifiedName }

    val featuresWithoutViewModels = sharedFeaturePackages.filter { featurePackage ->
      registeredViewModels.none { it.startsWith(featurePackage) }
    }

    assertThat(featuresWithoutViewModels).isEmpty()
  }

  @Test
  fun `every ViewModel and ViewModel factory in the graph can be built`() {
    val graph = createTestDesktopGraph(dataDirectory)

    graph.viewModelProviders.values.forEach { provider -> provider() }
    graph.assistedFactoryProviders.values.forEach { provider -> provider() }
    graph.manualAssistedFactoryProviders.values.forEach { provider -> provider() }
    assertThat(graph.metroViewModelFactory).isInstanceOf<DesktopViewModelFactory>()
  }

  @Test
  fun `tracking is the desktop client that sends nothing`() {
    val graph = createTestDesktopGraph(dataDirectory)

    assertThat(graph.eventTrackingClient).isInstanceOf<LoggingEventTrackingClient>()
  }

  @Test
  fun `the Apollo client logs out on an unauthenticated response, then logs the request`() {
    val graph = createTestDesktopGraph(dataDirectory)

    assertThat(graph.apolloClient.interceptors.map { it::class.qualifiedName }).containsExactly(
      "com.hedvig.android.app.apollo.LogoutOnUnauthenticatedInterceptor",
      "com.hedvig.android.app.apollo.LoggingInterceptor",
    )
  }

  private companion object {
    /**
     * One package per feature whose entries the shared provider registers, plus the cross-sell sheet `HedvigApp`
     * renders directly.
     */
    val sharedFeaturePackages = listOf(
      "com.hedvig.android.feature.addon.purchase",
      "com.hedvig.android.feature.change.tier",
      "com.hedvig.android.feature.chat.CbmChatViewModel",
      "com.hedvig.android.feature.chat.DeleteAccountViewModel",
      "com.hedvig.android.feature.chat.inbox",
      "com.hedvig.android.feature.chip.id",
      "com.hedvig.android.feature.claim.chat",
      "com.hedvig.android.feature.claim.details",
      "com.hedvig.android.feature.claimhistory",
      "com.hedvig.android.feature.connect.payment",
      "com.hedvig.android.feature.cross.sell.sheet",
      "com.hedvig.android.feature.editcoinsured",
      "com.hedvig.android.feature.help.center",
      "com.hedvig.android.feature.home",
      "com.hedvig.android.feature.insurance.certificate",
      "com.hedvig.android.feature.insurances",
      "com.hedvig.android.feature.login",
      "com.hedvig.android.feature.movingflow",
      "com.hedvig.android.feature.onboarding",
      "com.hedvig.android.feature.payin.account",
      "com.hedvig.android.feature.payments",
      "com.hedvig.android.feature.payoutaccount",
      "com.hedvig.android.feature.profile",
      "com.hedvig.android.feature.remove.addons",
      "com.hedvig.android.feature.terminateinsurance",
      "com.hedvig.android.feature.travelcertificate",
      "com.hedvig.android.shared.foreverui",
    )
  }
}

internal fun createTestDesktopGraph(
  dataDirectory: File,
  backend: DesktopBackend = DesktopBackend.Staging,
): DesktopGraph = createGraphFactory<DesktopGraph.Factory>().create(
  backstackController = newBackstackController(),
  appBuildConfig = DesktopAppBuildConfig(backend, versionName = "14.6.3", versionCode = 43),
  dataDirectory = dataDirectory,
)
