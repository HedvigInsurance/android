package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun AudioRecorderBubble(
  isCurrentStep: Boolean,
  recordingUrl: String?,
  onSubmit: (String) -> Unit,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = false,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = recordingUrl,
    content = {
      TODO()
    },
  )
}


@Composable
internal fun AssistantMessageBubble(
  text: String,
  comment: String?,
  isLoading: Boolean,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {

}

@Composable
internal fun YesNoBubble(
  questionLabel: String,
  answerSelected: Boolean?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  onSubmit: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = answerSelected,
    content = {
      TODO()
    },
  )
}

@Composable
internal fun SingleSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  onSubmit: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = selectedOptionId,
    content = {
      TODO()
    },
  )
}

@Composable
internal fun DateSelectBubble(
  questionLabel: String?,
  date: LocalDate?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  onSubmit: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    selectedAnswer = date,
    onSkip = onSkip,
    content = {
      TODO()
    },
  )
}

@Composable
internal fun TextInputBubble(
  questionLabel: String,
  text: String?,
  suffix: String?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSubmit: (String) -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    selectedAnswer = text,
    onSkip = onSkip,
    content = {
      TODO()
    },
  )
}

@Composable
internal fun ChatClaimSummary(
  recordingUrl: String?,
  displayItems: List<Pair<String, String>>,
  onSubmit: (Unit) -> Unit,
  modifier: Modifier = Modifier,
) {

}

@Composable
internal fun <T> StandardBubble(
  isPrefilledByAI: Boolean,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  selectedAnswer: T?,
  onSkip: () -> Unit,
  onSubmit: (T) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column {
    Box(modifier) {
      HedvigCard {
        content()
      }
      if (isPrefilledByAI) {
        Icon(
          imageVector = HedvigIcons.HelipadFilled,
          tint = HedvigTheme.colorScheme.signalAmberFill,
          contentDescription = null //todo
        )
      }
    }
    if (canSkip || isCurrentStep) {
      Spacer(Modifier.height(16.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (canSkip) {
          HedvigTextButton(
            stringResource(R.string.claims_skip_button),
            onClick = onSkip,
          )
        }
        if (isCurrentStep) {
          HedvigButton(
            text = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
            enabled = selectedAnswer != null,
            onClick = {
              if (selectedAnswer != null) onSubmit(selectedAnswer)
            },
            buttonSize = ButtonDefaults.ButtonSize.Small,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAssistantBubble() {
  HedvigTheme {
    Surface {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        AssistantMessageBubble(
          text = "Specify when and where it happened",
          comment = null,
          isLoading = false,
          isCurrentStep = false,
        )
        AssistantMessageBubble(
          text = "Processing audio information",
          comment = null,
          isLoading = true,
          isCurrentStep = false,
        )
        AssistantMessageBubble(
          text = "Processing audio information",
          comment = "Done!",
          isLoading = false,
          isCurrentStep = false,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimChatComponents(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) canBeChanged: Boolean,
) {
  HedvigTheme {
    Surface {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        AudioRecorderBubble(
          isCurrentStep = false,
          onSubmit = {},
          canSkip = false,
          canBeChanged = true,
          onSkip = {},
          recordingUrl = ""
        )
        YesNoBubble(
          questionLabel = "Was the bike electric?",
          answerSelected = true,
          isPrefilled = true,
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = false,
          onSubmit = {},
          onSkip = {}
        )
        SingleSelectBubbleWithDialog(
          questionLabel = "Location",
          options = listOf(
            RadioOption(RadioOptionId("01"), "At home"),
            RadioOption(RadioOptionId("02"), "Outside home"),
          ),
          selectedOptionId = RadioOptionId("01"),
          isPrefilled = false,
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = false,
          onSubmit = {},
          onSkip = {}
        )
        DateSelectBubble(
          questionLabel = null,
          date = LocalDate(2025, 11, 10),
          isPrefilled = true,
          isCurrentStep = false,
          canSkip = false,
          canBeChanged = true,
          onSubmit = {},
          onSkip = {}
        )
        TextInputBubble(
          questionLabel = "Re-purchase price",
          text = "15000",
          suffix = "SEK",
          isPrefilled = false,
          isCurrentStep = true,
          canBeChanged = true,
          canSkip = true,
          onSubmit = {},
          onSkip = {}
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSummary() {
  HedvigTheme {
    Surface {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        ChatClaimSummary(
          recordingUrl = "",
          displayItems = listOf(
            "Locked" to "Yes",
            "Electric bike" to "Yes",
          ),
          onSubmit = {},
        )
      }
    }
  }
}
