package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.apartment.data.ApartmentOffers

@Composable
internal fun ApartmentFormDestination(
  viewModel: ApartmentFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: ApartmentOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(ApartmentFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "Hemförsäkring",
  ) {
    when {
      uiState.isLoadingSession -> HedvigFullScreenCenterAlignedProgress()

      uiState.loadSessionError -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(ApartmentFormEvent.Retry) },
      )

      else -> {
        var street by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var livingSpace by remember { mutableStateOf("") }
        var numberCoInsured by remember { mutableIntStateOf(0) }

        ApartmentFormContent(
          street = street,
          zipCode = zipCode,
          livingSpace = livingSpace,
          numberCoInsured = numberCoInsured,
          streetError = uiState.streetError,
          zipCodeError = uiState.zipCodeError,
          livingSpaceError = uiState.livingSpaceError,
          isSubmitting = uiState.isSubmitting,
          onStreetChanged = { street = it },
          onZipCodeChanged = { value -> if (value.all { it.isDigit() }) zipCode = value },
          onLivingSpaceChanged = { value ->
            if (value.isEmpty() || value.toIntOrNull() != null) livingSpace = value
          },
          onNumberCoInsuredChanged = { numberCoInsured = it },
          onSubmit = {
            viewModel.emit(
              ApartmentFormEvent.SubmitForm(
                street = street,
                zipCode = zipCode,
                livingSpace = livingSpace,
                numberCoInsured = numberCoInsured,
              ),
            )
          },
          onRetry = { viewModel.emit(ApartmentFormEvent.Retry) },
        )
      }
    }
  }
}

@Composable
private fun ApartmentFormContent(
  street: String,
  zipCode: String,
  livingSpace: String,
  numberCoInsured: Int,
  streetError: String?,
  zipCodeError: String?,
  livingSpaceError: String?,
  isSubmitting: Boolean,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onNumberCoInsuredChanged: (Int) -> Unit,
  onSubmit: () -> Unit,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        labelText = "Adress",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = zipCode,
        onValueChange = onZipCodeChanged,
        labelText = "Postnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = zipCodeError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = livingSpace,
        onValueChange = onLivingSpaceChanged,
        labelText = "Boyta (kvm)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Done,
        ),
        enabled = !isSubmitting,
      )
      HedvigStepper(
        text = when (numberCoInsured) {
          0 -> "Bara du"
          else -> "Du + $numberCoInsured"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Antal medförsäkrade"),
        onMinusClick = { onNumberCoInsuredChanged(numberCoInsured - 1) },
        onPlusClick = { onNumberCoInsuredChanged(numberCoInsured + 1) },
        isPlusEnabled = !isSubmitting && numberCoInsured < 5,
        isMinusEnabled = !isSubmitting && numberCoInsured > 0,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Beräkna pris",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

private fun String?.toErrorState(): HedvigTextFieldDefaults.ErrorState {
  return if (this != null) {
    HedvigTextFieldDefaults.ErrorState.Error.WithMessage(this)
  } else {
    HedvigTextFieldDefaults.ErrorState.NoError
  }
}

@HedvigPreview
@Composable
private fun PreviewApartmentFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ApartmentFormContent(
        street = "",
        zipCode = "",
        livingSpace = "",
        numberCoInsured = 0,
        streetError = null,
        zipCodeError = null,
        livingSpaceError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onNumberCoInsuredChanged = {},
        onSubmit = {},
        onRetry = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewApartmentFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ApartmentFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        livingSpace = "65",
        numberCoInsured = 1,
        streetError = null,
        zipCodeError = null,
        livingSpaceError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onNumberCoInsuredChanged = {},
        onSubmit = {},
        onRetry = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewApartmentFormWithErrors() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ApartmentFormContent(
        street = "",
        zipCode = "123",
        livingSpace = "",
        numberCoInsured = 0,
        streetError = "Ange en adress",
        zipCodeError = "Ange ett giltigt postnummer (5 siffror)",
        livingSpaceError = "Ange boyta i kvadratmeter",
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onNumberCoInsuredChanged = {},
        onSubmit = {},
        onRetry = {},
      )
    }
  }
}
