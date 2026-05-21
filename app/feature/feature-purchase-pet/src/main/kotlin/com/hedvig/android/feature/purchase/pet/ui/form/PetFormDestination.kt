package com.hedvig.android.feature.purchase.pet.ui.form

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.purchase.pet.data.Breed
import com.hedvig.android.feature.purchase.pet.data.PetGender
import com.hedvig.android.feature.purchase.pet.data.PetOffers
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun PetFormDestination(
  viewModel: PetFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: PetOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(PetFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(navigateUp = navigateUp) {
    when {
      uiState.isLoadingSession -> HedvigFullScreenCenterAlignedProgress()

      uiState.loadSessionError -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(PetFormEvent.Retry) },
      )

      else -> PetFormBody(
        uiState = uiState,
        onSubmit = { event -> viewModel.emit(event) },
        onDismissError = { viewModel.emit(PetFormEvent.DismissError) },
      )
    }
  }
}

@Composable
private fun PetFormBody(
  uiState: PetFormState,
  onSubmit: (PetFormEvent.SubmitForm) -> Unit,
  onDismissError: () -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var selectedBreed: Breed? by remember { mutableStateOf(null) }
  var birthDate: LocalDate? by remember { mutableStateOf(null) }
  var gender: PetGender? by remember { mutableStateOf(null) }
  var isNeutered: Boolean? by remember { mutableStateOf(null) }
  var speciesAnswer: Boolean? by remember { mutableStateOf(null) }
  var street by rememberSaveable { mutableStateOf("") }
  var zipCode by rememberSaveable { mutableStateOf("") }

  if (uiState.submitError != null) {
    ErrorDialog(
      // TODO: Add "Something went wrong" / "Något gick fel" to Lokalise
      title = "Something went wrong",
      message = uiState.submitError,
      onDismiss = onDismissError,
    )
  }

  PetFormContent(
    isCat = uiState.isCat,
    breeds = uiState.breeds,
    name = name,
    selectedBreed = selectedBreed,
    birthDate = birthDate,
    gender = gender,
    isNeutered = isNeutered,
    speciesAnswer = speciesAnswer,
    street = street,
    zipCode = zipCode,
    errors = uiState,
    isSubmitting = uiState.isSubmitting,
    onNameChanged = { name = it },
    onBreedSelected = { selectedBreed = it },
    onBirthDateSelected = { birthDate = it },
    onGenderSelected = { gender = it },
    onIsNeuteredSelected = { isNeutered = it },
    onSpeciesAnswerSelected = { speciesAnswer = it },
    onStreetChanged = { street = it },
    onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
    onSubmit = {
      onSubmit(
        PetFormEvent.SubmitForm(
          name = name,
          breed = selectedBreed,
          birthDate = birthDate,
          gender = gender,
          isNeutered = isNeutered,
          speciesAnswer = speciesAnswer,
          street = street,
          zipCode = zipCode,
        ),
      )
    },
  )
}

@Composable
private fun PetFormContent(
  isCat: Boolean,
  breeds: List<Breed>,
  name: String,
  selectedBreed: Breed?,
  birthDate: LocalDate?,
  gender: PetGender?,
  isNeutered: Boolean?,
  speciesAnswer: Boolean?,
  street: String,
  zipCode: String,
  errors: PetFormState,
  isSubmitting: Boolean,
  onNameChanged: (String) -> Unit,
  onBreedSelected: (Breed) -> Unit,
  onBirthDateSelected: (LocalDate) -> Unit,
  onGenderSelected: (PetGender) -> Unit,
  onIsNeuteredSelected: (Boolean) -> Unit,
  onSpeciesAnswerSelected: (Boolean) -> Unit,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
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
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = name,
        onValueChange = onNameChanged,
        // TODO: Add "Pet name" / "Husdjurets namn" to Lokalise
        labelText = "Pet name",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.nameError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      BreedDropdown(
        breeds = breeds,
        selectedBreed = selectedBreed,
        onBreedSelected = onBreedSelected,
        hasError = errors.breedError != null,
        errorText = errors.breedError,
        isEnabled = !isSubmitting,
      )

      BirthDatePicker(
        birthDate = birthDate,
        onBirthDateSelected = onBirthDateSelected,
        hasError = errors.birthDateError != null,
        errorText = errors.birthDateError,
        isEnabled = !isSubmitting,
      )

      GenderDropdown(
        isCat = isCat,
        selected = gender,
        onSelected = onGenderSelected,
        hasError = errors.genderError != null,
        errorText = errors.genderError,
        isEnabled = !isSubmitting,
      )

      YesNoDropdown(
        // TODO: Add "Is your pet neutered?" / "Är ditt husdjur kastrerat?" to Lokalise
        label = "Is your pet neutered?",
        // TODO: Add "Select an option" / "Välj ett alternativ" to Lokalise
        hint = "Select an option",
        selected = isNeutered,
        onSelected = onIsNeuteredSelected,
        hasError = errors.isNeuteredError != null,
        errorText = errors.isNeuteredError,
        isEnabled = !isSubmitting,
      )

      YesNoDropdown(
        label = if (isCat) {
          // TODO: Add "Does your cat have outside access?" / "Har din katt utomhustillgång?" to Lokalise
          "Does your cat have outside access?"
        } else {
          // TODO: Add "Have you owned a dog before?" / "Har du haft hund tidigare?" to Lokalise
          "Have you owned a dog before?"
        },
        hint = "Select an option",
        selected = speciesAnswer,
        onSelected = onSpeciesAnswerSelected,
        hasError = errors.speciesAnswerError != null,
        errorText = errors.speciesAnswerError,
        isEnabled = !isSubmitting,
      )

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
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Done,
        ),
        enabled = !isSubmitting,
      )
    }
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
private fun BreedDropdown(
  breeds: List<Breed>,
  selectedBreed: Breed?,
  onBreedSelected: (Breed) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val selectedIndex = selectedBreed?.let { breeds.indexOf(it) }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = breeds.map { SimpleDropdownItem(it.displayName) },
      // TODO: Add "Breed" / "Ras" to Lokalise
      label = "Breed",
    ),
    size = DropdownSize.Medium,
    // TODO: Add "Choose breed" / "Välj ras" to Lokalise
    hintText = "Choose breed",
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onBreedSelected(breeds[index]) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun GenderDropdown(
  isCat: Boolean,
  selected: PetGender?,
  onSelected: (PetGender) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val options = listOf(
    PetGender.MALE to if (isCat) {
      // TODO: Add "Male (cat)" / "Hane" to Lokalise
      "Male"
    } else {
      // TODO: Add "Male (dog)" / "Hane" to Lokalise
      "Male"
    },
    PetGender.FEMALE to if (isCat) {
      // TODO: Add "Female (cat)" / "Hona" to Lokalise
      "Female"
    } else {
      // TODO: Add "Female (dog)" / "Tik" to Lokalise
      "Female"
    },
  )
  val selectedIndex = selected?.let { options.indexOfFirst { (gender, _) -> gender == it } }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = options.map { SimpleDropdownItem(it.second) },
      // TODO: Add "Gender" / "Kön" to Lokalise
      label = "Gender",
    ),
    size = DropdownSize.Medium,
    // TODO: Add "Choose gender" / "Välj kön" to Lokalise
    hintText = "Choose gender",
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onSelected(options[index].first) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun YesNoDropdown(
  label: String,
  hint: String,
  selected: Boolean?,
  onSelected: (Boolean) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  // TODO: Add "Yes" / "Ja" to Lokalise
  // TODO: Add "No" / "Nej" to Lokalise
  val options = listOf(true to "Yes", false to "No")
  val selectedIndex = selected?.let { options.indexOfFirst { (value, _) -> value == it } }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = options.map { SimpleDropdownItem(it.second) },
      label = label,
    ),
    size = DropdownSize.Medium,
    hintText = hint,
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onSelected(options[index].first) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun BirthDatePicker(
  birthDate: LocalDate?,
  onBirthDateSelected: (LocalDate) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val locale = getLocale()
  var showDialog by rememberSaveable { mutableStateOf(false) }
  val initialMillis = birthDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
    ?: Clock.System.now().toEpochMilliseconds()
  val datePickerState = remember(initialMillis) {
    HedvigDatePickerState(
      locale = locale,
      initialSelectedDateMillis = initialMillis,
      initialDisplayedMonthMillis = initialMillis,
      selectableDates = object : HedvigSelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
          val nowMillis = Clock.System.now().toEpochMilliseconds()
          val minMillis = LocalDate.parse("1990-01-01").atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
          return utcTimeMillis in minMillis..nowMillis
        }

        override fun isSelectableYear(year: Int): Boolean {
          val currentYear = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
          return year in 1990..currentYear
        }
      },
    )
  }
  if (showDialog) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDialog = false },
      onConfirmRequest = {
        val selected = datePickerState.selectedDateMillis
        if (selected != null) {
          val date = Instant.fromEpochMilliseconds(selected).toLocalDateTime(TimeZone.UTC).date
          onBirthDateSelected(date)
        }
        showDialog = false
      },
    )
  }
  HedvigCard(
    onClick = { if (isEnabled) showDialog = true },
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp)) {
      HedvigText(
        // TODO: Add "Birth date" / "Födelsedatum" to Lokalise
        text = "Birth date",
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      HedvigText(
        text = birthDate?.toString() ?: run {
          // TODO: Add "Select date" / "Välj datum" to Lokalise
          "Select date"
        },
        style = HedvigTheme.typography.bodyMedium,
      )
      if (hasError && errorText != null) {
        HedvigText(
          text = errorText,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.signalRedText,
        )
      }
    }
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
private fun PreviewPetFormDogEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PetFormContent(
        isCat = false,
        breeds = listOf(Breed("DOG_MIXED", "Mixed breed", true), Breed("DOG_LABRADOR", "Labrador", false)),
        name = "",
        selectedBreed = null,
        birthDate = null,
        gender = null,
        isNeutered = null,
        speciesAnswer = null,
        street = "",
        zipCode = "",
        errors = PetFormState(),
        isSubmitting = false,
        onNameChanged = {},
        onBreedSelected = {},
        onBirthDateSelected = {},
        onGenderSelected = {},
        onIsNeuteredSelected = {},
        onSpeciesAnswerSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPetFormCatFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PetFormContent(
        isCat = true,
        breeds = listOf(Breed("CAT_MAINE_COON", "Maine Coon", false)),
        name = "Whiskers",
        selectedBreed = Breed("CAT_MAINE_COON", "Maine Coon", false),
        birthDate = LocalDate.parse("2022-03-15"),
        gender = PetGender.FEMALE,
        isNeutered = true,
        speciesAnswer = false,
        street = "Storgatan 1",
        zipCode = "12345",
        errors = PetFormState(isCat = true),
        isSubmitting = false,
        onNameChanged = {},
        onBreedSelected = {},
        onBirthDateSelected = {},
        onGenderSelected = {},
        onIsNeuteredSelected = {},
        onSpeciesAnswerSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onSubmit = {},
      )
    }
  }
}
