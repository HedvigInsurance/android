package com.hedvig.android.feature.purchase.car.ui.form

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.car.data.CarOffers

@Composable
internal fun CarFormDestination(
  viewModel: CarFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: CarOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(CarFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "Bilf\u00f6rs\u00e4kring",
  ) {
    when {
      uiState.isLoadingSession -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      uiState.loadSessionError -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(CarFormEvent.Retry) },
        )
      }

      else -> {
        var ssn by remember { mutableStateOf("") }
        var registrationNumber by remember { mutableStateOf("") }
        var selectedMileage: MileageOption? by remember { mutableStateOf(null) }
        var street by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        CarFormContent(
          ssn = ssn,
          registrationNumber = registrationNumber,
          selectedMileage = selectedMileage,
          street = street,
          zipCode = zipCode,
          email = email,
          ssnError = uiState.ssnError,
          registrationNumberError = uiState.registrationNumberError,
          mileageError = uiState.mileageError,
          streetError = uiState.streetError,
          zipCodeError = uiState.zipCodeError,
          emailError = uiState.emailError,
          isSubmitting = uiState.isSubmitting,
          onSsnChanged = { value -> if (value.length <= 12 && value.all { it.isDigit() }) ssn = value },
          onRegistrationNumberChanged = { value ->
            registrationNumber = formatRegistrationNumber(value)
          },
          onMileageSelected = { selectedMileage = it },
          onStreetChanged = { street = it },
          onZipCodeChanged = { value -> if (value.all { it.isDigit() }) zipCode = value },
          onEmailChanged = { email = it },
          onSubmit = {
            viewModel.emit(
              CarFormEvent.SubmitForm(
                ssn = ssn,
                registrationNumber = registrationNumber,
                mileage = selectedMileage?.value,
                street = street,
                zipCode = zipCode,
                email = email,
              ),
            )
          },
        )
      }
    }
  }
}

internal enum class MileageOption(val value: Int, val displayName: String) {
  MILEAGE_0_1000(1000, "0 - 1 000 mil"),
  MILEAGE_1000_1500(1500, "1 000 - 1 500 mil"),
  MILEAGE_1500_2000(2000, "1 500 - 2 000 mil"),
  MILEAGE_2000_2500(2500, "2 000 - 2 500 mil"),
  MILEAGE_2500_PLUS(2501, "2 500+ mil"),
}

private fun formatRegistrationNumber(input: String): String {
  val cleaned = input.uppercase().filter { it.isLetterOrDigit() }
  if (cleaned.length <= 3) return cleaned
  return cleaned.take(3) + " " + cleaned.drop(3).take(3)
}

@Composable
private fun CarFormContent(
  ssn: String,
  registrationNumber: String,
  selectedMileage: MileageOption?,
  street: String,
  zipCode: String,
  email: String,
  ssnError: String?,
  registrationNumberError: String?,
  mileageError: String?,
  streetError: String?,
  zipCodeError: String?,
  emailError: String?,
  isSubmitting: Boolean,
  onSsnChanged: (String) -> Unit,
  onRegistrationNumberChanged: (String) -> Unit,
  onMileageSelected: (MileageOption) -> Unit,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onEmailChanged: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = "Fyll i dina uppgifter s\u00e5 ber\u00e4knar vi ditt pris",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = ssn,
        onValueChange = onSsnChanged,
        labelText = "Personnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = ssnError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = registrationNumber,
        onValueChange = onRegistrationNumberChanged,
        labelText = "Registreringsnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = registrationNumberError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Characters,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )

      DropdownWithDialog(
        style = DropdownStyle.Label(
          items = MileageOption.entries.map { SimpleDropdownItem(it.displayName) },
          label = "Miltal per \u00e5r",
        ),
        size = DropdownSize.Medium,
        hintText = "V\u00e4lj miltal",
        chosenItemIndex = selectedMileage?.let { MileageOption.entries.indexOf(it) },
        onItemChosen = { index -> onMileageSelected(MileageOption.entries[index]) },
        onSelectorClick = {},
        isEnabled = !isSubmitting,
        hasError = mileageError != null,
        errorText = mileageError,
        modifier = Modifier.fillMaxWidth(),
      )

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
        text = email,
        onValueChange = onEmailChanged,
        labelText = "E-post",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = emailError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Done,
        ),
        enabled = !isSubmitting,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Ber\u00e4kna pris",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
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
private fun PreviewCarFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CarFormContent(
        ssn = "",
        registrationNumber = "",
        selectedMileage = null,
        street = "",
        zipCode = "",
        email = "",
        ssnError = null,
        registrationNumberError = null,
        mileageError = null,
        streetError = null,
        zipCodeError = null,
        emailError = null,
        isSubmitting = false,
        onSsnChanged = {},
        onRegistrationNumberChanged = {},
        onMileageSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onEmailChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCarFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CarFormContent(
        ssn = "199001011234",
        registrationNumber = "ABC 12D",
        selectedMileage = MileageOption.MILEAGE_1000_1500,
        street = "Storgatan 1",
        zipCode = "12345",
        email = "test@hedvig.com",
        ssnError = null,
        registrationNumberError = null,
        mileageError = null,
        streetError = null,
        zipCodeError = null,
        emailError = null,
        isSubmitting = false,
        onSsnChanged = {},
        onRegistrationNumberChanged = {},
        onMileageSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onEmailChanged = {},
        onSubmit = {},
      )
    }
  }
}
