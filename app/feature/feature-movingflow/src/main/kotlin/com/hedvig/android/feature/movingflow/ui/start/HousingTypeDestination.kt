package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Medium
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.Vertical.Default
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.ui.MovingFlowTopAppBar
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeUiState.Content
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeUiState.HousingTypeError
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeUiState.HousingTypeError.GenericError
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeUiState.HousingTypeError.UserPresentable
import hedvig.resources.R

@Composable
internal fun HousingTypeDestination(
  viewModel: HousingTypeViewModel,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  onNavigateToNextStep: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content) {
    LaunchedEffect(uiState.navigateToNextStep) {
      if (uiState.navigateToNextStep != false) {
        viewModel.emit(HousingTypeEvent.NavigatedToNextStep)
        onNavigateToNextStep()
      }
    }
  }
  HousingTypeScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    exitFlow = exitFlow,
    onDismissStartError = { viewModel.emit(HousingTypeEvent.DismissStartError) },
    onSelectHousingType = { viewModel.emit(HousingTypeEvent.SelectHousingType(it)) },
    onSubmitHousingType = { viewModel.emit(HousingTypeEvent.SubmitHousingType) },
  )
}

@Composable
private fun HousingTypeScreen(
  uiState: HousingTypeUiState,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  onDismissStartError: () -> Unit,
  onSelectHousingType: (HousingType) -> Unit,
  onSubmitHousingType: () -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    Column {
      MovingFlowTopAppBar(navigateUp = navigateUp, exitFlow = exitFlow, withExitConfirmation = false)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        propagateMinConstraints = true,
      ) {
        when (uiState) {
          is HousingTypeError -> {
            HedvigErrorSection(
              onButtonClick = onDismissStartError,
              title = when (uiState) {
                is GenericError -> stringResource(R.string.something_went_wrong)
                is UserPresentable -> uiState.message
              },
              subTitle = when (uiState) {
                is GenericError -> stringResource(R.string.GENERAL_ERROR_BODY)
                is UserPresentable -> null
              },
            )
          }

          is Content -> {
            StartContentScreen(uiState, onSelectHousingType, onSubmitHousingType)
          }

          HousingTypeUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
        }
      }
    }
  }
}

@Composable
private fun StartContentScreen(
  uiState: Content,
  onSelectHousingType: (HousingType) -> Unit,
  onSubmitHousingType: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.padding(horizontal = 16.dp)) {
    FlowHeading(
      stringResource(R.string.insurance_details_change_address_button),
      stringResource(R.string.CHANGE_ADDRESS_SELECT_HOUSING_TYPE_TITLE),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      RadioGroup(
        radioGroupStyle = Default(
          uiState.possibleHousingTypes.map {
            RadioOptionGroupDataSimple(
              RadioOptionData(
                id = it.name,
                optionText = stringResource(it.stringResource()),
                chosenState = if (it == uiState.selectedHousingType) Chosen else NotChosen,
                lockedState = NotLocked,
              ),
            )
          },
        ),
        onOptionClick = { onSelectHousingType(HousingType.valueOf(it)) },
        radioGroupSize = Medium,
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.general_continue_button),
        onClick = onSubmitHousingType,
        enabled = !uiState.buttonLoading,
        isLoading = uiState.buttonLoading,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

internal fun HousingType.stringResource() = when (this) {
  HousingType.ApartmentRent -> R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.ApartmentOwn -> R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.Villa -> R.string.CHANGE_ADDRESS_VILLA_LABEL
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDestinationAnimation(
  @PreviewParameter(StartUiStateProvider::class) state: HousingTypeUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HousingTypeScreen(
        uiState = state,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class StartUiStateProvider : CollectionPreviewParameterProvider<HousingTypeUiState>(
  listOf(
    GenericError(ErrorMessage("Unknown MoveIntentV2CreateMutation error")),
    HousingTypeUiState.Loading,
    Content(
      possibleHousingTypes = HousingType.entries,
      selectedHousingType = HousingType.entries.first(),
      navigateToNextStep = false,
    ),
    Content(
      possibleHousingTypes = HousingType.entries,
      selectedHousingType = HousingType.entries[1],
      navigateToNextStep = false,
      buttonLoading = false,
    ),
    Content(
      possibleHousingTypes = HousingType.entries,
      selectedHousingType = HousingType.entries.first(),
      navigateToNextStep = false,
      buttonLoading = true,
    ),
  ),
)
