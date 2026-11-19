package com.hedvig.android.core.buildconstants

import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CommonHedvigBuildConstantsTest {
  @Test
  fun `an AppBuildConfig that does not name a platform reports the target's own platform`() {
    val buildConstants = CommonHedvigBuildConstants(FakeAppBuildConfig(), FakeLanguageService())

    assertEquals("jvm", buildConstants.platformName)
  }

  @Test
  fun `an AppBuildConfig that names a platform reports that platform`() {
    val buildConstants = CommonHedvigBuildConstants(
      object : AppBuildConfig by FakeAppBuildConfig() {
        override val platformName: String = "ANDROID"
      },
      FakeLanguageService(),
    )

    assertEquals("ANDROID", buildConstants.platformName)
  }

  @Test
  fun `the user agent is built from the AppBuildConfig`() {
    val buildConstants = CommonHedvigBuildConstants(FakeAppBuildConfig(), FakeLanguageService())

    assertEquals("com.hedvig.dev.app 1.2.3 (Android 16; brand model; device; sv-SE)", buildConstants.userAgent)
  }

  @Test
  fun `the deep link hosts are those of the configured flavor, in the same order for every flavor`() {
    val buildConstants = CommonHedvigBuildConstants(FakeAppBuildConfig(), FakeLanguageService())

    assertEquals(deepLinkHostsOf(Flavor.Develop), buildConstants.deepLinkHosts)
    assertEquals(
      listOf("link.dev.hedvigit.com", "dev.hedvigit.com/deeplink", "hedvigdevelop.page.link"),
      deepLinkHostsOf(Flavor.Develop),
    )
    assertEquals(
      listOf("link.hedvig.com", "www.hedvig.com/deeplink", "hedvig.page.link"),
      deepLinkHostsOf(Flavor.Production),
    )
  }
}

private class FakeAppBuildConfig : AppBuildConfig {
  override val debug: Boolean = true
  override val applicationId: String = "com.hedvig.dev.app"
  override val buildType: String = "debug"
  override val versionCode: Int = 1
  override val versionName: String = "1.2.3"
  override val appFlavor: Flavor = Flavor.Develop
  override val osReleaseVersion: String = "16"
  override val osSdkVersion: Int = 36
  override val brand: String = "brand"
  override val model: String = "model"
  override val device: String = "device"
  override val manufacturer: String = "manufacturer"
}

private class FakeLanguageService : LanguageService {
  override val language: StateFlow<Language> = MutableStateFlow(Language.SV_SE)

  override fun setLanguage(language: Language) {}

  override fun getLocale(): CommonLocale = Locale.forLanguageTag("sv-SE")
}
