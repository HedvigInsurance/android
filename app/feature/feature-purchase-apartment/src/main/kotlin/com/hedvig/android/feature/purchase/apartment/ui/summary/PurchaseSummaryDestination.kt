package com.hedvig.android.feature.purchase.apartment.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.TierOfferData

@Composable
internal fun PurchaseSummaryDestination(
  viewModel: PurchaseSummaryViewModel,
  navigateUp: () -> Unit,
  navigateToSigning: (SigningParameters) -> Unit,
  navigateToFailure: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState.signingToNavigate) {
    val signing = uiState.signingToNavigate ?: return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    navigateToSigning(signing)
  }

  LaunchedEffect(uiState.navigateToFailure) {
    if (!uiState.navigateToFailure) return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    navigateToFailure()
  }

  PurchaseSummaryScreen(
    params = uiState.params,
    isSubmitting = uiState.isSubmitting,
    navigateUp = navigateUp,
    onConfirm = { viewModel.emit(PurchaseSummaryEvent.Confirm) },
  )
}

@Composable
private fun PurchaseSummaryScreen(
  params: SummaryParameters,
  isSubmitting: Boolean,
  navigateUp: () -> Unit,
  onConfirm: () -> Unit,
) {
  HedvigScaffold(navigateUp) {
    val offer = params.selectedOffer
    HedvigCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Column(modifier = Modifier.padding(16.dp)) {
        HedvigText(
          text = params.productDisplayName,
          style = HedvigTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(4.dp))
        HedvigText(
          text = offer.tierDisplayName,
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = offer.exposureDisplayName,
          style = HedvigTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(
              text = "Pris",
              style = HedvigTheme.typography.bodySmall,
            )
          },
          spaceBetween = 8.dp,
          endSlot = {
            HedvigText(
              text = "${offer.netAmount} ${offer.netCurrencyCode}/mån",
              style = HedvigTheme.typography.bodySmall,
            )
          },
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Signera med BankID",
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      buttonStyle = Primary,
      buttonSize = Large,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      onClick = onConfirm,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewPurchaseSummary() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PurchaseSummaryScreen(
        params = SummaryParameters(
          shopSessionId = "session",
          selectedOffer = TierOfferData(
            offerId = "1",
            tierDisplayName = "Hem Standard",
            tierDescription = "Vår mest populära försäkring",
            grossAmount = 139.0,
            grossCurrencyCode = "SEK",
            netAmount = 118.0,
            netCurrencyCode = "SEK",
            usps = emptyList(),
            exposureDisplayName = "Storgatan 1",
            deductibleDisplayName = "1 500 kr",
            hasDiscount = true,
          ),
          productDisplayName = "Hemförsäkring Hyresrätt",
        ),
        isSubmitting = false,
        navigateUp = {},
        onConfirm = {},
      )
    }
  }
}
