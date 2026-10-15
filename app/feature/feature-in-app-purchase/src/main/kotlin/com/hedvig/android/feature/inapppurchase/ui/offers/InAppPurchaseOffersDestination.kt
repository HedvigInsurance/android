package com.hedvig.android.feature.inapppurchase.ui.offers

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COVERAGE_LABEL
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppPurchaseOffersDestination(viewModel: InAppPurchaseOffersViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InAppPurchaseOffersScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onSelectOffer = { viewModel.emit(InAppPurchaseOffersEvent.SelectOffer(it.id)) },
    onContinue = { viewModel.emit(InAppPurchaseOffersEvent.Continue) },
  )
}

@Composable
private fun InAppPurchaseOffersScreen(
  uiState: InAppPurchaseOffersUiState,
  navigateUp: () -> Unit,
  onSelectOffer: (InAppPurchaseOffer) -> Unit,
  onContinue: () -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp) {
    FlowHeading(
      // TODO: Add "Choose your coverage" / "Välj ditt skydd" to Lokalise
      title = "Choose your coverage",
      description = uiState.productDisplayName,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    OfferCard(
      offers = uiState.offers,
      selectedOffer = uiState.selectedOffer,
      onSelectOffer = onSelectOffer,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = onContinue,
      enabled = true,
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun OfferCard(
  offers: List<InAppPurchaseOffer>,
  selectedOffer: InAppPurchaseOffer,
  onSelectOffer: (InAppPurchaseOffer) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(shape = HedvigTheme.shapes.cornerXLarge, color = HedvigTheme.colorScheme.borderPrimary, width = 1.dp),
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(selectedOffer.variant.contractGroup.pillowResource()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
          HedvigText(selectedOffer.variant.displayName, style = HedvigTheme.typography.headlineSmall)
          HedvigText(
            text = selectedOffer.exposureDisplayName,
            style = HedvigTheme.typography.bodySmall,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      val label = selectedOffer.insuranceAmount?.displayName ?: stringResource(Res.string.TIER_FLOW_COVERAGE_LABEL)
      val selectedIndex = offers.indexOf(selectedOffer).takeIf { it >= 0 }
      DropdownWithDialog(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        style = Label(label = label, items = offers.map { SimpleDropdownItem(it.optionTitle()) }),
        size = Small,
        hintText = label,
        chosenItemIndex = selectedIndex,
        isEnabled = offers.size > 1,
        containerColor = HedvigTheme.colorScheme.surfacePrimary,
        modifier = Modifier.accessibilityForDropdown(
          labelText = label,
          selectedValue = selectedOffer.optionTitle(),
          isEnabled = offers.size > 1,
        ),
      ) { onDismissRequest ->
        OfferDialogContent(
          title = label,
          offers = offers,
          initiallySelectedOffer = selectedOffer,
          onConfirm = { offer ->
            onSelectOffer(offer)
            onDismissRequest()
          },
          onCancel = onDismissRequest,
        )
      }
      Spacer(Modifier.height(32.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(Res.string.TIER_FLOW_TOTAL), style = HedvigTheme.typography.bodySmall)
        },
        spaceBetween = 8.dp,
        endSlot = {
          Row(horizontalArrangement = Arrangement.End) {
            val cost = selectedOffer.cost
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
    }
  }
}

@Composable
private fun OfferDialogContent(
  title: String,
  offers: List<InAppPurchaseOffer>,
  initiallySelectedOffer: InAppPurchaseOffer,
  onConfirm: (InAppPurchaseOffer) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedInDialog by remember { mutableStateOf(initiallySelectedOffer) }
  Column(modifier.verticalScroll(rememberScrollState())) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = title,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    Spacer(Modifier.height(24.dp))
    RadioGroup(
      options = offers.map { RadioOption(id = RadioOptionId(it.id), text = it.optionTitle()) },
      selectedOption = RadioOptionId(selectedInDialog.id),
      onRadioOptionSelected = { id -> selectedInDialog = offers.first { it.id == id.id } },
      style = RadioGroupStyle.LeftAligned,
      textEndContent = { id ->
        val offer = offers.first { it.id == id.id }
        HighlightLabel(
          labelText = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, offer.cost.monthlyNet),
          size = HighLightSize.Small,
          color = HighlightColor.Grey(MEDIUM),
          modifier = Modifier.wrapContentSize(Alignment.TopEnd),
        )
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = { onConfirm(selectedInDialog) },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = onCancel,
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

private fun InAppPurchaseOffer.optionTitle(): String = variant.displayTierName ?: exposureDisplayName

@HedvigMultiScreenPreview
@Composable
private fun PreviewInAppPurchaseOffersScreen() {
  val offers = InAppPurchaseMocks.paymentProtectionQuote().offers
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InAppPurchaseOffersScreen(
        uiState = InAppPurchaseOffersUiState("Payment Protection", offers, offers.first()),
        navigateUp = {},
        onSelectOffer = {},
        onContinue = {},
      )
    }
  }
}
