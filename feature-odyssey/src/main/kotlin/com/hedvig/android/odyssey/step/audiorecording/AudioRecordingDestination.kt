package com.hedvig.android.odyssey.step.audiorecording

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.odyssey.renderers.audiorecorder.PlaybackWaveForm
import com.hedvig.odyssey.renderers.audiorecorder.RecordingAmplitudeIndicator
import com.hedvig.odyssey.renderers.utils.ScreenOnFlag
import hedvig.resources.R
import kotlinx.datetime.Clock
import java.io.File

@Composable
internal fun AudioRecordingDestination(
  viewModel: AudioRecordingViewModel,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = (uiState as? AudioRecordingUiState.Playback)?.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  AudioRecordingScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    questions = questions,
    clock = viewModel.clock,
    startRecording = viewModel::startRecording,
    stopRecording = viewModel::stopRecording,
    submitAudioFile = viewModel::submitAudioFile,
    redo = viewModel::redo,
    play = viewModel::play,
    pause = viewModel::pause,
    showedError = viewModel::showedError,
    navigateBack = navigateBack,
  )
}

@Composable
private fun AudioRecordingScreen(
  uiState: AudioRecordingUiState,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  clock: Clock,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = stringResource(R.string.claims_incident_screen_header),
        scrollBehavior = topAppBarScrollBehavior,
      )
      Column(
        Modifier
          .fillMaxSize()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        val sideSpacingModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
          Modifier
            .fillMaxWidth(0.8f)
            .wrapContentWidth(Alignment.Start)
            .align(Alignment.CenterHorizontally)
        } else {
          Modifier.padding(horizontal = 16.dp)
        }
        Spacer(Modifier.height(20.dp))
        for (question in questions) {
          HedvigCard(
            shape = RoundedCornerShape(12.dp),
            elevation = HedvigCardElevation.Elevated(),
            modifier = sideSpacingModifier.padding(end = 16.dp),
          ) {
            Text(
              text = question,
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
          Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        AudioRecordingSection(
          uiState = uiState,
          clock = clock,
          startRecording = startRecording,
          stopRecording = stopRecording,
          submitAudioFile = submitAudioFile,
          redo = redo,
          play = play,
          pause = pause,
          modifier = sideSpacingModifier,
        )
        Spacer(Modifier.height(16.dp))
        Spacer(
          Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
          ),
        )
      }
    }
    ErrorSnackbar(
      hasError = uiState.hasAudioSubmissionError,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingSection(
  uiState: AudioRecordingUiState,
  clock: Clock,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
  var openPermissionDialog by remember { mutableStateOf(false) }

  if (openPermissionDialog) {
    PermissionDialog(
      recordAudioPermissionState = recordAudioPermissionState,
      dismiss = { openPermissionDialog = false },
    )
  }

  AudioRecorder(
    uiState = uiState,
    startRecording = {
      when (recordAudioPermissionState.status) {
        PermissionStatus.Granted -> startRecording()
        is PermissionStatus.Denied -> openPermissionDialog = true
      }
    },
    clock = clock,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    redo = redo,
    play = play,
    pause = pause,
    startRecordingText = stringResource(R.string.EMBARK_START_RECORDING),
    stopRecordingText = stringResource(R.string.EMBARK_STOP_RECORDING),
    recordAgainText = stringResource(R.string.EMBARK_RECORD_AGAIN),
    submitClaimText = stringResource(R.string.general_continue_button),
    modifier = modifier,
  )
}

@Composable
private fun AudioRecorder(
  uiState: AudioRecordingUiState,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  startRecordingText: String,
  stopRecordingText: String,
  recordAgainText: String,
  submitClaimText: String,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    AudioRecordingUiState.NotRecording -> NotRecording(
      startRecording = startRecording,
      startRecordingText = startRecordingText,
      modifier = modifier,
    )
    is AudioRecordingUiState.Recording -> Recording(
      uiState = uiState,
      stopRecording = stopRecording,
      stopRecordingText = stopRecordingText,
      clock = clock,
      modifier = modifier,
    )
    is AudioRecordingUiState.Playback -> Playback(
      uiState = uiState,
      submit = {
        val filePath = uiState.filePath
        val audioFile = File(filePath)
        submitAudioFile(audioFile)
      },
      submitClaimText = submitClaimText,
      recordAgainText = recordAgainText,
      redo = redo,
      play = play,
      pause = pause,
      modifier = modifier,
    )
  }
}

@Composable
private fun NotRecording(
  startRecording: () -> Unit,
  startRecordingText: String,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    IconButton(
      onClick = startRecording,
      modifier = Modifier
        .padding(bottom = 24.dp)
        .then(Modifier.size(72.dp)),
    ) {
      Image(
        painter = painterResource(com.hedvig.android.odyssey.R.drawable.ic_record),
        contentDescription = startRecordingText,
      )
    }
    Text(
      text = startRecordingText,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun Recording(
  uiState: AudioRecordingUiState.Recording,
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
      if (uiState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier.size(72.dp),
      ) {
        Image(
          painter = painterResource(
            com.hedvig.android.odyssey.R.drawable.ic_record_stop,
          ),
          contentDescription = stopRecordingText,
        )
      }
    }
    val diff = clock.now() - uiState.startedAt
    val label = String.format("%02d:%02d", diff.inWholeMinutes, diff.inWholeSeconds % 60)
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun Playback(
  uiState: AudioRecordingUiState.Playback,
  submit: () -> Unit,
  submitClaimText: String,
  recordAgainText: String,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (!uiState.isPrepared) {
      CircularProgressIndicator()
    } else {
      PlaybackWaveForm(
        isPlaying = uiState.isPlaying,
        play = play,
        pause = pause,
        amplitudes = uiState.amplitudes,
        progress = uiState.progress,
      )
    }

    LargeContainedTextButton(
      onClick = submit,
      text = submitClaimText,
      modifier = Modifier.padding(top = 16.dp),
    )

    LargeTextButton(
      onClick = redo,
      modifier = Modifier.padding(top = 8.dp),
    ) {
      Text(recordAgainText)
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDialog(
  recordAudioPermissionState: PermissionState,
  dismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = dismiss,
    title = { Text(stringResource(R.string.PERMISSION_DIALOG_TITLE)) },
    text = { Text(stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE)) },
    dismissButton = {
      TextButton(
        onClick = dismiss,
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          dismiss()
          recordAudioPermissionState.launchPermissionRequest()
        },
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(stringResource(android.R.string.ok))
      }
    },
  )
}
