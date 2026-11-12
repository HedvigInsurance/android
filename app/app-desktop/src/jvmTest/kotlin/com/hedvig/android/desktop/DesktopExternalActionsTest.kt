package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.single
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.IOException
import java.net.URI
import org.junit.Rule
import org.junit.Test

class DesktopExternalActionsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val notices = mutableListOf<String>()

  @Test
  fun `a link opens in the browser`() {
    val desktop = RecordingDesktopIntegration()

    DesktopAppHost(desktop, notices::add).uriHandler.openUri("https://www.hedvig.com/se")

    assertThat(desktop.browsed).containsExactly(URI("https://www.hedvig.com/se"))
    assertThat(notices).isEmpty()
  }

  @Test
  fun `a link the browser cannot open shows a notice with the link`() {
    val desktop = RecordingDesktopIntegration(failure = IOException("no browser"))

    DesktopAppHost(desktop, notices::add).uriHandler.openUri("https://www.hedvig.com/se")

    assertThat(notices).single().contains("https://www.hedvig.com/se")
  }

  @Test
  fun `a malformed link shows a notice instead of throwing`() {
    DesktopAppHost(RecordingDesktopIntegration(), notices::add).uriHandler.openUri("not a uri")

    assertThat(notices).hasSize(1)
  }

  @Test
  fun `the app host asks for no permission rationale and handles the review prompt without throwing`() {
    val appHost = DesktopAppHost(RecordingDesktopIntegration(), notices::add)

    assertThat(appHost.shouldShowPermissionRationale("android.permission.POST_NOTIFICATIONS")).isFalse()
    appHost.tryShowAppStoreReviewDialog()
    assertThat(notices).isEmpty()
  }

  @Test
  fun `app settings and the Play Store show a notice`() {
    val navigator = DesktopExternalNavigator(RecordingDesktopIntegration(), notices::add)

    navigator.openAppSettings()
    navigator.tryOpenPlayStore()

    assertThat(notices).hasSize(2)
  }

  @Test
  fun `a phone number is handed to the system as a tel link`() {
    val desktop = RecordingDesktopIntegration()

    DesktopExternalNavigator(desktop, notices::add).tryToDialPhone("+46 10 45 99 200")

    assertThat(desktop.browsed).containsExactly(URI("tel", "+46 10 45 99 200", null))
    assertThat(notices).isEmpty()
  }

  @Test
  fun `a phone number that cannot be dialled is shown in a notice`() {
    val desktop = RecordingDesktopIntegration(failure = UnsupportedOperationException("no tel handler"))

    DesktopExternalNavigator(desktop, notices::add).tryToDialPhone("+46 10 45 99 200")

    assertThat(notices).single().contains("+46 10 45 99 200")
  }

  @Test
  fun `the email app opens through the system mail client`() {
    val desktop = RecordingDesktopIntegration()

    DesktopExternalNavigator(desktop, notices::add).openEmailApp()

    assertThat(desktop.mailOpened).containsExactly(Unit)
    assertThat(notices).isEmpty()
  }

  @Test
  fun `without a desktop the real integration fails into notices instead of throwing`() {
    val navigator = DesktopExternalNavigator(AwtDesktopIntegration, notices::add)

    navigator.openEmailApp()
    navigator.tryToDialPhone("+46 10 45 99 200")
    DesktopAppHost(AwtDesktopIntegration, notices::add).uriHandler.openUri("https://www.hedvig.com")

    assertThat(notices).hasSize(3)
  }

  @Test
  fun `a notice without a screen only logs`() {
    showDesktopNotice("Not available on desktop")
  }
}

private class RecordingDesktopIntegration(private val failure: Exception? = null) : DesktopIntegration {
  val browsed = mutableListOf<URI>()
  val mailOpened = mutableListOf<Unit>()

  override fun browse(uri: URI) {
    failure?.let { throw it }
    browsed += uri
  }

  override fun mail() {
    failure?.let { throw it }
    mailOpened += Unit
  }
}
