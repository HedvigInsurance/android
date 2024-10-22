package com.hedvig.android.feature.movingflow.ui.enternewaddress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialogError
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.feature.movingflow.compose.NoopValidator
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType
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
import kotlinx.datetime.TimeZone
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
  Column(modifier) {
    HedvigText("START Select a housing type")
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
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
        enabled = !uiState.shouldDisableInput,
      )
    }
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
    HedvigButton(
      onClick = submitInput,
      text = stringResource(R.string.general_continue_button),
      enabled = !uiState.shouldDisableInput,
      isLoading = uiState.isLoadingNextStep,
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

@Preview
@Composable
fun PreviewEnterNewAddressScreen() {
  EnterNewAddressScreen(
    uiState = EnterNewAddressUiState.Content(
      moveIntentId = "id",
      moveFromAddressId = "moveFromAddressId",
      movingDate =
        ValidatedInput(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date, NoopValidator()),
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
