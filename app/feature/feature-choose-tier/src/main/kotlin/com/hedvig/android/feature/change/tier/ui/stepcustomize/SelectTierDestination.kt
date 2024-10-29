package com.hedvig.android.feature.change.tier.ui.stepcustomize

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
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
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.ui.stepcustomize.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepcustomize.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigateFurtherStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigateToComparison
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Success
import hedvig.resources.R

@Composable
internal fun SelectTierDestination(
  viewModel: SelectCoverageViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (quote: TierDeductibleQuote) -> Unit,
  navigateToComparison: (listOfQuotes: List<TierDeductibleQuote>) -> Unit,
) {
  val uiState: SelectCoverageState by viewModel.uiState.collectAsStateWithLifecycle()
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      is Failure -> FailureScreen(
        reason = state.reason,
        reload = {
          viewModel.emit(SelectCoverageEvent.Reload)
        },
        popBackStack = popBackStack,
      )

      Loading -> HedvigFullScreenCenterAlignedProgress()
      is Success -> {
        LaunchedEffect(state.uiState.quoteToNavigateFurther) {
          if (state.uiState.quoteToNavigateFurther != null) {
            viewModel.emit(ClearNavigateFurtherStep)
            navigateToSummary(state.uiState.quoteToNavigateFurther)
          }
        }
        LaunchedEffect(state.uiState.quotesToCompare) {
          if (state.uiState.quotesToCompare != null) {
            viewModel.emit(ClearNavigateToComparison)
            navigateToComparison(state.uiState.quotesToCompare)
          }
        }
        SelectTierScreen(
          uiState = state.uiState,
          navigateUp = navigateUp,
          onCompareClick = {
            viewModel.emit(SelectCoverageEvent.LaunchComparison)
          },
          onContinueClick = {
            viewModel.emit(SelectCoverageEvent.SubmitChosenQuoteToContinue)
          },
          onChooseTierClick = {
            viewModel.emit(SelectCoverageEvent.ChangeTier)
          },
          onChooseDeductibleClick = {
            viewModel.emit(SelectCoverageEvent.ChangeDeductibleForChosenTier)
          },
          onChooseDeductibleInDialogClick = { quote ->
            viewModel.emit(SelectCoverageEvent.ChangeDeductibleInDialog(quote))
          },
          onChooseTierInDialogClick = { tier ->
            viewModel.emit(SelectCoverageEvent.ChangeTierInDialog(tier))
          },
          onSetTierBackToPreviouslyChosen = {
            viewModel.emit(SelectCoverageEvent.SetTierToPreviouslyChosen)
          },
          onSetDeductibleBackToPreviouslyChosen = {
            viewModel.emit(SelectCoverageEvent.SetDeductibleToPreviouslyChosen)
          },
        )
      }
    }
  }
}

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit, reason: FailureReason) {
  Box(Modifier.fillMaxSize()) {
    when (reason) {
      GENERAL -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.fillMaxSize(),
          subTitle = stringResource(R.string.GENERAL_ERROR_BODY),
          title = stringResource(R.string.GENERAL_ERROR_BODY),
          buttonText = stringResource(R.string.GENERAL_ERROR_BODY),
        )
      }

      QUOTES_ARE_EMPTY -> {
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
          EmptyState(
            text = stringResource(R.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
            iconStyle = INFO,
            buttonStyle = NoButton,
            description = null,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.weight(1f))
          HedvigTextButton(
            stringResource(R.string.general_close_button),
            onClick = popBackStack,
            buttonSize = Large,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(32.dp))
        }
      }
    }
  }
}

@Composable
private fun SelectTierScreen(
  uiState: SelectCoverageSuccessUiState,
  navigateUp: () -> Unit,
  onCompareClick: () -> Unit,
  onContinueClick: () -> Unit,
  onChooseDeductibleClick: () -> Unit,
  onChooseTierClick: () -> Unit,
  onChooseDeductibleInDialogClick: (quote: TierDeductibleQuote) -> Unit,
  onChooseTierInDialogClick: (tier: Tier) -> Unit,
  onSetDeductibleBackToPreviouslyChosen: () -> Unit,
  onSetTierBackToPreviouslyChosen: () -> Unit,
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
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.TIER_FLOW_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(R.string.TIER_FLOW_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    CustomizationCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      data = uiState.contractData,
      onChooseTierClick = onChooseTierClick,
      onChooseDeductibleClick = onChooseDeductibleClick,
      newDisplayPremium = uiState.chosenQuote?.premium,
      isCurrentChosen = uiState.isCurrentChosen,
      chosenQuote = uiState.chosenQuote,
      isTierChoiceEnabled = uiState.isTierChoiceEnabled,
      quotesForChosenTier = uiState.quotesForChosenTier,
      tiers = uiState.tiers,
      chosenTierInDialog = uiState.chosenInDialogTier,
      chosenQuoteInDialog = uiState.chosenInDialogQuote,
      onChooseDeductibleInDialogClick = onChooseDeductibleInDialogClick,
      onChooseTierInDialogClick = onChooseTierInDialogClick,
      chosenTierIndex = uiState.chosenTierIndex,
      chosenQuoteIndex = uiState.chosenQuoteIndex,
      onSetTierBackToPreviouslyChosen = onSetTierBackToPreviouslyChosen,
      onSetDeductibleBackToPreviouslyChosen = onSetDeductibleBackToPreviouslyChosen,
    )
    Spacer(Modifier.height(4.dp))
    HedvigTextButton(
      text = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      buttonSize = Large,
      onClick = {
        onCompareClick()
      },
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      buttonSize = Large,
      text = stringResource(R.string.general_continue_button),
      enabled = !uiState.isCurrentChosen,
      onClick = {
        onContinueClick()
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizationCard(
  data: ContractData,
  tiers: List<Pair<Tier, UiMoney>>,
  quotesForChosenTier: List<TierDeductibleQuote>,
  chosenTierIndex: Int?,
  chosenQuoteIndex: Int?,
  chosenQuote: TierDeductibleQuote?,
  chosenTierInDialog: Tier?,
  chosenQuoteInDialog: TierDeductibleQuote?,
  onChooseDeductibleInDialogClick: (quote: TierDeductibleQuote) -> Unit,
  onChooseTierInDialogClick: (tier: Tier) -> Unit,
  newDisplayPremium: UiMoney?,
  isTierChoiceEnabled: Boolean,
  onChooseDeductibleClick: () -> Unit,
  onSetDeductibleBackToPreviouslyChosen: () -> Unit,
  onSetTierBackToPreviouslyChosen: () -> Unit,
  onChooseTierClick: () -> Unit,
  isCurrentChosen: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      PillAndBasicInfo(
        contractGroup = data.contractGroup,
        displayName = chosenQuote?.productVariant?.displayName ?: data.contractDisplayName,
        displaySubtitle = data.contractDisplaySubtitle,
      )
      Spacer(Modifier.height(16.dp))
      val tierSimpleItems = buildList {
        for (tier in tiers) {
          add(SimpleDropdownItem(tier.first.tierDisplayName ?: "-"))
        }
      }
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        isEnabled = isTierChoiceEnabled,
        style = Label(
          label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
          items = tierSimpleItems,
        ),
        size = Small,
        hintText = stringResource(R.string.TIER_FLOW_COVERAGE_PLACEHOLDER),
        chosenItemIndex = chosenTierIndex,
        onSelectorClick = {},
        containerColor = HedvigTheme.colorScheme.fillNegative,
        onDoAlongWithDismissRequest = onSetTierBackToPreviouslyChosen,
      ) { onDismissRequest ->
        val listOfOptions = tiers.map { pair ->
          ExpandedRadioOptionData(
            chosenState = if (chosenTierInDialog == pair.first) Chosen else NotChosen,
            title = pair.first.tierDisplayName ?: "-",
            premium = stringResource(R.string.TIER_FLOW_PRICE_LABEL, pair.second.amount.toInt()),
            tierDescription = pair.first.tierDescription,
            onRadioOptionClick = {
              onChooseTierInDialogClick(pair.first)
            },
          )
        }
        DropdownContent(
          onContinueButtonClick = {
            onChooseTierClick()
            onDismissRequest()
          },
          onCancelButtonClick = {
            onDismissRequest()
          },
          title = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
          data = listOfOptions,
          subTitle = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_SUBTITLE),
        )
      }
      if (!isTierChoiceEnabled) {
        HedvigText(
          stringResource(R.string.TIER_FLOW_LOCKED_INFO_DESCRIPTION),
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label,
          modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 4.dp),
        )
      }
      val onlyNullDeductibles = quotesForChosenTier.none { it.deductible != null }
      if (quotesForChosenTier.isNotEmpty() && !onlyNullDeductibles) {
        Spacer(Modifier.height(4.dp))
        val deductibleSimpleItems = buildList {
          for (quote in quotesForChosenTier) {
            quote.deductible?.let {
              add(SimpleDropdownItem(it.optionText))
            }
          }
        }
        DropdownWithDialog(
          dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
          style = Label(
            label = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
            items = deductibleSimpleItems,
          ),
          isEnabled = quotesForChosenTier.size > 1,
          size = Small,
          hintText = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
          chosenItemIndex = chosenQuoteIndex,
          onSelectorClick = {},
          containerColor = HedvigTheme.colorScheme.fillNegative,
          onDoAlongWithDismissRequest = onSetDeductibleBackToPreviouslyChosen,
        ) { onDismissRequest ->
          val listOfOptions = buildList {
            quotesForChosenTier.forEach { quote ->
              quote.deductible?.let {
                add(
                  ExpandedRadioOptionData(
                    chosenState = if (chosenQuoteInDialog == quote) Chosen else NotChosen,
                    title = it.optionText,
                    premium = stringResource(R.string.TERMINATION_FLOW_PAYMENT_PER_MONTH, quote.premium.amount.toInt()),
                    tierDescription = it.description.takeIf { description -> description.isNotEmpty() },
                    onRadioOptionClick = {
                      onChooseDeductibleInDialogClick(quote)
                    },
                  ),
                )
              }
            }
          }
          DropdownContent(
            onContinueButtonClick = {
              onChooseDeductibleClick()
              onDismissRequest()
            },
            onCancelButtonClick = {
              onDismissRequest()
            },
            title = stringResource(R.string.TIER_FLOW_SELECT_DEDUCTIBLE_TITLE),
            data = listOfOptions,
            subTitle = stringResource(R.string.TIER_FLOW_SELECT_DEDUCTIBLE_SUBTITLE),
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text =
              newDisplayPremium?.let { stringResource(R.string.TERMINATION_FLOW_PAYMENT_PER_MONTH, it.amount.toInt()) }
                ?: "-",
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      if (!isCurrentChosen && data.activeDisplayPremium != null) {
        HedvigText(
          modifier = Modifier.fillMaxWidth(),
          textAlign = Companion.End,
          text = stringResource(R.string.TIER_FLOW_PREVIOUS_PRICE, data.activeDisplayPremium),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
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
      textAlign = Companion.Center,
    )
    HedvigText(
      subTitle,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = Companion.Center,
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
            comment = option.tierDescription,
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

private data class ExpandedRadioOptionData(
  val onRadioOptionClick: () -> Unit,
  val chosenState: ChosenState,
  val title: String,
  val premium: String,
  val tierDescription: String?,
)

@Composable
internal fun PillAndBasicInfo(contractGroup: ContractGroup, displayName: String, displaySubtitle: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
      painter = painterResource(id = contractGroup.toPillow()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      HedvigText(
        text = displayName,
        style = HedvigTheme.typography.headlineSmall,
      )
      HedvigText(
        color = HedvigTheme.colorScheme.textSecondary,
        text = displaySubtitle,
        style = HedvigTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun ExpandedOptionContent(
  title: String,
  premium: String,
  comment: String?,
  radioButtonIcon: @Composable () -> Unit,
) {
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
      if (comment != null) {
        Spacer(Modifier.height(4.dp))
        HedvigText(
          text = comment,
          modifier = Modifier.fillMaxWidth(),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun CustomizationCardPreview() {
  HedvigTheme {
    CustomizationCard(
      data = dataForPreview,
      onChooseTierClick = {},
      onChooseDeductibleClick = {},
      newDisplayPremium = UiMoney(199.0, SEK),
      isCurrentChosen = false,
      isTierChoiceEnabled = true,
      chosenQuote = quotesForPreview[0],
      quotesForChosenTier = quotesForPreview,
      tiers = listOf(
        Tier(
          "BAS",
          tierLevel = 0,
          tierDescription = "Vårt paket med grundläggande villkor.",
          tierDisplayName = "Bas",
        ) to UiMoney(199.0, SEK),
        Tier(
          "STANDARD",
          tierLevel = 1,
          tierDescription = "Vårt mellanpaket med hög ersättning.",
          tierDisplayName = "Standard",
        ) to UiMoney(155.0, SEK),
      ),
      chosenTierInDialog = Tier(
        "BAS",
        tierLevel = 0,
        tierDescription = "Vårt paket med grundläggande villkor.",
        tierDisplayName = "Bas",
      ),
      chosenQuoteInDialog = quotesForPreview[0],
      onChooseDeductibleInDialogClick = {},
      onChooseTierInDialogClick = {},
      chosenTierIndex = null,
      chosenQuoteIndex = null,
      onSetTierBackToPreviouslyChosen = {},
      onSetDeductibleBackToPreviouslyChosen = {},
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewDropdownContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DropdownContent(
        "Title",
        "Subtitle",
        {},
        {},
        List(2) {
          ExpandedRadioOptionData(
            {},
            Chosen,
            "Title",
            "Premium",
            "TierDescription",
          )
        },
      )
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun SelectTierScreenPreview() {
  HedvigTheme {
    SelectTierScreen(
      uiState = SelectCoverageSuccessUiState(
        contractData = dataForPreview,
        tiers = listOf(
          Tier(
            "BAS",
            tierLevel = 0,
            tierDescription = "Vårt paket med grundläggande villkor.",
            tierDisplayName = "Bas",
          ) to UiMoney(199.0, SEK),
          Tier(
            "STANDARD",
            tierLevel = 1,
            tierDescription = "Vårt mellanpaket med hög ersättning.",
            tierDisplayName = "Standard",
          ) to UiMoney(155.0, SEK),
        ),
        quotesForChosenTier = quotesForPreview,
        isCurrentChosen = false,
        isTierChoiceEnabled = true,
        chosenTier = Tier(
          "BAS",
          tierLevel = 0,
          tierDescription = "Vårt paket med grundläggande villkor.",
          tierDisplayName = "Bas",
        ),
        chosenQuote = quotesForPreview[0],
        chosenInDialogTier = Tier(
          "BAS",
          tierLevel = 0,
          tierDescription = "Vårt paket med grundläggande villkor.",
          tierDisplayName = "Bas",
        ),
        chosenInDialogQuote = quotesForPreview[0],
        chosenTierIndex = null,
        chosenQuoteIndex = null,
      ),
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

private val dataForPreview = ContractData(
  contractGroup = ContractGroup.HOMEOWNER,
  contractDisplayName = "Home Homeowner",
  contractDisplaySubtitle = "Addressvägen 777",
  activeDisplayPremium = "449 kr/mo",
)

private val quotesForPreview = listOf(
  TierDeductibleQuote(
    id = "id0",
    deductible = Deductible(
      UiMoney(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(199.0, SEK),
    tier = Tier(
      "BAS",
      tierLevel = 0,
      tierDescription = "Vårt paket med grundläggande villkor.",
      tierDisplayName = "Bas",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Bas",
      tierDescription = "Our most basic coverage",
    ),
  ),
  TierDeductibleQuote(
    id = "id1",
    deductible = Deductible(
      UiMoney(1000.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(255.0, SEK),
    tier = Tier(
      "BAS",
      tierLevel = 0,
      tierDescription = "Vårt paket med grundläggande villkor.",
      tierDisplayName = "Bas",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Bas",
      tierDescription = "Our most basic coverage",
    ),
  ),
  TierDeductibleQuote(
    id = "id2",
    deductible = Deductible(
      UiMoney(3500.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
    displayItems = listOf(),
    premium = UiMoney(355.0, SEK),
    tier = Tier(
      "BAS",
      tierLevel = 0,
      tierDescription = "Vårt paket med grundläggande villkor.",
      tierDisplayName = "Bas",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Bas",
      tierDescription = "Our most basic coverage",
    ),
  ),
  TierDeductibleQuote(
    id = "id3",
    deductible = Deductible(
      UiMoney(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(230.0, SEK),
    tier = Tier(
      "STANDARD",
      tierLevel = 1,
      tierDescription = "Vårt mellanpaket med hög ersättning.",
      tierDisplayName = "Standard",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Standard",
      tierDescription = "Our most standard coverage",
    ),
  ),
  TierDeductibleQuote(
    id = "id4",
    deductible = Deductible(
      UiMoney(3500.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
    displayItems = listOf(),
    premium = UiMoney(655.0, SEK),
    tier = Tier(
      "STANDARD",
      tierLevel = 1,
      tierDescription = "Vårt mellanpaket med hög ersättning.",
      tierDisplayName = "Standard",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Standard",
      tierDescription = "Our most standard coverage",
    ),
  ),
)
