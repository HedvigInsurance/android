package com.hedvig.android.feature.addon.purchase.ui.customize

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
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.AddonVariant
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun CustomizeTravelAddonDestination(
  viewModel: CustomizeTravelAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (summaryParameters: SummaryParameters) -> Unit,
) {
  val uiState: CustomizeTravelAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    navigateToSummary = { summaryParams ->
      navigateToSummary(summaryParams)
    },
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
  navigateToSummary: (SummaryParameters) -> Unit,
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  reload: () -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      is CustomizeTravelAddonState.Failure -> FailureScreen(state.errorMessage, reload, popBackStack)
      CustomizeTravelAddonState.Loading -> HedvigFullScreenCenterAlignedProgress()
      is CustomizeTravelAddonState.Success -> CustomizeTravelAddonScreenContent(
        uiState = state,
        navigateUp = navigateUp,
        navigateToSummary = { quote ->
          navigateToSummary(
            SummaryParameters(
              state.travelAddonOffer.title,
              quote,
              state.travelAddonOffer.activationDate,
            ),
          )
        },
        onChooseSelectedOption = onChooseSelectedOption,
        onChooseOptionInDialog = onChooseOptionInDialog,
        onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
      )
    }
  }
}

@Composable
private fun FailureScreen(errorMessage: String?, reload: () -> Unit, popBackStack: () -> Unit) {
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
        subTitle = errorMessage ?: stringResource(R.string.GENERAL_ERROR_BODY),
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
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  navigateToSummary: (travelAddonQuote: TravelAddonQuote) -> Unit,
) {
  val referralExplanationBottomSheetState = rememberHedvigBottomSheetState<Unit>()
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
    TravelPlusExplanationBottomSheet(referralExplanationBottomSheetState)
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
        referralExplanationBottomSheetState.show(Unit)
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      buttonSize = Large,
      text = stringResource(R.string.ADDON_FLOW_ADD_TO_INSURANCE_BUTTON),
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
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
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
        chosenOptionPremiumExtra = uiState.currentlyChosenOption.price,
        exposureName = uiState.travelAddonOffer.title,
        description = uiState.travelAddonOffer.description,
      )
      Spacer(Modifier.height(16.dp))
      val addonSimpleItems = buildList {
        for (option in uiState.travelAddonOffer.addonOptions) {
          add(SimpleDropdownItem(option.displayName))
        }
      }
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        isEnabled = uiState.travelAddonOffer.addonOptions.size > 1,
        // we shouldn't get to this destination if this list size <=1 at all tbh
        style = Label(
          label = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER),
          items = addonSimpleItems,
        ),
        size = Small,
        containerColor = HedvigTheme.colorScheme.fillNegative,
        hintText = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER), // there is always one option chosen, should never be shown anyway
        chosenItemIndex = uiState.travelAddonOffer.addonOptions.indexOf(uiState.currentlyChosenOption)
          .takeIf { it >= 0 },
        onSelectorClick = {
          // todo: check here! should be empty
        },
        onDoAlongWithDismissRequest = onSetOptionBackToPreviouslyChosen,
      ) { onDismissRequest ->
        val listOfOptions = uiState.travelAddonOffer.addonOptions.map { option ->
          ExpandedRadioOptionData(
            chosenState = if (uiState.currentlyChosenOptionInDialog == option) Chosen else NotChosen,
            title = option.displayName,
            premium = stringResource(R.string.ADDON_FLOW_PRICE_LABEL, option.price.amount.toInt()),
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
          title = stringResource(R.string.ADDON_FLOW_SELECT_SUBOPTION_TITLE),
          data = listOfOptions,
          subTitle = stringResource(R.string.ADDON_FLOW_SELECT_SUBOPTION_SUBTITLE),
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
  HedvigNotificationCard(
    modifier = modifier,
    message = stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_CARD_TEXT),
    priority = NotificationDefaults.NotificationPriority.InfoInline,
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
      text = stringResource(R.string.ADDON_FLOW_SELECT_BUTTON),
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
internal fun TravelPlusExplanationBottomSheet(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(sheetState) { _ ->
    HedvigText(
      text = stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_TITLE),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_DESCRIPTION),
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
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
// todo: here, check the padding!
  }
}

@HedvigMultiScreenPreview
@Composable
private fun SelectTierScreenPreview(
  @PreviewParameter(CustomizeTravelAddonPreviewProvider::class) uiState: CustomizeTravelAddonState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CustomizeTravelAddonScreen(
        uiState = uiState,
        {},
        {},
        { _ -> },
        {},
        {},
        {},
        {},
      )
    }
  }
}

internal class CustomizeTravelAddonPreviewProvider :
  CollectionPreviewParameterProvider<CustomizeTravelAddonState>(
    listOf(
      CustomizeTravelAddonState.Loading,
      CustomizeTravelAddonState.Success(
        travelAddonOffer = fakeTravelAddon,
        currentlyChosenOption = fakeTravelAddonQuote1,
        currentlyChosenOptionInDialog = fakeTravelAddonQuote1,
      ),
      CustomizeTravelAddonState.Failure("Ooops"),
    ),
  )

private val fakeTravelAddonQuote1 = TravelAddonQuote(
  quoteId = "id",
  addonId = "addonId1",
  displayName = "45 days",
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    displayDetails = listOf(),
  ),
  price = UiMoney(
    49.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddonQuote2 = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId1",
  quoteId = "id",
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    displayDetails = listOf(),
  ),
  price = UiMoney(
    60.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddon = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    fakeTravelAddonQuote1,
    fakeTravelAddonQuote2,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
)