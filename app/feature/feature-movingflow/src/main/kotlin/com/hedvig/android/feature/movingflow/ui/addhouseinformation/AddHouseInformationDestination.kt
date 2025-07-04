package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.PrimaryAlt
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.Error.WithMessage
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.NoError
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize.Small
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ListInput
import com.hedvig.android.feature.movingflow.compose.NoopValidator
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Attefall
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Barn
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Boathouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Carport
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Friggebod
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Garage
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Gazebo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Greenhouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Guesthouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Other
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Outhouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Sauna
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Shed
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Storehouse
import com.hedvig.android.feature.movingflow.ui.MovingFlowTopAppBar
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.NavigatedToChoseCoverage
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.Submit
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.NetworkFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.UserError
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Loading
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationValidationError.InvalidYearOfConstruction.Missing
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationValidationError.InvalidYearOfConstruction.TooEarly
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationValidationError.MissingAncillaryArea
import hedvig.resources.R

@Composable
internal fun AddHouseInformationDestination(
  viewModel: AddHouseInformationViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  exitFlow: () -> Unit,
  onNavigateToChoseCoverageLevelAndDeductible: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.navigateToChoseCoverage) {
    LaunchedEffect(uiState.navigateToChoseCoverage) {
      viewModel.emit(NavigatedToChoseCoverage)
      onNavigateToChoseCoverageLevelAndDeductible()
    }
  }
  AddHouseInformationScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    exitFlow = exitFlow,
    dismissSubmissionError = { viewModel.emit(DismissSubmissionError) },
    onSubmit = { viewModel.emit(Submit) },
  )
}

@Composable
private fun AddHouseInformationScreen(
  uiState: AddHouseInformationUiState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  exitFlow: () -> Unit,
  dismissSubmissionError: () -> Unit,
  onSubmit: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      MovingFlowTopAppBar(navigateUp = navigateUp, exitFlow = exitFlow)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        propagateMinConstraints = true,
      ) {
        when (uiState) {
          Loading -> HedvigFullScreenCenterAlignedProgress()
          MissingOngoingMovingFlow -> HedvigErrorSection(
            onButtonClick = popBackStack,
            subTitle = null,
            buttonText = stringResource(R.string.app_info_submit_bug_go_back),
          )

          is Content -> AddHouseInformationScreen(uiState, dismissSubmissionError, onSubmit)
        }
      }
    }
  }
}

@Composable
private fun AddHouseInformationScreen(
  content: Content,
  dismissSubmissionError: () -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (content.submittingInfoFailure != null) {
    ErrorDialog(
      title = stringResource(R.string.something_went_wrong),
      message = when (content.submittingInfoFailure) {
        NetworkFailure -> stringResource(R.string.GENERAL_ERROR_BODY)
        is UserError -> content.submittingInfoFailure.message
      },
      buttonText = stringResource(R.string.GENERAL_RETRY),
      onButtonClick = dismissSubmissionError,
      onDismiss = dismissSubmissionError,
    )
  }
  Column(modifier.padding(horizontal = 16.dp)) {
    FlowHeading(
      stringResource(R.string.insurance_details_change_address_button),
      stringResource(R.string.CHANGE_ADDRESS_INFORMATION_ABOUT_YOUR_HOUSE),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HedvigTextField(
          text = content.addressInput.yearOfConstruction.value?.toString() ?: "",
          onValueChange = {
            content.addressInput.yearOfConstruction.updateValue(it.toIntOrNull())
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = TextFieldSize.Medium,
          errorState = when (val validationError = content.addressInput.yearOfConstruction.validationError) {
            null -> NoError
            else -> WithMessage(validationError.string())
          },
          enabled = !content.shouldDisableInput,
        )
        HedvigTextField(
          text = content.addressInput.ancillaryArea.value?.toString() ?: "",
          onValueChange = {
            content.addressInput.ancillaryArea.updateValue(it.toIntOrNull())
          },
          labelText = stringResource(R.string.CHANGE_ADDRESS_ANCILLARY_AREA_LABEL),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          textFieldSize = TextFieldSize.Medium,
          errorState = when (val validationError = content.addressInput.ancillaryArea.validationError) {
            null -> NoError
            else -> WithMessage(validationError.string())
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
          toggleStyle = ToggleStyle.Default(Small),
          turnedOn = content.addressInput.isSublet.value,
          onClick = { content.addressInput.isSublet.updateValue(it) },
          enabled = !content.shouldDisableInput,
          modifier = Modifier.fillMaxWidth(),
        )
        ExtraBuildingsCard(
          extraBuildings = content.addressInput.extraBuildings,
          shouldDisableInput = content.shouldDisableInput,
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
        onClick = onSubmit,
        isLoading = content.shouldDisableInput,
        enabled = !content.shouldDisableInput,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun ExtraBuildingsCard(
  extraBuildings: ListInput<ExtraBuildingInfo>,
  shouldDisableInput: Boolean,
  modifier: Modifier = Modifier,
) {
  var extraBuildingsDialogOpen by rememberSaveable { mutableStateOf(false) }
  if (extraBuildingsDialogOpen) {
    HedvigDialog(
      contentPadding = PaddingValues(0.dp),
      dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
      onDismissRequest = { extraBuildingsDialogOpen = false },
      style = NoButtons,
    ) {
      ExtraBuildingsDialogContent(
        extraBuildings = extraBuildings,
        dismissDialog = { extraBuildingsDialogOpen = false },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
  HedvigCard(modifier.fillMaxWidth()) {
    Column(
      Modifier.padding(
        start = 16.dp,
        top = 12.dp,
        end = 16.dp,
        bottom = 16.dp,
      ),
    ) {
      HedvigText(
        text = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_LABEL),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      if (extraBuildings.value.isNotEmpty()) {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          for ((index, extraBuilding) in extraBuildings.value.withIndex()) {
            if (index != 0) {
              HorizontalDivider()
            }
            key(extraBuilding) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                  HedvigText(extraBuilding.type.string())
                  HedvigText(
                    buildString {
                      append(extraBuilding.area)
                      append(" ")
                      append(stringResource(R.string.CHANGE_ADDRESS_SIZE_SUFFIX))
                      if (extraBuilding.hasWaterConnected) {
                        append(" ∙ ")
                        append(stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_LABEL))
                      }
                    },
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
                  )
                }
                IconButton(
                  onClick = {
                    extraBuildings.removeItem(extraBuilding)
                  },
                  enabled = !shouldDisableInput,
                ) {
                  Icon(HedvigIcons.Close, stringResource(R.string.GENERAL_REMOVE), Modifier.size(16.dp))
                }
              }
            }
          }
        }
      } else {
        Spacer(Modifier.height(8.dp))
      }
      HedvigButton(
        text = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE),
        onClick = { extraBuildingsDialogOpen = true },
        enabled = !shouldDisableInput,
        buttonStyle = PrimaryAlt,
        buttonSize = ButtonSize.Medium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun ExtraBuildingsDialogContent(
  extraBuildings: ListInput<ExtraBuildingInfo>,
  dismissDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var chosenBuilding: MoveExtraBuildingType? by remember { mutableStateOf(null) }
  var size: Int? by remember { mutableStateOf(null) }
  var isConnectedToWater: Boolean by remember { mutableStateOf(false) }
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        HedvigCard {
          RadioGroup(
            radioGroupStyle = RadioGroupStyle.VerticalWithGroupLabel.LeftAligned(
              groupLabelText = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDING_CONTAINER_TITLE),
              dataList = MoveExtraBuildingType.entries.map { extraBuildingType ->
                RadioOptionGroupDataSimple(
                  RadioOptionData(
                    id = extraBuildingType.name,
                    optionText = extraBuildingType.string(),
                    chosenState = if (chosenBuilding == extraBuildingType) Chosen else NotChosen,
                  ),
                )
              },
            ),
            onOptionClick = { chosenBuilding = MoveExtraBuildingType.valueOf(it) },
            radioGroupSize = RadioGroupSize.Medium,
          )
        }
        HedvigTextField(
          text = size?.toString() ?: "",
          onValueChange = { size = it.toIntOrNull() },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          labelText = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDING_SIZE_LABEL),
          textFieldSize = TextFieldSize.Medium,
        )
        HedvigToggle(
          labelText = stringResource(R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_INPUT_LABEL),
          turnedOn = isConnectedToWater,
          onClick = { isConnectedToWater = it },
          enabled = true,
          toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Medium),
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.general_save_button),
        onClick = {
          val area = size ?: return@HedvigButton
          val type = chosenBuilding ?: return@HedvigButton
          extraBuildings.updateValue(extraBuildings.value + ExtraBuildingInfo(area, type, isConnectedToWater))
          dismissDialog()
        },
        enabled = chosenBuilding != null && size != null,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(R.string.general_cancel_button),
        onClick = dismissDialog,
        enabled = true,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun MoveExtraBuildingType.string(): String {
  return stringResource(
    when (this) {
      Garage -> R.string.FIELD_EXTRA_BUIDLINGS_GARAGE_LABEL
      Carport -> R.string.FIELD_EXTRA_BUIDLINGS_CARPORT_LABEL
      Shed -> R.string.FIELD_EXTRA_BUIDLINGS_SHED_LABEL
      Storehouse -> R.string.FIELD_EXTRA_BUIDLINGS_STOREHOUSE_LABEL
      Friggebod -> R.string.FIELD_EXTRA_BUIDLINGS_FRIGGEBOD_LABEL
      Attefall -> R.string.FIELD_EXTRA_BUIDLINGS_ATTEFALL_LABEL
      Outhouse -> R.string.FIELD_EXTRA_BUIDLINGS_OUTHOUSE_LABEL
      Guesthouse -> R.string.FIELD_EXTRA_BUIDLINGS_GUESTHOUSE_LABEL
      Gazebo -> R.string.FIELD_EXTRA_BUIDLINGS_GAZEBO_LABEL
      Greenhouse -> R.string.FIELD_EXTRA_BUIDLINGS_GREENHOUSE_LABEL
      Sauna -> R.string.FIELD_EXTRA_BUIDLINGS_SAUNA_LABEL
      Barn -> R.string.FIELD_EXTRA_BUIDLINGS_BARN_LABEL
      Boathouse -> R.string.FIELD_EXTRA_BUIDLINGS_BOATHOUSE_LABEL
      Other -> R.string.FIELD_EXTRA_BUIDLINGS_OTHER_LABEL
    },
  )
}

@Composable
private fun AddHouseInformationValidationError.string(): String {
  return when (this) {
    Missing -> stringResource(R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_ERROR)
    TooEarly -> stringResource(R.string.GENERAL_INVALID_INPUT)
    MissingAncillaryArea -> stringResource(R.string.CHANGE_ADDRESS_ANCILLARY_AREA_ERROR)
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
            extraBuildings = ListInput(
              List(3) {
                ExtraBuildingInfo(it * 10, Barn, it % 2 == 0)
              },
            ),
          ),
          isLoadingNextStep = false,
          submittingInfoFailure = null,
          navigateToChoseCoverage = false,
        ),
        navigateUp = {},
        popBackStack = {},
        exitFlow = {},
        dismissSubmissionError = {},
        onSubmit = {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewAddHouseInformationScreenFailure() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddHouseInformationScreen(
        uiState = AddHouseInformationUiState.MissingOngoingMovingFlow,
        navigateUp = {},
        popBackStack = {},
        exitFlow = {},
        dismissSubmissionError = {},
        onSubmit = {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewExtraBuildingsDialogContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ExtraBuildingsDialogContent(
        ListInput(List(3) { ExtraBuildingInfo(it * 10, Barn, it % 2 == 0) }),
        {},
      )
    }
  }
}
