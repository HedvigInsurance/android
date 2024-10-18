package com.hedvig.android.feature.movingflow.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class ValidatedInput<Input, ValidationError>(
  initialValue: Input,
  private val validators: List<(Input) -> ValidationError?>,
) {
  private var showErrorMessage: Boolean by mutableStateOf(false)
  private val _validationError: ValidationError? by derivedStateOf {
    validators.firstNotNullOfOrNull { validate ->
      validate(value)
    }
  }

  var value: Input by mutableStateOf(initialValue)
    private set

  fun updateValue(newValue: Input) {
    showErrorMessage = false
    value = newValue
  }

  val isValid: Boolean by derivedStateOf { _validationError == null }
  val validationError: ValidationError? by derivedStateOf {
    _validationError.takeIf { showErrorMessage }
  }

  fun validate() {
    if (!isValid) {
      showErrorMessage = true
    }
  }
}
