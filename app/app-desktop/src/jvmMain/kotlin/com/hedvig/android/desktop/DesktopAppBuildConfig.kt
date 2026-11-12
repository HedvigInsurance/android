package com.hedvig.android.desktop

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor

/** The backend the desktop app talks to, picked with the `hedvig.desktop.production` Gradle property. */
internal enum class DesktopBackend(val directoryName: String) {
  Staging("staging"),
  Production("production"),
}

/**
 * Identifies the desktop app to the backend as the Android app: the develop build on [DesktopBackend.Staging] and the
 * release build on [DesktopBackend.Production]. The device fields name the desktop, so the `User-Agent` and the
 * device model read "Hedvig Desktop" and backend logs can tell this client apart.
 */
internal class DesktopAppBuildConfig(
  backend: DesktopBackend,
  override val versionName: String,
  override val versionCode: Int,
) : AppBuildConfig {
  override val platformName: String = "ANDROID"
  override val debug: Boolean = backend == DesktopBackend.Staging
  override val applicationId: String = when (backend) {
    DesktopBackend.Staging -> "com.hedvig.dev.app"
    DesktopBackend.Production -> "com.hedvig.app"
  }
  override val buildType: String = when (backend) {
    DesktopBackend.Staging -> "debug"
    DesktopBackend.Production -> "release"
  }
  override val appFlavor: Flavor = when (backend) {
    DesktopBackend.Staging -> Flavor.Develop
    DesktopBackend.Production -> Flavor.Production
  }
  override val osReleaseVersion: String = "16"
  override val osSdkVersion: Int = 36
  override val brand: String = "Hedvig"
  override val model: String = "Desktop"
  override val device: String = "desktop"
  override val manufacturer: String = "Hedvig"
}
