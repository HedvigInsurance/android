package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ToggleDefaults
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ListInput
import com.hedvig.android.feature.movingflow.compose.NoopValidator
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.NetworkFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.UserError
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Loading
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationValidationError.InvalidYearOfConstruction
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationValidationError.MissingAncillaryArea
import hedvig.resources.R

@Composable
internal fun AddHouseInformationDestination(
  viewModel: AddHouseInformationViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  onNavigateToChoseCoverageLevelAndDeductible: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.navigateToChoseCoverage) {
    LaunchedEffect(uiState.navigateToChoseCoverage) {
      viewModel.emit(AddHouseInformationEvent.NavigatedToChoseCoverage)
      onNavigateToChoseCoverageLevelAndDeductible()
    }
  }
  AddHouseInformationScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    dismissSubmissionError = { viewModel.emit(AddHouseInformationEvent.DismissSubmissionError) },
    onSubmit = { viewModel.emit(AddHouseInformationEvent.Submit) },
  )
}

@Composable
private fun AddHouseInformationScreen(
  uiState: AddHouseInformationUiState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  dismissSubmissionError: () -> Unit,
  onSubmit: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = null,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      Loading -> HedvigFullScreenCenterAlignedProgress()
      MissingOngoingMovingFlow -> HedvigErrorSection(
        onButtonClick = popBackStack,
        subTitle = null,
        buttonText = stringResource(R.string.app_info_submit_bug_go_back),
      )

      is Content -> AddHouseInformationScreen(uiState, dismissSubmissionError, onSubmit, Modifier.weight(1f))
    }
  }
}

@Composable
private fun AddHouseInformationScreen(
  content: AddHouseInformationUiState.Content,
  dismissSubmissionError: () -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (content.submittingInfoFailure != null) {
    HedvigDialogError(
      titleText = stringResource(R.string.something_went_wrong),
      descriptionText = when (content.submittingInfoFailure) {
        NetworkFailure -> stringResource(R.string.GENERAL_ERROR_BODY)
        is UserError -> content.submittingInfoFailure.message
      },
      buttonText = stringResource(R.string.GENERAL_RETRY),
      onButtonClick = dismissSubmissionError,
      onDismissRequest = dismissSubmissionError,
    )
  }
  Column(modifier.padding(horizontal = 16.dp)) {
    HedvigText(
      text = stringResource(R.string.insurance_details_change_address_button),
      style = HedvigTheme.typography.bodyMedium,
    )
    HedvigText(
      text = stringResource(R.string.CHANGE_ADDRESS_INFORMATION_ABOUT_YOUR_HOUSE),
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HedvigTextField(
          text = content.addressInput.yearOfConstruction.value?.toString() ?: "",
          onValueChange = {
            val number = it.toIntOrNull()
            if (it.isDigitsOnly() && number != null) {
              content.addressInput.yearOfConstruction.updateValue(it.toIntOrNull())
            }
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = when (val validationError = content.addressInput.yearOfConstruction.validationError) {
            null -> HedvigTextFieldDefaults.ErrorState.NoError
            else -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(validationError.string())
          },
          enabled = !content.shouldDisableInput,
        )
        HedvigTextField(
          text = content.addressInput.ancillaryArea.value?.toString() ?: "",
          onValueChange = {
            val number = it.toIntOrNull()
            if (it.isDigitsOnly() && number != null) {
              content.addressInput.ancillaryArea.updateValue(it.toIntOrNull())
            }
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_ANCILLARY_AREA_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = when (val validationError = content.addressInput.ancillaryArea.validationError) {
            null -> HedvigTextFieldDefaults.ErrorState.NoError
            else -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(validationError.string())
          },
          enabled = !content.shouldDisableInput,
        )
        HedvigStepper(
          text = content.addressInput.numberOfBathrooms.value.toString(),
          stepperSize = Medium,
          stepperStyle = Labeled(stringResource(R.string.CHANGE_ADDRESS_BATHROOMS_LABEL)),
          onMinusClick = {
            content.addressInput.numberOfBathrooms.updateValue(content.addressInput.numberOfBathrooms.value - 1)
          },
          onPlusClick = {
            content.addressInput.numberOfBathrooms.updateValue(content.addressInput.numberOfBathrooms.value + 1)
          },
          isPlusEnabled = !content.isLoadingNextStep && content.addressInput.numberOfBathrooms.canIncrement,
          isMinusEnabled = !content.isLoadingNextStep && content.addressInput.numberOfBathrooms.canDecrement,
        )
        HedvigToggle(
          labelText = stringResource(R.string.CHANGE_ADDRESS_SUBLET_LABEL),
          toggleStyle = ToggleStyle.Default(ToggleDefaults.ToggleDefaultStyleSize.Small),
          turnedOn = content.addressInput.isSublet.value,
          onClick = { content.addressInput.isSublet.updateValue(it) },
          enabled = !content.shouldDisableInput,
          modifier = Modifier.fillMaxWidth(),
        )
        // todo extra buildings
      }
      Spacer(Modifier.height(16.dp))
      HedvigNotificationCard(
        message = stringResource(R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
        priority = Info,
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
        onClick = onSubmit,
        isLoading = !content.shouldDisableInput,
        enabled = !content.shouldDisableInput,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun AddHouseInformationValidationError.string(): String {
  return this.toString()
  @Suppress("UNREACHABLE_CODE")
  return when (this) {
    InvalidYearOfConstruction.Missing -> TODO()
    InvalidYearOfConstruction.TooEarly -> TODO()
    MissingAncillaryArea -> TODO()
  }
}

@Preview
@Composable
private fun PreviewAddHouseInformationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddHouseInformationScreen(
        uiState = Content(
          moveFromAddressId = "",
          addressInput = AddressInput(
            yearOfConstruction = ValidatedInput(1900, NoopValidator()),
            ancillaryArea = ValidatedInput(155, NoopValidator()),
            numberOfBathrooms = ConstrainedNumberInput(1, 1..10),
            isSublet = BooleanInput(true),
            possibleExtraBuildingTypes = emptyList(),
            extraBuildings = ListInput(emptyList()),
          ),
          isLoadingNextStep = false,
          submittingInfoFailure = null,
          navigateToChoseCoverage = false,
        ),
        navigateUp = {},
        popBackStack = {},
        dismissSubmissionError = {},
        onSubmit = {},
      )
    }
  }
}
