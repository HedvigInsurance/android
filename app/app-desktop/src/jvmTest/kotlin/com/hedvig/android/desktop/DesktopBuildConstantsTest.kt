package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.After
import org.junit.Rule
import org.junit.Test

class DesktopBuildConstantsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val dataDirectory: File = createTempDirectory("desktop-build-constants-test").toFile()

  @After
  fun tearDown() {
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `on staging the desktop app identifies as the Android develop build`() {
    val buildConstants = createTestDesktopGraph(dataDirectory, DesktopBackend.Staging).hedvigBuildConstants

    assertThat(buildConstants.platformName).isEqualTo("ANDROID")
    assertThat(buildConstants.appPackageId).isEqualTo("com.hedvig.dev.app")
    assertThat(buildConstants.appVersionName).isEqualTo("14.6.3")
    assertThat(buildConstants.model).isEqualTo("Hedvig Desktop")
    assertThat(buildConstants.userAgent).startsWith("com.hedvig.dev.app 14.6.3 (Android 16; Hedvig Desktop; desktop; ")
    assertThat(buildConstants.urlGraphqlOctopus).isEqualTo("https://apollo-router.dev.hedvigit.com")
    assertThat(buildConstants.isProduction).isFalse()
  }

  @Test
  fun `on production the desktop app identifies as the Android release build`() {
    val buildConstants = createTestDesktopGraph(dataDirectory, DesktopBackend.Production).hedvigBuildConstants

    assertThat(buildConstants.platformName).isEqualTo("ANDROID")
    assertThat(buildConstants.appPackageId).isEqualTo("com.hedvig.app")
    assertThat(buildConstants.model).isEqualTo("Hedvig Desktop")
    assertThat(buildConstants.urlGraphqlOctopus).isEqualTo("https://apollo-router.prod.hedvigit.com")
    assertThat(buildConstants.isProduction).isTrue()
  }
}
