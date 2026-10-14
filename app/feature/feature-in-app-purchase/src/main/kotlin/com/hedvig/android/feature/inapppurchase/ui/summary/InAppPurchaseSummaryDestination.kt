package com.hedvig.android.feature.inapppurchase.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.ui.tiersandaddons.DisplayDocument
import com.hedvig.android.ui.tiersandaddons.QuoteCard
import com.hedvig.android.ui.tiersandaddons.QuoteDisplayItem
import hedvig.resources.AUTHENTICATION_BANKID_OPEN_BUTTON
import hedvig.resources.GENERAL_ARE_YOU_SURE
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.GENERAL_NO
import hedvig.resources.GENERAL_PROGRESS_WILL_BE_LOST_ALERT
import hedvig.resources.GENERAL_YES
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_SUMMARY_TITLE
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.general_cancel_button
import hedvig.resources.general_close_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppPurchaseSummaryDestination(
  viewModel: InAppPurchaseSummaryViewModel,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val bankIdLauncher = rememberBankIdLauncher()
  val signingState = uiState.signingState
  if (signingState is SigningState.AwaitingBankId && signingState.autoStartToken != null) {
    LaunchedEffect(signingState.signingId) {
      bankIdLauncher.open(signingState.autoStartToken)
    }
  }
  InAppPurchaseSummaryScreen(
    uiState = uiState,
    canOpenBankId = bankIdLauncher.canOpenBankId,
    navigateUp = navigateUp,
    exitFlow = dropUnlessResumed { exitFlow() },
    onSign = { viewModel.emit(InAppPurchaseSummaryEvent.Sign) },
    onOpenBankId = { token -> bankIdLauncher.open(token) },
    dismissFailure = { viewModel.emit(InAppPurchaseSummaryEvent.DismissFailure) },
  )
}

@Composable
private fun InAppPurchaseSummaryScreen(
  uiState: InAppPurchaseSummaryUiState,
  canOpenBankId: Boolean,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  onSign: () -> Unit,
  onOpenBankId: (autoStartToken: String) -> Unit,
  dismissFailure: () -> Unit,
) {
  when (val signingState = uiState.signingState) {
    is SigningState.AwaitingBankId -> SigningInProgress(
      // TODO: Add "Waiting for BankID" / "Väntar på BankID" to Lokalise
      title = "Waiting for BankID",
      // TODO: Add "Open the BankID app to sign." / "Öppna BankID-appen för att signera." to Lokalise
      description = "Open the BankID app to sign.".takeIf { !signingState.bankIdAppOpened },
      autoStartToken = signingState.autoStartToken?.takeIf { canOpenBankId },
      onOpenBankId = onOpenBankId,
    )

    SigningState.Creating -> SigningInProgress(
      // TODO: Add "Creating your insurance" / "Skapar din försäkring" to Lokalise
      title = "Creating your insurance",
      description = null,
      autoStartToken = null,
      onOpenBankId = onOpenBankId,
    )

    SigningState.Idle,
    SigningState.Starting,
    is SigningState.Failed,
    -> SummaryContent(
      uiState = uiState,
      navigateUp = navigateUp,
      exitFlow = exitFlow,
      onSign = onSign,
      dismissFailure = dismissFailure,
    )
  }
}

@Composable
private fun SummaryContent(
  uiState: InAppPurchaseSummaryUiState,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  onSign: () -> Unit,
  dismissFailure: () -> Unit,
) {
  val signingState = uiState.signingState
  if (signingState is SigningState.Failed) {
    ErrorDialog(
      title = stringResource(Res.string.something_went_wrong),
      message = signingState.message ?: stringResource(Res.string.GENERAL_ERROR_BODY),
      onDismiss = dismissFailure,
    )
  }
  var showExitDialog by rememberSaveable { mutableStateOf(false) }
  if (showExitDialog) {
    ExitFlowDialog(
      onDismiss = { showExitDialog = false },
      onConfirm = {
        showExitDialog = false
        exitFlow()
      },
    )
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.TIER_FLOW_SUMMARY_TITLE),
    topAppBarActions = {
      IconButton(onClick = { showExitDialog = true }) {
        Icon(HedvigIcons.Close, stringResource(Res.string.general_close_button), Modifier.size(24.dp))
      }
    },
  ) {
    OfferQuoteCard(
      productDisplayName = uiState.productDisplayName,
      offer = uiState.offer,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(Modifier.padding(horizontal = 16.dp)) {
      TotalPrice(uiState.offer)
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        // TODO: Add "Sign with BankID" / "Signera med BankID" to Lokalise
        text = "Sign with BankID",
        onClick = onSign,
        enabled = signingState.canStartSigning,
        isLoading = signingState is SigningState.Starting,
        buttonSize = Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = { showExitDialog = true },
        buttonSize = Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun OfferQuoteCard(productDisplayName: String, offer: InAppPurchaseOffer, modifier: Modifier = Modifier) {
  val cost = offer.cost
  QuoteCard(
    displayName = productDisplayName,
    contractGroup = offer.variant.contractGroup,
    insurableLimits = offer.variant.insurableLimits,
    documents = offer.variant.documents.map { DisplayDocument(it.displayName, it.url) },
    subtitle = offer.exposureDisplayName,
    premium = cost.monthlyNet,
    previousPremium = cost.monthlyGross.takeIf { it != cost.monthlyNet },
    costBreakdown = offer.costBreakdown,
    displayItems = offer.displayItems.map { QuoteDisplayItem(it.title, it.value, null) },
    modifier = modifier,
  )
}

@Composable
private fun TotalPrice(offer: InAppPurchaseOffer) {
  val cost = offer.cost
  HorizontalItemsWithMaximumSpaceTaken(
    modifier = Modifier.fillMaxWidth(),
    startSlot = { HedvigText(stringResource(Res.string.TIER_FLOW_TOTAL), style = HedvigTheme.typography.bodySmall) },
    spaceBetween = 8.dp,
    endSlot = {
      Row(horizontalArrangement = Arrangement.End) {
        if (cost.monthlyGross != cost.monthlyNet) {
          HedvigText(
            text = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, cost.monthlyGross),
            style = HedvigTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
            color = HedvigTheme.colorScheme.textSecondary,
          )
          Spacer(Modifier.width(8.dp))
        }
        HedvigText(
          text = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, cost.monthlyNet),
          textAlign = TextAlign.End,
          style = HedvigTheme.typography.bodySmall,
        )
      }
    },
  )
  val startDate = offer.startDate
  if (startDate != null) {
    val formatter = rememberHedvigDateTimeFormatter()
    HedvigText(
      // TODO: Add "Starts %s" / "Börjar gälla %s" to Lokalise
      text = "Starts ${formatter.format(startDate)}",
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.textSecondary,
      textAlign = TextAlign.End,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun SigningInProgress(
  title: String,
  description: String?,
  autoStartToken: String?,
  onOpenBankId: (autoStartToken: String) -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(16.dp),
    ) {
      Spacer(Modifier.weight(1f))
      HedvigCircularProgressIndicator()
      Spacer(Modifier.height(16.dp))
      HedvigText(title, textAlign = TextAlign.Center)
      if (description != null) {
        HedvigText(description, textAlign = TextAlign.Center, color = HedvigTheme.colorScheme.textSecondary)
      }
      Spacer(Modifier.weight(1f))
      if (autoStartToken != null) {
        HedvigButton(
          text = stringResource(Res.string.AUTHENTICATION_BANKID_OPEN_BUTTON),
          onClick = { onOpenBankId(autoStartToken) },
          enabled = true,
          buttonStyle = Secondary,
          buttonSize = Large,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun ExitFlowDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  HedvigDialog(
    onDismissRequest = onDismiss,
    style = Buttons(
      onDismissRequest = onDismiss,
      dismissButtonText = stringResource(Res.string.GENERAL_NO),
      onConfirmButtonClick = onConfirm,
      confirmButtonText = stringResource(Res.string.GENERAL_YES),
    ),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      HedvigText(stringResource(Res.string.GENERAL_ARE_YOU_SURE), textAlign = TextAlign.Center)
      HedvigText(
        text = stringResource(Res.string.GENERAL_PROGRESS_WILL_BE_LOST_ALERT),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewInAppPurchaseSummaryScreen(
  @PreviewParameter(SummaryUiStateProvider::class) uiState: InAppPurchaseSummaryUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InAppPurchaseSummaryScreen(uiState, true, {}, {}, {}, {}, {})
    }
  }
}

private class SummaryUiStateProvider : CollectionPreviewParameterProvider<InAppPurchaseSummaryUiState>(
  listOf(
    SigningState.Idle,
    SigningState.Starting,
    SigningState.AwaitingBankId("signing", "token", bankIdAppOpened = false),
    SigningState.Creating,
    SigningState.Failed(InAppPurchaseMocks.QUOTE_NO_LONGER_VALID_MESSAGE),
  ).map { signingState ->
    InAppPurchaseSummaryUiState(
      productDisplayName = "Olycksfallsförsäkring",
      offer = InAppPurchaseMocks.accidentQuote().offers.first(),
      signingState = signingState,
    )
  },
)
