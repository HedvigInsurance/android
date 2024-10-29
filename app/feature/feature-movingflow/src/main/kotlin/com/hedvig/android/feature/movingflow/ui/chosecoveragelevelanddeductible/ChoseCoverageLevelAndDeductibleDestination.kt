package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup.ACCIDENT
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_BRF
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Content
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Loading
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun ChoseCoverageLevelAndDeductibleDestination(
  viewModel: ChoseCoverageLevelAndDeductibleViewModel,
  navigateUp: () -> Unit,
  onNavigateToSummaryScreen: (homeQuoteId: String) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  ChoseCoverageLevelAndDeductibleScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onSubmit = { selectedHomeQuoteId -> onNavigateToSummaryScreen(selectedHomeQuoteId) },
    onSelectCoverageOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectCoverage(it)) },
    onSelectDeductibleOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectDeductible(it)) },
  )
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  uiState: ChoseCoverageLevelAndDeductibleUiState,
  navigateUp: () -> Unit,
  onSubmit: (String) -> Unit,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
) {
  when (uiState) {
    Loading -> {
      HedvigText("Loading")
    }

    MissingOngoingMovingFlow -> {
      HedvigText("MissingOngoingMovingFlow")
    }

    is Content -> ChoseCoverageLevelAndDeductibleScreen(
      content = uiState,
      navigateUp = navigateUp,
      onSubmit = uiState.tiersInfo.selectedHomeQuoteId?.let { { onSubmit(it) } },
      onSelectCoverageOption = onSelectCoverageOption,
      onSelectDeductibleOption = onSelectDeductibleOption,
    )
  }
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  content: Content,
  navigateUp: () -> Unit,
  onSubmit: (() -> Unit)?,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
) {
  HedvigScaffold(navigateUp) {
    Column(Modifier.weight(1f)) {
      HedvigText(
        text = stringResource(R.string.TIER_FLOW_TITLE),
        style = HedvigTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      HedvigText(
        text = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
        style = HedvigTheme.typography.bodyMedium,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(8.dp))
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
      ) {
        Spacer(Modifier.height(8.dp))
        CoverageCard(content.tiersInfo, onSelectCoverageOption, onSelectDeductibleOption)
        Spacer(Modifier.height(8.dp))
        // todo Add comparison API results here
        if (content.tiersInfo.coverageOptions.isNotEmpty()) {
          HedvigTextButton(
            text = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
            modifier = Modifier.fillMaxWidth(),
          ) {
            // onCompareCoverageClicked()
          }
          Spacer(Modifier.height(4.dp))
        }
        HedvigButton(
          text = stringResource(R.string.general_continue_button),
          onClick = dropUnlessResumed {
            onSubmit?.invoke()
          },
          enabled = onSubmit != null && content.canSubmit,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun CoverageCard(
  tiersInfo: TiersInfo,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(16.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(tiersInfo.selectedCoverage.productVariant.contractGroup.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          HedvigText(
            text = tiersInfo.selectedCoverage.productVariant.displayName,
          )
          HedvigText(
            text = tiersInfo.selectedCoverage.exposureName,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val chosenCoverageItemIndex = tiersInfo.coverageOptions.indexOfFirst {
          it.moveHomeQuoteId == tiersInfo.selectedCoverage.id
        }.let {
          if (it == -1) null else it
        }
        DropdownWithDialog(
          style = Label(
            items = tiersInfo.coverageOptions.map { coverageInfo ->
              SimpleDropdownItem(coverageInfo.tierName)
            },
            label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
          ),
          size = Small,
          hintText = tiersInfo.selectedCoverage.tierDisplayName,
          chosenItemIndex = chosenCoverageItemIndex,
          applyDefaultDialogPadding = false,
          dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        ) { onDismissRequest ->
          CoverageChoiceDialogContent(
            coverageOptions = tiersInfo.coverageOptions,
            initiallyChosenItemIndex = chosenCoverageItemIndex,
            onDismissRequest = onDismissRequest,
            onItemSelected = {
              onSelectCoverageOption(tiersInfo.coverageOptions[it].moveHomeQuoteId)
            },
          )
        }
        when (val deductibleOptions = tiersInfo.deductibleOptions) {
          NoOptions -> {}
          is MutlipleOptions -> {
            val chosenDeductibleItemIndex = deductibleOptions.deductibleOptions.indexOfFirst {
              it.homeQuoteId == tiersInfo.selectedDeductible?.id
            }.let {
              if (it == -1) null else it
            }
            DropdownWithDialog(
              style = Label(
                items = deductibleOptions.deductibleOptions.map { coverageInfo ->
                  SimpleDropdownItem(coverageInfo.deductible.amount.toString())
                },
                label = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
              ),
              size = Small,
              hintText = tiersInfo.selectedDeductible?.tierDisplayName
                ?: stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
              chosenItemIndex = chosenDeductibleItemIndex,
              applyDefaultDialogPadding = false,
              dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            ) { onDismissRequest ->
              DeductibleChoiceDialogContent(
                deductibleOptions = deductibleOptions.deductibleOptions,
                initiallyChosenItemIndex = chosenDeductibleItemIndex,
                onDismissRequest = onDismissRequest,
                onItemSelected = {
                  onSelectDeductibleOption(deductibleOptions.deductibleOptions[it].homeQuoteId)
                },
              )
            }
          }

          is OneOption -> {
            HedvigTextField(
              text = deductibleOptions.deductibleOption.deductible.displayText,
              onValueChange = {},
              textFieldSize = TextFieldSize.Small,
              labelText = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
              readOnly = true,
            )
          }
        }
      }
      HorizontalItemsWithMaximumSpaceTaken(
        { HedvigText(stringResource(R.string.CHANGE_ADDRESS_TOTAL)) },
        {
          HedvigText(
            text = stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              tiersInfo.selectedCoverage.premium.toString(),
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.wrapContentWidth(Alignment.End),
          )
        },
        Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun CoverageChoiceDialogContent(
  coverageOptions: List<CoverageInfo>,
  initiallyChosenItemIndex: Int?,
  onItemSelected: (Int) -> Unit,
  onDismissRequest: () -> Unit,
) {
  CommonChoiceDialogContent(
    firstText = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
    secondText = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_SUBTITLE),
    radioOptions = coverageOptions.map {
      RadioOptionCoverageInfo(
        it.tierName,
        stringResource(R.string.TIER_FLOW_PRICE_LABEL_WITHOUT_CURRENCY, it.minimumPremiumForCoverage.toString()),
        it.tierDescription,
      )
    },
    initiallyChosenItemIndex = initiallyChosenItemIndex,
    onItemSelected = onItemSelected,
    onDismissRequest = onDismissRequest,
  )
}

@Composable
private fun DeductibleChoiceDialogContent(
  deductibleOptions: List<DeductibleOption>,
  initiallyChosenItemIndex: Int?,
  onItemSelected: (Int) -> Unit,
  onDismissRequest: () -> Unit,
) {
  CommonChoiceDialogContent(
    firstText = stringResource(R.string.TIER_FLOW_SELECT_DEDUCTIBLE_TITLE),
    secondText = stringResource(R.string.TIER_FLOW_SELECT_DEDUCTIBLE_SUBTITLE),
    radioOptions = deductibleOptions.map {
      RadioOptionCoverageInfo(
        buildString {
          append(it.deductible.amount.toString())
          val percentage = it.deductible.percentage
          if (percentage != null) {
            append(" + $percentage%")
          }
        },
        stringResource(R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL, it.homeQuotePremium.toString()),
        null,
      )
    },
    initiallyChosenItemIndex = initiallyChosenItemIndex,
    onItemSelected = onItemSelected,
    onDismissRequest = onDismissRequest,
  )
}

private data class RadioOptionCoverageInfo(
  val title: String,
  val premiumLabel: String,
  val description: String?,
)

@Composable
private fun CommonChoiceDialogContent(
  firstText: String,
  secondText: String,
  radioOptions: List<RadioOptionCoverageInfo>,
  initiallyChosenItemIndex: Int?,
  onItemSelected: (Int) -> Unit,
  onDismissRequest: () -> Unit,
) {
  var chosenItemIndex by remember { mutableStateOf(initiallyChosenItemIndex) }
  Column(Modifier.padding(16.dp)) {
    HedvigText(
      text = firstText,
      style = HedvigTheme.typography.bodySmall,
    )
    HedvigText(
      text = secondText,
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(12.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(12.dp))
      radioOptions.forEachIndexed { index, option ->
        RadioOption(
          chosenState = if (chosenItemIndex == index) Chosen else NotChosen,
          onClick = {
            chosenItemIndex = index
          },
          optionContent = { radioButtonIcon ->
            Row {
              radioButtonIcon()
              Spacer(Modifier.width(8.dp))
              Column(Modifier.weight(1f)) {
                HorizontalItemsWithMaximumSpaceTaken(
                  startSlot = {
                    HedvigText(
                      text = option.title,
                      modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                    )
                  },
                  endSlot = {
                    HighlightLabel(
                      labelText = option.premiumLabel,
                      size = HighLightSize.Small,
                      color = HighlightColor.Grey(MEDIUM),
                      modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                    )
                  },
                  modifier = Modifier.fillMaxWidth(),
                  spaceBetween = 8.dp,
                )
                if (option.description != null) {
                  HedvigText(
                    text = option.description,
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
                  )
                }
              }
            }
          },
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.GENERAL_CONFIRM),
      onClick = {
        if (chosenItemIndex != null) {
          onItemSelected(chosenItemIndex!!)
          onDismissRequest()
        }
      },
      buttonSize = Large,
      enabled = chosenItemIndex != null,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      onClick = onDismissRequest,
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@HedvigPreview
@Composable
fun PreviewCoverageChoiceDialogContent() {
  CoverageChoiceDialogContent(
    coverageOptions = List(3) {
      CoverageInfo(
        tierName = "tierName#$it",
        moveHomeQuoteId = "moveHomeQuoteId#$it",
        tierDescription = "tierDescription#$it".repeat(5),
        minimumPremiumForCoverage = UiMoney(
          amount = it.toDouble(),
          currencyCode = SEK,
        ),
      )
    },
    initiallyChosenItemIndex = null,
    onItemSelected = {},
    onDismissRequest = {},
  )
}

@HedvigPreview
@Composable
fun PreviewDeductibleChoiceDialogContent() {
  DeductibleChoiceDialogContent(
    deductibleOptions = List(3) {
      DeductibleOption(
        homeQuoteId = it.toString(),
        homeQuotePremium = UiMoney(it.toDouble(), SEK),
        deductible = Deductible(UiMoney(it.toDouble(), SEK), 15, "Display text#$it"),
      )
    },
    initiallyChosenItemIndex = null,
    onItemSelected = {},
    onDismissRequest = {},
  )
}

@HedvigPreview
@Composable
fun PreviewChoseCoverageLevelAndDeductibleScreen() {
  val allOptions: List<MoveHomeQuote> = List(3) {
    MoveHomeQuote(
      id = it.toString(),
      premium = UiMoney(
        amount = (it + 1) * 100.0,
        currencyCode = SEK,
      ),
      startDate = LocalDate.parse("2025-01-01"),
      displayItems = List(2) {
        DisplayItem(
          title = "title#$it",
          subtitle = "subtitle#$it",
          value = "value#$it",
        )
      },
      exposureName = "exposureName",
      productVariant = ProductVariant(
        displayName = "displayName",
        contractGroup = ACCIDENT,
        contractType = SE_APARTMENT_BRF,
        partner = null,
        perils = listOf(),
        insurableLimits = listOf(),
        documents = listOf(),
        displayTierName = null,
        tierDescription = null,
      ),
      tierName = "Pat Vance",
      tierLevel = 1299,
      tierDescription = "tierDescription#$it",
      deductible = null,
      defaultChoice = false,
    )
  }
  ChoseCoverageLevelAndDeductibleScreen(
    uiState = ChoseCoverageLevelAndDeductibleUiState.Content(
      tiersInfo = TiersInfo(
        allOptions = allOptions,
        coverageOptions = List(2) {
          CoverageInfo(it.toString(), "tierName#$it", "tierDescription#$it", UiMoney(it.toDouble(), SEK))
        },
        selectedCoverage = allOptions[0],
        selectedDeductible = allOptions[0],
      ),
    ),
    navigateUp = {},
    onSubmit = {},
    onSelectCoverageOption = {},
    onSelectDeductibleOption = {},
  )
}
