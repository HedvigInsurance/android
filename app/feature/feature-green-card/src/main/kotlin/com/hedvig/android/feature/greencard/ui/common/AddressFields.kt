package com.hedvig.android.feature.greencard.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import hedvig.resources.CHANGE_ADDRESS_NEW_ADDRESS_LABEL
import hedvig.resources.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * The fields edit these states in place, which keeps the cursor where the member put it.
 */
internal class AddressFieldsState(
  val street: TextFieldState = TextFieldState(),
  val postalCode: TextFieldState = TextFieldState(),
  val city: TextFieldState = TextFieldState(),
  val country: TextFieldState = TextFieldState(),
) {
  fun isComplete(): Boolean = listOf(street, postalCode, city, country).none { it.text.isBlank() }

  fun toAddress(): GreenCardAddress = GreenCardAddress(
    street = street.trimmedText(),
    postalCode = postalCode.trimmedText(),
    city = city.trimmedText(),
    country = country.trimmedText(),
  )
}

internal fun TextFieldState.trimmedText(): String = text.trim().toString()

@Composable
internal fun AddressFields(
  address: AddressFieldsState,
  showMissingFieldErrors: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    RequiredTextField(
      state = address.street,
      labelText = stringResource(Res.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.postalCode,
      labelText = stringResource(Res.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.city,
      // TODO: Add "City" / "Ort" to Lokalise
      labelText = "City",
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.country,
      // TODO: Add "Country" / "Land" to Lokalise
      labelText = "Country",
      showMissingFieldError = showMissingFieldErrors,
    )
  }
}

@Composable
internal fun RequiredTextField(state: TextFieldState, labelText: String, showMissingFieldError: Boolean) {
  HedvigTextField(
    state = state,
    labelText = labelText,
    textFieldSize = Medium,
    errorState = if (showMissingFieldError && state.text.isBlank()) {
      // TODO: Add "Fill in this field" / "Fyll i det här fältet" to Lokalise
      ErrorState.Error.WithMessage("Fill in this field")
    } else {
      ErrorState.NoError
    },
    modifier = Modifier.fillMaxWidth(),
  )
}
