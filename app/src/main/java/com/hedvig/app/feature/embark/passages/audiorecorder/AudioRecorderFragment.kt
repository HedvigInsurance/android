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
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.showPermissionExplanationDialog
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Clock

class AudioRecorderFragment : Fragment() {
    private val embarkViewModel: EmbarkViewModel by sharedViewModel()
    private val model: AudioRecorderViewModel by viewModel()
    private val clock: Clock by inject()

    private val permission = Manifest.permission.RECORD_AUDIO

    private val permissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
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
                    stopRecording = model::stopRecording,
                    submit = {
                        val filePath = (state as? AudioRecorderViewModel.ViewState.Playback)?.filePath
                        if (filePath != null) {
                            embarkViewModel.putInStore(parameters.key, filePath)
                            embarkViewModel.submitAction(parameters.link)
                        }
                    },
                    redo = model::redo,
                    play = model::play,
                    pause = model::pause,
                )
            }
        }
    }

    private fun askForPermission() {
        if (requireActivity().hasPermissions(permission)) {
            model.startRecording()
        } else {
            permissionResultLauncher.launch(permission)
        }
    }

    companion object {
        private const val PARAMETERS = "PARAMETERS"
        private const val REQUEST_AUDIO_PERMISSION = 12994

        fun newInstance(parameters: AudioRecorderParameters) = AudioRecorderFragment().apply {
            arguments = bundleOf(
                PARAMETERS to parameters,
            )
        }
    }
}
