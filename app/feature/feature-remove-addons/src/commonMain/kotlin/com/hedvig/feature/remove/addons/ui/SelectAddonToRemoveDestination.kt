package com.hedvig.feature.remove.addons.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SelectAddonToRemoveDestination(
  contractId: String,
  preselectedAddonId: String?,
  navigateUp: () -> Unit,
  navigateToSummary: (
    contractId: String, addonsToRemove: List<CurrentlyActiveAddon>, activationDate: LocalDate, baseCost: ItemCost,
    currentTotalCost: ItemCost, productVariant: ProductVariant, allAddons: List<CurrentlyActiveAddon>
  ) -> Unit,
) {
  val viewModel: SelectAddonToRemoveViewModel = koinViewModel {
    parametersOf(contractId to preselectedAddonId)
  }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectAddonToRemoveScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = {
      viewModel.emit(SelectAddonToRemoveEvent.Retry)
    },
    navigateToSummary = { params ->
      viewModel.emit(SelectAddonToRemoveEvent.ClearNavigation)
      navigateToSummary(
        params.contractId,
        params.addonsToRemove,
        params.activationDate,
        params.baseCost,
        params.currentTotalCost,
        params.productVariant,
        params.existingAddons
      )
    },
    onSubmit = {
      viewModel.emit(SelectAddonToRemoveEvent.Submit)
    },
    onToggleOption = {
      viewModel.emit(SelectAddonToRemoveEvent.ToggleOption(it))
    },
  )
}

@Composable
private fun SelectAddonToRemoveScreen(
  uiState: SelectAddonToRemoveState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onSubmit: () -> Unit,
  onToggleOption: (CurrentlyActiveAddon) -> Unit,
  navigateToSummary: (params: CommonSummaryParameters) -> Unit,
) {
  when (uiState) {
    is SelectAddonToRemoveState.Error -> HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      HedvigErrorSection(
        title = uiState.message ?: stringResource(Res.string.something_went_wrong),
        onButtonClick = reload,
        modifier = Modifier.weight(1f),
      )
    }

    is SelectAddonToRemoveState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is SelectAddonToRemoveState.Success -> {
      LaunchedEffect(uiState.paramsToNavigateToSummary) {
        val summaryParams = uiState.paramsToNavigateToSummary
        if (summaryParams != null) {
          navigateToSummary(summaryParams)
        }
      }
      SelectAddonToRemoveSuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onToggleOption = onToggleOption,
        onSubmit = onSubmit,
      )
    }
  }
}

@Composable
private fun SelectAddonToRemoveSuccessScreen(
  uiState: SelectAddonToRemoveState.Success,
  onToggleOption: (CurrentlyActiveAddon) -> Unit,
  onSubmit: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { navigateUp() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(Res.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowHeading(
      uiState.addonOffer.pageTitle,
      uiState.addonOffer.pageDescription,
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    ToggleableAddons(
      allAddonsToRemove = uiState.addonOffer.existingAddonsToRemove,
      currentlyChosenOptions = uiState.addonsChosenForRemoval,
      onToggleOption = onToggleOption,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(12.dp))
    HedvigButton(
      stringResource(Res.string.general_continue_button),
      enabled = uiState.addonsChosenForRemoval.isNotEmpty(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = onSubmit,
      isLoading = false,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ToggleableAddons(
  allAddonsToRemove: List<CurrentlyActiveAddon>,
  currentlyChosenOptions: List<CurrentlyActiveAddon>,
  onToggleOption: (CurrentlyActiveAddon) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    allAddonsToRemove.forEachIndexed { index, addon ->
      AddonCheckbox(
        option = AddonCheckboxOption(
          title = addon.displayTitle,
          description = addon.displayDescription,
          monthlyGross = addon.cost.monthlyGross,
        ),
        selected = currentlyChosenOptions.contains(addon),
        onCheckboxSelected = {
          onToggleOption(addon)
        },
      )
      if (index != allAddonsToRemove.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}

@Composable
private fun AddonCheckbox(
  option: AddonCheckboxOption,
  selected: Boolean,
  onCheckboxSelected: () -> Unit,
) {
  Checkbox(
    option = CheckboxOption(
      text = option.title,
      label = option.description,
    ),
    selected = selected,
    style = RadioGroupStyle.LeftAligned,
    onCheckboxSelected = onCheckboxSelected,
    enabled = true,
    textEndContent = {
      val pricePerMonth = option.monthlyGross.getPerMonthDescription()
      HighlightLabel(
        labelText = stringResource(
          Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
          option.monthlyGross,
        ),
        size = HighLightSize.Small,
        color = Grey(MEDIUM),
        modifier = Modifier
          .wrapContentSize(Alignment.TopEnd)
          .clearAndSetSemantics {
            contentDescription = pricePerMonth
          },
      )
    },
  )
}

private data class AddonCheckboxOption(
  val monthlyGross: UiMoney,
  val title: String,
  val description: String?,
)


