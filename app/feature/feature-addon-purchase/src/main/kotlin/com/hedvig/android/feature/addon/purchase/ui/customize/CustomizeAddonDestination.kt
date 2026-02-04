package com.hedvig.android.feature.addon.purchase.ui.customize

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.ItemCostDiscount
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuoteInsuranceDocument
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonState.Failure
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonState.Loading
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ChooseOptionInDialog
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ChooseSelectedOption
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ClearNavigation
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.Reload
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.SetSelectedOptionBackToPreviouslyChosen
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.SubmitSelected
import hedvig.resources.ADDON_BADGE_ACTIVE
import hedvig.resources.ADDON_FLOW_COVER_BUTTON
import hedvig.resources.ADDON_FLOW_PRICE_LABEL
import hedvig.resources.ADDON_FLOW_SELECT_BUTTON
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.open_chat
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CustomizeAddonDestination(
  viewModel: CustomizeAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  popAddonFlow: () -> Unit,
  navigateToSummary: (summaryParameters: SummaryParameters) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<Pair<String?, List<PerilData>>>) -> Unit,
) {
  val uiState: CustomizeAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    popAddonFlow = popAddonFlow,
    submitSelected = {
      viewModel.emit(SubmitSelected)
    },
    submitToggled = {
      viewModel.emit(CustomizeTravelAddonEvent.SubmitToggled)
    },
    reload = {
      viewModel.emit(Reload)
    },
    onChooseOptionInDialog = { option ->
      viewModel.emit(ChooseOptionInDialog(option))
    },
    onChooseSelectedOption = {
      viewModel.emit(ChooseSelectedOption)
    },
    onSetOptionBackToPreviouslyChosen = {
      viewModel.emit(SetSelectedOptionBackToPreviouslyChosen)
    },
    navigateToSummary = { params ->
      viewModel.emit(ClearNavigation)
      navigateToSummary(params)
    },
    onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
    navigateToChat = onNavigateToNewConversation,
    onToggleOption = {
      viewModel.emit(CustomizeTravelAddonEvent.ToggleOption(it))
    },
  )
}

@Composable
private fun CustomizeTravelAddonScreen(
  uiState: CustomizeAddonState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  submitSelected: () -> Unit,
  submitToggled: () -> Unit,
  navigateToSummary: (summaryParameters: SummaryParameters) -> Unit,
  onChooseOptionInDialog: (AddonQuote) -> Unit,
  onToggleOption: (AddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<Pair<String?, List<PerilData>>>) -> Unit,
  reload: () -> Unit,
  popAddonFlow: () -> Unit,
  navigateToChat: () -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      is Failure -> FailureScreen(
        errorMessage = uiState.errorMessage,
        reload = reload,
        popBackStack = popBackStack,
        navigateToChat = navigateToChat,
      )

      Loading -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      is CustomizeAddonState.Success -> {
        LaunchedEffect(uiState.commonParams.summaryParamsToNavigateFurther) {
          val summaryParams = uiState.commonParams.summaryParamsToNavigateFurther
          if (summaryParams != null) {
            navigateToSummary(summaryParams)
          }
        }
        CustomizeSelectableAddonScreenContent(
          uiState = uiState,
          navigateUp = navigateUp,
          submitSelected = submitSelected,
          onChooseSelectedOption = onChooseSelectedOption,
          onChooseOptionInDialog = onChooseOptionInDialog,
          onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
          onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
          popAddonFlow = popAddonFlow,
          onToggleOption = onToggleOption,
          submitToggled = submitToggled,
        )
      }
    }
  }
}

@Composable
private fun FailureScreen(
  errorMessage: String?,
  reload: () -> Unit,
  popBackStack: () -> Unit,
  navigateToChat: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(
          WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal +
              WindowInsetsSides.Bottom,
          ),
        ),
    ) {
      Spacer(Modifier.weight(1f))
      val buttonText = if (errorMessage == null) {
        stringResource(Res.string.GENERAL_RETRY)
      } else {
        stringResource(Res.string.open_chat)
      }
      HedvigErrorSection(
        onButtonClick = if (errorMessage == null) reload else navigateToChat,
        subTitle = errorMessage ?: stringResource(Res.string.GENERAL_ERROR_BODY),
        modifier = Modifier.fillMaxSize(),
        buttonText = buttonText,
      )
      Spacer(Modifier.weight(1f))
      HedvigTextButton(
        stringResource(Res.string.general_close_button),
        onClick = dropUnlessResumed { popBackStack() },
        buttonSize = Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(32.dp))
    }
  }
}

@Composable
private fun CustomizeSelectableAddonScreenContent(
  uiState: CustomizeAddonState.Success,
  navigateUp: () -> Unit,
  popAddonFlow: () -> Unit,
  onChooseOptionInDialog: (AddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<Pair<String?, List<PerilData>>>) -> Unit,
  onToggleOption: (AddonQuote) -> Unit,
  submitSelected: () -> Unit,
  submitToggled: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { popAddonFlow() },
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
      uiState.commonParams.pageTitle,
      uiState.commonParams.pageDescription,
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    CustomizeAddonCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      uiState = uiState,
      onChooseOptionInDialog = onChooseOptionInDialog,
      onChooseSelectedOption = onChooseSelectedOption,
      onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
      onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
      onToggleOption = onToggleOption,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      buttonSize = Large,
      text = stringResource(Res.string.general_continue_button),
      enabled = when (uiState) {
        is CustomizeAddonState.Success.Selectable -> {
          true
        }

        is CustomizeAddonState.Success.Toggleable -> {
          uiState.currentlyChosenOptions.isNotEmpty()
        }
      },
      onClick = dropUnlessResumed {
        when (uiState) {
          is CustomizeAddonState.Success.Selectable -> {
            submitSelected()
          }

          is CustomizeAddonState.Success.Toggleable -> {
            submitToggled()
          }
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
      buttonSize = Large,
      onClick = { popAddonFlow() },
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizeAddonCard(
  uiState: CustomizeAddonState.Success,
  onChooseOptionInDialog: (AddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onToggleOption: (AddonQuote) -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<Pair<String?, List<PerilData>>>) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(
        shape = HedvigTheme.shapes.cornerXLarge,
        color = HedvigTheme.colorScheme.borderPrimary,
        width = 1.dp,
      ),
    color = HedvigTheme.colorScheme.backgroundPrimary,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      HeaderInfoWithCurrentPrice(
        chosenOptionPremiumExtra = when (uiState) {
          is CustomizeAddonState.Success.Selectable -> uiState.chosenOptionPremiumExtra
          is CustomizeAddonState.Success.Toggleable -> uiState.totalPremiumExtra
        },
        exposureName = uiState.commonParams.umbrellaDisplayTitle,
        description = uiState.commonParams.umbrellaDisplayDescription,
      )
      Spacer(Modifier.height(16.dp))
      when (uiState) {
        is CustomizeAddonState.Success.Selectable -> {
          SelectableAddons(
            addonOptions = uiState.addonOffer.addonOptions,
            fieldTitle = uiState.addonOffer.fieldTitle,
            selectionTitle = uiState.addonOffer.selectionTitle,
            selectionDescription = uiState.addonOffer.selectionDescription,
            currentlyChosenOption = uiState.currentlyChosenOption,
            onChooseOptionInDialog = onChooseOptionInDialog,
            onChooseSelectedOption = onChooseSelectedOption,
            onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
            currentlyChosenOptionInDialog = uiState.currentlyChosenOptionInDialog,
          )
        }

        is CustomizeAddonState.Success.Toggleable -> {
          ToggleableAddons(
            currentlyActiveAddons = uiState.currentlyActiveAddons,
            addonOptions = uiState.addonOffer.addonOptions,
            currentlyChosenOptions = uiState.currentlyChosenOptions,
            onToggleOption = onToggleOption,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigButtonGhostWithBorder(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.ADDON_FLOW_COVER_BUTTON),
        onClick = dropUnlessResumed {
          val data = when (uiState) {
            is CustomizeAddonState.Success.Selectable -> listOf(
              null to
                uiState.currentlyChosenOption.addonVariant.perils.map {
                  PerilData(
                    title = it.title,
                    description = it.description,
                    covered = it.covered,
                    colorCode = it.colorCode,
                  )
                },
            )

            is CustomizeAddonState.Success.Toggleable -> uiState.addonOffer.addonOptions.map {
              it.displayTitle to it.addonVariant.perils.map { peril ->
                PerilData(
                  title = peril.title,
                  description = peril.description,
                  covered = peril.covered,
                  colorCode = peril.colorCode,
                )
              }
            }
          }
          onNavigateToTravelInsurancePlusExplanation(data)
        },
      )
    }
  }
}

@Composable
private fun SelectableAddons(
  addonOptions: NonEmptyList<AddonQuote>,
  fieldTitle: String,
  selectionTitle: String,
  selectionDescription: String,
  currentlyChosenOption: AddonQuote,
  currentlyChosenOptionInDialog: AddonQuote?,
  onChooseOptionInDialog: (AddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
) {
  val addonSimpleItems = buildList {
    for (option in addonOptions) {
      add(SimpleDropdownItem(option.displayTitle))
    }
  }
  val isDropdownEnabled = addonOptions.size > 1
  DropdownWithDialog(
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    // Locked option if there is nothing else to chose from
    isEnabled = isDropdownEnabled,
    style = Label(
      label = fieldTitle,
      items = addonSimpleItems,
    ),
    size = Small,
    containerColor = HedvigTheme.colorScheme.surfacePrimary,
    // there is always one option chosen, should never be shown anyway
    hintText = fieldTitle,
    chosenItemIndex = addonOptions.indexOf(currentlyChosenOption)
      .takeIf { it >= 0 },
    onDoAlongWithDismissRequest = onSetOptionBackToPreviouslyChosen,
    modifier = Modifier.accessibilityForDropdown(
      labelText = fieldTitle,
      selectedValue = currentlyChosenOption.displayTitle,
      isEnabled = isDropdownEnabled,
    ),
  ) { onDismissRequest ->
    DropdownContent(
      onContinueButtonClick = {
        onChooseSelectedOption()
        onDismissRequest()
      },
      onCancelButtonClick = {
        onDismissRequest()
      },
      title = selectionTitle,
      subTitle = selectionDescription,
      addonOptions = addonOptions,
      currentlyChosenOptionInDialog = currentlyChosenOptionInDialog,
      onChooseOptionInDialog = { option -> onChooseOptionInDialog(option) },
    )
  }
}

@Composable
private fun ToggleableAddons(
  currentlyActiveAddons: List<CurrentlyActiveAddon>,
  addonOptions: NonEmptyList<AddonQuote>,
  currentlyChosenOptions: List<AddonQuote>,
  onToggleOption: (AddonQuote) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    currentlyActiveAddons.forEach { activeAddon ->
      AddonCheckbox(
        option = AddonCheckboxOption(
          title = activeAddon.displayTitle,
          description = activeAddon.displayDescription,
          type = AddonCheckBoxOptionType.Active,
        ),
        selected = true,
        enabled = false,
        onCheckboxSelected = {},
      )
      Spacer(Modifier.height(4.dp))
    }
    addonOptions.forEachIndexed { index, addonQuote ->
      AddonCheckbox(
        option = AddonCheckboxOption(
          title = addonQuote.displayTitle,
          description = addonQuote.displayDescription,
          type = AddonCheckBoxOptionType.NotActive(addonQuote.itemCost.monthlyNet),
        ),
        selected = currentlyChosenOptions.contains(addonQuote),
        onCheckboxSelected = {
          onToggleOption(addonQuote)
        },
        enabled = true
      )
      if (index != addonOptions.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}

@Composable
private fun AddonCheckbox(
  option: AddonCheckboxOption,
  selected: Boolean,
  enabled: Boolean,
  onCheckboxSelected: () -> Unit,
) {
  Checkbox(
    option = CheckboxOption(
      text = option.title,
      label = option.description
    ),
    selected = selected,
    style = RadioGroupStyle.LeftAligned,
    onCheckboxSelected = onCheckboxSelected,
    enabled = enabled,
    textEndContent = {
      when (option.type) {
        AddonCheckBoxOptionType.Active -> HighlightLabel(
          stringResource(Res.string.ADDON_BADGE_ACTIVE),
          size = HighLightSize.Small,
          color = HighlightLabelDefaults.HighlightColor.Green(HighlightLabelDefaults.HighlightShade.MEDIUM),
        )

        is AddonCheckBoxOptionType.NotActive -> {
          val pricePerMonth = option.type.monthlyNet.getPerMonthDescription()
          HighlightLabel(
            labelText = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              option.type.monthlyNet),
            size = HighLightSize.Small,
            color = Grey(MEDIUM),
            modifier = Modifier
              .wrapContentSize(Alignment.TopEnd)
              .clearAndSetSemantics {
                contentDescription = pricePerMonth
              },
          )
        }
      }
    },
  )
}

private data class AddonCheckboxOption(
  val type: AddonCheckBoxOptionType,
  val title: String,
  val description: String?,
)

private sealed interface AddonCheckBoxOptionType {
  data object Active : AddonCheckBoxOptionType
  data class NotActive(val monthlyNet: UiMoney) : AddonCheckBoxOptionType
}

@Composable
private fun HeaderInfoWithCurrentPrice(
  chosenOptionPremiumExtra: UiMoney?,
  exposureName: String,
  description: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth()) {
    val pricePerMonth = chosenOptionPremiumExtra.getPerMonthDescription()
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(exposureName)
      },
      endSlot = {
        if (chosenOptionPremiumExtra != null) {
          Row(horizontalArrangement = Arrangement.End) {
            HighlightLabel(
              labelText = stringResource(Res.string.ADDON_FLOW_PRICE_LABEL, chosenOptionPremiumExtra),
              size = HighLightSize.Small,
              color = Grey(MEDIUM),
              modifier = Modifier
                .wrapContentSize(Alignment.TopEnd)
                .clearAndSetSemantics {
                  contentDescription = pricePerMonth
                },
            )
          }
        }
      },
      spaceBetween = 8.dp,
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = description,
      lineHeight = HedvigTheme.typography.label.lineHeight,
      fontStyle = HedvigTheme.typography.label.fontStyle,
      fontWeight = HedvigTheme.typography.label.fontWeight,
      fontFamily = HedvigTheme.typography.label.fontFamily,
      fontSize = HedvigTheme.typography.label.fontSize,
      color = HedvigTheme.colorScheme.textSecondary,
    )
  }
}

@Composable
private fun DropdownContent(
  title: String,
  subTitle: String,
  onContinueButtonClick: () -> Unit,
  onCancelButtonClick: () -> Unit,
  addonOptions: NonEmptyList<AddonQuote>,
  currentlyChosenOptionInDialog: AddonQuote?,
  onChooseOptionInDialog: (AddonQuote) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier.verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      title,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
      textAlign = TextAlign.Center,
    )
    HedvigText(
      subTitle,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
    RadioGroup(
      options = addonOptions.map { addonQuote ->
        RadioOption(
          id = RadioOptionId(addonQuote.addonId),
          text = addonQuote.displayTitle,
        )
      },
      selectedOption = currentlyChosenOptionInDialog?.addonId?.let { RadioOptionId(it) },
      onRadioOptionSelected = { id ->
        onChooseOptionInDialog(addonOptions.first { it.addonId == id.id })
      },
      style = RadioGroupStyle.LeftAligned,
      textEndContent = { id ->
        val addon = addonOptions.first { it.addonId == id.id }
        HighlightLabel(
          labelText = stringResource(Res.string.ADDON_FLOW_PRICE_LABEL, addon.itemCost.monthlyNet),
          size = HighLightSize.Small,
          color = Grey(MEDIUM),
        )
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.ADDON_FLOW_SELECT_BUTTON),
      onClick = onContinueButtonClick,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
      buttonSize = Large,
      onClick = onCancelButtonClick,
    )
  }
}

@Composable
private fun ExpandedOptionContent(title: String, premium: String, radioButtonIcon: @Composable () -> Unit) {
  Row {
    radioButtonIcon()
    Spacer(Modifier.width(8.dp))
    Column(Modifier.weight(1f)) {
      HorizontalItemsWithMaximumSpaceTaken(
        { HedvigText(title) },
        {
          HighlightLabel(
            labelText = premium,
            size = HighLightSize.Small,
            color = Grey(MEDIUM),
            modifier = Modifier.wrapContentSize(Alignment.TopEnd),
          )
        },
        spaceBetween = 8.dp,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDropdownContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DropdownContent(
        title = "title",
        subTitle = "subTitle",
        onContinueButtonClick = {},
        onCancelButtonClick = {},
        addonOptions = fakeTravelAddon.addonOptions,
        currentlyChosenOptionInDialog = fakeTravelAddon.addonOptions.first(),
        onChooseOptionInDialog = {},
      )
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun SelectTierScreenPreview(
  @PreviewParameter(CustomizeTravelAddonPreviewProvider::class) uiState: CustomizeAddonState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CustomizeTravelAddonScreen(
        uiState = uiState,
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {}, {},
      )
    }
  }
}

internal class CustomizeTravelAddonPreviewProvider :
  CollectionPreviewParameterProvider<CustomizeAddonState>(
    listOf(
      Loading,
      CustomizeAddonState.Success.Selectable(
        addonOffer = fakeTravelAddon,
        currentlyChosenOption = fakeAddonQuote1,
        currentlyChosenOptionInDialog = fakeAddonQuote1,
        commonParams = CommonSuccessParameters(
          summaryParamsToNavigateFurther = null,
          umbrellaDisplayTitle = "Display title",
          umbrellaDisplayDescription = "Display description",
          activationDate = LocalDate(2026, 2, 20),
          baseQuoteCost = ItemCost(
            UiMoney(100.0, UiCurrencyCode.SEK),
            UiMoney(100.0, UiCurrencyCode.SEK),
            emptyList(),
          ),
          pageTitle = "Page title",
          pageDescription = "Page description",
          currentTotalCost = ItemCost(
            UiMoney(149.0, UiCurrencyCode.SEK),
            UiMoney(149.0, UiCurrencyCode.SEK),
            emptyList(),
          ),
          quoteId = "quoteId",
          notificationMessage = "notificationMessage",
          productVariant = fakeProductVariant,
          contractId = "contractId",
        ),

        currentlyActiveAddon = CurrentlyActiveAddon(
          "CurrentAddon Display Title",
          "CurrentAddon Display Description",
          cost = ItemCost(
            UiMoney(49.0, UiCurrencyCode.SEK),
            UiMoney(49.0, UiCurrencyCode.SEK),
            emptyList(),
          ),
        ),
        chosenOptionPremiumExtra = UiMoney(10.0, UiCurrencyCode.SEK),

        ),
      Failure("Ooops"),
    ),
  )

private val fakeProductVariant = ProductVariant(
  displayName = "fakeProductVariant.displayName",
  contractGroup = ContractGroup.CAR,
  contractType = ContractType.SE_CAR_FULL,
  partner = null,
  perils = listOf(),
  insurableLimits = listOf(),
  documents = listOf(),
  displayTierName = "fakeProductVariant.displayTierName",
  tierDescription = "fakeProductVariant.tierDescription",
  termsVersion = "fakeProductVariant.termsVersion",
)
private val fakeAddonQuote1 = AddonQuote(
  addonId = "addonId1",
  displayTitle = "45 days",
  displayDetails = listOf(),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    perils = listOf(),
    displayName = "45 days",
    product = "",
  ),
  documents = listOf(
    TravelAddonQuoteInsuranceDocument(
      "Some terms",
      "url",
    ),
  ),
  displayDescription = "Travel Plus 45 days",
  itemCost = ItemCost(
    UiMoney(59.0, UiCurrencyCode.SEK),
    UiMoney(69.0, UiCurrencyCode.SEK),
    discounts = listOf(
      ItemCostDiscount(
        campaignCode = "Bundle",
        displayName = "15% bundle discount",
        displayValue = "-19kr/mo",
        explanation = "some explanation",
      ),
    ),
  ),
  addonSubtype = "DAYS_45",
)
private val fakeAddonQuote2 = AddonQuote(
  displayTitle = "60 days",
  addonId = "addonId1",
  displayDetails = listOf(),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    perils = listOf(),
    displayName = "60 days",
    product = "",
  ),
  documents = listOf(
    TravelAddonQuoteInsuranceDocument(
      "Some terms",
      "url",
    ),
  ),
  displayDescription = "Travel Plus 60 days",
  itemCost = ItemCost(
    UiMoney(79.0, UiCurrencyCode.SEK),
    UiMoney(89.0, UiCurrencyCode.SEK),
    discounts = listOf(
      ItemCostDiscount(
        campaignCode = "Bundle",
        displayName = "15% bundle discount",
        displayValue = "-19kr/mo",
        explanation = "some explanation",
      ),
    ),
  ),
  addonSubtype = "DAYS_60",
)
private val fakeTravelAddon = Selectable(
  addonOptions = nonEmptyListOf(
    fakeAddonQuote1,
    fakeAddonQuote2,
  ),
  fieldTitle = "Field Title",
  selectionTitle = "Selection title",
  selectionDescription = "Selection Description",
)
