package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.CLAIM_CHAT_SKIPPED_STEP
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SkippedLabel(modifier: Modifier = Modifier) {
  val skippedLabelText = stringResource(Res.string.CLAIM_CHAT_SKIPPED_STEP)
  RoundCornersPill(modifier) {
    HedvigText(
      skippedLabelText,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
  }
}
