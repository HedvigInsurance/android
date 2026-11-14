package com.hedvig.android.core.buildconstants

import kotlin.test.Test
import kotlin.test.assertEquals

class NativeUserAgentTest {
  @Test
  fun `user agent matches the iOS format`() {
    val config = object : AppBuildConfig {
      override val debug: Boolean = true
      override val applicationId: String = "com.hedvig.test"
      override val buildType: String = "debug"
      override val versionCode: Int = 1
      override val versionName: String = "14.6.3"
      override val appFlavor: Flavor = Flavor.Develop
      override val osReleaseVersion: String = "26.4"
      override val osSdkVersion: Int = 0
      override val brand: String = "Apple"
      override val model: String = "iPhone"
      override val device: String = "iPhone18,1"
      override val manufacturer: String = "Apple"
    }

    assertEquals("com.hedvig.test 14.6.3 (iOS 26.4)", platformUserAgent("en-SE", config))
  }
}
