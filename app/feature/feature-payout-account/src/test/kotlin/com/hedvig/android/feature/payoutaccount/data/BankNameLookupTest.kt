package com.hedvig.android.feature.payoutaccount.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class BankNameLookupTest {
  @Test
  fun `common clearing numbers resolve to their bank`() {
    assertThat(bankNameForClearingNumber("3300")).isEqualTo("Nordea")
    assertThat(bankNameForClearingNumber("5000")).isEqualTo("SEB")
    assertThat(bankNameForClearingNumber("6000")).isEqualTo("Handelsbanken")
    assertThat(bankNameForClearingNumber("8327")).isEqualTo("Swedbank")
    assertThat(bankNameForClearingNumber("9550")).isEqualTo("Avanza Bank")
  }

  @Test
  fun `range edges belong to the right bank`() {
    assertThat(bankNameForClearingNumber("3399")).isEqualTo("Nordea")
    assertThat(bankNameForClearingNumber("3400")).isEqualTo("Länsförsäkringar Bank")
    assertThat(bankNameForClearingNumber("3409")).isEqualTo("Länsförsäkringar Bank")
    assertThat(bankNameForClearingNumber("3410")).isEqualTo("Nordea")
    assertThat(bankNameForClearingNumber("9124")).isEqualTo("SEB")
  }

  @Test
  fun `unassigned and unreadable clearing numbers have no bank`() {
    assertThat(bankNameForClearingNumber("2200")).isNull()
    assertThat(bankNameForClearingNumber("9125")).isNull()
    assertThat(bankNameForClearingNumber("0999")).isNull()
    assertThat(bankNameForClearingNumber("")).isNull()
    assertThat(bankNameForClearingNumber("83a7")).isNull()
  }
}
