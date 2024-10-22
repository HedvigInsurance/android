package com.hedvig.android.feature.movingflow.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.right

@Stable
internal interface ValidatedInput<Input, Output, ValidationError> {
  val value: Input

  fun updateValue(newValue: Input)

  val validationError: ValidationError?

  fun validate(): Either<ValidationError, Output>
}

internal fun <Input, Output, ValidationError> ValidatedInput(
  initialValue: Input,
  validator: (Input) -> Either<ValidationError, Output>,
): ValidatedInput<Input, Output, ValidationError> = ValidatedInputImpl(initialValue, validator)

class ValidatedInputImpl<Input, Output, ValidationError>(
  initialValue: Input,
  private val validator: (Input) -> Either<ValidationError, Output>,
) : ValidatedInput<Input, Output, ValidationError> {
  private var showErrorMessage: Boolean by mutableStateOf(false)
  private val validationResult: Either<ValidationError, Output> by derivedStateOf {
    validator(value)
  }

  override var value: Input by mutableStateOf(initialValue)
    private set

  override fun updateValue(newValue: Input) {
    showErrorMessage = false
    value = newValue
  }

  override val validationError: ValidationError? by derivedStateOf {
    validationResult.leftOrNull().takeIf { showErrorMessage }
  }

  override fun validate(): Either<ValidationError, Output> {
    return validationResult.onLeft {
      showErrorMessage = true
    }
  }
}

@Suppress("ktlint:standard:function-naming")
internal fun <I> NoopValidator(): (I) -> Either<Nothing, I & Any> = { it!!.right() }
