package com.hedvig.android.feature.purchase.house.ui.house

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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingsSection
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.allExtraBuildingTypes

@Composable
internal fun HouseFormDestination(
  viewModel: HouseFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: HouseOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(HouseFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(navigateUp = navigateUp) {
    when {
      uiState.isLoadingSession -> HedvigFullScreenCenterAlignedProgress()

      uiState.loadSessionError -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(HouseFormEvent.Retry) },
      )

      else -> HouseFormBody(
        uiState = uiState,
        onEvent = { event -> viewModel.emit(event) },
      )
    }
  }
}

@Composable
private fun HouseFormBody(uiState: HouseFormState, onEvent: (HouseFormEvent) -> Unit) {
  var street by rememberSaveable { mutableStateOf("") }
  var zipCode by rememberSaveable { mutableStateOf("") }
  var livingSpace by rememberSaveable { mutableStateOf("") }
  var ancillaryArea by rememberSaveable { mutableStateOf("") }
  var numberCoInsured by rememberSaveable { mutableIntStateOf(1) }
  var yearOfConstruction by rememberSaveable { mutableStateOf("") }
  var numberOfBathrooms by rememberSaveable { mutableIntStateOf(1) }

  if (uiState.submitError != null) {
    ErrorDialog(
      // TODO: Add "Something went wrong" / "Något gick fel" to Lokalise
      title = "Something went wrong",
      message = uiState.submitError,
      onDismiss = { onEvent(HouseFormEvent.DismissError) },
    )
  }

  HouseFormContent(
    street = street,
    zipCode = zipCode,
    livingSpace = livingSpace,
    ancillaryArea = ancillaryArea,
    numberCoInsured = numberCoInsured,
    yearOfConstruction = yearOfConstruction,
    numberOfBathrooms = numberOfBathrooms,
    isSubleted = uiState.isSubleted,
    extraBuildings = uiState.extraBuildings,
    errors = uiState,
    isSubmitting = uiState.isSubmitting,
    onStreetChanged = { street = it },
    onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
    onLivingSpaceChanged = { value ->
      if (value.isEmpty() || value.toIntOrNull() != null) livingSpace = value
    },
    onAncillaryAreaChanged = { value ->
      if (value.isEmpty() || value.toIntOrNull() != null) ancillaryArea = value
    },
    onNumberCoInsuredChanged = { numberCoInsured = it },
    onYearOfConstructionChanged = { value ->
      if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 4)) yearOfConstruction = value
    },
    onNumberOfBathroomsChanged = { numberOfBathrooms = it },
    onIsSubletedSelected = { onEvent(HouseFormEvent.UpdateIsSubleted(it)) },
    onAddExtraBuilding = { onEvent(HouseFormEvent.AddExtraBuilding(it)) },
    onRemoveExtraBuilding = { onEvent(HouseFormEvent.RemoveExtraBuilding(it)) },
    onSubmit = {
      onEvent(
        HouseFormEvent.SubmitForm(
          street = street,
          zipCode = zipCode,
          livingSpace = livingSpace,
          ancillaryArea = ancillaryArea,
          numberCoInsured = numberCoInsured,
          yearOfConstruction = yearOfConstruction,
          numberOfBathrooms = numberOfBathrooms,
        ),
      )
    },
  )
}

@Composable
private fun HouseFormContent(
  street: String,
  zipCode: String,
  livingSpace: String,
  ancillaryArea: String,
  numberCoInsured: Int,
  yearOfConstruction: String,
  numberOfBathrooms: Int,
  isSubleted: Boolean?,
  extraBuildings: List<ExtraBuildingInfo>,
  errors: HouseFormState,
  isSubmitting: Boolean,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onAncillaryAreaChanged: (String) -> Unit,
  onNumberCoInsuredChanged: (Int) -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onNumberOfBathroomsChanged: (Int) -> Unit,
  onIsSubletedSelected: (Boolean) -> Unit,
  onAddExtraBuilding: (ExtraBuildingInfo) -> Unit,
  onRemoveExtraBuilding: (ExtraBuildingInfo) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Fill in your details so we can calculate your price" / "Fyll i dina uppgifter så beräknar vi ditt pris" to Lokalise
      text = "Fill in your details so we can calculate your price",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        // TODO: Add "Address" / "Adress" to Lokalise
        labelText = "Address",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = zipCode,
        onValueChange = onZipCodeChanged,
        // TODO: Add "Zip code" / "Postnummer" to Lokalise
        labelText = "Zip code",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.zipCodeError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = livingSpace,
        onValueChange = onLivingSpaceChanged,
        // TODO: Add "Living space (m²)" / "Boyta (kvm)" to Lokalise
        labelText = "Living space (m²)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = ancillaryArea,
        onValueChange = onAncillaryAreaChanged,
        // TODO: Add "Ancillary area (m²)" / "Biyta (kvm)" to Lokalise
        labelText = "Ancillary area (m²)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.ancillaryAreaError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      // TODO: Add "Household size" / "Hushållsstorlek" to Lokalise
      // TODO: Add "Only you" / "Bara du" to Lokalise
      // TODO: Add "You + {count}" / "Du + {count}" to Lokalise
      HedvigStepper(
        text = when (numberCoInsured) {
          1 -> "Only you"
          else -> "You + ${numberCoInsured - 1}"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Household size"),
        onMinusClick = { onNumberCoInsuredChanged(numberCoInsured - 1) },
        onPlusClick = { onNumberCoInsuredChanged(numberCoInsured + 1) },
        isPlusEnabled = !isSubmitting && numberCoInsured < 5,
        isMinusEnabled = !isSubmitting && numberCoInsured > 1,
      )

      HedvigTextField(
        text = yearOfConstruction,
        onValueChange = onYearOfConstructionChanged,
        // TODO: Add "Year of construction" / "Byggår" to Lokalise
        labelText = "Year of construction",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.yearOfConstructionError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      // TODO: Add "Number of bathrooms" / "Antal badrum" to Lokalise
      // TODO: Add "1 bathroom" / "1 badrum" to Lokalise (singular)
      // TODO: Add "{count} bathrooms" / "{count} badrum" to Lokalise (plural)
      HedvigStepper(
        text = if (numberOfBathrooms == 1) "1 bathroom" else "$numberOfBathrooms bathrooms",
        stepperSize = Medium,
        stepperStyle = Labeled("Number of bathrooms"),
        onMinusClick = { onNumberOfBathroomsChanged(numberOfBathrooms - 1) },
        onPlusClick = { onNumberOfBathroomsChanged(numberOfBathrooms + 1) },
        isPlusEnabled = !isSubmitting && numberOfBathrooms < 10,
        isMinusEnabled = !isSubmitting && numberOfBathrooms > 1,
      )

      // TODO: Add "Do you sublet all or parts of the house?" / "Hyr du ut hela eller delar av huset?" to Lokalise
      RadioChoiceRow(
        label = "Do you sublet all or parts of the house?",
        selectedId = isSubleted?.toString(),
        options = yesNoOptions(),
        onSelected = { id -> onIsSubletedSelected(id.toBoolean()) },
        errorText = errors.isSubletedError,
        isEnabled = !isSubmitting,
      )
    }
    Spacer(Modifier.height(16.dp))
    ExtraBuildingsSection(
      extraBuildings = extraBuildings,
      allowedExtraBuildings = allExtraBuildingTypes,
      onAddBuilding = onAddExtraBuilding,
      onRemoveBuilding = onRemoveExtraBuilding,
      enabled = !isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      // TODO: Add "Calculate price" / "Beräkna pris" to Lokalise
      text = "Calculate price",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RadioChoiceRow(
  label: String,
  selectedId: String?,
  options: List<Pair<String, String>>,
  onSelected: (String) -> Unit,
  errorText: String?,
  isEnabled: Boolean,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    RadioGroup(
      options = options.map { (id, text) -> RadioOption(RadioOptionId(id), text) },
      selectedOption = selectedId?.let(::RadioOptionId),
      onRadioOptionSelected = { id -> onSelected(id.id) },
      style = RadioGroupStyle.Labeled.HorizontalFlow(label = label),
      enabled = isEnabled,
      modifier = Modifier.fillMaxWidth(),
    )
    if (errorText != null) {
      HedvigText(
        text = errorText,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

// TODO: Add "Yes" / "Ja" to Lokalise
// TODO: Add "No" / "Nej" to Lokalise
private fun yesNoOptions(): List<Pair<String, String>> = listOf("true" to "Yes", "false" to "No")

private fun String?.toErrorState(): HedvigTextFieldDefaults.ErrorState {
  return if (this != null) {
    HedvigTextFieldDefaults.ErrorState.Error.WithMessage(this)
  } else {
    HedvigTextFieldDefaults.ErrorState.NoError
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "",
        zipCode = "",
        livingSpace = "",
        ancillaryArea = "",
        numberCoInsured = 1,
        yearOfConstruction = "",
        numberOfBathrooms = 1,
        isSubleted = null,
        extraBuildings = emptyList(),
        errors = HouseFormState(),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onAddExtraBuilding = {},
        onRemoveExtraBuilding = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        livingSpace = "120",
        ancillaryArea = "20",
        numberCoInsured = 3,
        yearOfConstruction = "1985",
        numberOfBathrooms = 2,
        isSubleted = false,
        extraBuildings = emptyList(),
        errors = HouseFormState(),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onAddExtraBuilding = {},
        onRemoveExtraBuilding = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormErrors() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "",
        zipCode = "12",
        livingSpace = "",
        ancillaryArea = "",
        numberCoInsured = 1,
        yearOfConstruction = "1500",
        numberOfBathrooms = 1,
        isSubleted = null,
        extraBuildings = emptyList(),
        errors = HouseFormState(
          streetError = "Enter an address",
          zipCodeError = "Enter a valid zip code (5 digits)",
          livingSpaceError = "Enter living space",
          ancillaryAreaError = "Enter ancillary area",
          yearOfConstructionError = "Enter a valid year of construction",
          isSubletedError = "Choose an option",
        ),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onAddExtraBuilding = {},
        onRemoveExtraBuilding = {},
        onSubmit = {},
      )
    }
  }
}
