package com.hedvig.android.feature.inapppurchase.ui.form

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField.SingleSelect.Option
import org.junit.Test

class SearchableSelectStateTest {
  private val options = listOf("Labrador retriever", "Golden retriever", "Beagle").map { Option(it, it, true, null) }

  @Test
  fun `filter matches titles case-insensitively`() {
    assertThat(filterOptions(options, "RETRIEVER").map { it.value })
      .containsExactly("Labrador retriever", "Golden retriever")
  }

  @Test
  fun `a blank query keeps every option`() {
    assertThat(filterOptions(options, "  ").map { it.value })
      .containsExactly("Labrador retriever", "Golden retriever", "Beagle")
  }

  @Test
  fun `toggle adds a value under the limit`() {
    assertThat(toggle(listOf("Beagle"), "Golden retriever", maxSelections = 2))
      .containsExactly("Beagle", "Golden retriever")
  }

  @Test
  fun `toggle ignores a new value at the limit`() {
    assertThat(toggle(listOf("Beagle", "Golden retriever"), "Labrador retriever", maxSelections = 2))
      .containsExactly("Beagle", "Golden retriever")
  }

  @Test
  fun `toggle removes a selected value`() {
    assertThat(toggle(listOf("Beagle", "Golden retriever"), "Beagle", maxSelections = 2))
      .containsExactly("Golden retriever")
  }

  @Test
  fun `toggle replaces the value when one selection is allowed`() {
    assertThat(toggle(listOf("Beagle"), "Golden retriever", maxSelections = 1)).containsExactly("Golden retriever")
  }
}
