package com.hedvig.android.feature.movingflow.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

@Stable
internal interface ConstrainedNumberInput {
  val value: Int

  fun updateValue(newValue: Int)

  val canIncrement: Boolean
  val canDecrement: Boolean
}

internal fun ConstrainedNumberInput(initialValue: Int, validRange: ClosedRange<Int>): ConstrainedNumberInput =
  ConstrainedNumberInputImpl(initialValue, validRange)

private class ConstrainedNumberInputImpl(
  initialValue: Int,
  private val validRange: ClosedRange<Int>,
) : ConstrainedNumberInput {
  override var value: Int by mutableIntStateOf(initialValue.coerceIn(validRange))
    private set

  override fun updateValue(newValue: Int) {
    value = newValue.coerceIn(validRange)
  }

  override val canDecrement: Boolean by derivedStateOf {
    value - 1 in validRange
  }
  override val canIncrement: Boolean by derivedStateOf {
    value + 1 in validRange
  }
}
