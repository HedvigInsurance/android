package com.hedvig.android.desktop

import com.hedvig.android.navigation.activity.ExternalNavigator
import java.net.URI

/**
 * Hands calls and email to the operating system. App settings and the Play Store do not exist on desktop, so those
 * show a notice instead. The desktop app reports the version of the checkout it was built from, so the way to update
 * it is to update the checkout.
 */
internal class DesktopExternalNavigator(
  private val desktopIntegration: DesktopIntegration = AwtDesktopIntegration,
  private val showNotice: (String) -> Unit = ::showDesktopNotice,
) : ExternalNavigator {
  override fun openAppSettings() {
    showNotice("App settings are not available on desktop.")
  }

  override fun tryOpenPlayStore() {
    showNotice(
      "This desktop build is out of date. Update your checkout of the Android repository and start the app again " +
        "with ./gradlew :app-desktop:run.",
    )
  }

  override fun tryToDialPhone(number: String) {
    val opened = runDesktopAction("call $number") { desktopIntegration.browse(URI("tel", number, null)) }
    if (!opened) {
      showNotice("Calling is not available on this computer. The number is $number.")
    }
  }

  override fun openEmailApp() {
    val opened = runDesktopAction("open the email app") { desktopIntegration.mail() }
    if (!opened) {
      showNotice("Could not open the email app on this computer.")
    }
  }
}
