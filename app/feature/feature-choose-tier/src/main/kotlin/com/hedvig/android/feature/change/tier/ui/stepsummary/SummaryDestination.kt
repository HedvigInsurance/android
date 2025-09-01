package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleAddonQuote
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.changetier.data.TotalCost
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.android.tiersandaddons.QuoteDisplayItem
import com.hedvig.android.tiersandaddons.rememberQuoteCardState
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun ChangeTierSummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  onExitTierFlow: () -> Unit,
  onSuccess: () -> Unit,
  onFailure: () -> Unit,
) {
  val uiState: SummaryState by viewModel.uiState.collectAsStateWithLifecycle()
  SummaryScreen(
    uiState = uiState,
    onReload = {
      viewModel.emit(SummaryEvent.Reload)
    },
    onSuccess = {
      viewModel.emit(SummaryEvent.ClearNavigation)
      onSuccess()
    },
    onFailure = {
      viewModel.emit(SummaryEvent.ClearNavigation)
      onFailure()
    },
    navigateUp = navigateUp,
    onSubmitQuoteClick = {
      viewModel.emit(SummaryEvent.SubmitQuote)
    },
    onExitTierFlow = onExitTierFlow,
  )
}

@Composable
private fun SummaryScreen(
  uiState: SummaryState,
  onReload: () -> Unit,
  onSuccess: () -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  onSubmitQuoteClick: () -> Unit,
  onExitTierFlow: () -> Unit,
) {
  when (uiState) {
    Failure -> HedvigScaffold(navigateUp) {
      Spacer(Modifier.weight(1f))
      HedvigErrorSection(
        modifier = Modifier.weight(1f),
        onButtonClick = onReload,
      )
      Spacer(Modifier.weight(1f))
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()

    is MakingChanges -> {
      LaunchedEffect(uiState.navigateToSuccess) {
        val success = uiState.navigateToSuccess
        if (success) {
          onSuccess()
        }
      }
      MakingChangesScreen()
    }

    is Success -> {
      LaunchedEffect(uiState.navigateToFail) {
        if (uiState.navigateToFail) {
          onFailure()
        }
      }

      SummarySuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
        onExitTierFlow = onExitTierFlow,
      )
    }
  }
}

@Composable
private fun MakingChangesScreen() {
  HedvigFullScreenCenterAlignedLinearProgress(
    title = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE),
  )
}

@Composable
private fun SummarySuccessScreen(
  uiState: Success,
  onConfirmClick: () -> Unit,
  navigateUp: () -> Unit,
  onExitTierFlow: () -> Unit,
) {
  var showExitDialog by rememberSaveable { mutableStateOf(false) }
  HedvigScaffold(
    navigateUp,
    topAppBarActions = {
      SummaryTopAppBar(
        {
          showExitDialog = true
        },
      )
    },
    topAppBarText = stringResource(R.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    if (showExitDialog) {
      HedvigDialog(
        onDismissRequest = { showExitDialog = false },
        style = Buttons(
          onDismissRequest = { showExitDialog = false },
          dismissButtonText = stringResource(R.string.GENERAL_NO),
          onConfirmButtonClick = dropUnlessResumed {
            showExitDialog = false
            onExitTierFlow()
          },
          confirmButtonText = stringResource(R.string.GENERAL_YES),
        ),
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          HedvigText(
            text = stringResource(R.string.GENERAL_ARE_YOU_SURE),
            textAlign = TextAlign.Center,
          )
          HedvigText(
            text = stringResource(R.string.GENERAL_PROGRESS_WILL_BE_LOST_ALERT),
            textAlign = TextAlign.Center,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    val dateFormatter = rememberHedvigDateTimeFormatter()
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(R.string.TIER_FLOW_SUMMARY_CONFIRM_BUTTON),
        text = stringResource(
          R.string.CONFIRM_CHANGES_SUBTITLE,
          dateFormatter.format(uiState.activationDate.toJavaLocalDate()),
        ),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        confirmButtonLabel = stringResource(R.string.GENERAL_CONFIRM),
        dismissButtonLabel = stringResource(R.string.general_cancel_button),
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.fillMaxWidth(),
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          Row(horizontalArrangement = Arrangement.End) {
            val grossPriceVoiceDescription = stringResource(
              R.string.TALK_BACK_YOUR_PRICE_BEFORE_DISCOUNTS,
              uiState.quote.newTotalCost.monthlyGross.getPerMonthDescription(),
            )
            val netPriceVoiceDescription =
              stringResource(R.string.TALK_BACK_YOUR_PRICE_AFTER_DISCOUNTS, uiState.totalNet.getPerMonthDescription())
            HedvigText(
              text = stringResource(
                R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                uiState.quote.newTotalCost.monthlyGross,
              ),
              textAlign = TextAlign.End,
              style = LocalTextStyle.current.copy(
                color = HedvigTheme.colorScheme.textSecondaryTranslucent,
                textDecoration = TextDecoration.LineThrough,
              ),
              modifier = Modifier.semantics {
                contentDescription = grossPriceVoiceDescription
              },
            )
            Spacer(Modifier.width(8.dp))
            HedvigText(
              text = stringResource(
                R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                uiState.totalNet,
              ),
              textAlign = TextAlign.End,
              style = HedvigTheme.typography.bodySmall,
              modifier = Modifier.semantics {
                contentDescription = netPriceVoiceDescription
              },
            )
          }
        },
      )
      Row(horizontalArrangement = Arrangement.End) {
        val dateTimeFormatter = rememberHedvigDateTimeFormatter()
        HedvigText(
          text = stringResource(
            id = R.string.SUMMARY_TOTAL_PRICE_SUBTITLE,
            dateTimeFormatter.format(uiState.activationDate.toJavaLocalDate()),
          ),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.TIER_FLOW_SUMMARY_CONFIRM_BUTTON),
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = Primary,
        buttonSize = Large,
        enabled = true,
        onClick = {
          showConfirmationDialog = true
        },
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        buttonSize = Large,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        text = stringResource(R.string.general_cancel_button),
        onClick = {
          showExitDialog = true
        },
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SummaryTopAppBar(onIconClick: () -> Unit) {
  IconButton(onIconClick) {
    Icon(
      HedvigIcons.Close,
      stringResource(R.string.general_close_button),
      Modifier.size(24.dp),
    )
  }
}

@Composable
private fun SummaryCard(uiState: Success, modifier: Modifier = Modifier) {
  val state = rememberQuoteCardState()
  val addonDocs = uiState.quote.addons.flatMap { it.addonVariant.documents }
  val allDocuments: List<InsuranceVariantDocument> =
    uiState.quote.productVariant.documents + addonDocs
  QuoteCard(
    subtitle = uiState.currentContractData.contractDisplaySubtitle,
    isExcluded = false,
    premium = uiState.quote.newTotalCost.monthlyNet,
    costBreakdown = uiState.quote.costBreakdown,
    previousPremium = uiState.quote.newTotalCost.monthlyGross,
    displayItems = uiState.quote.displayItems.map {
      QuoteDisplayItem(
        it.displayTitle,
        null,
        it.displayValue,
      )
    },
    modifier = modifier,
    quoteCardState = state,
    displayName = uiState.quote.productVariant.displayName,
    contractGroup = uiState.quote.productVariant.contractGroup,
    insurableLimits = uiState.quote.productVariant.insurableLimits,
    documents = allDocuments,
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewSummaryScreen(
  @PreviewParameter(
    SummaryUiStateProvider::class,
  ) uiState: SummaryState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SummaryScreen(
        uiState,
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

private class SummaryUiStateProvider :
  CollectionPreviewParameterProvider<SummaryState>(
    listOf(
      Success(
        currentContractData = ContractData(
          contractGroup = ContractGroup.HOMEOWNER,
          contractDisplayName = "Home Homeowner",
          contractDisplaySubtitle = "Addressvägen 777",
          activeDisplayPremium = UiMoney(449.0, SEK),
        ),
        activationDate = LocalDate(2024, 5, 1),
        quote = TierDeductibleQuote(
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
            termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
          ),
          addons = List(2) {
            ChangeTierDeductibleAddonQuote(
              addonId = "addonId",
              displayName = "Addon Quote Name",
              displayItems = listOf(
                ChangeTierDeductibleDisplayItem(
                  displayTitle = "Coinsured",
                  displayValue = "You + 1",
                  displaySubtitle = null,
                ),
              ),
              previousPremium = UiMoney(29.0, SEK),
              premium = UiMoney(45.0, SEK),
              addonVariant = AddonVariant(
                displayName = "Addon Name",
                perils = listOf(),
                documents = listOf(
                  InsuranceVariantDocument(
                    "Document name",
                    "",
                    InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
                  ),
                ),
                termsVersion = "RESESKYDD-20230330-HEDVIG-null",
                product = "product",
              ),
            )
          },
          currentTotalCost = TotalCost(
            monthlyGross = UiMoney(175.0, UiCurrencyCode.SEK),
            monthlyNet = UiMoney(150.0, UiCurrencyCode.SEK),
          ),
          newTotalCost = TotalCost(
            monthlyGross = UiMoney(380.0, UiCurrencyCode.SEK),
            monthlyNet = UiMoney(304.0, UiCurrencyCode.SEK),
          ),
          costBreakdown = listOf(
            "Home Insurance Max" to "300 kr/mo",
            "Travel Plus" to "80 kr/mo",
            "Bundle discount 20%" to "76 kr/mo",
          ),
        ),
      ),
      Failure,
      Loading,
      MakingChanges(false),
    ),
  )
