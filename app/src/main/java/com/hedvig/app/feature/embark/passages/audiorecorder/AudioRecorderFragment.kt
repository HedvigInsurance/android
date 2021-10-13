package com.hedvig.app.feature.embark.passages.audiorecorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.hedvig.app.ui.compose.theme.HedvigTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Clock

class AudioRecorderFragment : Fragment() {
    private val model: AudioRecorderViewModel by viewModel()
    private val clock: Clock by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                    startRecording = model::startRecording,
                    clock = clock,
                    stopRecording = model::stopRecording,
                    submit = { /* TODO */ },
                    redo = model::redo,
                    play = model::play,
                    pause = model::pause,
                )
            }
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
