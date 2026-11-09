package com.hedvig.android.feature.claim.chat.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class NumberFieldInputTest {
  @Test
  fun `plain digits are accepted`() {
    assertThat(isValidNumberFieldInput("1500")).isTrue()
    assertThat(isValidNumberFieldInput("0")).isTrue()
    assertThat(isValidNumberFieldInput("")).isTrue()
  }

  @Test
  fun `swedish price notation, ranges and separators are rejected`() {
    listOf("500,-", "1 500:-", "1000-1500", "12,50", "12.50", "-5", "500kr", "1 500").forEach {
      assertThat(isValidNumberFieldInput(it)).isFalse()
    }
  }

  @Test
  fun `non-ascii digits are rejected`() {
    assertThat(isValidNumberFieldInput("١٢٣")).isFalse()
  }
}
