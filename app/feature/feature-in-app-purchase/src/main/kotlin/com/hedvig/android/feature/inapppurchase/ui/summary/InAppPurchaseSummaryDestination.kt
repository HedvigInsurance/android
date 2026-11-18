package com.hedvig.android.feature.inapppurchase.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
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
import com.hedvig.android.ui.bankid.BankIdQrCode
import com.hedvig.android.ui.bankid.rememberBankIdState
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
import hedvig.resources.TIER_FLOW_COVERAGE_LABEL
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
  InAppPurchaseSummaryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    exitFlow = dropUnlessResumed { exitFlow() },
    onSign = { viewModel.emit(InAppPurchaseSummaryEvent.Sign) },
    dismissFailure = { viewModel.emit(InAppPurchaseSummaryEvent.DismissFailure) },
  )
}

@Composable
private fun InAppPurchaseSummaryScreen(
  uiState: InAppPurchaseSummaryUiState,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  onSign: () -> Unit,
  dismissFailure: () -> Unit,
) {
  when (val signingState = uiState.signingState) {
    is SigningState.AwaitingBankId -> AwaitingBankIdSignature(signingState)

    is SigningState.Creating -> CreatingInsurance()

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
    displayItems = buildList {
      offer.insuranceAmount?.let {
        add(QuoteDisplayItem(stringResource(Res.string.TIER_FLOW_COVERAGE_LABEL), it.displayName, null))
      }
      offer.displayItems.mapTo(this) { QuoteDisplayItem(it.title, it.value, null) }
    },
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
private fun AwaitingBankIdSignature(signingState: SigningState.AwaitingBankId) {
  val bankIdState = signingState.autoStartToken?.let { rememberBankIdState(it) }
  LaunchedEffect(signingState.signingId) {
    bankIdState?.tryOpenBankId()
  }
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(16.dp),
    ) {
      Spacer(Modifier.weight(1f))
      BankIdQrCode(liveQrCodeData = signingState.liveQrCodeData, modifier = Modifier.size(180.dp))
      Spacer(Modifier.height(32.dp))
      HedvigText(
        // TODO: Add "Sign with BankID" / "Signera med BankID" to Lokalise
        text = "Sign with BankID",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      HedvigText(
        // TODO: Add "Open the BankID app on your phone or scan the QR code to sign" / "Öppna BankID-appen på din telefon eller skanna QR-koden för att signera" to Lokalise
        text = "Open the BankID app on your phone or scan the QR code to sign",
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.weight(1f))
      if (bankIdState?.canOpenBankId == true) {
        HedvigButton(
          text = stringResource(Res.string.AUTHENTICATION_BANKID_OPEN_BUTTON),
          onClick = bankIdState::tryOpenBankId,
          enabled = true,
          buttonStyle = Primary,
          buttonSize = Large,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun CreatingInsurance() {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(16.dp),
    ) {
      HedvigCircularProgressIndicator()
      Spacer(Modifier.height(16.dp))
      // TODO: Add "Creating your insurance" / "Skapar din försäkring" to Lokalise
      HedvigText("Creating your insurance", textAlign = TextAlign.Center)
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
      InAppPurchaseSummaryScreen(uiState, {}, {}, {}, {})
    }
  }
}

private class SummaryUiStateProvider : CollectionPreviewParameterProvider<InAppPurchaseSummaryUiState>(
  listOf(
    SigningState.Idle,
    SigningState.Starting,
    SigningState.AwaitingBankId("signing", "token", liveQrCodeData = "bankid.preview"),
    SigningState.Creating("signing"),
    SigningState.Failed(InAppPurchaseMocks.QUOTE_NO_LONGER_VALID_MESSAGE),
  ).map { signingState ->
    InAppPurchaseSummaryUiState(
      productDisplayName = "Accident Insurance",
      offer = InAppPurchaseMocks.accidentQuote().offers.first(),
      signingState = signingState,
    )
  },
)
