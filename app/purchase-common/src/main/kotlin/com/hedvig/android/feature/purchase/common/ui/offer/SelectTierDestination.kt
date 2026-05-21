package com.hedvig.android.feature.purchase.common.ui.offer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun SelectTierDestination(
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
    onSelectTierInDialog = { viewModel.emit(SelectTierEvent.SelectTierInDialog(it)) },
    onConfirmTier = { viewModel.emit(SelectTierEvent.ConfirmTier) },
    onRevertTier = { viewModel.emit(SelectTierEvent.RevertTierToConfirmed) },
    onSelectDeductibleInDialog = { viewModel.emit(SelectTierEvent.SelectDeductibleInDialog(it)) },
    onConfirmDeductible = { viewModel.emit(SelectTierEvent.ConfirmDeductible) },
    onRevertDeductible = { viewModel.emit(SelectTierEvent.RevertDeductibleToConfirmed) },
    onContinue = { viewModel.emit(SelectTierEvent.Continue) },
  )
}

@Composable
private fun SelectTierContent(
  uiState: SelectTierUiState,
  navigateUp: () -> Unit = {},
  onSelectTierInDialog: (String) -> Unit = {},
  onConfirmTier: () -> Unit = {},
  onRevertTier: () -> Unit = {},
  onSelectDeductibleInDialog: (String) -> Unit = {},
  onConfirmDeductible: () -> Unit = {},
  onRevertDeductible: () -> Unit = {},
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
      text = "V\u00e4lj skyddsniv\u00e5 och sj\u00e4lvrisk",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    CustomizationCard(
      uiState = uiState,
      onSelectTierInDialog = onSelectTierInDialog,
      onConfirmTier = onConfirmTier,
      onRevertTier = onRevertTier,
      onSelectDeductibleInDialog = onSelectDeductibleInDialog,
      onConfirmDeductible = onConfirmDeductible,
      onRevertDeductible = onRevertDeductible,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = dropUnlessResumed { onContinue() },
      enabled = uiState.selectedTierName.isNotEmpty() && uiState.selectedDeductible != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizationCard(
  uiState: SelectTierUiState,
  onSelectTierInDialog: (String) -> Unit,
  onConfirmTier: () -> Unit,
  onRevertTier: () -> Unit,
  onSelectDeductibleInDialog: (String) -> Unit,
  onConfirmDeductible: () -> Unit,
  onRevertDeductible: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(
        shape = HedvigTheme.shapes.cornerXLarge,
        color = HedvigTheme.colorScheme.borderPrimary,
        width = 1.dp,
      ),
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(Modifier.padding(16.dp)) {
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        style = Label(
          label = "Skyddsniv\u00e5",
          items = uiState.tierGroups.map { SimpleDropdownItem(it.tierDisplayName) },
        ),
        size = Small,
        hintText = "V\u00e4lj skyddsniv\u00e5",
        chosenItemIndex = uiState.selectedTierIndex,
        onDoAlongWithDismissRequest = onRevertTier,
        containerColor = HedvigTheme.colorScheme.surfacePrimary,
      ) { onDismissRequest ->
        TierDialogContent(
          tierGroups = uiState.tierGroups,
          dialogTierName = uiState.dialogTierName,
          onSelectTierInDialog = onSelectTierInDialog,
          onConfirm = {
            onConfirmTier()
            onDismissRequest()
          },
          onCancel = onDismissRequest,
        )
      }
      if (uiState.currentDeductibleOptions.size > 1) {
        Spacer(Modifier.height(4.dp))
        DropdownWithDialog(
          dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
          style = Label(
            label = "Sj\u00e4lvrisk",
            items = uiState.currentDeductibleOptions.map { SimpleDropdownItem(it.deductibleDisplayName) },
          ),
          size = Small,
          hintText = "V\u00e4lj sj\u00e4lvrisk",
          chosenItemIndex = uiState.selectedDeductibleIndex,
          onDoAlongWithDismissRequest = onRevertDeductible,
          containerColor = HedvigTheme.colorScheme.surfacePrimary,
        ) { onDismissRequest ->
          DeductibleDialogContent(
            deductibleOptions = uiState.currentDeductibleOptions,
            dialogDeductibleId = uiState.dialogDeductibleId,
            onSelectDeductibleInDialog = onSelectDeductibleInDialog,
            onConfirm = {
              onConfirmDeductible()
              onDismissRequest()
            },
            onCancel = onDismissRequest,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      val selectedDeductible = uiState.selectedDeductible
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            "Totalt",
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text = if (selectedDeductible != null) {
              formatPrice(selectedDeductible.netAmount, selectedDeductible.netCurrencyCode)
            } else {
              "-"
            },
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
    }
  }
}

@Composable
private fun TierDialogContent(
  tierGroups: List<TierGroup>,
  dialogTierName: String?,
  onSelectTierInDialog: (String) -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  Column(
    Modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      "V\u00e4lj skyddsniv\u00e5",
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    HedvigText(
      "Skyddsniv\u00e5n avg\u00f6r vad din f\u00f6rs\u00e4kring t\u00e4cker",
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
    RadioGroup(
      options = tierGroups.map { group ->
        RadioOption(
          id = RadioOptionId(group.tierDisplayName),
          text = group.tierDisplayName,
          label = group.tierDescription,
        )
      },
      selectedOption = dialogTierName?.let { RadioOptionId(it) },
      onRadioOptionSelected = { onSelectTierInDialog(it.id) },
      style = RadioGroupStyle.LeftAligned,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = onConfirm,
      enabled = dialogTierName != null,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = "Avbryt",
      modifier = Modifier.fillMaxWidth(),
      buttonSize = ButtonSize.Large,
      onClick = onCancel,
    )
  }
}

@Composable
private fun DeductibleDialogContent(
  deductibleOptions: List<DeductibleOption>,
  dialogDeductibleId: String?,
  onSelectDeductibleInDialog: (String) -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  Column(
    Modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      "V\u00e4lj sj\u00e4lvrisk",
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    HedvigText(
      "En h\u00f6gre sj\u00e4lvrisk ger l\u00e4gre m\u00e5nadskostnad",
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
    RadioGroup(
      options = deductibleOptions.map { option ->
        RadioOption(
          id = RadioOptionId(option.offerId),
          text = option.deductibleDisplayName,
          label = formatPrice(option.netAmount, option.netCurrencyCode),
        )
      },
      selectedOption = dialogDeductibleId?.let { RadioOptionId(it) },
      onRadioOptionSelected = { onSelectDeductibleInDialog(it.id) },
      style = RadioGroupStyle.LeftAligned,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = onConfirm,
      enabled = dialogDeductibleId != null,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = "Avbryt",
      modifier = Modifier.fillMaxWidth(),
      buttonSize = ButtonSize.Large,
      onClick = onCancel,
    )
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
        dialogTierName = null,
        dialogDeductibleId = null,
        shopSessionId = "session",
        productDisplayName = "Hemf\u00f6rs\u00e4kring Hyresr\u00e4tt",
        summaryToNavigate = null,
      ),
    )
  }
}
