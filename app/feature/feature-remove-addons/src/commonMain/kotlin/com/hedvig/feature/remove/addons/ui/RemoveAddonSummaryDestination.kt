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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.tiersandaddons.CostBreakdownEntry
import com.hedvig.android.tiersandaddons.DisplayDocument
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import hedvig.resources.ADDON_FLOW_SUMMARY_ACTIVE_FROM
import hedvig.resources.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON
import hedvig.resources.REMOVE_ADDON_CONFIRMATION_BUTTON
import hedvig.resources.REMOVE_ADDON_CONFIRMATION_DESCRIPTION
import hedvig.resources.REMOVE_ADDON_CONFIRMATION_TITLE
import hedvig.resources.Res
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
        existingAddons = existingAddonsToRemove
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
  )
}


@Composable
private fun RemoveAddonSummaryScreen(
  uiState: RemoveAddonSummaryState,
  onSuccess: (LocalDate) -> Unit,
  onFailure: () -> Unit,
  navigateUp: () -> Unit,
  onSubmitQuoteClick: () -> Unit,
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
    val formattedDate = remember(uiState.activationDate, locale) {
      HedvigDateTimeFormatterDefaults.dateMonthAndYear(
        locale,
      ).format(uiState.activationDate)
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(Res.string.REMOVE_ADDON_CONFIRMATION_TITLE),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        buttonSize = DialogDefaults.ButtonSize.BIG,
        confirmButtonLabel = stringResource(Res.string.REMOVE_ADDON_CONFIRMATION_BUTTON),
        dismissButtonLabel = stringResource(Res.string.general_close_button),
        text = stringResource(
          Res.string.REMOVE_ADDON_CONFIRMATION_DESCRIPTION,
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
        text = stringResource(Res.string.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON),
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
  val leftAddons = uiState.summaryParams.existingAddons.filter {
    !uiState.chosenAddons.contains(it)
  }
  val breakdown = buildList { //todo: there will be a separate entry point!!!
    add(
      CostBreakdownEntry(
        uiState.summaryParams. productVariant.displayName,
        uiState.summaryParams.baseCost.monthlyNet,
        false,
      ),
    )

    leftAddons.forEach {
      add(
        CostBreakdownEntry(
          it.displayTitle,
          it.cost.monthlyNet,
          false,
        ),
      )
    }
    uiState.chosenAddons.forEach {
      add(
        CostBreakdownEntry(
          it.displayTitle,
          it.cost.monthlyNet,
          true,
        ),
      )
    }
  }
  val newNetPremiumAmount = leftAddons.sumOf {
    it.cost.monthlyNet.amount
  } + uiState.summaryParams.baseCost.monthlyNet.amount
  val newPremium = UiMoney(newNetPremiumAmount, uiState.summaryParams.baseCost.monthlyNet.currencyCode)

  QuoteCard(
    subtitle = stringResource(
      Res.string.ADDON_FLOW_SUMMARY_ACTIVE_FROM,
      formattedDate,
    ),
    contractGroup = uiState.summaryParams.productVariant.contractGroup,
    premium = newPremium,
    costBreakdown = breakdown,
    previousPremium = uiState.summaryParams.currentTotalCost.monthlyNet,
    displayItems = emptyList(),
    modifier = modifier,
    displayName = uiState.summaryParams.productVariant.displayName,
    insurableLimits = emptyList(),
    documents = uiState.summaryParams. productVariant.documents.map {
      DisplayDocument(
        displayName = it.displayName,
        url = it.url,
      )
    },
  )
}
