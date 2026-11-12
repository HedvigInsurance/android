package com.hedvig.android.feature.login.otpinput

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class OtpInputAcceptanceTest {
  @Test
  fun `an empty field is accepted, so the member can clear it`() {
    assertThat(isAcceptedOtpInput("")).isTrue()
  }

  @Test
  fun `ASCII digits up to six are accepted`() {
    assertThat(isAcceptedOtpInput("012345")).isTrue()
  }

  @Test
  fun `a seventh digit is rejected`() {
    assertThat(isAcceptedOtpInput("0123456")).isFalse()
  }

  @Test
  fun `a letter is rejected`() {
    assertThat(isAcceptedOtpInput("12a4")).isFalse()
  }

  @Test
  fun `a space is rejected`() {
    assertThat(isAcceptedOtpInput("12 4")).isFalse()
  }

  @Test
  fun `an Arabic-Indic digit is accepted, as Android's TextUtils isDigitsOnly does`() {
    assertThat(isAcceptedOtpInput("12٣")).isTrue()
  }
}
