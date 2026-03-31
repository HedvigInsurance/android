package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
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
      else -> ApartmentFormContent(
        uiState = uiState,
        onStreetChanged = { viewModel.emit(ApartmentFormEvent.UpdateStreet(it)) },
        onZipCodeChanged = { viewModel.emit(ApartmentFormEvent.UpdateZipCode(it)) },
        onLivingSpaceChanged = { viewModel.emit(ApartmentFormEvent.UpdateLivingSpace(it)) },
        onNumberCoInsuredChanged = { viewModel.emit(ApartmentFormEvent.UpdateNumberCoInsured(it)) },
        onSubmit = { viewModel.emit(ApartmentFormEvent.Submit) },
        onRetry = { viewModel.emit(ApartmentFormEvent.Retry) },
      )
    }
  }
}

@Composable
private fun ApartmentFormContent(
  uiState: ApartmentFormState,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onNumberCoInsuredChanged: (Int) -> Unit,
  onSubmit: () -> Unit,
  onRetry: () -> Unit,
) {
  if (uiState.submitError != null) {
    HedvigErrorSection(
      onButtonClick = onRetry,
    )
    return
  }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = uiState.street,
        onValueChange = onStreetChanged,
        labelText = "Adress",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = uiState.streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next,
        ),
        enabled = !uiState.isSubmitting,
      )
      HedvigTextField(
        text = uiState.zipCode,
        onValueChange = { value ->
          if (value.all { it.isDigit() }) {
            onZipCodeChanged(value)
          }
        },
        labelText = "Postnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = uiState.zipCodeError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !uiState.isSubmitting,
      )
      HedvigTextField(
        text = uiState.livingSpace,
        onValueChange = { value ->
          if (value.isEmpty() || value.toIntOrNull() != null) {
            onLivingSpaceChanged(value)
          }
        },
        labelText = "Boyta (kvm)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = uiState.livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Done,
        ),
        enabled = !uiState.isSubmitting,
      )
      HedvigStepper(
        text = when (uiState.numberCoInsured) {
          0 -> "Bara du"
          else -> "Du + ${uiState.numberCoInsured}"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Antal medförsäkrade"),
        onMinusClick = { onNumberCoInsuredChanged(uiState.numberCoInsured - 1) },
        onPlusClick = { onNumberCoInsuredChanged(uiState.numberCoInsured + 1) },
        isPlusEnabled = !uiState.isSubmitting && uiState.numberCoInsured < 5,
        isMinusEnabled = !uiState.isSubmitting && uiState.numberCoInsured > 0,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Beräkna pris",
      onClick = onSubmit,
      enabled = !uiState.isSubmitting,
      isLoading = uiState.isSubmitting,
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
