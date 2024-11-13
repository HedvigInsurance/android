package com.hedvig.android.feature.movingflow.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal interface BooleanInput {
  val value: Boolean

  fun updateValue(value: Boolean)
}

internal fun BooleanInput(initialValue: Boolean): BooleanInput = BooleanInputImpl(initialValue)

private class BooleanInputImpl(initialValue: Boolean) : BooleanInput {
  override var value: Boolean by mutableStateOf(initialValue)
    private set

  override fun updateValue(value: Boolean) {
    this.value = value
  }
}
