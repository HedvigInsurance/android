package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import hedvig.resources.PAYMENT_SWISH_EXPLANATION_BUTTON
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecurringSwishExplanationBottomSheet(sheetState: HedvigBottomSheetState<Unit>, onLearnMore: () -> Unit) {
  HedvigBottomSheet(sheetState) {
    RecurringSwishExplanationContent(
      onLearnMore = onLearnMore,
      onDismiss = { sheetState.dismiss() },
    )
  }
}

@Composable
private fun RecurringSwishExplanationContent(onLearnMore: () -> Unit, onDismiss: () -> Unit) {
  HedvigText(
    text = stringResource(Res.string.PAYMENT_SWISH_EXPLANATION_BUTTON),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 24.dp),
  )
  Column(
    verticalArrangement = Arrangement.spacedBy(24.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    ExplanationItem(
      // TODO: Add "Paid automatically" / "Betalas automatiskt" to Lokalise
      title = "Paid automatically",
      // TODO: Add "Your premium is then charged automatically each month, no monthly approval needed." /
      //  "Din premie dras sedan automatiskt varje månad, utan att du behöver godkänna varje gång." to Lokalise
      description = "Your premium is then charged automatically each month, no monthly approval needed.",
    )
    ExplanationItem(
      // TODO: Add "Get paid instantly" / "Få betalt direkt" to Lokalise
      title = "Get paid instantly",
      // TODO: Add "When a claim is approved, your compensation lands in your account right away via Swish." /
      //  "När en skadeanmälan godkänns landar din ersättning direkt på ditt konto via Swish." to Lokalise
      description = "When a claim is approved, your compensation lands in your account right away via Swish.",
    )
    ExplanationItem(
      // TODO: Add "Get notified" / "Få påminnelser" to Lokalise
      title = "Get notified",
      // TODO: Add "We'll send you a reminder a few days before each monthly payment." /
      //  "Vi skickar en påminnelse några dagar före varje månadsbetalning." to Lokalise
      description = "We'll send you a reminder a few days before each monthly payment.",
    )
    ExplanationItem(
      // TODO: Add "Change anytime" / "Ändra när du vill" to Lokalise
      title = "Change anytime",
      // TODO: Add "You can change or remove Swish anytime in Settings." /
      //  "Du kan ändra eller ta bort Swish när som helst i inställningarna." to Lokalise
      description = "You can change or remove Swish anytime in Settings.",
    )
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    // TODO: Add "Learn more" / "Läs mer" to Lokalise
    text = "Learn more",
    onClick = onLearnMore,
    enabled = true,
    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigButton(
    // TODO: Add "Got it" / "Okej" to Lokalise
    text = "Got it",
    onClick = onDismiss,
    enabled = true,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(32.dp))
}

@Composable
private fun ExplanationItem(title: String, description: String) {
  Column(Modifier.fillMaxWidth()) {
    HedvigText(title)
    HedvigText(text = description, color = HedvigTheme.colorScheme.textSecondary)
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewRecurringSwishExplanationContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        RecurringSwishExplanationContent(onLearnMore = {}, onDismiss = {})
      }
    }
  }
}
