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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
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
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
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
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuoteInsuranceDocument
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ChooseOptionInDialog
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ChooseSelectedOption
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.ClearNavigation
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.Reload
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.SetOptionBackToPreviouslyChosen
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent.SubmitSelected
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonState.Failure
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonState.Loading
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonState.Success
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun CustomizeTravelAddonDestination(
  viewModel: CustomizeTravelAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  popAddonFlow: () -> Unit,
  navigateToSummary: (summaryParameters: SummaryParameters) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<PerilData>) -> Unit,
) {
  val uiState: CustomizeTravelAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    popAddonFlow = popAddonFlow,
    submitToSummary = {
      viewModel.emit(SubmitSelected)
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
      viewModel.emit(SetOptionBackToPreviouslyChosen)
    },
    navigateToSummary = { params ->
      viewModel.emit(ClearNavigation)
      navigateToSummary(params)
    },
    onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
    navigateToChat = onNavigateToNewConversation,
  )
}

@Composable
private fun CustomizeTravelAddonScreen(
  uiState: CustomizeTravelAddonState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  submitToSummary: () -> Unit,
  navigateToSummary: (summaryParameters: SummaryParameters) -> Unit,
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<PerilData>) -> Unit,
  reload: () -> Unit,
  popAddonFlow: () -> Unit,
  navigateToChat: () -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      is Failure -> FailureScreen(
        errorMessage = state.errorMessage,
        reload = reload,
        popBackStack = popBackStack,
        navigateToChat = navigateToChat,
      )

      Loading -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      is Success -> {
        LaunchedEffect(state.summaryParamsToNavigateFurther) {
          if (state.summaryParamsToNavigateFurther != null) {
            navigateToSummary(state.summaryParamsToNavigateFurther)
          }
        }
        CustomizeTravelAddonScreenContent(
          uiState = state,
          navigateUp = navigateUp,
          submitToSummary = submitToSummary,
          onChooseSelectedOption = onChooseSelectedOption,
          onChooseOptionInDialog = onChooseOptionInDialog,
          onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
          onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
          popAddonFlow = popAddonFlow,
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
        stringResource(R.string.GENERAL_RETRY)
      } else {
        stringResource(R.string.open_chat)
      }
      HedvigErrorSection(
        onButtonClick = if (errorMessage == null) reload else navigateToChat,
        subTitle = errorMessage ?: stringResource(R.string.GENERAL_ERROR_BODY),
        modifier = Modifier.fillMaxSize(),
        buttonText = buttonText,
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
  uiState: Success,
  navigateUp: () -> Unit,
  popAddonFlow: () -> Unit,
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<PerilData>) -> Unit,
  submitToSummary: () -> Unit,
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
            contentDescription = stringResource(R.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowHeading(
      stringResource(R.string.ADDON_FLOW_TITLE),
      stringResource(R.string.ADDON_FLOW_SUBTITLE),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    CustomizeTravelAddonCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      uiState = uiState,
      onChooseOptionInDialog = onChooseOptionInDialog,
      onChooseSelectedOption = onChooseSelectedOption,
      onSetOptionBackToPreviouslyChosen = onSetOptionBackToPreviouslyChosen,
      onNavigateToTravelInsurancePlusExplanation = onNavigateToTravelInsurancePlusExplanation,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      buttonSize = Large,
      text = stringResource(R.string.general_continue_button),
      enabled = true,
      onClick = dropUnlessResumed {
        submitToSummary()
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
      buttonSize = Large,
      onClick = { popAddonFlow() },
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizeTravelAddonCard(
  uiState: Success,
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  onChooseSelectedOption: () -> Unit,
  onSetOptionBackToPreviouslyChosen: () -> Unit,
  onNavigateToTravelInsurancePlusExplanation: (List<PerilData>) -> Unit,
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
      val isDropdownEnabled = uiState.travelAddonOffer.addonOptions.size > 1
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        // Locked option if there is nothing else to chose from
        isEnabled = isDropdownEnabled,
        style = Label(
          label = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER),
          items = addonSimpleItems,
        ),
        size = Small,
        containerColor = HedvigTheme.colorScheme.surfacePrimary,
        // there is always one option chosen, should never be shown anyway
        hintText = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER),
        chosenItemIndex = uiState.travelAddonOffer.addonOptions.indexOf(uiState.currentlyChosenOption)
          .takeIf { it >= 0 },
        onDoAlongWithDismissRequest = onSetOptionBackToPreviouslyChosen,
        modifier = Modifier.accessibilityForDropdown(
          labelText = stringResource(R.string.ADDON_FLOW_SELECT_DAYS_PLACEHOLDER),
          selectedValue = uiState.currentlyChosenOption.displayName,
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
          title = stringResource(R.string.ADDON_FLOW_SELECT_SUBOPTION_TITLE),
          subTitle = stringResource(R.string.ADDON_FLOW_SELECT_SUBOPTION_SUBTITLE),
          addonOptions = uiState.travelAddonOffer.addonOptions,
          currentlyChosenOptionInDialog = uiState.currentlyChosenOptionInDialog,
          onChooseOptionInDialog = { option -> onChooseOptionInDialog(option) },
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButtonGhostWithBorder(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.ADDON_FLOW_COVER_BUTTON),
        onClick = dropUnlessResumed {
          onNavigateToTravelInsurancePlusExplanation(
            uiState.currentlyChosenOption.addonVariant.perils.map {
              PerilData(
                title = it.title,
                description = it.description,
                covered = it.covered,
                colorCode = it.colorCode,
              )
            },
          )
        },
      )
    }
  }
}

private data class ExpandedRadioOptionData(
  val onRadioOptionClick: () -> Unit,
  val chosenState: ChosenState,
  val title: String,
  val premium: String,
  val voiceoverDescription: String,
)

@Composable
private fun HeaderInfoWithCurrentPrice(
  chosenOptionPremiumExtra: UiMoney,
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
        Row(horizontalArrangement = Arrangement.End) {
          HighlightLabel(
            labelText = stringResource(R.string.ADDON_FLOW_PRICE_LABEL, chosenOptionPremiumExtra),
            size = HighLightSize.Small,
            color = Grey(MEDIUM),
            modifier = Modifier
              .wrapContentSize(Alignment.TopEnd)
              .semantics {
                contentDescription = pricePerMonth
              },
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
private fun DropdownContent(
  title: String,
  subTitle: String,
  onContinueButtonClick: () -> Unit,
  onCancelButtonClick: () -> Unit,
  addonOptions: NonEmptyList<TravelAddonQuote>,
  currentlyChosenOptionInDialog: TravelAddonQuote?,
  onChooseOptionInDialog: (TravelAddonQuote) -> Unit,
  modifier: Modifier = Modifier,
) {
  val data = addonOptions.map { option ->
    val pricePerMonth = option.price.getPerMonthDescription()
    ExpandedRadioOptionData(
      chosenState = if (currentlyChosenOptionInDialog == option) Chosen else NotChosen,
      title = option.displayName,
      premium = stringResource(R.string.ADDON_FLOW_PRICE_LABEL, option.price),
      onRadioOptionClick = {
        onChooseOptionInDialog(option)
      },
      voiceoverDescription = "${option.displayName}, $pricePerMonth",
    )
  }
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
        modifier = Modifier.clearAndSetSemantics {
          contentDescription = option.voiceoverDescription
          role = Role.RadioButton
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
  @PreviewParameter(CustomizeTravelAddonPreviewProvider::class) uiState: CustomizeTravelAddonState,
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
      )
    }
  }
}

internal class CustomizeTravelAddonPreviewProvider :
  CollectionPreviewParameterProvider<CustomizeTravelAddonState>(
    listOf(
      Loading,
      Success(
        travelAddonOffer = fakeTravelAddon,
        currentlyChosenOption = fakeTravelAddonQuote1,
        currentlyChosenOptionInDialog = fakeTravelAddonQuote1,
        summaryParamsToNavigateFurther = null,
      ),
      Failure("Ooops"),
    ),
  )

private val fakeTravelAddonQuote1 = TravelAddonQuote(
  quoteId = "id",
  addonId = "addonId1",
  displayName = "45 days",
  displayDetails = listOf(),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    perils = listOf(),
    displayName = "45 days",
    product = "",
  ),
  addonSubtype = "45 days",
  price = UiMoney(
    49.0,
    SEK,
  ),
  documents = listOf(
    TravelAddonQuoteInsuranceDocument(
      "Some terms",
      "url",
    ),
  ),
)
private val fakeTravelAddonQuote2 = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId1",
  quoteId = "id",
  displayDetails = listOf(),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    perils = listOf(),
    displayName = "60 days",
    product = "",
  ),
  addonSubtype = "45 days",
  price = UiMoney(
    60.0,
    SEK,
  ),
  documents = listOf(
    TravelAddonQuoteInsuranceDocument(
      "Some terms",
      "url",
    ),
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
  currentTravelAddon = null,
)
