package com.hedvig.feature.claim.com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.feature.claim.chat.audiorecorder.AudioContent
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorder
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.NotRecording
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.PrerecordedWithAudioContent
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.Recording
import com.hedvig.feature.claim.chat.audiorecorder.AudioUrl
import com.hedvig.feature.claim.chat.audiorecorder.PrerecordedPlayback
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds


@HedvigPreview
@Composable
private fun PreviewNotRecording() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecorder(
        uiState = NotRecording,
        startRecording = {},
        stopRecording = {},
        submitAudioFile = {},
        submitAudioUrl = {},
        redo = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRecording() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecorder(
        uiState = Recording(listOf(70), Clock.System.now().minus(1019.seconds), ""),
        startRecording = { },
        stopRecording = { },
        submitAudioFile = {},
        submitAudioUrl = {},
        redo = { },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPrerecordedPlayback() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PrerecordedPlayback(
        uiState = PrerecordedWithAudioContent(AudioContent(AudioUrl(""), AudioUrl(""))),
        redo = {},
        submitAudioUrl = {},
        modifier = Modifier,
      )
    }
  }
}

