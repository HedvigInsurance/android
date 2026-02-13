package com.hedvig.feature.remove.addons.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import com.hedvig.ui.tiersandaddons.DisplayDocument
import com.hedvig.ui.tiersandaddons.QuoteCard
import com.hedvig.ui.tiersandaddons.QuoteCostBreakdown
import hedvig.resources.ADDON_FLOW_SUMMARY_ACTIVE_FROM
import hedvig.resources.CONFIRM_CHANGES_SUBTITLE
import hedvig.resources.CONFIRM_CHANGES_TITLE
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_SUMMARY_CONFIRM_BUTTON
import hedvig.resources.TIER_FLOW_SUMMARY_TITLE
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun RemoveAddonSummaryDestination(
  contractId: String,
  addonsToRemove: List<CurrentlyActiveAddon>,
  activationDate: LocalDate,
  baseCost: ItemCost,
  currentTotalCost: ItemCost,
  existingAddonsToRemove: List<CurrentlyActiveAddon>,
  productVariant: ProductVariant,
  navigateToSuccess: (activationDate: LocalDate) -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
) {
  val viewModel: RemoveAddonSummaryViewModel = koinViewModel {
    parametersOf(
      CommonSummaryParameters(
        contractId = contractId,
        addonsToRemove = addonsToRemove,
        activationDate = activationDate,
        baseCost = baseCost,
        currentTotalCost = currentTotalCost,
        productVariant = productVariant,
        existingAddons = existingAddonsToRemove,
      ),
    )
  }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  RemoveAddonSummaryScreen(
    uiState = uiState,
    onSuccess = { date ->
      viewModel.emit(RemoveAddonSummaryEvent.ReturnToInitialState)
      navigateToSuccess(date)
    },
    onFailure = {
      viewModel.emit(RemoveAddonSummaryEvent.ReturnToInitialState)
      onFailure()
    },
    navigateUp = navigateUp,
    onSubmitQuoteClick = {
      viewModel.emit(RemoveAddonSummaryEvent.Submit)
    },
    reload = {
      viewModel.emit(RemoveAddonSummaryEvent.Retry)
    },
  )
}


@Composable
private fun RemoveAddonSummaryScreen(
  uiState: RemoveAddonSummaryState,
  onSuccess: (LocalDate) -> Unit,
  onFailure: () -> Unit,
  navigateUp: () -> Unit,
  onSubmitQuoteClick: () -> Unit,
  reload: () -> Unit,
) {
  when (uiState) {
    is RemoveAddonSummaryState.Loading -> {
      LaunchedEffect(uiState.activationDateToNavigateToSuccess) {
        val date = uiState.activationDateToNavigateToSuccess
        if (date != null) {
          onSuccess(date)
        }
      }
      HedvigFullScreenCenterAlignedProgress()
    }

    is RemoveAddonSummaryState.Content -> {
      LaunchedEffect(uiState.navigateToFailure) {
        val fail = uiState.navigateToFailure
        if (fail != null) {
          onFailure()
        }
      }
      SummaryContentScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
      )
    }

    RemoveAddonSummaryState.Failure -> HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
    }
  }
}


@Composable
private fun SummaryContentScreen(
  uiState: RemoveAddonSummaryState.Content,
  navigateUp: () -> Unit,
  onConfirmClick: () -> Unit,
) {
  HedvigScaffold(
    navigateUp,
    topAppBarText = stringResource(Res.string.TIER_FLOW_SUMMARY_TITLE),
  ) {
    val locale = getLocale()
    val formattedDate = remember(uiState.summaryParams.activationDate, locale) {
      HedvigDateTimeFormatterDefaults.dateMonthAndYear(
        locale,
      ).format(uiState.summaryParams.activationDate)
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(Res.string.CONFIRM_CHANGES_TITLE),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        buttonSize = DialogDefaults.ButtonSize.BIG,
        confirmButtonLabel = stringResource(Res.string.GENERAL_CONFIRM),
        dismissButtonLabel = stringResource(Res.string.general_close_button),
        text = stringResource(
          Res.string.CONFIRM_CHANGES_SUBTITLE,
          formattedDate,
        ),
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      formattedDate = formattedDate,
    )
    Spacer(Modifier.weight(1f))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(Res.string.TIER_FLOW_SUMMARY_CONFIRM_BUTTON),
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
private fun SummaryCard(
  uiState: RemoveAddonSummaryState.Content,
  formattedDate: String,
  modifier: Modifier = Modifier,
) {
  val breakdown = uiState.costBreakdown.entries
  val newPremium = uiState.costBreakdown.totalMonthlyNet
  val grossPremium = uiState.costBreakdown.totalMonthlyGross

  QuoteCard(
    subtitle = uiState.exposureName,
    contractGroup = uiState.summaryParams.productVariant.contractGroup,
    premium = newPremium,
    costBreakdown = breakdown,
    previousPremium = grossPremium,
    displayItems = emptyList(),
    modifier = modifier,
    displayName = uiState.summaryParams.productVariant.displayName,
    insurableLimits = emptyList(),
    documents = uiState.summaryParams.productVariant.documents.map {
      DisplayDocument(
        displayName = it.displayName,
        url = it.url,
      )
    },
  )
}


@HedvigPreview
@Composable
private fun PreviewRemoveAddonSummaryScreen(
  @PreviewParameter(
    RemoveAddonSummaryStateUiStateProvider::class,
  ) uiState: RemoveAddonSummaryState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      RemoveAddonSummaryScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class RemoveAddonSummaryStateUiStateProvider :
  CollectionPreviewParameterProvider<RemoveAddonSummaryState>(
    listOf(
      RemoveAddonSummaryState.Loading(),
      RemoveAddonSummaryState.Failure,
      RemoveAddonSummaryState.Content(
        summaryParams = CommonSummaryParameters(
          contractId = "contractId",
          addonsToRemove = listOf(
            CurrentlyActiveAddon(
              id = "addonToRemove",
              displayTitle = "addonToRemove",
              displayDescription = "addonToRemove description",
              cost = ItemCost(
                UiMoney(70.0, UiCurrencyCode.SEK),
                UiMoney(70.0, UiCurrencyCode.SEK),
                emptyList(),
              ),
            )
          ),
          activationDate = LocalDate(2026, 9, 1),
          baseCost = ItemCost(
            UiMoney(200.0, UiCurrencyCode.SEK),
            UiMoney(200.0, UiCurrencyCode.SEK),
            emptyList(),
          ),
          currentTotalCost = ItemCost(
            UiMoney(319.0, UiCurrencyCode.SEK),
            UiMoney(319.0, UiCurrencyCode.SEK),
            emptyList(),
          ),
          productVariant = ProductVariant(
            displayName = "base insurance product variant display name",
            contractGroup = ContractGroup.CAR,
            contractType = ContractType.SE_CAR_HALF,
            partner = null,
            perils = emptyList(),
            insurableLimits = emptyList(),
            documents = emptyList(),
            displayTierName = "base insurance product variant displayTierName",
            tierDescription = "tierDescription",
            termsVersion = "termsVersion"
          ),
          existingAddons = listOf(
            CurrentlyActiveAddon(
              id = "leftAddon1",
              displayTitle = "leftAddon1",
              displayDescription = "leftAddon1 description",
              cost = ItemCost(
                UiMoney(49.0, UiCurrencyCode.SEK),
                UiMoney(49.0, UiCurrencyCode.SEK),
                emptyList(),
              ),
            ),
            CurrentlyActiveAddon(
              id = "addonToRemove",
              displayTitle = "addonToRemove",
              displayDescription = "addonToRemove description",
              cost = ItemCost(
                UiMoney(70.0, UiCurrencyCode.SEK),
                UiMoney(70.0, UiCurrencyCode.SEK),
                emptyList(),
              ),
            ),
          ),
        ),
        costBreakdown = QuoteCostBreakdown(
          totalMonthlyNet = UiMoney(249.0, UiCurrencyCode.SEK),
          totalMonthlyGross = UiMoney(249.0, UiCurrencyCode.SEK),
          entries = listOf(
            CostBreakdownEntry(
              displayName = "base insurance",
              displayValue = "200 kr/mo",
              hasStrikethrough = false,
            ),
            CostBreakdownEntry(
              displayName = "leftAddon1",
              displayValue = "49 kr/mo",
              hasStrikethrough = false,
            ),
            CostBreakdownEntry(
              displayName = "addon to remove",
              displayValue = "70 kr/mo",
              hasStrikethrough = true,
            ),
          ),
        ),
        navigateToFailure = null,
        exposureName = "exposureName",
      ),
    ),
  )
