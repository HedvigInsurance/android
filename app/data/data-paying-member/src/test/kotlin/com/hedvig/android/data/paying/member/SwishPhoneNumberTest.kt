package com.hedvig.android.data.paying.member

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

internal class SwishPhoneNumberTest {
  @Test
  fun `a domestic number is kept as it is`() {
    assertThat(swishPhoneNumberOrNull("0701234567")).isEqualTo("0701234567")
  }

  @Test
  fun `a country-code number is kept as it is rather than made domestic`() {
    assertThat(swishPhoneNumberOrNull("46701234567")).isEqualTo("46701234567")
  }

  @Test
  fun `a leading plus is kept`() {
    assertThat(swishPhoneNumberOrNull("+46701234567")).isEqualTo("+46701234567")
  }

  @Test
  fun `separators are dropped from every form`() {
    assertThat(swishPhoneNumberOrNull("070-123 45 67")).isEqualTo("0701234567")
    assertThat(swishPhoneNumberOrNull("+46 70 123 45 67")).isEqualTo("+46701234567")
  }

  @Test
  fun `a letter makes the whole value unreadable instead of being stripped`() {
    assertThat(swishPhoneNumberOrNull("07012345ab")).isNull()
  }

  @Test
  fun `eight digits is enough, since that is Swish's own floor`() {
    assertThat(swishPhoneNumberOrNull("07012345")).isEqualTo("07012345")
    assertThat(swishPhoneNumberOrNull("0701234")).isNull()
  }

  @Test
  fun `a number past the E164 cap is not prefilled`() {
    assertThat(swishPhoneNumberOrNull("07012345678901234")).isNull()
  }

  @Test
  fun `a non-Swedish-mobile number is not prefilled`() {
    assertThat(swishPhoneNumberOrNull("0812345678")).isNull()
    assertThat(swishPhoneNumberOrNull("+4681234567")).isNull()
    assertThat(swishPhoneNumberOrNull("")).isNull()
  }

  /**
   * Swish pays only Swedish mobile numbers, so a member reachable on another Nordic number has
   * nothing to prefill here even though that number is perfectly valid for contacting them.
   */
  @Test
  fun `another country's code is not a Swish number`() {
    assertThat(swishPhoneNumberOrNull("+4790123456")).isNull()
    assertThat(swishPhoneNumberOrNull("4790123456")).isNull()
    assertThat(swishPhoneNumberOrNull("+4512345678")).isNull()
    assertThat(swishPhoneNumberOrNull("+358401234567")).isNull()
  }

  @Test
  fun `a Swedish landline or non-mobile prefix is not a Swish number`() {
    assertThat(swishPhoneNumberOrNull("+46812345678")).isNull()
    assertThat(swishPhoneNumberOrNull("46812345678")).isNull()
  }

  @Test
  fun `a plus on a domestic number is not a form the backend takes`() {
    assertThat(swishPhoneNumberOrNull("+0701234567")).isNull()
  }
}
