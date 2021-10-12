package com.hedvig.app.feature.embark.passages.audiorecorder

import android.media.MediaPlayer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.composables.buttons.LargeTextButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Timer
import java.util.TimerTask

inline fun timerTask(crossinline run: () -> Unit) = object : TimerTask() {
    override fun run() {
        run()
    }
}

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

class AudioRecorderViewModel(
    private val clock: Clock,
) : ViewModel() {
    sealed class ViewState {
        object NotRecording : ViewState()
        data class Recording(
            val amplitudes: List<Int>,
            val startedAt: Instant,
            val filePath: String,
        ) : ViewState()

        data class Playback(
            val filePath: String,
            val isPlaying: Boolean,
        ) : ViewState()
    }

    private var recorder: MediaRecorder? = null
    private var timer: Timer? = null
    private var player: MediaPlayer? = null

    private val _viewState = MutableStateFlow<ViewState>(ViewState.NotRecording)
    val viewState = _viewState.asStateFlow()

    fun startRecording() {
        if (recorder == null) {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                val filePath = File.createTempFile("test_claim_file", null).absolutePath
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
                _viewState.value = ViewState.Recording(emptyList(), Instant.now(clock), filePath)
            }
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

    fun stopRecording() {
        val currentState = viewState.value
        if (currentState !is ViewState.Recording) {
            throw IllegalStateException("Must be in Recording-state to stop recording")
        }
        cleanup()
        _viewState.value = ViewState.Playback(
            currentState.filePath,
            false,
        )
    }

    fun redo() {
        cleanup()
        _viewState.value = ViewState.NotRecording
    }

    fun play() {
        val currentState = viewState.value
        if (currentState !is ViewState.Playback) {
            throw IllegalStateException("Must be in Playback-state to play")
        }
        viewModelScope.launch {
            player = MediaPlayer().apply {
                setDataSource(currentState.filePath)
                setOnPreparedListener {
                    _viewState.value = currentState.copy(isPlaying = true) // TODO: Copy current state instead
                    start()
                }
                setOnCompletionListener {
                    _viewState.value = currentState.copy(isPlaying = false)
                }
                prepareAsync()
            }
        }
    }

    fun pause() {
        player?.pause()
    }

    override fun onCleared() {
        super.onCleared()

        cleanup()
    }

    private fun cleanup() {
        timer?.cancel()
        timer = null

        recorder?.stop()
        recorder?.release()
        recorder = null

        player?.stop()
        player?.release()
        player = null
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
    clock: Clock,
    stopRecording: () -> Unit,
    submit: () -> Unit,
    redo: () -> Unit,
    play: () -> Unit,
    pause: () -> Unit,
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

        when (viewState) {
            AudioRecorderViewModel.ViewState.NotRecording -> NotRecording(
                startRecording = startRecording,
            )
            is AudioRecorderViewModel.ViewState.Recording -> Recording(
                viewState = viewState,
                stopRecording = stopRecording,
                clock = clock,
            )
            is AudioRecorderViewModel.ViewState.Playback -> Playback(
                viewState = viewState,
                submit = submit,
                redo = redo,
                play = play,
                pause = pause,
            )
        }
    }
}

@Composable
fun NotRecording(startRecording: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val label = "Start Recording"
        IconButton(
            onClick = startRecording,
            modifier = Modifier
                .padding(bottom = 24.dp)
        ) {
            Image(
                painter = painterResource(
                    R.drawable.ic_record
                ),
                contentDescription = label,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun Recording(
    viewState: AudioRecorderViewModel.ViewState.Recording,
    stopRecording: () -> Unit,
    clock: Clock,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        RecordingWaveForm(
            amplitudes = viewState.amplitudes,
            modifier = Modifier.padding(16.dp),
        )
        IconButton(
            onClick = stopRecording,
            modifier = Modifier
                .padding(bottom = 24.dp)
        ) {
            Image(
                painter = painterResource(
                    R.drawable.ic_record_stop
                ),
                contentDescription = "Stop Recording", // TODO: String Resource
            )
        }
        val diff = Duration.between(
            viewState.startedAt, Instant.now(clock)
        )
        val label = String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun Playback(
    viewState: AudioRecorderViewModel.ViewState.Playback,
    submit: () -> Unit,
    redo: () -> Unit,
    play: () -> Unit,
    pause: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        // TODO: WaveForm, but for playback
        IconButton(
            onClick = if (viewState.isPlaying) {
                pause
            } else {
                play
            }
        ) {
            Image(
                painter = painterResource(
                    if (viewState.isPlaying) {
                        android.R.drawable.ic_media_pause
                    } else {
                        android.R.drawable.ic_media_play
                    }
                ),
                contentDescription = "Play"
            )
        }
        LargeContainedButton(
            onClick = submit,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Submit Claim")
        }
        LargeTextButton(
            onClick = redo,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Record again")
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
            startRecording = {},
            clock = Clock.systemDefaultZone(),
            stopRecording = {},
            submit = {},
            redo = {},
            play = {},
            pause = {},
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
                ),
                Instant.ofEpochSecond(1634025260),
                "",
            ),
            startRecording = {},
            clock = Clock.fixed(Instant.ofEpochSecond(1634025262), ZoneId.systemDefault()),
            stopRecording = {},
            submit = {},
            redo = {},
            play = {},
            pause = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AudioRecorderScreenPlaybackPreview() {
    HedvigTheme {
        AudioRecorderScreen(
            parameters = AudioRecorderParameters(listOf("Hello", "World")),
            viewState = AudioRecorderViewModel.ViewState.Playback("", false),
            startRecording = {},
            clock = Clock.systemDefaultZone(),
            stopRecording = {},
            submit = {},
            redo = {},
            play = {},
            pause = {},
        )
    }
}
