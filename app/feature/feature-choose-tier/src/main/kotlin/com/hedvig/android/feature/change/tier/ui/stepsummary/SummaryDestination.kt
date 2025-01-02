package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleAddonQuote
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
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
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.android.tiersandaddons.QuoteDisplayItem
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
        modifier = Modifier.fillMaxSize(),
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
        val fail = uiState.navigateToFail
        if (fail) {
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
    title =
      stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE),
  )
}

@Composable
private fun SummarySuccessScreen(
  uiState: Success,
  onConfirmClick: () -> Unit,
  navigateUp: () -> Unit,
  onExitTierFlow: () -> Unit,
) {
  HedvigScaffold(
    navigateUp,
    topAppBarActions = {
      SummaryTopAppBar(onExitTierFlow)
    },
    topAppBarText = stringResource(R.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(R.string.TIER_FLOW_CONFIRMATION_DIALOG_TEXT),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        confirmButtonLabel = stringResource(R.string.GENERAL_CONFIRM),
        dismissButtonLabel = stringResource(R.string.general_cancel_button),
        text = null,
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (addon in uiState.quote.addons) {
        AddonCard(
          addonQuote = addon,
          activationDate = uiState.activationDate,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
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
            text = stringResource(
              R.string.TERMINATION_FLOW_PAYMENT_PER_MONTH,
              uiState.total.amount.toInt(),
            ),
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
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
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun SummaryTopAppBar(onExitTierFlow: () -> Unit) {
  var showExitDialog by rememberSaveable { mutableStateOf(false) }
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
  IconButton({ showExitDialog = true }) {
    Icon(HedvigIcons.Close, null, Modifier.size(24.dp))
  }
}

@Composable
private fun SummaryCard(uiState: Success, modifier: Modifier = Modifier) {
  QuoteCard(
    productVariant = uiState.quote.productVariant,
    subtitle = stringResource(
      R.string.CHANGE_ADDRESS_ACTIVATION_DATE,
      formatStartDate(uiState.activationDate),
    ),
    premium = uiState.quote.premium.toString(),
    displayItems = uiState.quote.displayItems.map {
      QuoteDisplayItem(
        it.displayTitle,
        null,
        it.displayValue,
      )
    },
    underTitleContent = {
      HedvigText(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        textAlign = TextAlign.End,
        text = stringResource(
          R.string.TIER_FLOW_PREVIOUS_PRICE,
          stringResource(
            R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            uiState.currentContractData.activeDisplayPremium.toString(),
          ),
        ),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun AddonCard(
  addonQuote: ChangeTierDeductibleAddonQuote,
  activationDate: LocalDate,
  modifier: Modifier = Modifier,
) {
  val startDate = formatStartDate(activationDate)
  val subtitle = stringResource(R.string.CHANGE_ADDRESS_ACTIVATION_DATE, startDate)
  QuoteCard(
    displayName = addonQuote.addonVariant.displayName,
    contractGroup = null,
    insurableLimits = emptyList(),
    // todo: here we don't want to show insurable limits for addons, that may change later
    documents = addonQuote.addonVariant.documents,
    subtitle = subtitle,
    premium = addonQuote.premium.toString(),
    displayItems = addonQuote.displayItems.map {
      QuoteDisplayItem(
        title = it.displayTitle,
        subtitle = it.displaySubtitle,
        value = it.displayValue,
      )
    },
    modifier = modifier,
    underTitleContent = {
      HedvigText(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        textAlign = TextAlign.End,
        text = stringResource(
          R.string.TIER_FLOW_PREVIOUS_PRICE,
          stringResource(
            R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            addonQuote.previousPremium.toString(),
          ),
        ),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    },
  )
}

@Composable
private fun formatStartDate(startDate: LocalDate): String {
  val locale = getLocale()
  return remember(startDate) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale).format(startDate.toJavaLocalDate())
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewChooseInsuranceScreen(
  @PreviewParameter(
    ChooseInsuranceUiStateProvider::class,
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

private class ChooseInsuranceUiStateProvider :
  CollectionPreviewParameterProvider<SummaryState>(
    listOf(
      Success(
        currentContractData = ContractData(
          contractGroup = ContractGroup.HOMEOWNER,
          contractDisplayName = "Home Homeowner",
          contractDisplaySubtitle = "Addressvägen 777",
          activeDisplayPremium = "449 kr",
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
                insurableLimits = listOf(),
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
        ),
      ),
      Failure,
      Loading,
      MakingChanges(false),
    ),
  )
