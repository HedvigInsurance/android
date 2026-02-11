package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.common.BlinkingAiDot
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import hedvig.resources.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaskStep(taskContent: StepContent.Task, modifier: Modifier = Modifier) {
  val taskContentDescription = stringResource(Res.string.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION)
  Column(
    modifier.clearAndSetSemantics { contentDescription = taskContentDescription },
  ) {
    if (taskContent.descriptions.isNotEmpty()) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BlinkingAiDot()
          if (taskContent.descriptions.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            AnimatedContent(taskContent.descriptions.last()) { target ->
              RoundCornersPill(
                onClick = null,
              ) {
                HedvigText(target)
              }
            }
          }
        }
      }
    }
  }
}
