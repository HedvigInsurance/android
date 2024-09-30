package com.hedvig.android.feature.change.tier.ui.stepcustomize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
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
import com.hedvig.android.feature.change.tier.data.Deductible
import com.hedvig.android.feature.change.tier.data.Tier
import com.hedvig.android.feature.change.tier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigationStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Success
import hedvig.resources.R

@Composable
internal fun SelectTierDestination(
  viewModel: SelectCoverageViewModel,
  navigateUp: () -> Unit,
  navigateToSummary: (quote: TierDeductibleQuote) -> Unit,
) {
  val uiState: SelectCoverageState by viewModel.uiState.collectAsStateWithLifecycle()
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      Failure -> FailureScreen(
        reload = {
          viewModel.emit(SelectCoverageEvent.Reload)
        },
      )

      Loading -> LoadingScreen()
      is Success -> {
        LaunchedEffect(state.uiState.quoteToNavigateFurther) {
          if (state.uiState.quoteToNavigateFurther != null) {
            viewModel.emit(ClearNavigationStep)
            navigateToSummary(state.uiState.quoteToNavigateFurther) //todo: check here
          }
        }
        var showComparisonTable by remember { mutableStateOf(false) }
        SelectTierScreen(
          uiState = state.uiState,
          navigateUp = navigateUp,
          onCompareClick = {
            showComparisonTable = true
          },
          onContinueClick = {
            viewModel.emit(SelectCoverageEvent.SubmitChosenQuoteToContinue)
          },
        )
        HedvigAlertDialog(
          title = "Here be dragons", //TODO: instead of the dialog comparison screen here!
          onConfirmClick = { showComparisonTable = false },
          onDismissRequest = { showComparisonTable = false },
          text = null,
        )
      }
    }
  }
}

@Composable
private fun FailureScreen(
  reload: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    HedvigErrorSection(onButtonClick = reload, modifier = Modifier.fillMaxSize())
  }
}

@Composable
private fun LoadingScreen() {
  //todo
}


@Composable
private fun SelectTierScreen(
  uiState: SelectCoverageSuccessUiState,
  navigateUp: () -> Unit,
  onCompareClick: () -> Unit,
  onContinueClick: () -> Unit,
) {
  var showTierDialog by remember { mutableStateOf(false) }
  var showDeductibleDialog by remember { mutableStateOf(false) }
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
      onChooseTierClick = {
        showTierDialog = true
      },
      onChooseDeductibleClick = {
        showDeductibleDialog = true
      },
      newDisplayPremium = uiState.chosenQuote?.premium,
      isCurrentChosen = uiState.isCurrentChosen,
      chosenTier = uiState.chosenTier,
      chosenQuote = uiState.chosenQuote,
      isTierChoiceEnabled = uiState.isTierChoiceEnabled,
      quotesForChosenTier = uiState.quotesForChosenTier,
      tiers = uiState.tiers,
    )
    Spacer(Modifier.height(4.dp))
    HedvigTextButton(
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      text = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
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
  tiers: List<Pair<Tier, String>>,
  quotesForChosenTier: List<TierDeductibleQuote>,
  chosenTier: Tier?,
  chosenQuote: TierDeductibleQuote?,
  newDisplayPremium: UiMoney?,
  isTierChoiceEnabled: Boolean,
  onChooseDeductibleClick: (quote: TierDeductibleQuote) -> Unit,
  onChooseTierClick: (tier: Tier) -> Unit,
  isCurrentChosen: Boolean,
  modifier: Modifier = Modifier,
) {
  var locallyChosenTier by remember { mutableStateOf(chosenTier) }
  var locallyChosenQuote by remember { mutableStateOf(chosenQuote) }
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      PillAndBasicInfo(
        contractGroup = data.contractGroup,
        displayName = data.contractDisplayName,
        displaySubtitle = data.contractDisplaySubtitle,
      )
      Spacer(Modifier.height(16.dp))
      val tierSimpleItems = buildList {
        for (tier in tiers) {
          add(SimpleDropdownItem(tier.first.tierName))
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
        onItemChosen = { _ -> }, // not needed, as we not use the default dialog content
        chosenItemIndex = tiers.indexOfFirst { pair ->
          pair.first == locallyChosenTier
        },
        onSelectorClick = {},
        containerColor = HedvigTheme.colorScheme.fillNegative,
      ) { onDismissRequest ->
        val listOfOptions = buildList {
          tiers.forEachIndexed { index, pair ->
            add(
              ExpandedRadioOptionData(
                chosenState = if (locallyChosenTier == pair.first) Chosen else NotChosen,
                title = pair.first.tierName,
                premium = pair.second,
                info = pair.first.info,
                onRadioOptionClick = {
                  locallyChosenTier = tiers.getOrNull(index)?.first
                },
              ),
            )
          }
        }
        DropdownContent(
          onContinueButtonClick = {
            val chosen = locallyChosenTier
            if (chosen != null) {
              onChooseTierClick(chosen)
              onDismissRequest()
            }
          },
          onCancelButtonClick = {
            locallyChosenTier = chosenTier
            onDismissRequest()
          },
          title = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
          data = listOfOptions,
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
      if (quotesForChosenTier.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        val deductibleSimpleItems = buildList {
          for (quote in quotesForChosenTier) {
            add(SimpleDropdownItem(quote.deductible.optionText))
          }
        }
        DropdownWithDialog(
          dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
          style = Label(
            label = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
            items = deductibleSimpleItems,
          ),
          size = Small,
          hintText = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
          onItemChosen = { _ -> }, // not needed, as we not use the default dialog content,
          chosenItemIndex = quotesForChosenTier.indexOfFirst { quote ->
            quote == locallyChosenQuote
          },
          onSelectorClick = {},
          containerColor = HedvigTheme.colorScheme.fillNegative,
        ) { onDismissRequest ->
          val listOfOptions = buildList {
            quotesForChosenTier.forEach { quote ->
              add(
                ExpandedRadioOptionData(
                  chosenState = if (locallyChosenQuote == quote) Chosen else NotChosen,
                  title = quote.deductible.optionText,
                  premium = quote.premium.toString(),
                  info = quote.deductible.description,
                  onRadioOptionClick = {
                    locallyChosenQuote = quote
                  },
                ),
              )
            }
          }
          DropdownContent(
            onContinueButtonClick = {
              val chosen = locallyChosenQuote
              if (chosen != null) {
                onChooseDeductibleClick(chosen)
                onDismissRequest()
              }
            },
            onCancelButtonClick = {
              locallyChosenQuote = chosenQuote
              onDismissRequest()
            },
            title = stringResource(R.string.TIER_FLOW_SELECT_DEDUCTIBLE_TITLE),
            data = listOfOptions,
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
            text = newDisplayPremium.toString(),
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      if (!isCurrentChosen) {
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
    Spacer(Modifier.height(24.dp))
    data.forEachIndexed { index, option ->
      RadioOption(
        chosenState = option.chosenState,
        onClick = option.onRadioOptionClick,
        optionContent = {
          ExpandedOptionContent(
            title = option.title,
            premium = option.premium,
            comment = option.info,
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
      onClick = onCancelButtonClick,
      modifier = Modifier.fillMaxWidth(),
      buttonSize = Large,
    )
  }
}

private data class ExpandedRadioOptionData(
  val onRadioOptionClick: () -> Unit,
  val chosenState: ChosenState,
  val title: String,
  val premium: String,
  val info: String?,
)

@Composable
private fun PillAndBasicInfo(contractGroup: ContractGroup, displayName: String, displaySubtitle: String) {
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
private fun ExpandedOptionContent(title: String, premium: String, comment: String?) {
  Column {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(title)
      },
      spaceBetween = 8.dp,
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          HighlightLabel(labelText = premium, size = HighLightSize.Small, color = HighlightColor.Grey(MEDIUM))
        }
      },
    )
    if (comment != null) {
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = comment,
        modifier = Modifier.fillMaxWidth(),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun CustomizationCardPreview() {
  HedvigTheme {
    CustomizationCard(
      data = dataForPreview,
      chosenTier = Tier("Bas", tierLevel = 0, info = "Vårt paket med grundläggande villkor."),
      chosenDeductible = 1,
      onChooseTierClick = {},
      onChooseDeductibleClick = {},
      newDisplayPremium = "249 kr/mo",
      isCurrentChosen = false,
      isTierChoiceEnabled = true,
    )
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
          Tier("Bas", tierLevel = 0, info = "Vårt paket med grundläggande villkor.") to "199 kr/mo",
          Tier("Standard", tierLevel = 1, info = "Vårt mellanpaket med hög ersättning.") to "449 kr/mo",
          Tier("Max", tierLevel = 1, info = "Vårt största paket med högst ersättning.") to "799 kr/mo",
        ),
        quotesForChosenTier = ,
        isCurrentChosen = false,
        isTierChoiceEnabled = false,
        chosenTier = Tier("Bas", tierLevel = 0, info = "Vårt paket med grundläggande villkor."),
        chosenQuote =
      ),
      {}, {}, {},
    )
  }
}

private val dataForPreview = ContractData(
  contractGroup = ContractGroup.HOMEOWNER,
  contractDisplayName = "Home Homeowner",
  contractDisplaySubtitle = "Addressvägen 777",
  activeDisplayPremium = "999 kr/mån",

  deductibleData = listOf(
    Deductible(
      "0 kr",
      deductiblePercentage = "25%",
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    Deductible(
      "1500 kr",
      deductiblePercentage = "25%",
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
    Deductible(
      "3500 kr",
      deductiblePercentage = "25%",
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
  ),
)
