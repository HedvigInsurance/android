package com.hedvig.android.feature.purchase.apartment.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
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
  SelectTierScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onSelectOffer = { viewModel.emit(SelectTierEvent.SelectOffer(it)) },
    onContinue = { viewModel.emit(SelectTierEvent.Continue) },
  )
}

@Composable
private fun SelectTierScreen(
  uiState: SelectTierUiState,
  navigateUp: () -> Unit,
  onSelectOffer: (String) -> Unit,
  onContinue: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      TopAppBarWithBack(
        title = "",
        onClick = navigateUp,
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
      ) {
        Spacer(Modifier.height(16.dp))
        HedvigText(
          text = "Anpassa din f\u00f6rs\u00e4kring",
          style = HedvigTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(4.dp))
        HedvigText(
          text = "V\u00e4lj den skyddsniv\u00e5 som passar dig b\u00e4st",
          style = HedvigTheme.typography.bodyMedium,
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(Modifier.height(24.dp))
        for ((index, offer) in uiState.offers.withIndex()) {
          val isSelected = offer.offerId == uiState.selectedOfferId
          TierCard(
            offer = offer,
            isSelected = isSelected,
            onSelect = { onSelectOffer(offer.offerId) },
          )
          if (index < uiState.offers.lastIndex) {
            Spacer(Modifier.height(12.dp))
          }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        HedvigButton(
          text = "Forts\u00e4tt",
          onClick = dropUnlessResumed { onContinue() },
          enabled = uiState.offers.any { it.offerId == uiState.selectedOfferId },
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

@Composable
private fun TierCard(
  offer: TierOfferData,
  isSelected: Boolean,
  onSelect: () -> Unit,
) {
  HedvigCard(
    onClick = onSelect,
    color = if (isSelected) {
      HedvigTheme.colorScheme.surfacePrimary
    } else {
      HedvigTheme.colorScheme.surfacePrimary
    },
    borderColor = if (isSelected) {
      HedvigTheme.colorScheme.signalGreenElement
    } else {
      HedvigTheme.colorScheme.borderSecondary
    },
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
  val format = NumberFormat.getCurrencyInstance(Locale.of("sv", "SE"))
  format.currency = Currency.getInstance(currencyCode)
  format.maximumFractionDigits = 0
  return "${format.format(amount)}/m\u00e5n"
}
