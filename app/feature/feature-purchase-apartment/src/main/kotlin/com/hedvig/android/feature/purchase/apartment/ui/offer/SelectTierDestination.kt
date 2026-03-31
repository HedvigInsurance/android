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
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
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
    onSelectTier = { viewModel.emit(SelectTierEvent.SelectTier(it)) },
    onSelectDeductible = { tierName, offerId ->
      viewModel.emit(SelectTierEvent.SelectDeductible(tierName, offerId))
    },
    onContinue = { viewModel.emit(SelectTierEvent.Continue) },
  )
}

@Composable
private fun SelectTierContent(
  uiState: SelectTierUiState,
  navigateUp: () -> Unit = {},
  onSelectTier: (String) -> Unit = {},
  onSelectDeductible: (tierName: String, offerId: String) -> Unit = { _, _ -> },
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
    for ((index, tierGroup) in uiState.tierGroups.withIndex()) {
      val isSelected = tierGroup.tierDisplayName == uiState.selectedTierName
      val selectedDeductibleId = uiState.selectedDeductibleByTier[tierGroup.tierDisplayName]
      val selectedDeductible = tierGroup.deductibleOptions.firstOrNull { it.offerId == selectedDeductibleId }
        ?: tierGroup.deductibleOptions.firstOrNull()
      TierGroupCard(
        tierGroup = tierGroup,
        isSelected = isSelected,
        selectedDeductibleId = selectedDeductible?.offerId ?: "",
        onSelectTier = { onSelectTier(tierGroup.tierDisplayName) },
        onSelectDeductible = { offerId -> onSelectDeductible(tierGroup.tierDisplayName, offerId) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      if (index < uiState.tierGroups.lastIndex) {
        Spacer(Modifier.height(12.dp))
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = dropUnlessResumed { onContinue() },
      enabled = uiState.selectedTierName.isNotEmpty(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TierGroupCard(
  tierGroup: TierGroup,
  isSelected: Boolean,
  selectedDeductibleId: String,
  onSelectTier: () -> Unit,
  onSelectDeductible: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedOption = tierGroup.deductibleOptions.firstOrNull { it.offerId == selectedDeductibleId }
  HedvigCard(
    onClick = onSelectTier,
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
          text = tierGroup.tierDisplayName,
          style = HedvigTheme.typography.bodyLarge,
        )
        if (selectedOption != null) {
          HedvigText(
            text = formatPrice(selectedOption.netAmount, selectedOption.netCurrencyCode),
            style = HedvigTheme.typography.bodyLarge,
          )
        }
      }
      AnimatedVisibility(
        visible = isSelected,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          if (tierGroup.usps.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            for (usp in tierGroup.usps) {
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
          if (tierGroup.deductibleOptions.size > 1) {
            Spacer(Modifier.height(12.dp))
            HedvigText(
              text = "Sj\u00e4lvrisk",
              style = HedvigTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            RadioGroup(
              options = tierGroup.deductibleOptions.map { option ->
                RadioOption(
                  id = RadioOptionId(option.offerId),
                  text = option.deductibleDisplayName,
                  label = formatPrice(option.netAmount, option.netCurrencyCode),
                )
              },
              selectedOption = RadioOptionId(selectedDeductibleId),
              onRadioOptionSelected = { onSelectDeductible(it.id) },
              size = RadioGroupSize.Small,
            )
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
            text = "V\u00e4lj ${tierGroup.tierDisplayName}",
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

private val previewTierGroups = listOf(
  TierGroup(
    tierDisplayName = "Hem Max",
    tierDescription = "V\u00e5rt mest omfattande skydd",
    usps = listOf(
      "F\u00f6rs\u00e4kringsbelopp 1 000 000 kr",
      "Drulle upp till 50 000 kr ing\u00e5r",
      "ID-skydd och flyttskydd",
    ),
    deductibleOptions = listOf(
      DeductibleOption("1a", "1 500 kr", 189.0, "SEK", 189.0, "SEK", false),
      DeductibleOption("1b", "3 000 kr", 169.0, "SEK", 169.0, "SEK", false),
      DeductibleOption("1c", "5 000 kr", 149.0, "SEK", 149.0, "SEK", false),
    ),
  ),
  TierGroup(
    tierDisplayName = "Hem Standard",
    tierDescription = "V\u00e5r mest popul\u00e4ra f\u00f6rs\u00e4kring",
    usps = listOf(
      "F\u00f6rs\u00e4kringsbelopp 1 000 000 kr",
      "Drulle upp till 50 000 kr ing\u00e5r",
    ),
    deductibleOptions = listOf(
      DeductibleOption("2a", "1 500 kr", 139.0, "SEK", 118.0, "SEK", true),
      DeductibleOption("2b", "3 000 kr", 119.0, "SEK", 99.0, "SEK", true),
      DeductibleOption("2c", "5 000 kr", 99.0, "SEK", 85.0, "SEK", true),
    ),
  ),
  TierGroup(
    tierDisplayName = "Hem Bas",
    tierDescription = "Inneh\u00e5ller v\u00e5rt grundskydd",
    usps = listOf("Grundskydd"),
    deductibleOptions = listOf(
      DeductibleOption("3a", "1 500 kr", 99.0, "SEK", 99.0, "SEK", false),
      DeductibleOption("3b", "3 000 kr", 79.0, "SEK", 79.0, "SEK", false),
      DeductibleOption("3c", "5 000 kr", 65.0, "SEK", 65.0, "SEK", false),
    ),
  ),
)

@HedvigPreview
@Composable
private fun PreviewSelectTierStandard() {
  HedvigTheme {
    SelectTierContent(
      uiState = SelectTierUiState(
        tierGroups = previewTierGroups,
        selectedTierName = "Hem Standard",
        selectedDeductibleByTier = mapOf(
          "Hem Max" to "1c",
          "Hem Standard" to "2a",
          "Hem Bas" to "3c",
        ),
        shopSessionId = "session",
        productDisplayName = "Hemf\u00f6rs\u00e4kring Hyresr\u00e4tt",
        summaryToNavigate = null,
      ),
    )
  }
}
