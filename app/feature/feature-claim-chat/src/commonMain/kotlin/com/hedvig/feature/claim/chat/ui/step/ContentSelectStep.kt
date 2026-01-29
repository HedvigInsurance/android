package com.hedvig.feature.claim.chat.ui.step

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.common.ContentSelectChips
import com.hedvig.feature.claim.chat.ui.common.EditButton
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.common.SkippedLabel
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.Res
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
  Column(modifier) {
    AnimatedContent(
      isCurrentStep,
      transitionSpec = {
        (fadeIn() + scaleIn()).togetherWith(fadeOut(animationSpec = tween(0)))
      },
    ) { targetState ->
      Column {
        if (targetState) {
          ContentSelectChips(
            options = options,
            onOptionClick = { option ->
              if (!currentContinueButtonLoading) {
                onEvent(
                  ClaimChatEvent.Select(
                    itemId,
                    option.id,
                  ),
                )
              }
            },
            selectedOptionId = stepContent.selectedOptionId,
            style = stepContent.style,
          )
          Spacer(Modifier.height(16.dp))
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
            enabled = !currentContinueButtonLoading && selectedOptionId != null,
            modifier = Modifier.fillMaxWidth(),
          )
          if (canSkip) {
            Spacer(Modifier.height(8.dp))
            HedvigButton(
              stringResource(Res.string.claims_skip_button),
              onClick = onSkip,
              isLoading = skipButtonLoading,
              enabled = !skipButtonLoading,
              modifier = Modifier.fillMaxWidth(),
              buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            )
          }
        } else {
          Column {
            val selected = options.firstOrNull { it.id == selectedOptionId }
            if (selected != null) {
              Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                  .fillMaxWidth(),
              ) {
                RoundCornersPill(
                  onClick = null,
                ) {
                  HedvigText(selected.title)
                }
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
}
