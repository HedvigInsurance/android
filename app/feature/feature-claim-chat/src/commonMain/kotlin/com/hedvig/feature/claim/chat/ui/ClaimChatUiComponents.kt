package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.locale.previewCommonLocale
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.ui.common.YesNoBubble
import com.hedvig.feature.claim.chat.ui.step.ChatClaimSummaryTopContent
import com.hedvig.feature.claim.chat.ui.step.DateSelectBubble
import com.hedvig.feature.claim.chat.ui.step.MultiSelectBubbleWithDialog
import com.hedvig.feature.claim.chat.ui.step.SingleSelectBubbleWithDialog
import com.hedvig.feature.claim.chat.ui.step.TextInputBubble
import com.hedvig.feature.claim.chat.ui.step.audiorecording.AudioRecorderBubble
import kotlin.time.Clock

@HedvigPreview
@Composable
private fun PreviewClaimChatComponents() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End,
      ) {
        AudioRecorderBubble(
          isCurrentStep = true,
          recordingState = AudioRecordingStepState.FreeTextDescription(
            errorType = null,
            canSubmit = true,
          ),
          clock = Clock.System,
          onShouldShowRequestPermissionRationale = {
            false
          },
          startRecording = {},
          stopRecording = {},
          submitAudioFile = {},
          redoRecording = {},
          openAppSettings = {},
          freeTextAvailable = true,
          submitFreeText = {},
          onSwitchToFreeText = {},
          onSwitchToAudioRecording = {},
          onLaunchFullScreenEditText = {},
          canSkip = true,
          onSkip = {},
          freeText = "some not really long free text",
          continueButtonLoading = false,
          skipButtonLoading = false,
        )
        Spacer(Modifier.height(16.dp))
        YesNoBubble(
          answerSelected = "No",
          onSelect = {},
          questionText = "Was it electric?",
        )
        Spacer(Modifier.height(16.dp))
        SingleSelectBubbleWithDialog(
          questionLabel = "Location",
          options = listOf(
            RadioOption(RadioOptionId("01"), "At home"),
            RadioOption(RadioOptionId("02"), "Outside home"),
          ),
          selectedOptionId = RadioOptionId("01"),
          onSelect = {},
        )
        Spacer(Modifier.height(16.dp))
        MultiSelectBubbleWithDialog(
          questionLabel = "Select the damage type",
          options = listOf(
            RadioOption(RadioOptionId("01"), "Wheel"),
            RadioOption(RadioOptionId("02"), "Seat"),
          ),
          selectedOptionIds = listOf(RadioOptionId("01"), RadioOptionId("02")),
          onSelect = {},
        )
        Spacer(Modifier.height(16.dp))
        DateSelectBubble(
          questionLabel = "Date of occurence",
          datePickerState = DatePickerUiState(previewCommonLocale, null),
        )
        Spacer(Modifier.height(16.dp))
        TextInputBubble(
          questionLabel = "Re-purchase price",
          text = "15000",
          suffix = "SEK",
          onInput = {},
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}
