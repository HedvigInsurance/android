package com.hedvig.android.feature.movingflow.compose

import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.Test

class InputTest {
  @Test
  fun `constrained number input coerces values into its range`() {
    val input = ConstrainedNumberInput(initialValue = 20, validRange = 1..10)
    assertThat(input.value).isEqualTo(10)

    input.updateValue(-3)
    assertThat(input.value).isEqualTo(1)

    input.updateValue(5)
    assertThat(input.value).isEqualTo(5)
  }

  @Test
  fun `constrained number input only allows stepping within its range`() {
    val input = ConstrainedNumberInput(initialValue = 1, validRange = 1..2)
    assertThat(input.canDecrement).isFalse()
    assertThat(input.canIncrement).isTrue()

    input.updateValue(2)
    assertThat(input.canDecrement).isTrue()
    assertThat(input.canIncrement).isFalse()
  }

  @Test
  fun `validated input hides its error until validation is requested`() {
    val input = ValidatedInput<Int?, Int, String>(
      initialValue = null,
      validator = { either { ensureNotNull(it) { "missing" } } },
    )
    assertThat(input.validationError).isNull()

    assertThat(input.validate()).isEqualTo("missing".left())
    assertThat(input.validationError).isEqualTo("missing")
  }

  @Test
  fun `updating a validated input clears a shown error`() {
    val input = ValidatedInput<Int?, Int, String>(
      initialValue = null,
      validator = { either { ensureNotNull(it) { "missing" } } },
    )
    input.validate()

    input.updateValue(null)
    assertThat(input.validationError).isNull()

    input.updateValue(3)
    assertThat(input.validate()).isEqualTo(3.right())
    assertThat(input.validationError).isNull()
  }

  @Test
  fun `list input removes and replaces items`() {
    val input = ListInput(listOf("a", "b", "c"))

    input.removeItem("b")
    assertThat(input.value).containsExactly("a", "c")

    input.updateValue(listOf("x"))
    assertThat(input.value).containsExactly("x")
  }

  @Test
  fun `boolean input holds the latest value`() {
    val input = BooleanInput(false)

    input.updateValue(true)

    assertThat(input.value).isTrue()
  }
}
