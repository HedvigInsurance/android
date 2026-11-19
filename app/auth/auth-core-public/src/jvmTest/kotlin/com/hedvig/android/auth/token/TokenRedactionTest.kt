package com.hedvig.android.auth.token

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import com.hedvig.android.auth.AuthStatus
import kotlin.time.Instant
import org.junit.Test

/**
 * These values get interpolated into logs that ship to Datadog and Crashlytics, so a token surviving
 * [toString] is a live credential leaving the device.
 */
internal class TokenRedactionTest {
  private val accessToken = LocalAccessToken("access-token-secret", Instant.fromEpochSeconds(1))
  private val refreshToken = LocalRefreshToken("refresh-token-secret", Instant.fromEpochSeconds(2))

  @Test
  fun `access token is not rendered by toString`() {
    assertThat(accessToken.toString()).doesNotContain("access-token-secret")
  }

  @Test
  fun `refresh token is not rendered by toString`() {
    assertThat(refreshToken.toString()).doesNotContain("refresh-token-secret")
  }

  @Test
  fun `expiry stays visible so the log remains useful`() {
    assertThat(accessToken.toString()).contains("expiryDate")
  }

  @Test
  fun `rendering the whole auth status leaks neither token`() {
    val rendered = AuthStatus.LoggedIn(accessToken, refreshToken).toString()

    assertThat(rendered).doesNotContain("access-token-secret")
    assertThat(rendered).doesNotContain("refresh-token-secret")
  }
}
