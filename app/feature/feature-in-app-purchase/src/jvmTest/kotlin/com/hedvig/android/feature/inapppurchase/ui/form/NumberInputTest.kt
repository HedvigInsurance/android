package com.hedvig.android.feature.inapppurchase.ui.form

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import org.junit.Test

class NumberInputTest {
  @Test
  fun `a small range like co-insured uses the stepper`() {
    assertThat(number(min = 0, max = 10).usesStepper()).isTrue()
  }

  @Test
  fun `a wide range like year of construction is typed`() {
    assertThat(number(min = 1700, max = 2026).usesStepper()).isFalse()
  }

  @Test
  fun `a range without a maximum like living space is typed`() {
    assertThat(number(min = 1, max = null).usesStepper()).isFalse()
  }

  private fun number(min: Int?, max: Int?) =
    InAppPurchaseFormField.Number(id = "n", title = "n", defaultValue = null, minValue = min, maxValue = max)
}
