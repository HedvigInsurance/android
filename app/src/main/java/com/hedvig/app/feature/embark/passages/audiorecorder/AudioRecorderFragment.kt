package com.hedvig.app.feature.embark.passages.audiorecorder

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.showPermissionExplanationDialog
import com.hedvig.hanalytics.HAnalytics
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Clock

class AudioRecorderFragment : Fragment() {
  private val embarkViewModel: EmbarkViewModel by sharedViewModel()
  private val model: AudioRecorderViewModel by viewModel()
  private val clock: Clock by inject()
  private val hAnalytics: HAnalytics by inject()

  private val permission = Manifest.permission.RECORD_AUDIO

  private val permissionResultLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { permissionGranted ->
    if (permissionGranted) {
      model.startRecording()
    } else {
      requireActivity().showPermissionExplanationDialog(permission)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ) = ComposeView(requireContext()).apply {
    isTransitionGroup = true // https://issuetracker.google.com/issues/206947893
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    val parameters = requireArguments().getParcelable<AudioRecorderParameters>(PARAMETERS)
      ?: throw IllegalArgumentException("Programmer error: Missing PARAMETERS in ${this.javaClass.name}")

    setContent {
      HedvigTheme {
        val state by model.viewState.collectAsState()
        AudioRecorderScreen(
          parameters = parameters,
          viewState = state,
          startRecording = ::askForPermission,
          clock = clock,
          stopRecording = {
            model.stopRecording()
            logWithStoryAndStore(hAnalytics::embarkAudioRecordingStopped)
          },
          submit = {
            submitAudioRecording(state, parameters)
            logWithStoryAndStore(hAnalytics::embarkAudioRecordingSubmitted)
          },
          redo = {
            model.redo()
            logWithStoryAndStore(hAnalytics::embarkAudioRecordingRetry)
          },
          play = {
            model.play()
            logWithStoryAndStore(hAnalytics::embarkAudioRecordingPlayback)
          },
          pause = {
            model.pause()
            logWithStoryAndStore(hAnalytics::embarkAudioRecordingStopped)
          },
        )
      }
    }
  }

  private fun logWithStoryAndStore(action: (storyName: String, store: Map<String, String?>) -> Unit) {
    action(embarkViewModel.storyName, embarkViewModel.getStoreAsMap())
  }

  private fun submitAudioRecording(
    state: AudioRecorderViewModel.ViewState,
    parameters: AudioRecorderParameters,
  ) {
    val playbackState = state as? AudioRecorderViewModel.ViewState.Playback ?: return
    val isAlreadyPerformingNetworkRequest = embarkViewModel.loadingState.value
    if (!isAlreadyPerformingNetworkRequest) {
      embarkViewModel.putInStore(parameters.key, playbackState.filePath)
      embarkViewModel.submitAction(parameters.link)
    }
  }

  private fun askForPermission() {
    if (requireActivity().hasPermissions(permission)) {
      model.startRecording()
      logWithStoryAndStore(hAnalytics::embarkAudioRecordingBegin)
    } else {
      permissionResultLauncher.launch(permission)
    }
  }

  companion object {
    private const val PARAMETERS = "PARAMETERS"

    fun newInstance(parameters: AudioRecorderParameters) = AudioRecorderFragment().apply {
      arguments = bundleOf(
        PARAMETERS to parameters,
      )
    }
  }
}
