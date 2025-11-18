package com.hedvig.feature.claim.chat.ui.audiorecording

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import com.hedvig.android.design.system.hedvig.PermissionDialog
import hedvig.resources.R
import java.io.File
import kotlin.time.Clock
import kotlin.time.Instant
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.serialization.Serializable

sealed interface AudioRecordingState {

  data object NotRecording : AudioRecordingState

  data class Recording(
    val amplitudes: List<Int>,
    val startedAt: Instant,
    val filePath: String,
  ) : AudioRecordingState

  data class PrerecordedWithAudioContent(
    val audioContent: AudioContent,
    val canSubmit: Boolean,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,

  ) : AudioRecordingState

  data class Playback(
    val filePath: String,
    val isPlaying: Boolean,
    val isPrepared: Boolean,
    val amplitudes: List<Int>,
    val isLoading: Boolean,
    val hasError: Boolean,
    val canSubmit: Boolean
  ) : AudioRecordingState
}

@Serializable
@JvmInline
value class AudioUrl(val value: String)

@Immutable
@Serializable
data class AudioContent(
  /**
   * The url to be used to play back the audio file
   */
  val signedUrl: AudioUrl,
  /**
   * The url that the backend expects when trying to go to the next step of the flow
   */
  val audioUrl: AudioUrl,
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingSection(
  uiState: AudioRecordingState,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  launchFreeText: () -> Unit,
  allowFreeText: Boolean,
  modifier: Modifier = Modifier,
) {
  var showPermissionDialog by remember { mutableStateOf(false) }
  val recordAudioPermissionState = if (LocalInspectionMode.current) {
    object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Granted

      override fun launchPermissionRequest() {}
    }
  } else {
    rememberPermissionState(Manifest.permission.RECORD_AUDIO) { isGranted ->
      if (isGranted) {
        startRecording()
      } else {
        showPermissionDialog = true
      }
    }
  }
  if (showPermissionDialog) {
    PermissionDialog(
      permissionDescription = stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
      isPermanentlyDeclined = !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO),
      onDismiss = { showPermissionDialog = false },
      okClick = recordAudioPermissionState::launchPermissionRequest,
      openAppSettings = openAppSettings,
    )
  }
  AudioRecorder(
    uiState = uiState,
    startRecording = recordAudioPermissionState::launchPermissionRequest,
    clock = clock,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    submitAudioUrl = submitAudioUrl,
    redo = redo,
    modifier = modifier,
    allowFreeText = allowFreeText,
    onLaunchFreeText = launchFreeText,
  )
}
