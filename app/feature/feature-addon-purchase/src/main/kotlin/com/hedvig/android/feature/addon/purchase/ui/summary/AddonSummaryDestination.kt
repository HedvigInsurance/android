package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.addon.purchase.data.AddonVariant
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.tiersandaddons.QuoteCard
import com.hedvig.android.tiersandaddons.QuoteDisplayItem
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun AddonSummaryDestination(
  viewModel: AddonSummaryViewModel,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  onSuccess: (activationDate: LocalDate) -> Unit,
) {
  val uiState: AddonSummaryState by viewModel.uiState.collectAsStateWithLifecycle()
  AddonSummaryScreen(
    uiState = uiState,
    onSuccess = { date ->
      viewModel.emit(AddonSummaryEvent.ReturnToInitialState)
      onSuccess(date)
    },
    onFailure = {
      viewModel.emit(AddonSummaryEvent.ReturnToInitialState)
      onFailure()
    },
    navigateUp = navigateUp,
    onSubmitQuoteClick = {
      viewModel.emit(AddonSummaryEvent.Submit)
    },
  )
}

@Composable
private fun AddonSummaryScreen(
  uiState: AddonSummaryState,
  onSuccess: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  onFailure: () -> Unit,
  onSubmitQuoteClick: () -> Unit,
) {
  when (uiState) {
    Loading -> HedvigFullScreenCenterAlignedProgress()

    is Content -> {
      LaunchedEffect(uiState.navigateToFailure) {
        val fail = uiState.navigateToFailure
        if (fail) {
          onFailure()
        }
      }
      LaunchedEffect(uiState.activationDateForSuccessfullyPurchasedAddon) {
        val date = uiState.activationDateForSuccessfullyPurchasedAddon
        if (date != null) {
          onSuccess(date)
        }
      }

      SummarySuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        onConfirmClick = onSubmitQuoteClick,
      )
    }
  }
}

@Composable
private fun SummarySuccessScreen(uiState: Content, onConfirmClick: () -> Unit, navigateUp: () -> Unit) {
  HedvigScaffold(
    navigateUp,
    topAppBarText = stringResource(R.string.TIER_FLOW_SUMMARY_TITLE), // todo: change copy here mb?
  ) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
      HedvigAlertDialog(
        title = stringResource(R.string.ADDON_FLOW_CONFIRMATION_TITLE),
        onDismissRequest = { showConfirmationDialog = false },
        onConfirmClick = onConfirmClick,
        buttonSize = DialogDefaults.ButtonSize.BIG,
        confirmButtonLabel = stringResource(R.string.ADDON_FLOW_CONFIRMATION_BUTTON),
        dismissButtonLabel = stringResource(R.string.general_close_button),
        text = stringResource(
          R.string.ADDON_FLOW_CONFIRMATION_DESCRIPTION,
          uiState.summaryParameters.activationDate,
        ),
      )
    }
    SummaryCard(
      uiState = uiState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Column(
      Modifier
        .padding(horizontal = 16.dp),
    ) {
      HedvigNotificationCard(
        message = stringResource(R.string.ADDON_FLOW_SUMMARY_INFO_TEXT),
        priority = NotificationDefaults.NotificationPriority.Info,
      )
      Spacer(Modifier.height(40.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL), // todo: change copy prob?
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text = stringResource(
              R.string.ADDON_FLOW_PRICE_LABEL,
              uiState.summaryParameters.quote.price.amount.toInt(),
            ),
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        HedvigText(
          text = stringResource(R.string.ADDON_FLOW_SUMMARY_PRICE_SUBTITLE),
          textAlign = TextAlign.End,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(R.string.ADDON_FLOW_SUMMARY_CONFIRM_BUTTON),
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
private fun SummaryCard(uiState: Content, modifier: Modifier = Modifier) {
  QuoteCard(
    subtitle = stringResource(R.string.ADDON_FLOW_SUMMARY_ACTIVE_FROM, uiState.summaryParameters.activationDate),
    premium = uiState.summaryParameters.quote.price.toString(),
    displayItems = uiState.summaryParameters.quote.addonVariant.displayDetails.map {
      QuoteDisplayItem(
        it.first,
        null,
        it.second,
      )
    },
    underTitleContent = {
      // todo: check here!
    },
    modifier = modifier,
    displayName = uiState.summaryParameters.offerDisplayName,
    contractGroup = null,
    insurableLimits = emptyList(),
    documents = uiState.summaryParameters.quote.addonVariant.documents,
  )
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    ChooseInsuranceForAddonUiStateProvider::class,
  ) uiState: AddonSummaryState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddonSummaryScreen(
        uiState,
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceForAddonUiStateProvider :
  CollectionPreviewParameterProvider<AddonSummaryState>(
    listOf(
      Loading,
      Content(
        SummaryParameters(
          offerDisplayName = "TravelPlus",
          activationDate = LocalDate(2025, 1, 1),
          quote = TravelAddonQuote(
            displayName = "45 days",
            addonId = "addonId1",
            quoteId = "id",
            addonVariant = AddonVariant(
              termsVersion = "terms",
              displayDetails = listOf(
                "Amount of insured people" to "You +1",
                "Coverage" to "45 days",
              ),
              documents = listOf(
                InsuranceVariantDocument(
                  "Terms and Conditions",
                  "url",
                  InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
                ),
              ),
            ),
            price = UiMoney(
              60.0,
              UiCurrencyCode.SEK,
            ),
          ),
        ),
        null,
        false,
      ),
    ),
  )