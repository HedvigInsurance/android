package com.hedvig.android.feature.addon.purchase.ui

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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.nonEmptyListOf
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelPlusAddon
import com.hedvig.android.feature.addon.purchase.data.TravelAddonOption
import com.hedvig.android.feature.addon.purchase.ui.CustomizeTravelAddonProvider
import com.hedvig.android.feature.addon.purchase.ui.CustomizeTravelAddonState.Failure
import com.hedvig.android.feature.addon.purchase.ui.CustomizeTravelAddonState.Loading
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun CustomizeTravelAddonDestination(
  viewModel: CustomizeTravelAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
) {
  val uiState: CustomizeTravelAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    navigateToSummary = navigateToSummary,
    reload = {
      viewModel.emit(CustomizeTravelAddonEvent.Reload)
    },
    onChooseOptionInDialog = { option ->
      viewModel.emit(CustomizeTravelAddonEvent.ChooseOptionInDialog(option))
    },
    onChooseSelectedOption = {
      viewModel.emit(CustomizeTravelAddonEvent.ChooseSelectedOption)
    },
    onSetOptionBackToPreviouslyChosen = {
      viewModel.emit(CustomizeTravelAddonEvent.SetOptionBackToPreviouslyChosen)
    },
  )
}

@Composable
private fun CustomizeTravelAddonScreen(
  uiState: CustomizeTravelAddonState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
  onChooseOptionInDialog: (TravelAddonOption) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  reload: () -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      Failure -> FailureScreen(reload, popBackStack)
      Loading -> HedvigFullScreenCenterAlignedProgress()
      is CustomizeTravelAddonState.Success -> CustomizeTravelAddonScreenContent(
        uiState = state,
        navigateUp = navigateUp,
        navigateToSummary = navigateToSummary,
        onChooseSelectedOption = onChooseSelectedOption,
        onChooseOptionInDialog = onChooseOptionInDialog,
        onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
      )
    }
  }
}

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit) {
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
      HedvigErrorSection(
        onButtonClick = reload,
        modifier = Modifier.fillMaxSize(),
      )
      Spacer(Modifier.weight(1f))
      HedvigTextButton(
        stringResource(R.string.general_close_button),
        onClick = dropUnlessResumed { popBackStack() },
        buttonSize = Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(32.dp))
    }
  }
}

@Composable
private fun CustomizeTravelAddonScreenContent(
  uiState: CustomizeTravelAddonState.Success,
  navigateUp: () -> Unit,
  onChooseOptionInDialog: (TravelAddonOption) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
) {
  val referralExplanationBottomSheetState = rememberHedvigBottomSheetState<String>()
  TravelPlusExplanationBottomSheet(referralExplanationBottomSheetState)
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
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.ADDON_FLOW_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(R.string.ADDON_FLOW_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    CustomizeTravelAddonCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      uiState = uiState,
      onChooseOptionInDialog = onChooseOptionInDialog,
      onChooseSelectedOption = onChooseSelectedOption,
      onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
    )
    Spacer(Modifier.height(8.dp))
    TravelPlusInfoCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      onButtonClick = {
        referralExplanationBottomSheetState.show(uiState.travelPlusAddon.additionalInfo)
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      buttonSize = Large,
      text = stringResource(R.string.general_continue_button),
      enabled = true,
      onClick = dropUnlessResumed { navigateToSummary(uiState.currentlyChosenOption) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizeTravelAddonCard(
  uiState: CustomizeTravelAddonState.Success,
  onChooseOptionInDialog: (TravelAddonOption) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      HeaderInfoWithCurrentPrice(
        chosenOptionPremiumExtra = uiState.currentlyChosenOption.extraAmount,
        exposureName = uiState.travelPlusAddon.exposureName,
        description = uiState.travelPlusAddon.description,
      )
      Spacer(Modifier.height(16.dp))
      val addonSimpleItems = buildList {
        for (option in uiState.travelPlusAddon.addonOptions) {
          add(SimpleDropdownItem(option.optionName))
        }
      }
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        isEnabled = uiState.travelPlusAddon.addonOptions.size > 1,
        style = Label(
          label = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER), // todo: check here
          items = addonSimpleItems,
        ),
        size = Small,
        hintText = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER), // todo: check here
        chosenItemIndex = uiState.travelPlusAddon.addonOptions.indexOf(uiState.currentlyChosenOption)
          .takeIf { it >= 0 },
        onSelectorClick = {
          // todo: check here!
        },
        onDoAlongWithDismissRequest = onSetOptionBackToPreviouslyChosen,
      ) { onDismissRequest ->
        val listOfOptions = uiState.travelPlusAddon.addonOptions.map { option ->
          ExpandedRadioOptionData(
            chosenState = if (uiState.currentlyChosenOption == option) Chosen else NotChosen,
            title = option.optionName,
            premium = stringResource(R.string.ADDON_FLOW_SUCCESS_SUBTITLE, option.extraAmount.amount.toInt()),
            onRadioOptionClick = {
              onChooseOptionInDialog(option)
            },
          )
        }
        DropdownContent(
          onContinueButtonClick = {
            onChooseSelectedOption()
            onDismissRequest()
          },
          onCancelButtonClick = {
            onDismissRequest()
          },
          title = "Select number of covered days", // TODO
          data = listOfOptions,
          subTitle = "Pick the right fit for your travel needs", // TODO
        )
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}

private data class ExpandedRadioOptionData(
  val onRadioOptionClick: () -> Unit,
  val chosenState: ChosenState,
  val title: String,
  val premium: String,
)

@Composable
private fun HeaderInfoWithCurrentPrice(
  chosenOptionPremiumExtra: UiMoney,
  exposureName: String,
  description: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth()) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(exposureName)
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          HighlightLabel(
            labelText = stringResource(R.string.ADDON_FLOW_PRICE_LABEL, chosenOptionPremiumExtra.amount.toInt()),
            size = HighLightSize.Small,
            color = HighlightColor.Grey(MEDIUM),
            modifier = Modifier.wrapContentSize(Alignment.TopEnd),
          )
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
private fun TravelPlusInfoCard(onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
  // todo: add another color for notification priority!
  HedvigNotificationCard(
    modifier = modifier,
    message = "Click to learn more about our extended travel coverage Travel Insurance Plus", // todo: here: from BE or not?
    priority = NotificationDefaults.NotificationPriority.NeutralToast, // todo: CHANGE here
    style = NotificationDefaults.InfoCardStyle.Button(
      onButtonClick = onButtonClick,
      buttonText = stringResource(R.string.ADDON_FLOW_LEARN_MORE_BUTTON),
    ),
  )
}

@Composable
private fun DropdownContent(
  title: String,
  subTitle: String,
  onContinueButtonClick: () -> Unit,
  onCancelButtonClick: () -> Unit,
  data: List<ExpandedRadioOptionData>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      title,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    HedvigText(
      subTitle,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
    data.forEachIndexed { index, option ->
      RadioOption(
        chosenState = option.chosenState,
        onClick = option.onRadioOptionClick,
        optionContent = { radioButtonIcon ->
          ExpandedOptionContent(
            title = option.title,
            premium = option.premium,
            radioButtonIcon = radioButtonIcon,
          )
        },
      )
      if (index != data.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onContinueButtonClick,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
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
            color = HighlightColor.Grey(MEDIUM),
            modifier = Modifier.wrapContentSize(Alignment.TopEnd),
          )
        },
        spaceBetween = 8.dp,
      )
    }
  }
}

@Composable
internal fun TravelPlusExplanationBottomSheet(sheetState: HedvigBottomSheetState<String>) {
  HedvigBottomSheet(sheetState) { explanation ->
    HedvigText(
      text = "What is Travel Plus?", // todo: here, from BE or static?
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = explanation,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      buttonSize = Large,
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

// @HedvigMultiScreenPreview
// @Composable
// private fun SelectTierScreenPreview(
//  @PreviewParameter(CustomizeTravelAddonProvider::class) uiState: CustomizeTravelAddonState,
// ) {
//  HedvigTheme {
//    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
//    }
//  }
// }

@HedvigMultiScreenPreview
@Composable
private fun SelectTierScreenPreview(
  @PreviewParameter(CustomizeTravelAddonProvider::class) uiState: CustomizeTravelAddonState,
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
      )
    }
  }
}

internal class CustomizeTravelAddonProvider :
  CollectionPreviewParameterProvider<CustomizeTravelAddonState>(
    listOf(
      Loading,
      CustomizeTravelAddonState.Success(
        travelPlusAddon = fakeTravelAddon,
        currentlyChosenOption = fakeTravelAddonOption1,
        currentlyChosenOptionInDialog = fakeTravelAddonOption1,
      ),
      Failure,
    ),
  )

private val fakeTravelAddonOption1 = TravelAddonOption.TravelOption45(
  optionName = "45 days",
  extraAmount = UiMoney(
    49.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddonOption2 = TravelAddonOption.TravelOption60(
  optionName = "60 days",
  extraAmount = UiMoney(
    60.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddon = TravelPlusAddon(
  addonOptions = nonEmptyListOf(fakeTravelAddonOption1, fakeTravelAddonOption2),
  exposureName = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  additionalInfo = "Lorem ipsum dolor sit amet consectetur.",
  activationDate = LocalDate(2024, 12, 30),
)
