package com.hedvig.android.feature.change.mileage.ui.start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.change.mileage.data.MileageContract
import com.hedvig.android.feature.change.mileage.data.MileageDisplayItem
import com.hedvig.android.feature.change.mileage.data.MileageQuote
import com.hedvig.android.feature.change.mileage.ui.ContractHeader
import com.hedvig.android.feature.change.mileage.ui.MileageCard
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_PREVIOUS_PRICE
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.general_cancel_button
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangeMileageDestination(
  viewModel: ChangeMileageViewModel,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
) {
  val uiState: ChangeMileageUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChangeMileageScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackstack = popBackstack,
    reload = { viewModel.emit(ChangeMileageEvent.Reload) },
    onSelectQuote = { viewModel.emit(ChangeMileageEvent.SelectQuote(it)) },
    onContinue = { viewModel.emit(ChangeMileageEvent.Continue) },
  )
}

@Composable
private fun ChangeMileageScreen(
  uiState: ChangeMileageUiState,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  reload: () -> Unit,
  onSelectQuote: (quoteId: String) -> Unit,
  onContinue: () -> Unit,
) {
  when (uiState) {
    ChangeMileageUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    ChangeMileageUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is ChangeMileageUiState.UserError -> {
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = uiState.message,
          description = null,
          iconStyle = INFO,
          buttonStyle = NoButton,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        HedvigTextButton(
          text = stringResource(Res.string.general_close_button),
          onClick = popBackstack,
          buttonSize = Large,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }

    is ChangeMileageUiState.Success -> {
      ChangeMileageSuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        popBackstack = popBackstack,
        onSelectQuote = onSelectQuote,
        onContinue = onContinue,
      )
    }
  }
}

@Composable
private fun ChangeMileageSuccessScreen(
  uiState: ChangeMileageUiState.Success,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  onSelectQuote: (quoteId: String) -> Unit,
  onContinue: () -> Unit,
) {
  val howFarDoIDriveSheetState = rememberHedvigBottomSheetState<Unit>()
  HedvigBottomSheet(howFarDoIDriveSheetState) {
    // TODO: Add "How far do I drive?" / "Hur långt kör jag?" to Lokalise
    HedvigText(
      text = "Hur långt kör jag?",
      modifier = Modifier.semantics { heading() },
    )
    // TODO: Add the explanation copy for "How far do I drive?" to Lokalise once product has provided it
    HedvigText(
      text = "Räkna med all körning under ett år, av alla som kör bilen. " +
        "Jämför gärna mätarställningen med förra årets besiktning.",
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(24.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_close_button),
      buttonSize = Large,
      onClick = { howFarDoIDriveSheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = popBackstack,
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(Res.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(Modifier.height(8.dp))
    // TODO: Add "Change mileage" / "Ändra körsträcka" to Lokalise
    HedvigText(
      text = "Ändra körsträcka",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    // TODO: Add "Choose how far the car is driven per year" / "Välj hur långt bilen körs per år" to Lokalise
    HedvigText(
      text = "Välj hur långt bilen körs per år",
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    MileageCustomizationCard(
      uiState = uiState,
      onSelectQuote = onSelectQuote,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      buttonSize = Large,
      enabled = !uiState.isCurrentSelected,
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      // TODO: Add "How far do I drive?" / "Hur långt kör jag?" to Lokalise
      text = "Hur långt kör jag?",
      buttonSize = Large,
      onClick = { howFarDoIDriveSheetState.show() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun MileageCustomizationCard(
  uiState: ChangeMileageUiState.Success,
  onSelectQuote: (quoteId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  MileageCard(modifier) {
    ContractHeader(
      contractGroup = uiState.contract.contractGroup,
      displayName = uiState.contract.displayName,
      subtitle = uiState.contract.exposureDisplayName,
    )
    Spacer(Modifier.height(16.dp))
    // TODO: Add "Mileage per year" / "Körsträcka per år" to Lokalise
    val label = "Körsträcka per år"
    DropdownWithDialog(
      dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
      style = Label(
        label = label,
        items = uiState.quotes.map { SimpleDropdownItem(it.displayName) },
      ),
      size = Small,
      hintText = label,
      chosenItemIndex = uiState.quotes.indexOf(uiState.selectedQuote),
      containerColor = HedvigTheme.colorScheme.surfacePrimary,
      modifier = Modifier.accessibilityForDropdown(
        labelText = label,
        selectedValue = uiState.selectedQuote.displayName,
        isEnabled = true,
      ),
    ) { onDismissRequest ->
      MileagePickerDialogContent(
        quotes = uiState.quotes,
        initiallySelectedQuoteId = uiState.selectedQuoteId,
        onConfirm = { quoteId ->
          onSelectQuote(quoteId)
          onDismissRequest()
        },
        onCancel = onDismissRequest,
      )
    }
    if (uiState.isLowerThanCurrentSelected) {
      Spacer(Modifier.height(4.dp))
      HedvigNotificationCard(
        // TODO: Add "If the car is driven further than you have chosen, the compensation can be lower in case of damage. You can change the mileage whenever you want." / "Om bilen körs längre än du har valt kan ersättningen bli lägre vid en skada. Du kan ändra körsträckan när du vill." to Lokalise
        message = "Om bilen körs längre än du har valt kan ersättningen bli lägre vid en skada. " +
          "Du kan ändra körsträckan när du vill.",
        priority = NotificationDefaults.NotificationPriority.Info,
        style = NotificationDefaults.InfoCardStyle.Default,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    val newPrice = uiState.selectedQuote.totalCost.monthlyNet
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(
          text = stringResource(Res.string.TIER_FLOW_TOTAL),
          style = HedvigTheme.typography.bodySmall,
        )
      },
      spaceBetween = 8.dp,
      endSlot = {
        val description = newPrice.getPerMonthDescription()
        HedvigText(
          text = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, newPrice),
          textAlign = TextAlign.End,
          style = HedvigTheme.typography.bodySmall,
          modifier = Modifier.semantics { contentDescription = description },
        )
      },
    )
    if (!uiState.isCurrentSelected) {
      val previousPrice = uiState.currentTotalCost.monthlyNet
      val voiceDescription = stringResource(Res.string.TIER_FLOW_PREVIOUS_PRICE, previousPrice.getPerMonthDescription())
      HedvigText(
        text = stringResource(
          Res.string.TIER_FLOW_PREVIOUS_PRICE,
          stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, previousPrice),
        ),
        textAlign = TextAlign.End,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier
          .fillMaxWidth()
          .semantics { contentDescription = voiceDescription },
      )
    }
  }
}

@Composable
private fun MileagePickerDialogContent(
  quotes: List<MileageQuote>,
  initiallySelectedQuoteId: String,
  onConfirm: (quoteId: String) -> Unit,
  onCancel: () -> Unit,
) {
  var selectedQuoteId by rememberSaveable { mutableStateOf(initiallySelectedQuoteId) }
  Column(Modifier.verticalScroll(rememberScrollState())) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Choose mileage" / "Välj körsträcka" to Lokalise
      text = "Välj körsträcka",
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    HedvigText(
      // TODO: Add "How far the car is driven per year, by everyone who drives it" / "Hur långt bilen körs per år, av alla som kör den" to Lokalise
      text = "Hur långt bilen körs per år, av alla som kör den",
      color = HedvigTheme.colorScheme.textSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    // TODO: Add "Current" / "Nuvarande" to Lokalise
    val currentLabel = "Nuvarande"
    RadioGroup(
      options = quotes.map { quote ->
        RadioOption(
          id = RadioOptionId(quote.id),
          text = quote.displayName,
          label = currentLabel.takeIf { quote.isCurrent },
        )
      },
      selectedOption = RadioOptionId(selectedQuoteId),
      onRadioOptionSelected = { selectedQuoteId = it.id },
      style = RadioGroupStyle.LeftAligned,
      textEndContent = { id ->
        val quote = quotes.first { it.id == id.id }
        HighlightLabel(
          labelText = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, quote.totalCost.monthlyNet),
          size = HighLightSize.Small,
          color = HighlightColor.Grey(MEDIUM),
          modifier = Modifier.wrapContentSize(Alignment.TopEnd),
        )
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.GENERAL_CONFIRM),
      enabled = true,
      onClick = { onConfirm(selectedQuoteId) },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      buttonSize = Large,
      onClick = onCancel,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewChangeMileageScreen(
  @PreviewParameter(ChangeMileageUiStateProvider::class) uiState: ChangeMileageUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChangeMileageScreen(uiState, {}, {}, {}, {}, {})
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewMileagePickerDialogContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MileagePickerDialogContent(previewQuotes, previewQuotes[0].id, {}, {})
    }
  }
}

private class ChangeMileageUiStateProvider : CollectionPreviewParameterProvider<ChangeMileageUiState>(
  listOf(
    previewSuccessState.copy(selectedQuoteId = previewQuotes[1].id),
    previewSuccessState.copy(selectedQuoteId = previewQuotes[0].id),
    previewSuccessState.copy(selectedQuoteId = previewQuotes[2].id),
    ChangeMileageUiState.Loading,
    ChangeMileageUiState.Failure,
    ChangeMileageUiState.UserError("Körsträckan kan inte ändras för den här bilen just nu."),
  ),
)

private fun previewCost(amount: Double): ItemCost {
  val money = UiMoney(amount, UiCurrencyCode.SEK)
  return ItemCost(monthlyNet = money, monthlyGross = money, discounts = emptyList())
}

internal val previewQuotes: List<MileageQuote> = listOf(
  "1 000 mil" to 371.0,
  "1 500 mil" to 412.0,
  "2 000 mil" to 449.0,
  "2 500 mil" to 486.0,
  "Över 2 500 mil" to 529.0,
).mapIndexed { index, (name, price) ->
  MileageQuote(
    id = "$index",
    displayName = name,
    isCurrent = index == 1,
    totalCost = previewCost(price),
    displayItems = listOf(
      MileageDisplayItem("Körsträcka per år", name, if (index == 1) null else "1 500 mil"),
      MileageDisplayItem("Registreringsnummer", "ABC 123", null),
      MileageDisplayItem("Skyddsnivå", "Helförsäkring", null),
    ),
  )
}

private val previewSuccessState = ChangeMileageUiState.Success(
  contract = MileageContract(
    id = "contractId",
    displayName = "Bilförsäkring Helförsäkring",
    exposureDisplayName = "ABC 123 • Volvo XC40",
    contractGroup = ContractGroup.CAR,
  ),
  activationDate = LocalDate(2026, 11, 17),
  currentTotalCost = previewCost(412.0),
  quotes = previewQuotes,
  selectedQuoteId = previewQuotes[1].id,
)
