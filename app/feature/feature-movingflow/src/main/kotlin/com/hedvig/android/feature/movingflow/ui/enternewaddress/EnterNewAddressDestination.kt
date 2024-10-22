package com.hedvig.android.feature.movingflow.ui.enternewaddress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialogError
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.movingflow.compose.NoopValidator
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.Apartment.WithStudentOption
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.SubmittingInfoFailure.NetworkFailure
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.SubmittingInfoFailure.UserError
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Loading
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.EmptyAddress
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidMovingDate
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidNumberCoInsured
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.InvalidLength
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.Missing
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.MustBeOnlyDigits
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidSquareMeters
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun EnterNewAddressDestination(
  viewModel: EnterNewAddressViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  onNavigateToAddHouseInformation: () -> Unit,
  onNavigateToChoseCoverageLevelAndDeductible: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.navigateToChoseCoverage) {
    LaunchedEffect(uiState.navigateToChoseCoverage) {
      viewModel.emit(EnterNewAddressEvent.NavigatedToChoseCoverage)
      onNavigateToChoseCoverageLevelAndDeductible()
    }
  }
  EnterNewAddressScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    submitInput = { viewModel.emit(EnterNewAddressEvent.Submit) },
    dismissSubmissionError = { viewModel.emit(EnterNewAddressEvent.DismissSubmissionError) },
  )
}

@Composable
private fun EnterNewAddressScreen(
  uiState: EnterNewAddressUiState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  submitInput: () -> Unit,
  dismissSubmissionError: () -> Unit,
) {
  HedvigScaffold(navigateUp) {
    when (uiState) {
      Loading -> HedvigFullScreenCenterAlignedProgress()
      MissingOngoingMovingFlow -> HedvigErrorSection(
        onButtonClick = popBackStack,
        subTitle = null,
        buttonText = "Go back",
      )

      is Content -> EnterNewAddressScreen(
        uiState = uiState,
        submitInput = submitInput,
        dismissSubmissionError = dismissSubmissionError,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun EnterNewAddressScreen(
  uiState: EnterNewAddressUiState.Content,
  submitInput: () -> Unit,
  dismissSubmissionError: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (uiState.submittingInfoFailure != null) {
    HedvigDialogError(
      titleText = stringResource(R.string.something_went_wrong),
      descriptionText = when (uiState.submittingInfoFailure) {
        NetworkFailure -> stringResource(R.string.GENERAL_ERROR_BODY)
        is UserError -> uiState.submittingInfoFailure.message
      },
      buttonText = stringResource(R.string.GENERAL_RETRY),
      onButtonClick = dismissSubmissionError,
      onDismissRequest = dismissSubmissionError,
    )
  }
  Column(
    modifier
      .clearFocusOnTap()
      .padding(horizontal = 16.dp),
  ) {
    HedvigText(
      text = stringResource(R.string.insurance_details_change_address_button),
      style = HedvigTheme.typography.bodyMedium,
    )
    HedvigText(
      text = stringResource(R.string.CHANGE_ADDRESS_ENTER_NEW_ADDRESS_TITLE),
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HedvigTextField(
          text = uiState.address.value ?: "",
          onValueChange = {
            uiState.address.updateValue(it)
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = when (val validationError = uiState.address.validationError) {
            null -> HedvigTextFieldDefaults.ErrorState.NoError
            else -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(validationError.string())
          },
          enabled = !uiState.shouldDisableInput,
        )
        HedvigTextField(
          text = uiState.postalCode.value ?: "",
          onValueChange = onValueChange@{
            if (it.isEmpty()) {
              uiState.postalCode.updateValue(null)
            } else if (it.isDigitsOnly()) {
              uiState.postalCode.updateValue(it)
            }
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = when (val validationError = uiState.postalCode.validationError) {
            null -> HedvigTextFieldDefaults.ErrorState.NoError
            else -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(validationError.string())
          },
          enabled = !uiState.shouldDisableInput,
        )
        HedvigTextField(
          text = uiState.squareMeters.value?.toString() ?: "",
          onValueChange = onValueChange@{
            if (it.isEmpty()) {
              uiState.squareMeters.updateValue(null)
            } else {
              val number = it.toIntOrNull()
              if (number != null) {
                uiState.squareMeters.updateValue(number)
              }
            }
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = when (val validationError = uiState.squareMeters.validationError) {
            null -> HedvigTextFieldDefaults.ErrorState.NoError
            else -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(validationError.string())
          },
          suffix = {
            HedvigText(
              text = stringResource(R.string.CHANGE_ADDRESS_SIZE_SUFFIX),
              color = HedvigTheme.colorScheme.textSecondary,
            )
          },
          enabled = !uiState.shouldDisableInput,
        )
        HedvigStepper(
          text = when (val numberCoInsured = uiState.numberCoInsured.value) {
            0 -> stringResource(R.string.CHANGE_ADDRESS_ONLY_YOU)
            else -> stringResource(R.string.CHANGE_ADDRESS_YOU_PLUS, numberCoInsured)
          },
          stepperSize = Medium,
          stepperStyle = Labeled(stringResource(R.string.CHANGE_ADDRESS_CO_INSURED_LABEL)),
          onMinusClick = { uiState.numberCoInsured.updateValue(uiState.numberCoInsured.value - 1) },
          onPlusClick = { uiState.numberCoInsured.updateValue(uiState.numberCoInsured.value + 1) },
          isPlusEnabled = !uiState.isLoadingNextStep,
          isMinusEnabled = !uiState.isLoadingNextStep,
          errorText = uiState.numberCoInsured.validationError?.string(),
        )
        DatePickerField(uiState.movingDate, uiState.allowedMovingDateRange, uiState.shouldDisableInput)
        if (uiState.propertyType is WithStudentOption) {
          HedvigToggle(
            labelText = stringResource(R.string.CHANGE_ADDRESS_STUDENT_LABEL),
            turnedOn = uiState.propertyType.isStudentSelected,
            onClick = { uiState.propertyType.selectedIsStudent.updateValue(it) },
            enabled = !uiState.shouldDisableInput,
            toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Medium),
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigNotificationCard(stringResource(R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT), NotificationPriority.Info)
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        onClick = submitInput,
        text = stringResource(R.string.general_continue_button),
        enabled = !uiState.shouldDisableInput,
        isLoading = uiState.isLoadingNextStep,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DatePickerField(
  input: ValidatedInput<LocalDate?, LocalDate, EnterNewAddressValidationError>,
  allowedMovingDateRange: ClosedRange<LocalDate>,
  shouldDisableInput: Boolean,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val selectedDateMillis = input.value?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
  HedvigDatePicker(
    isVisible = showDatePicker,
    datePickerState = HedvigDatePickerState(
      selectedDateMillis = selectedDateMillis,
      displayedMonthMillis = selectedDateMillis,
      yearRange = IntRange(
        start = allowedMovingDateRange.start.year,
        endInclusive = allowedMovingDateRange.endInclusive.year,
      ),
      minDateInMillis = allowedMovingDateRange.start.atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds(),
      maxDateInMillis = allowedMovingDateRange.endInclusive.atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds(),
      locale = getLocale(),
    ),
    onDismissRequest = { showDatePicker = false },
    onConfirmRequest = { showDatePicker = false },
    onSelectedDateChanged = {
      if (it != null) {
        input.updateValue(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date)
      }
    },
  )
  val locale = getLocale()
  val rememberHedvigDateTimeFormatter = remember(locale) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale)
  }
  val text = input.value?.let { localDate ->
    rememberHedvigDateTimeFormatter.format(localDate.toJavaLocalDate())
  }
  // Workaround to get the layout of the hedvigTextField, without the functionality of it. Perhaps room for another
  //  component here
  Box(modifier) {
    HedvigTextField(
      text = text ?: "",
      onValueChange = {},
      readOnly = true,
      enabled = !shouldDisableInput,
      labelText = stringResource(R.string.CHANGE_ADDRESS_MOVING_DATE_LABEL),
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      trailingContent = {},
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clickable(enabled = !shouldDisableInput) {
          showDatePicker = true
        },
    )
  }
}

// todo string resources
@Composable
private fun EnterNewAddressValidationError.string(): String {
  return this.toString()
  @Suppress("UNREACHABLE_CODE")
  return when (this) {
    EmptyAddress -> "EmptyAddress:$this"
    is InvalidMovingDate -> "is InvalidMovingDate:$this"
    is InvalidNumberCoInsured -> "is InvalidNumberCoInsured:$this"
    InvalidLength -> "InvalidLength:$this"
    Missing -> "Missing:$this"
    MustBeOnlyDigits -> "MustBeOnlyDigits:$this"
    is InvalidSquareMeters -> "is InvalidSquareMeters:$this"
  }
}

@HedvigMultiScreenPreview
@Composable
fun PreviewEnterNewAddressScreen() {
  EnterNewAddressScreen(
    uiState = EnterNewAddressUiState.Content(
      moveFromAddressId = "moveFromAddressId",
      movingDate = ValidatedInput(
        Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
        NoopValidator(),
      ),
      allowedMovingDateRange = LocalDate(2023, 1, 1)..LocalDate(2100, 1, 1),
      address = ValidatedInput<String?, String, EnterNewAddressValidationError>("address", NoopValidator()),
      postalCode = ValidatedInput("postalCode", NoopValidator()),
      squareMeters = ValidatedInput(0, NoopValidator()),
      numberCoInsured = ValidatedInput(0, NoopValidator()),
      propertyType = PropertyType.House,
      submittingInfoFailure = null,
      isLoadingNextStep = false,
      navigateToChoseCoverage = false,
      navigateToAddHouseInformation = false,
    ),
    submitInput = {},
    dismissSubmissionError = {},
  )
}
