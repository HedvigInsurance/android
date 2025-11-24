package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup.ACCIDENT
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_BRF
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.ui.MovingFlowTopAppBar
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Content
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Loading
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.tiersandaddons.CostBreakdownEntry
import com.hedvig.android.tiersandaddons.DiscountCostBreakdown
import hedvig.resources.Res
import hedvig.resources.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL
import hedvig.resources.CHANGE_ADDRESS_TOTAL
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.MOVING_FLOW_ADDON_REMOVE_OPTION
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.TIER_FLOW_COMPARE_BUTTON
import hedvig.resources.TIER_FLOW_COVERAGE_LABEL
import hedvig.resources.TIER_FLOW_DEDUCTIBLE_LABEL
import hedvig.resources.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER
import hedvig.resources.TIER_FLOW_PRICE_LABEL_CURRENCY
import hedvig.resources.TIER_FLOW_PRICE_LABEL_WITHOUT_CURRENCY
import hedvig.resources.TIER_FLOW_SELECT_COVERAGE_SUBTITLE
import hedvig.resources.TIER_FLOW_SELECT_COVERAGE_TITLE
import hedvig.resources.TIER_FLOW_SELECT_DEDUCTIBLE_SUBTITLE
import hedvig.resources.TIER_FLOW_SELECT_DEDUCTIBLE_TITLE
import hedvig.resources.TIER_FLOW_SUBTITLE
import hedvig.resources.TIER_FLOW_TITLE
import hedvig.resources.general_back_button
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChoseCoverageLevelAndDeductibleDestination(
  viewModel: ChoseCoverageLevelAndDeductibleViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  exitFlow: () -> Unit,
  onNavigateToSummaryScreen: (homeQuoteId: String) -> Unit,
  navigateToComparison: (comparisonParameters: ComparisonParameters) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.navigateToSummaryScreenWithHomeQuoteId != null) {
    LaunchedEffect(uiState.navigateToSummaryScreenWithHomeQuoteId) {
      viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.NavigatedToSummary)
      onNavigateToSummaryScreen(uiState.navigateToSummaryScreenWithHomeQuoteId)
    }
  }
  if (uiState is Content && uiState.comparisonParameters != null) {
    LaunchedEffect(uiState.comparisonParameters) {
      viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.ClearNavigateToComparison)
      navigateToComparison(uiState.comparisonParameters)
    }
  }
  ChoseCoverageLevelAndDeductibleScreen(
    onCompareCoverageClicked = {
      viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.LaunchComparison)
    },
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    exitFlow = exitFlow,
    onSubmit = { selectedHomeQuoteId ->
      viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SubmitSelectedHomeQuoteId(selectedHomeQuoteId))
    },
    onSelectCoverageOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectCoverage(it)) },
    onSelectDeductibleOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectDeductible(it)) },
    onChangeAddonExclusion = { addonId, exclude ->
      viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.AlterAddon(addonId, exclude))
    },
  )
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  uiState: ChoseCoverageLevelAndDeductibleUiState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  exitFlow: () -> Unit,
  onSubmit: (String) -> Unit,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
  onChangeAddonExclusion: (AddonId, Boolean) -> Unit,
  onCompareCoverageClicked: () -> Unit,
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
          Loading -> {
            HedvigFullScreenCenterAlignedProgress()
          }

          MissingOngoingMovingFlow -> {
            HedvigErrorSection(
              onButtonClick = popBackStack,
              buttonText = stringResource(Res.string.general_back_button),
            )
          }

          is Content -> ChoseCoverageLevelAndDeductibleScreen(
            content = uiState,
            onSubmit = uiState.tiersInfo.selectedHomeQuoteId?.let { { onSubmit(it) } },
            onSelectCoverageOption = onSelectCoverageOption,
            onSelectDeductibleOption = onSelectDeductibleOption,
            onCompareCoverageClicked = onCompareCoverageClicked,
            onChangeAddonExclusion = onChangeAddonExclusion,
          )
        }
      }
    }
  }
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  content: Content,
  onSubmit: (() -> Unit)?,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
  onCompareCoverageClicked: () -> Unit,
  onChangeAddonExclusion: (AddonId, Boolean) -> Unit,
) {
  Column(Modifier.padding(horizontal = 16.dp)) {
    FlowHeading(
      stringResource(Res.string.TIER_FLOW_TITLE),
      stringResource(Res.string.TIER_FLOW_SUBTITLE),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      CoverageCard(
        content.tiersInfo,
        content.costBreakdown,
        content.premium,
        content.grossPremium,
        onSelectCoverageOption,
        onSelectDeductibleOption,
        onChangeAddonExclusion,
      )
      Spacer(Modifier.height(8.dp))
      if (content.tiersInfo.coverageOptions.isNotEmpty()) {
        HedvigTextButton(
          text = stringResource(Res.string.TIER_FLOW_COMPARE_BUTTON),
          modifier = Modifier.fillMaxWidth(),
          buttonSize = Large,
        ) {
          onCompareCoverageClicked()
        }
        Spacer(Modifier.height(4.dp))
      }
      HedvigButton(
        text = stringResource(Res.string.general_continue_button),
        onClick = dropUnlessResumed {
          onSubmit?.invoke()
        },
        isLoading = content.isSubmitting,
        enabled = onSubmit != null && content.canSubmit,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun CoverageCard(
  tiersInfo: TiersInfo,
  costBreakdown: List<CostBreakdownEntry>?,
  premium: UiMoney?,
  grossPremium: UiMoney?,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
  onChangeAddonExclusion: (AddonId, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(
        shape = HedvigTheme.shapes.cornerXLarge,
        color = HedvigTheme.colorScheme.borderPrimary,
        width = 1.dp,
      ),
  ) {
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
            label = stringResource(Res.string.TIER_FLOW_COVERAGE_LABEL),
          ),
          size = Small,
          hintText = tiersInfo.selectedCoverage.tierDisplayName,
          chosenItemIndex = chosenCoverageItemIndex,
          dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        ) { onDismissRequest ->
          CoverageChoiceDialogContent(
            coverageOptions = tiersInfo.coverageOptions,
            chosenItemIndex = chosenCoverageItemIndex,
            onDismissRequest = onDismissRequest,
            isPremiumPriceExact = false,
            onItemSelected = {
              onSelectCoverageOption(tiersInfo.coverageOptions[it].moveHomeQuoteId)
            },
          )
        }
        val relatedAddonQuotes = tiersInfo.selectedCoverage.relatedAddonQuotes
        for (relatedAddonQuote in relatedAddonQuotes) {
          val relatedAddonQuoteOptions = listOf(
            relatedAddonQuote.coverageDisplayName,
            stringResource(Res.string.MOVING_FLOW_ADDON_REMOVE_OPTION),
          )
          val chosenItemIndex = when (relatedAddonQuote.isExcludedByUser) {
            false -> 0
            true -> 1
          }
          DropdownWithDialog(
            style = Label(
              items = relatedAddonQuoteOptions.map {
                SimpleDropdownItem(it)
              },
              label = relatedAddonQuote.exposureName,
            ),
            size = Small,
            hintText = "",
            chosenItemIndex = chosenItemIndex,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
          ) { onDismissRequest ->
            CoverageChoiceDialogContent(
              coverageOptions = relatedAddonQuoteOptions.mapIndexed { index, optionName ->
                CoverageInfo(
                  moveHomeQuoteId = relatedAddonQuote.addonId.id,
                  tierName = optionName,
                  tierDescription = null,
                  minimumPremiumForCoverage = if (index == 1) {
                    UiMoney(0.0, relatedAddonQuote.premium.currencyCode)
                  } else {
                    relatedAddonQuote.premium
                  },
                )
              },
              chosenItemIndex = chosenItemIndex,
              isPremiumPriceExact = true,
              onDismissRequest = onDismissRequest,
              onItemSelected = {
                val exclude = it == 1
                onChangeAddonExclusion(relatedAddonQuote.addonId, exclude)
              },
            )
          }
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
                label = stringResource(Res.string.TIER_FLOW_DEDUCTIBLE_LABEL),
              ),
              size = Small,
              hintText = tiersInfo.selectedDeductible?.tierDisplayName
                ?: stringResource(Res.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
              chosenItemIndex = chosenDeductibleItemIndex,
              dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            ) { onDismissRequest ->
              DeductibleChoiceDialogContent(
                deductibleOptions = deductibleOptions.deductibleOptions,
                chosenItemIndex = chosenDeductibleItemIndex,
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
              labelText = stringResource(Res.string.TIER_FLOW_DEDUCTIBLE_LABEL),
              readOnly = true,
            )
          }
        }
      }
      if (costBreakdown != null) {
        DiscountCostBreakdown(costBreakdown)
      }
      HorizontalDivider()
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { HedvigText(stringResource(Res.string.CHANGE_ADDRESS_TOTAL)) },
        endSlot = {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End)) {
            if (grossPremium != null && grossPremium.amount != premium?.amount) {
              HedvigText(
                text = stringResource(
                  Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                  grossPremium,
                ),
                textAlign = TextAlign.End,
                style = LocalTextStyle.current.copy(
                  textDecoration = TextDecoration.LineThrough,
                ),
                modifier = Modifier.semantics { hideFromAccessibility() },
              )
            }
            HedvigText(
              text = stringResource(
                Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                premium ?: tiersInfo.selectedCoverage.netPremiumWithAddons.toString(),
              ),
              textAlign = TextAlign.End,
              modifier = Modifier.wrapContentWidth(Alignment.End),
            )
          }
        },
        spaceBetween = 8.dp,
        Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun CoverageChoiceDialogContent(
  coverageOptions: List<CoverageInfo>,
  chosenItemIndex: Int?,
  isPremiumPriceExact: Boolean,
  onItemSelected: (Int) -> Unit,
  onDismissRequest: () -> Unit,
) {
  var dialogLocalChosenItemIndex by remember { mutableStateOf(chosenItemIndex) }
  CommonChoiceDialogContent(
    firstText = stringResource(Res.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
    secondText = stringResource(Res.string.TIER_FLOW_SELECT_COVERAGE_SUBTITLE),
    radioGroup = {
      RadioGroup(
        options = coverageOptions.map { coverageInfo ->
          RadioOption(
            id = RadioOptionId(coverageInfo.id),
            text = coverageInfo.tierName,
            label = coverageInfo.tierDescription,
          )
        },
        selectedOption = dialogLocalChosenItemIndex?.let { RadioOptionId(coverageOptions[it].id) },
        onRadioOptionSelected = { id ->
          dialogLocalChosenItemIndex = coverageOptions.indexOfFirst { it.id == id.id }
        },
        style = RadioGroupStyle.LeftAligned,
        textEndContent = { id ->
          val coverageInfo = coverageOptions.first { it.id == id.id }
          HighlightLabel(
            labelText = stringResource(
              if (isPremiumPriceExact) {
                Res.string.TIER_FLOW_PRICE_LABEL_CURRENCY
              } else {
                Res.string.TIER_FLOW_PRICE_LABEL_WITHOUT_CURRENCY
              },
              coverageInfo.minimumPremiumForCoverage.toString(),
            ),
            size = HighLightSize.Small,
            color = HighlightColor.Grey(MEDIUM),
          )
        },
      )
    },
    selectedOptionIndex = chosenItemIndex,
    onConfirm = {
      onItemSelected(dialogLocalChosenItemIndex!!)
      onDismissRequest()
    },
    onDismissRequest = {
      onDismissRequest()
    },
  )
}

@Composable
private fun DeductibleChoiceDialogContent(
  deductibleOptions: List<DeductibleOption>,
  chosenItemIndex: Int?,
  onItemSelected: (Int) -> Unit,
  onDismissRequest: () -> Unit,
) {
  var dialogLocalChosenItemIndex by remember { mutableStateOf(chosenItemIndex) }
  CommonChoiceDialogContent(
    firstText = stringResource(Res.string.TIER_FLOW_SELECT_DEDUCTIBLE_TITLE),
    secondText = stringResource(Res.string.TIER_FLOW_SELECT_DEDUCTIBLE_SUBTITLE),
    radioGroup = {
      RadioGroup(
        options = deductibleOptions.map { deductibleOption ->
          RadioOption(
            id = RadioOptionId(deductibleOption.id),
            text = buildString {
              append(deductibleOption.deductible.amount.toString())
              val percentage = deductibleOption.deductible.percentage
              if (percentage != null) {
                append(" + $percentage%")
              }
            },
          )
        },
        selectedOption = dialogLocalChosenItemIndex?.let { RadioOptionId(deductibleOptions[it].id) },
        onRadioOptionSelected = { id ->
          dialogLocalChosenItemIndex = deductibleOptions.indexOfFirst { it.id == id.id }
        },
        style = RadioGroupStyle.LeftAligned,
        textEndContent = { id ->
          val deductibleOption = deductibleOptions.first { it.id == id.id }
          HighlightLabel(
            labelText = stringResource(
              Res.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL,
              deductibleOption.homeQuotePremium.toString(),
            ),
            size = HighLightSize.Small,
            color = HighlightColor.Grey(MEDIUM),
            modifier = Modifier.wrapContentSize(Alignment.TopEnd),
          )
        },
      )
    },
    selectedOptionIndex = chosenItemIndex,
    onConfirm = {
      onItemSelected(dialogLocalChosenItemIndex!!)
      onDismissRequest()
    },
    onDismissRequest = onDismissRequest,
  )
}

@Composable
private fun CommonChoiceDialogContent(
  firstText: String,
  secondText: String,
  radioGroup: @Composable () -> Unit,
  selectedOptionIndex: Int?,
  onConfirm: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  Column(
    Modifier.verticalScroll(rememberScrollState()),
  ) {
    FlowHeading(
      firstText,
      secondText,
      baseStyle = HedvigTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(30.dp))
    radioGroup()
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.GENERAL_CONFIRM),
      onClick = onConfirm,
      buttonSize = Large,
      enabled = selectedOptionIndex != null,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
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
    chosenItemIndex = null,
    isPremiumPriceExact = false,
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
    chosenItemIndex = null,
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
      premium = UiMoney(100.0, SEK),
      netPremiumWithAddons = UiMoney(
        amount = (it + 1) * 100.0,
        currencyCode = SEK,
      ),
      grossPremiumWithAddons = UiMoney(
        amount = (it + 1) * 110.0,
        currencyCode = SEK,
      ),
      startDate = LocalDate.parse("2025-01-01"),
      discounts = emptyList(),
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
        termsVersion = "termsVersion",
      ),
      relatedAddonQuotes = emptyList(),
      tierName = "Pat Vance",
      tierLevel = 1299,
      tierDescription = "tierDescription#$it",
      deductible = null,
      defaultChoice = false,
    )
  }
  ChoseCoverageLevelAndDeductibleScreen(
    uiState = Content(
      tiersInfo = TiersInfo(
        allOptions = allOptions,
        mtaQuotes = emptyList(),
        coverageOptions = List(2) {
          CoverageInfo(it.toString(), "tierName#$it", "tierDescription#$it", UiMoney(it.toDouble(), SEK))
        },
        selectedCoverage = allOptions[0],
        selectedDeductible = allOptions[0],
      ),
      costBreakdown = List(2) {
        CostBreakdownEntry(
          "title#$it",
          UiMoney(it.toDouble(), SEK),
        )
      },
      premium = UiMoney(100.0, UiCurrencyCode.SEK),
      grossPremium = UiMoney(110.0, UiCurrencyCode.SEK),
      navigateToSummaryScreenWithHomeQuoteId = null,
      isSubmitting = false,
      comparisonParameters = null,
    ),
    navigateUp = {},
    popBackStack = {},
    onSubmit = {},
    exitFlow = {},
    onSelectCoverageOption = {},
    onSelectDeductibleOption = {},
    onCompareCoverageClicked = {},
    onChangeAddonExclusion = { _, _ -> },
  )
}

@HedvigPreview
@Composable
fun PreviewChoseCoverageLevelAndDeductibleScreenFailure() {
  ChoseCoverageLevelAndDeductibleScreen(
    uiState = MissingOngoingMovingFlow,
    navigateUp = {},
    popBackStack = {},
    onSubmit = {},
    exitFlow = {},
    onSelectCoverageOption = {},
    onSelectDeductibleOption = {},
    onCompareCoverageClicked = {},
    onChangeAddonExclusion = { _, _ -> },
  )
}
