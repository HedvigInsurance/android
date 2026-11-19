package com.hedvig.android.feature.greencard.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.greencard.data.Country
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import hedvig.resources.CHANGE_ADDRESS_NEW_ADDRESS_LABEL
import hedvig.resources.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * The fields edit these states in place, which keeps the cursor where the member put it. Most addresses are in
 * Sweden, so the country starts there.
 */
internal class AddressFieldsState(
  val street: TextFieldState = TextFieldState(),
  val postalCode: TextFieldState = TextFieldState(),
  val city: TextFieldState = TextFieldState(),
  countryState: MutableState<Country?> = mutableStateOf(Country.SWEDEN),
) {
  var country: Country? by countryState

  fun isComplete(): Boolean = listOf(street, postalCode, city).none { it.text.isBlank() } && country != null

  /**
   * Only call once [isComplete] is true.
   */
  fun toAddress(): GreenCardAddress = GreenCardAddress(
    street = street.trimmedText(),
    postalCode = postalCode.trimmedText(),
    city = city.trimmedText(),
    country = checkNotNull(country),
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
    CountryField(
      selectedCountry = address.country,
      onCountrySelected = { address.country = it },
      errorText = if (showMissingFieldErrors && address.country == null) MISSING_FIELD_TEXT else null,
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
      ErrorState.Error.WithMessage(MISSING_FIELD_TEXT)
    } else {
      ErrorState.NoError
    },
    modifier = Modifier.fillMaxWidth(),
  )
}

// TODO: Add "Fill in this field" / "Fyll i det här fältet" to Lokalise
internal const val MISSING_FIELD_TEXT = "Fill in this field"

@HedvigPreview
@Composable
private fun PreviewAddressFieldsFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddressFields(
        address = AddressFieldsState(
          street = TextFieldState("Storgatan 1"),
          postalCode = TextFieldState("111 22"),
          city = TextFieldState("Stockholm"),
        ),
        showMissingFieldErrors = false,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAddressFieldsMissing() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddressFields(
        address = AddressFieldsState(countryState = mutableStateOf(null)),
        showMissingFieldErrors = true,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
