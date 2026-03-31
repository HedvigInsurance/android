package com.hedvig.android.feature.purchase.apartment.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.TierOfferData
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
internal fun SelectTierDestination(
  viewModel: SelectTierViewModel,
  navigateUp: () -> Unit,
  onContinueToSummary: (SummaryParameters) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState.summaryToNavigate != null) {
    LaunchedEffect(uiState.summaryToNavigate) {
      viewModel.emit(SelectTierEvent.ClearNavigation)
      onContinueToSummary(uiState.summaryToNavigate)
    }
  }
  SelectTierContent(
    uiState = uiState,
    navigateUp = navigateUp,
    onSelectOffer = { viewModel.emit(SelectTierEvent.SelectOffer(it)) },
    onContinue = { viewModel.emit(SelectTierEvent.Continue) },
  )
}

@Composable
private fun SelectTierContent(
  uiState: SelectTierUiState,
  navigateUp: () -> Unit = {},
  onSelectOffer: (String) -> Unit = {},
  onContinue: () -> Unit = {},
) {
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = "Anpassa din f\u00f6rs\u00e4kring",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    HedvigText(
      text = "V\u00e4lj den skyddsniv\u00e5 som passar dig b\u00e4st",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    for ((index, offer) in uiState.offers.withIndex()) {
      TierCard(
        offer = offer,
        isSelected = offer.offerId == uiState.selectedOfferId,
        onSelect = { onSelectOffer(offer.offerId) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      if (index < uiState.offers.lastIndex) {
        Spacer(Modifier.height(12.dp))
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = dropUnlessResumed { onContinue() },
      enabled = uiState.offers.any { it.offerId == uiState.selectedOfferId },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TierCard(
  offer: TierOfferData,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onSelect,
    borderColor = if (isSelected) {
      HedvigTheme.colorScheme.signalGreenElement
    } else {
      HedvigTheme.colorScheme.borderSecondary
    },
    modifier = modifier,
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = offer.tierDisplayName,
          style = HedvigTheme.typography.bodyLarge,
        )
        HedvigText(
          text = formatPrice(offer.netAmount, offer.netCurrencyCode),
          style = HedvigTheme.typography.bodyLarge,
        )
      }
      AnimatedVisibility(
        visible = isSelected,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          if (offer.usps.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            for (usp in offer.usps) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
              ) {
                Icon(
                  HedvigIcons.Checkmark,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = HedvigTheme.colorScheme.signalGreenElement,
                )
                Spacer(Modifier.width(8.dp))
                HedvigText(
                  text = usp,
                  style = HedvigTheme.typography.bodyMedium,
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              }
            }
          }
        }
      }
      AnimatedVisibility(
        visible = !isSelected,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          Spacer(Modifier.height(8.dp))
          HedvigText(
            text = "V\u00e4lj ${offer.tierDisplayName}",
            style = HedvigTheme.typography.bodyMedium,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
    }
  }
}

private fun formatPrice(amount: Double, currencyCode: String): String {
  @Suppress("DEPRECATION")
  val format = NumberFormat.getCurrencyInstance(Locale("sv", "SE"))
  format.currency = Currency.getInstance(currencyCode)
  format.maximumFractionDigits = 0
  return "${format.format(amount)}/m\u00e5n"
}

private val previewOffers = listOf(
  TierOfferData(
    offerId = "1",
    tierDisplayName = "Hem Max",
    tierDescription = "Vårt mest omfattande skydd",
    grossAmount = 189.0,
    grossCurrencyCode = "SEK",
    netAmount = 189.0,
    netCurrencyCode = "SEK",
    usps = listOf("Försäkringsbelopp 1 000 000 kr", "Drulle upp till 50 000 kr ingår", "ID-skydd och flyttskydd"),
    exposureDisplayName = "Storgatan 1",
    deductibleDisplayName = "1 500 kr",
    hasDiscount = false,
  ),
  TierOfferData(
    offerId = "2",
    tierDisplayName = "Hem Standard",
    tierDescription = "Vår mest populära försäkring",
    grossAmount = 139.0,
    grossCurrencyCode = "SEK",
    netAmount = 118.0,
    netCurrencyCode = "SEK",
    usps = listOf("Försäkringsbelopp 1 000 000 kr", "Drulle upp till 50 000 kr ingår"),
    exposureDisplayName = "Storgatan 1",
    deductibleDisplayName = "1 500 kr",
    hasDiscount = true,
  ),
  TierOfferData(
    offerId = "3",
    tierDisplayName = "Hem Bas",
    tierDescription = "Innehåller vårt grundskydd",
    grossAmount = 99.0,
    grossCurrencyCode = "SEK",
    netAmount = 99.0,
    netCurrencyCode = "SEK",
    usps = listOf("Grundskydd"),
    exposureDisplayName = "Storgatan 1",
    deductibleDisplayName = "1 500 kr",
    hasDiscount = false,
  ),
)

@HedvigPreview
@Composable
private fun PreviewSelectTierStandard() {
  HedvigTheme {
    SelectTierContent(
      uiState = SelectTierUiState(
        offers = previewOffers,
        selectedOfferId = "2",
        shopSessionId = "session",
        productDisplayName = "Hemförsäkring Hyresrätt",
        summaryToNavigate = null
      ),
      onSelectOffer = {},
      onContinue = {},
    )
  }
}
