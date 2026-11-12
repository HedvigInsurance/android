package com.hedvig.android.desktop

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import com.hedvig.android.app.navigation.HostEntryContext
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Rule
import org.junit.Test

class DesktopHostEntriesTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  private val dataDirectory: File = createTempDirectory("desktop-host-entries-test").toFile()

  private val backstack = newBackstackController()

  @After
  fun tearDown() {
    dataDirectory.deleteRecursively()
  }

  /**
   * Builds the entry provider the way the running desktop app does: the shared provider's registrations followed by
   * [desktopHostEntries], with no fallback. Navigation 3 throws on a second registration of a key class, so a key
   * registered both by the shared provider and as a desktop placeholder fails this test, where the app would throw
   * on its first frame. Every known key must then resolve through that provider.
   */
  @Test
  fun `the shared provider and the desktop host register every known key exactly once`() {
    val provider = entryProvider<HedvigNavKey> {
      registerSharedEntries(this, createTestDesktopGraph(dataDirectory)) { desktopHostEntries(it.backstack) }
    }

    (keysLeftToTheHost + keysTheSharedProviderRegisters).forEach { key ->
      assertThat(provider(key).contentKey.toString()).startsWith(key.toString())
    }
  }

  @Test
  fun `the shared provider registers its keys without the desktop host`() {
    val provider = entryProvider<HedvigNavKey> {
      registerSharedEntries(this, createTestDesktopGraph(dataDirectory)) {}
    }

    keysTheSharedProviderRegisters.forEach { key ->
      assertThat(provider(key).contentKey.toString()).startsWith(key.toString())
    }
  }

  @Test
  fun `the shared tab roots keep the navigation bar`() {
    val provider = entryProvider<HedvigNavKey> {
      registerSharedEntries(this, createTestDesktopGraph(dataDirectory)) { desktopHostEntries(it.backstack) }
    }
    val showNavBar = NavSuiteSceneDecoratorStrategy.showNavBar()

    listOf(HomeKey, InsurancesKey).forEach { key ->
      assertThat(provider(key).metadata.entries.containsAll(showNavBar.entries)).isTrue()
    }
  }

  @Test
  fun `a key without an entry is rendered by the fallback and logged once`() {
    val provider = entryProvider(fallback = desktopFallbackEntry(backstack)) {
      desktopHostEntries(backstack)
    }

    repeat(3) { assertThat(provider(UnregisteredKey).contentKey.toString()).startsWith(UnregisteredKey.toString()) }

    assertThat(recordingLogger.lines.filter { it.first == LogPriority.WARN }).containsExactly(
      LogPriority.WARN to "No entry is registered for UnregisteredKey, showing it as not available on desktop",
    )
  }

  private companion object {
    /** The keys the shared provider can put on the back stack and leaves to the host to register. */
    val keysLeftToTheHost = listOf(
      ChatKey("conversation-id"),
      InboxKey,
      TrustlyKey(),
      ImageViewerKey("https://example.com/image.png", "cache-key"),
      ClaimDetailsKey("claim-id"),
    )

    /** Keys the shared provider registers itself, which the desktop host must not register again. */
    val keysTheSharedProviderRegisters = listOf(
      LoginKey,
      HomeKey,
      InsurancesKey,
      OnboardingKey,
      TravelCertificateKey,
      InsuranceEvidenceKey,
    )
  }
}

/**
 * Calls the shared `hedvigEntryProvider`, which is `internal` to `:app-shell`, through reflection, with the desktop
 * graph's dependencies and [hostEntries] as the host block. Its JVM signature is the Kotlin one with the receiver
 * first, so a change to its parameters fails here with a message rather than silently.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun registerSharedEntries(
  scope: EntryProviderScope<HedvigNavKey>,
  graph: DesktopGraph,
  hostEntries: EntryProviderScope<HedvigNavKey>.(HostEntryContext) -> Unit,
) {
  val method = Class.forName("com.hedvig.android.app.navigation.HedvigEntryProviderKt")
    .declaredMethods
    .single { it.name == "hedvigEntryProvider" }
  method.isAccessible = true
  val arguments = arrayOf<Any>(
    scope,
    newBackstackController(),
    CoroutineScope(Job()),
    WindowSizeClass.calculateFromSize(DpSize(412.dp, 915.dp)),
    graph.memberIdService,
    GlobalSnackBarState(),
    DesktopExternalNavigator(showNotice = {}),
    DesktopAppHost(showNotice = {}),
    { _: String -> },
    { _: String -> true },
    graph.imageLoader,
    graph.languageService,
    graph.hedvigBuildConstants,
    graph.resetOnboardingSeenUseCase,
    hostEntries,
  )
  check(method.parameterTypes.size == arguments.size) {
    "hedvigEntryProvider takes ${method.parameterTypes.toList()}, update this call"
  }
  try {
    method.invoke(null, *arguments)
  } catch (e: InvocationTargetException) {
    throw e.targetException
  }
}

private data object UnregisteredKey : HedvigNavKey
