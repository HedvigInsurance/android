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
import hedvig.resources.HOME_ADDONS_READ_MORE_BUTTON
import hedvig.resources.PAYMENT_SWISH_EXPLANATION_BUTTON
import hedvig.resources.Res
import hedvig.resources.SWISH_EXPLANATION_ITEM_1_TEXT
import hedvig.resources.SWISH_EXPLANATION_ITEM_1_TITLE
import hedvig.resources.SWISH_EXPLANATION_ITEM_2_TEXT
import hedvig.resources.SWISH_EXPLANATION_ITEM_2_TITLE
import hedvig.resources.SWISH_EXPLANATION_ITEM_3_TEXT
import hedvig.resources.SWISH_EXPLANATION_ITEM_3_TITLE
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecurringSwishExplanationBottomSheet(
  sheetState: HedvigBottomSheetState<Unit>,
  onLearnMore: (() -> Unit)? = null,
) {
  HedvigBottomSheet(sheetState) {
    RecurringSwishExplanationContent(
      onLearnMore = onLearnMore,
      onDismiss = { sheetState.dismiss() },
    )
  }
}

@Composable
private fun RecurringSwishExplanationContent(onLearnMore: (() -> Unit)? = null, onDismiss: () -> Unit) {
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
      title = stringResource(Res.string.SWISH_EXPLANATION_ITEM_1_TITLE),
      description = stringResource(Res.string.SWISH_EXPLANATION_ITEM_1_TEXT),
    )
    ExplanationItem(
      title = stringResource(Res.string.SWISH_EXPLANATION_ITEM_2_TITLE),
      description = stringResource(Res.string.SWISH_EXPLANATION_ITEM_2_TEXT),
    )
    ExplanationItem(
      title = stringResource(Res.string.SWISH_EXPLANATION_ITEM_3_TITLE),
      description = stringResource(Res.string.SWISH_EXPLANATION_ITEM_3_TEXT),
    )
  }
  Spacer(Modifier.height(16.dp))
  if (onLearnMore != null) {
    HedvigButton(
      text = stringResource(Res.string.HOME_ADDONS_READ_MORE_BUTTON),
      onClick = onLearnMore,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier.fillMaxWidth(),
    )
  }

  Spacer(Modifier.height(8.dp))
  HedvigButton(
    text = stringResource(Res.string.general_close_button),
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
