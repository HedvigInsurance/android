package com.hedvig.android.feature.claim.chat.ui.step

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.feature.claim.chat.ClaimChatEvent
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import com.hedvig.android.feature.claim.chat.ui.common.ContentSelectChips
import com.hedvig.android.feature.claim.chat.ui.common.EditButton
import com.hedvig.android.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.android.feature.claim.chat.ui.common.SkippedLabel
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.Res
import hedvig.resources.TALKBACK_CLAIM_CHAT_YOUR_ANSWER
import hedvig.resources.claims_skip_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContentSelectStep(
  stepContent: StepContent.ContentSelect,
  itemId: StepId,
  isRegrettable: Boolean,
  isCurrentStep: Boolean,
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onEvent: (ClaimChatEvent) -> Unit,
  currentContinueButtonLoading: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  skipButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  // Confirming and skipping both answer the same step, so either one being in flight has to lock the other out.
  val isSubmitting = currentContinueButtonLoading || skipButtonLoading
  Column(modifier) {
    AnimatedContent(
      isCurrentStep,
      transitionSpec = {
        (fadeIn() + scaleIn()).togetherWith(fadeOut(animationSpec = tween(0)))
      },
    ) { isCurrentStep ->
      if (isCurrentStep) {
        // An answer already on the step is something to confirm or change, so it keeps the confirm button,
        // whether or not the step can also be skipped: without it a prefilled skippable step could be changed
        // but never submitted. With nothing filled in there is nothing to confirm and a tap answers the step
        // outright, so the only button left is skip, where the step offers it.
        val hasPrefilledAnswer = rememberSaveable(itemId) { stepContent.selectedOptionId != null }
        val answersOnClick = !hasPrefilledAnswer
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          ContentSelectChips(
            options = options,
            onOptionClick = { option ->
              if (!isSubmitting) {
                onEvent(ClaimChatEvent.Select(itemId, option.id))
                if (answersOnClick) {
                  onEvent(ClaimChatEvent.SubmitSelect(itemId))
                }
              }
            },
            selectedOptionId = stepContent.selectedOptionId,
            style = stepContent.style,
            answersOnClick = answersOnClick,
          )
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasPrefilledAnswer) {
              HedvigButton(
                text = stringResource(Res.string.GENERAL_CONFIRM),
                onClick = {
                  if (selectedOptionId != null) {
                    onEvent(
                      ClaimChatEvent.SubmitSelect(
                        itemId,
                      ),
                    )
                  }
                },
                isLoading = currentContinueButtonLoading,
                enabled = !isSubmitting && selectedOptionId != null,
                modifier = Modifier.fillMaxWidth(),
              )
            }
            if (canSkip) {
              HedvigButton(
                stringResource(Res.string.claims_skip_button),
                onClick = onSkip,
                isLoading = skipButtonLoading,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
              )
            }
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          val selected = options.firstOrNull { it.id == selectedOptionId }
          if (selected != null) {
            val description = stringResource(Res.string.TALKBACK_CLAIM_CHAT_YOUR_ANSWER) + selected.title
            RoundCornersPill(
              modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .clearAndSetSemantics {
                  contentDescription = description
                },
            ) {
              HedvigText(selected.title)
            }
          } else {
            SkippedLabel()
          }
          EditButton(
            isRegrettable,
            onRegret = {
              onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
            },
          )
        }
      }
    }
  }
}
