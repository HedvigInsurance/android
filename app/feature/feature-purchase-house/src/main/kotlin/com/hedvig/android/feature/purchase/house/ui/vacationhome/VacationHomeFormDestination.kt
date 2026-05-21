package com.hedvig.android.feature.purchase.house.ui.vacationhome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.house.data.HouseOffers

@Composable
internal fun VacationHomeFormDestination(
  viewModel: VacationHomeFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: HouseOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(VacationHomeFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    when {
      uiState.isLoadingSession -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      uiState.loadSessionError -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(VacationHomeFormEvent.Retry) },
        )
      }

      else -> {
        var street by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var multipleOwners by remember { mutableStateOf<Boolean?>(null) }
        var yearOfConstruction by remember { mutableStateOf("") }
        var livingSpace by remember { mutableStateOf("") }
        var hasWaterConnected by remember { mutableStateOf<Boolean?>(null) }
        var numberOfBathrooms by remember { mutableIntStateOf(1) }
        var isSubleted by remember { mutableStateOf<Boolean?>(null) }

        if (uiState.submitError != null) {
          ErrorDialog(
            title = "Något gick fel",
            message = uiState.submitError,
            onDismiss = { viewModel.emit(VacationHomeFormEvent.DismissError) },
          )
        }
        VacationHomeFormContent(
          street = street,
          zipCode = zipCode,
          multipleOwners = multipleOwners,
          yearOfConstruction = yearOfConstruction,
          livingSpace = livingSpace,
          hasWaterConnected = hasWaterConnected,
          numberOfBathrooms = numberOfBathrooms,
          isSubleted = isSubleted,
          streetError = uiState.streetError,
          zipCodeError = uiState.zipCodeError,
          multipleOwnersError = uiState.multipleOwnersError,
          yearOfConstructionError = uiState.yearOfConstructionError,
          livingSpaceError = uiState.livingSpaceError,
          hasWaterConnectedError = uiState.hasWaterConnectedError,
          isSubletedError = uiState.isSubletedError,
          isSubmitting = uiState.isSubmitting,
          onStreetChanged = { street = it },
          onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
          onMultipleOwnersChanged = { multipleOwners = it },
          onYearOfConstructionChanged = { value ->
            if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 4)) yearOfConstruction = value
          },
          onLivingSpaceChanged = { value ->
            if (value.isEmpty() || value.toIntOrNull() != null) livingSpace = value
          },
          onHasWaterConnectedChanged = { hasWaterConnected = it },
          onNumberOfBathroomsChanged = { numberOfBathrooms = it },
          onIsSubletedChanged = { isSubleted = it },
          onSubmit = {
            viewModel.emit(
              VacationHomeFormEvent.SubmitForm(
                street = street,
                zipCode = zipCode,
                multipleOwners = multipleOwners,
                yearOfConstruction = yearOfConstruction,
                livingSpace = livingSpace,
                hasWaterConnected = hasWaterConnected,
                numberOfBathrooms = numberOfBathrooms,
                isSubleted = isSubleted,
              ),
            )
          },
        )
      }
    }
  }
}

@Composable
private fun VacationHomeFormContent(
  street: String,
  zipCode: String,
  multipleOwners: Boolean?,
  yearOfConstruction: String,
  livingSpace: String,
  hasWaterConnected: Boolean?,
  numberOfBathrooms: Int,
  isSubleted: Boolean?,
  streetError: String?,
  zipCodeError: String?,
  multipleOwnersError: String?,
  yearOfConstructionError: String?,
  livingSpaceError: String?,
  hasWaterConnectedError: String?,
  isSubletedError: String?,
  isSubmitting: Boolean,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onMultipleOwnersChanged: (Boolean) -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onHasWaterConnectedChanged: (Boolean) -> Unit,
  onNumberOfBathroomsChanged: (Int) -> Unit,
  onIsSubletedChanged: (Boolean) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    // TODO: Add "Fill in your details and we'll calculate your price" / "Fyll i dina uppgifter så beräknar vi ditt pris" to Lokalise
    HedvigText(
      text = "Fyll i dina uppgifter så beräknar vi ditt pris",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      // TODO: Add "Address" / "Adress" to Lokalise
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        labelText = "Adress",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      // TODO: Add "Postal code" / "Postnummer" to Lokalise
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

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Do you own the house with someone else?" / "Äger du huset tillsammans med någon annan?" to Lokalise
      HedvigText(
        text = "Äger du huset tillsammans med någon annan?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = multipleOwners,
        onSelectionChanged = onMultipleOwnersChanged,
        enabled = !isSubmitting,
        errorText = multipleOwnersError,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Year built" / "Byggår" to Lokalise
      HedvigTextField(
        text = yearOfConstruction,
        onValueChange = onYearOfConstructionChanged,
        labelText = "Byggår",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = yearOfConstructionError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )
      // TODO: Add "Living space (m²)" / "Boyta (kvm)" to Lokalise
      HedvigTextField(
        text = livingSpace,
        onValueChange = onLivingSpaceChanged,
        labelText = "Boyta (kvm)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Is water connected?" / "Är vatten anslutet?" to Lokalise
      HedvigText(
        text = "Är vatten anslutet?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = hasWaterConnected,
        onSelectionChanged = onHasWaterConnectedChanged,
        enabled = !isSubmitting,
        errorText = hasWaterConnectedError,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Number of bathrooms" / "Antal badrum" to Lokalise
      HedvigStepper(
        text = when (numberOfBathrooms) {
          1 -> "1 badrum"
          else -> "$numberOfBathrooms badrum"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Antal badrum"),
        onMinusClick = { onNumberOfBathroomsChanged(numberOfBathrooms - 1) },
        onPlusClick = { onNumberOfBathroomsChanged(numberOfBathrooms + 1) },
        isPlusEnabled = !isSubmitting && numberOfBathrooms < 10,
        isMinusEnabled = !isSubmitting && numberOfBathrooms > 1,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Do you sublet all or parts of the house?" / "Hyr du ut hela eller delar av huset?" to Lokalise
      HedvigText(
        text = "Hyr du ut hela eller delar av huset?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = isSubleted,
        onSelectionChanged = onIsSubletedChanged,
        enabled = !isSubmitting,
        errorText = isSubletedError,
      )
    }
    Spacer(Modifier.height(16.dp))
    // TODO: Add "Calculate price" / "Beräkna pris" to Lokalise
    HedvigButton(
      text = "Beräkna pris",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

private const val OPTION_YES = "YES"
private const val OPTION_NO = "NO"

@Composable
private fun YesNoRadio(
  selected: Boolean?,
  onSelectionChanged: (Boolean) -> Unit,
  enabled: Boolean,
  errorText: String?,
) {
  val options = listOf(
    // TODO: Add "Yes" / "Ja" to Lokalise
    RadioOption(id = RadioOptionId(OPTION_YES), text = "Ja"),
    // TODO: Add "No" / "Nej" to Lokalise
    RadioOption(id = RadioOptionId(OPTION_NO), text = "Nej"),
  )
  val selectedId = when (selected) {
    true -> RadioOptionId(OPTION_YES)
    false -> RadioOptionId(OPTION_NO)
    null -> null
  }
  RadioGroup(
    options = options,
    selectedOption = selectedId,
    onRadioOptionSelected = { id ->
      onSelectionChanged(id == RadioOptionId(OPTION_YES))
    },
    style = RadioGroupStyle.Horizontal,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
  if (errorText != null) {
    HedvigText(
      text = errorText,
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.signalRedElement,
    )
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
private fun PreviewVacationHomeFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "",
        zipCode = "",
        multipleOwners = null,
        yearOfConstruction = "",
        livingSpace = "",
        hasWaterConnected = null,
        numberOfBathrooms = 1,
        isSubleted = null,
        streetError = null,
        zipCodeError = null,
        multipleOwnersError = null,
        yearOfConstructionError = null,
        livingSpaceError = null,
        hasWaterConnectedError = null,
        isSubletedError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewVacationHomeFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        multipleOwners = false,
        yearOfConstruction = "1985",
        livingSpace = "60",
        hasWaterConnected = true,
        numberOfBathrooms = 1,
        isSubleted = false,
        streetError = null,
        zipCodeError = null,
        multipleOwnersError = null,
        yearOfConstructionError = null,
        livingSpaceError = null,
        hasWaterConnectedError = null,
        isSubletedError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewVacationHomeFormErrors() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "",
        zipCode = "12",
        multipleOwners = null,
        yearOfConstruction = "1500",
        livingSpace = "",
        hasWaterConnected = null,
        numberOfBathrooms = 1,
        isSubleted = null,
        streetError = "Ange en adress",
        zipCodeError = "Ange ett giltigt postnummer (5 siffror)",
        multipleOwnersError = "Välj ett alternativ",
        yearOfConstructionError = "Ange ett giltigt byggår",
        livingSpaceError = "Ange boyta",
        hasWaterConnectedError = "Välj ett alternativ",
        isSubletedError = "Välj ett alternativ",
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}
