package com.hedvig.app.feature.embark.passages.audiorecorder

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import i
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.Timer
import java.util.TimerTask

inline fun timerTask(crossinline run: () -> Unit) = object : TimerTask() {
    override fun run() {
        run()
    }
}

class AudioRecorderFragment : Fragment() {
    private val model: AudioRecorderViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
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

class AudioRecorderViewModel : ViewModel() {
    sealed class ViewState {
        object NotRecording : ViewState()
        data class Recording(
            val amplitudes: List<Int>,
        ) : ViewState()
    }

    private var recorder: MediaRecorder? = null
    private var timer: Timer? = null

    private val _viewState = MutableStateFlow<ViewState>(ViewState.NotRecording)
    val viewState = _viewState.asStateFlow()

    fun startRecording() {
        if (recorder == null) {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setOutputFile(File.createTempFile("test_claim_file", null).absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
            _viewState.value = ViewState.Recording(emptyList())
            timer = Timer()
            timer?.schedule(
                timerTask {
                    recorder?.maxAmplitude?.let { amplitude ->
                        _viewState.update { vs ->
                            if (vs is ViewState.Recording) {
                                vs.copy(amplitudes = vs.amplitudes + amplitude)
                            } else {
                                vs
                            }
                        }
                    }
                },
                0,
                1000L / 60,
            )
        }
    }

    private fun stopRecording() {
        if (recorder != null) {
            timer?.cancel()
            timer = null
            recorder?.stop()
            recorder?.release()
            recorder = null
        }
    }

    override fun onCleared() {
        super.onCleared()

        stopRecording()
    }
}

@Parcelize
data class AudioRecorderParameters(
    val messages: List<String>,
) : Parcelable

@Composable
fun AudioRecorderScreen(
    parameters: AudioRecorderParameters,
    viewState: AudioRecorderViewModel.ViewState,
    startRecording: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            items(parameters.messages) { message ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier
                            .padding(16.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val amplitudes = (viewState as? AudioRecorderViewModel.ViewState.Recording)?.amplitudes ?: emptyList()
            RecordingWaveForm(amplitudes)
            IconButton(
                onClick = startRecording,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = "Start Recording", // TODO: String Resource
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioRecorderScreenNotRecordingPreview() {
    HedvigTheme {
        AudioRecorderScreen(
            parameters = AudioRecorderParameters(listOf("Hello", "World")),
            viewState = AudioRecorderViewModel.ViewState.NotRecording,
            startRecording = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AudioRecorderScreenRecordingPreview() {
    HedvigTheme {
        AudioRecorderScreen(
            parameters = AudioRecorderParameters(listOf("Hello", "World")),
            viewState = AudioRecorderViewModel.ViewState.Recording(
                listOf(
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                    100, 200, 150, 250, 0,
                )
            ),
            startRecording = {}
        )
    }
}
