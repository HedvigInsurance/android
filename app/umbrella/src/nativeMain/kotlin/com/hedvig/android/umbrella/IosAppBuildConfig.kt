@file:OptIn(ExperimentalForeignApi::class)

package com.hedvig.android.umbrella

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.posix.uname
import platform.posix.utsname

/**
 * The build the iOS app is, read from the app's bundle and the device, so only [appFlavor] comes from Swift. A
 * production flavor is a release build; every other flavor is a debug build. [platformName] keeps the native default,
 * `ios`.
 */
internal class IosAppBuildConfig(override val appFlavor: Flavor) : AppBuildConfig {
  override val debug: Boolean = appFlavor != Flavor.Production
  override val buildType: String = if (appFlavor == Flavor.Production) "release" else "debug"
  override val applicationId: String = NSBundle.mainBundle.bundleIdentifier.orEmpty()
  override val versionName: String = infoString("CFBundleShortVersionString")
  override val versionCode: Int = infoString("CFBundleVersion").toIntOrNull() ?: 0
  override val osReleaseVersion: String = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
    "$majorVersion.$minorVersion.$patchVersion"
  }
  override val osSdkVersion: Int = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
    majorVersion.toInt()
  }
  override val device: String = machine()
  override val model: String =
    (NSProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] as? String) ?: device
  override val brand: String = "Apple"
  override val manufacturer: String = "Apple"
}

private fun infoString(key: String): String = NSBundle.mainBundle.infoDictionary?.get(key) as? String ?: ""

/** The hardware identifier, such as `iPhone16,1`, or `arm64` on the simulator. */
private fun machine(): String = memScoped {
  val name = alloc<utsname>()
  uname(name.ptr)
  name.machine.toKString()
}
