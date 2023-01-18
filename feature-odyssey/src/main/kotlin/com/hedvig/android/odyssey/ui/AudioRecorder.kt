package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.odyssey.AudioRecorderViewModel
import com.hedvig.common.designsystem.LargeTextButton
import com.hedvig.common.designsystem.ProgressableLargeContainedButton
import com.hedvig.common.renderers.audiorecorder.PlaybackWaveForm
import com.hedvig.common.renderers.audiorecorder.RecordingAmplitudeIndicator
import com.hedvig.common.renderers.utils.ScreenOnFlag
import com.hedvig.common.traits.composeTextStyle
import java.time.Clock
import java.time.Duration
import java.time.Instant
import com.hedvig.android.odyssey.R
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.file.FileContent
import com.hedvig.common.utils.contentType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun AudioRecorderScreen(
  questions: List<String?>,
  onAudioFile: suspend (File) -> Unit,
  onNext: suspend () -> Unit,
) {
  val audioRecorderViewModel = getViewModel<AudioRecorderViewModel>()
  val audioRecorderViewState by audioRecorderViewModel.viewState.collectAsState()

  val coroutineScope = rememberCoroutineScope()

  Box(Modifier.fillMaxHeight()) {

    Column(Modifier.padding(16.dp)) {
      questions.forEach {
        Text(it ?: "-")
      }
    }

    Box(Modifier.align(Alignment.BottomCenter)) {
      AudioRecorder(
        viewState = audioRecorderViewState,
        startRecording = audioRecorderViewModel::startRecording,
        clock = audioRecorderViewModel.clock,
        stopRecording = audioRecorderViewModel::stopRecording,
        submit = {
          val filePath = (audioRecorderViewState as? AudioRecorderViewModel.ViewState.Playback)!!.filePath
          val audioFile = File(
            name = "AudioRecording",
            content = FileContent(path = filePath),
            contentType = filePath.contentType(),
          )
          coroutineScope.launch {
            onAudioFile(audioFile)
            onNext()
          }
        },
        redo = audioRecorderViewModel::redo,
        play = audioRecorderViewModel::play,
        pause = audioRecorderViewModel::pause,
        isLoading = false,
        startRecordingText = "Start recording",
        stopRecordingText = "Stop Recording",
        recordAgainText = "Record Again",
        submitClaimText = "Continue",
      )
    }
  }
}

@Composable
fun AudioRecorder(
  viewState: AudioRecorderViewModel.ViewState,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submit: () -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  isLoading: Boolean,
  startRecordingText: String,
  stopRecordingText: String,
  recordAgainText: String,
  submitClaimText: String,
) {
  when (viewState) {
    AudioRecorderViewModel.ViewState.NotRecording -> NotRecording(
      startRecording = startRecording,
      startRecordingText = startRecordingText,
    )

    is AudioRecorderViewModel.ViewState.Recording -> Recording(
      viewState = viewState,
      stopRecording = stopRecording,
      stopRecordingText = stopRecordingText,
      clock = clock,
    )

    is AudioRecorderViewModel.ViewState.Playback -> Playback(
      viewState = viewState,
      submit = submit,
      submitClaimText = submitClaimText,
      recordAgainText = recordAgainText,
      redo = redo,
      play = play,
      pause = pause,
      isLoading = isLoading,
    )
  }
}

@Composable
fun NotRecording(startRecording: () -> Unit, startRecordingText: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    IconButton(
      onClick = startRecording,
      modifier = Modifier
        .padding(bottom = 24.dp)
        .then(Modifier.size(72.dp)),
    ) {
      Image(
        painter = painterResource(
          R.drawable.ic_record,
        ),
        contentDescription = startRecordingText,
      )
    }
    Text(
      text = startRecordingText,
      style = com.hedvig.common.remote.traits.TextStyle.CAPTION_ONE.composeTextStyle(),
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Recording(
  viewState: AudioRecorderViewModel.ViewState.Recording,
  stopRecording: () -> Unit,
  stopRecordingText: String,
  clock: Clock,
  modifier: Modifier = Modifier,
) {
  ScreenOnFlag()

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(bottom = 24.dp),
    ) {
      if (viewState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = viewState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier.size(72.dp),
      ) {
        Image(
          painter = painterResource(
            R.drawable.ic_record_stop,
          ),
          contentDescription = stopRecordingText,
        )
      }
    }
    val diff = Duration.between(
      viewState.startedAt,
      Instant.now(clock),
    )
    val label = String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
    Text(
      text = label,
      style = com.hedvig.common.remote.traits.TextStyle.CAPTION_ONE.composeTextStyle(),
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Playback(
  viewState: AudioRecorderViewModel.ViewState.Playback,
  submit: () -> Unit,
  submitClaimText: String,
  recordAgainText: String,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  isLoading: Boolean,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    if (!viewState.isPrepared) {
      CircularProgressIndicator()
    } else {
      PlaybackWaveForm(
        isPlaying = viewState.isPlaying,
        play = play,
        pause = pause,
        amplitudes = viewState.amplitudes,
        progress = viewState.progress,
      )
    }

    ProgressableLargeContainedButton(
      onClick = submit,
      modifier = Modifier.padding(top = 16.dp),
      isLoading = isLoading,
    ) {
      Text(submitClaimText)
    }

    LargeTextButton(
      onClick = redo,
      modifier = Modifier.padding(top = 8.dp),
    ) {
      Text(recordAgainText)
    }
  }
}
