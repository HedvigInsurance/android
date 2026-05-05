package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.animationSize
import com.hedvig.feature.claim.chat.ui.common.HelipadRiveAnimation
import hedvig.resources.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaskStepTopContent(
  taskContent: StepContent.Task,
  stepId: String,
  isLastStep: Boolean,
  modifier: Modifier = Modifier,
) {
  val taskContentDescription = stringResource(Res.string.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION)
  Column(
    modifier.clearAndSetSemantics {
      if (isLastStep) {
        contentDescription = taskContentDescription
      }
      if (taskContent.failedToSubmit) {
        hideFromAccessibility()
      }
    },
  ) {
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val showBlinkingAiDot = !taskContent.failedToSubmit
        val lastDescription = taskContent.descriptions.lastOrNull()
        val showPill = lastDescription != null
        AnimatedContent(showBlinkingAiDot) { show ->
          val density = LocalDensity.current
          if (show) {
            HelipadRiveAnimation(
              bottomAnimationFinished = taskContent.isAnimationFinished,
              modifier = Modifier.size(
                with(density) {
                  animationSize.toDp()
                },
              ),
              stepId = stepId,
            )
          }
        }
        if (taskContent.descriptions.isNotEmpty() && isLastStep) {
          AnimatedContent(showBlinkingAiDot && showPill) {
            Spacer(Modifier.width(8.dp))
          }
          AnimatedContent(lastDescription) { description ->
            if (description != null) {
              HedvigText(
                description,
                color = HedvigTheme.colorScheme.textSecondary,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun TaskStepBottomContent(
  taskContent: StepContent.Task,
  onRetrySubmittingTask: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    if (taskContent.failedToSubmit) {
      HedvigButton(
        text = stringResource(Res.string.GENERAL_RETRY),
        enabled = true,
        onClick = {
          onRetrySubmittingTask()
        },
        isLoading = false,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      )
    }
  }
}
