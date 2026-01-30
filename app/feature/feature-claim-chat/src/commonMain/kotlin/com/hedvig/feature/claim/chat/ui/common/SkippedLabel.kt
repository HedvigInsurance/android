package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.CLAIM_CHAT_SKIPPED_STEP
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SkippedLabel() {
  val skippedLabelText = stringResource(Res.string.CLAIM_CHAT_SKIPPED_STEP)
  RoundCornersPill(
    onClick = null,
    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
  ) {
    HedvigText(
      skippedLabelText,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
  }
}
