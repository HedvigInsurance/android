package com.hedvig.android.feature.change.mileage.ui.summary

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.change.mileage.data.MileageDisplayItem
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryParameters
import com.hedvig.android.feature.change.mileage.ui.ContractHeader
import com.hedvig.android.feature.change.mileage.ui.MileageCard
import com.hedvig.android.feature.change.mileage.ui.start.previewQuotes
import hedvig.resources.CHANGE_MILEAGE_SUMMARY_FOOTER
import hedvig.resources.CONTRACT_ADD_COINSURED_STARTS_FROM
import hedvig.resources.GENERAL_ARE_YOU_SURE
import hedvig.resources.GENERAL_NO
import hedvig.resources.GENERAL_PROGRESS_WILL_BE_LOST_ALERT
import hedvig.resources.GENERAL_YES
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE
import hedvig.resources.TIER_FLOW_SUMMARY_CONFIRM_BUTTON
import hedvig.resources.TIER_FLOW_SUMMARY_TITLE
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.general_cancel_button
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangeMileageSummaryDestination(
  viewModel: ChangeMileageSummaryViewModel,
  navigateUp: () -> Unit,
  onExitFlow: () -> Unit,
) {
  val uiState: ChangeMileageSummaryUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChangeMileageSummaryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onConfirm = { viewModel.emit(ChangeMileageSummaryEvent.Submit) },
    onExitFlow = onExitFlow,
  )
}

@Composable
private fun ChangeMileageSummaryScreen(
  uiState: ChangeMileageSummaryUiState,
  navigateUp: () -> Unit,
  onConfirm: () -> Unit,
  onExitFlow: () -> Unit,
) {
  if (uiState.isSubmitting) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(Res.string.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE),
    )
    return
  }
  var showExitDialog by rememberSaveable { mutableStateOf(false) }
  if (showExitDialog) {
    HedvigDialog(
      onDismissRequest = { showExitDialog = false },
      style = Buttons(
        onDismissRequest = { showExitDialog = false },
        dismissButtonText = stringResource(Res.string.GENERAL_NO),
        onConfirmButtonClick = dropUnlessResumed {
          showExitDialog = false
          onExitFlow()
        },
        confirmButtonText = stringResource(Res.string.GENERAL_YES),
      ),
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HedvigText(
          text = stringResource(Res.string.GENERAL_ARE_YOU_SURE),
          textAlign = TextAlign.Center,
        )
        HedvigText(
          text = stringResource(Res.string.GENERAL_PROGRESS_WILL_BE_LOST_ALERT),
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.TIER_FLOW_SUMMARY_TITLE),
    topAppBarActions = {
      IconButton(onClick = { showExitDialog = true }) {
        Icon(
          imageVector = HedvigIcons.Close,
          contentDescription = stringResource(Res.string.general_close_button),
          modifier = Modifier.size(24.dp),
        )
      }
    },
  ) {
    SummaryCard(
      params = uiState.params,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(Res.string.CHANGE_MILEAGE_SUMMARY_FOOTER),
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.textSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.TIER_FLOW_SUMMARY_CONFIRM_BUTTON),
      buttonSize = Large,
      enabled = true,
      onClick = onConfirm,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      buttonSize = Large,
      onClick = { showExitDialog = true },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun SummaryCard(params: ChangeMileageSummaryParameters, modifier: Modifier = Modifier) {
  val dateFormatter = rememberHedvigDateTimeFormatter()
  MileageCard(modifier) {
    ContractHeader(
      contractGroup = params.contractGroup,
      displayName = params.contractDisplayName,
      subtitle = stringResource(
        Res.string.CONTRACT_ADD_COINSURED_STARTS_FROM,
        dateFormatter.format(params.activationDate),
      ),
    )
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      for (item in params.quote.displayItems) {
        SummaryRow(
          title = item.title,
          previousValue = item.previousValue,
          value = item.value,
          isTotal = false,
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
    val newPrice = params.quote.totalCost.monthlyNet
    val totalDescription = newPrice.getPerMonthDescription()
    SummaryRow(
      title = stringResource(Res.string.TIER_FLOW_TOTAL),
      previousValue = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, params.currentMonthlyCost)
        .takeIf { params.currentMonthlyCost != newPrice },
      value = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, newPrice),
      isTotal = true,
      valueContentDescription = totalDescription,
    )
  }
}

@Composable
private fun SummaryRow(
  title: String,
  previousValue: String?,
  value: String,
  isTotal: Boolean,
  valueContentDescription: String? = null,
) {
  val style = if (isTotal) HedvigTheme.typography.bodySmall else HedvigTheme.typography.label
  val secondaryColor = HedvigTheme.colorScheme.textSecondary
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      HedvigText(
        text = title,
        style = style,
        color = if (isTotal) HedvigTheme.colorScheme.textPrimary else secondaryColor,
      )
    },
    spaceBetween = 8.dp,
    endSlot = {
      Row(horizontalArrangement = Arrangement.End) {
        if (previousValue != null) {
          HedvigText(
            text = previousValue,
            style = style.copy(textDecoration = TextDecoration.LineThrough),
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            textAlign = TextAlign.End,
          )
          Spacer(Modifier.width(8.dp))
        }
        HedvigText(
          text = value,
          style = style,
          color = if (isTotal || previousValue != null) HedvigTheme.colorScheme.textPrimary else secondaryColor,
          textAlign = TextAlign.End,
          modifier = if (valueContentDescription != null) {
            Modifier.semantics { contentDescription = valueContentDescription }
          } else {
            Modifier
          },
        )
      }
    },
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewChangeMileageSummaryScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChangeMileageSummaryScreen(
        uiState = ChangeMileageSummaryUiState(
          params = ChangeMileageSummaryParameters(
            contractDisplayName = "Bilförsäkring Helförsäkring",
            contractGroup = ContractGroup.CAR,
            activationDate = LocalDate(2026, 11, 17),
            currentMonthlyCost = UiMoney(412.0, UiCurrencyCode.SEK),
            quote = previewQuotes[0],
          ),
          isSubmitting = false,
        ),
        navigateUp = {},
        onConfirm = {},
        onExitFlow = {},
      )
    }
  }
}
