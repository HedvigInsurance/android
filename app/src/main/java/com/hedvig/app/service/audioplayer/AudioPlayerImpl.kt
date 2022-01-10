package com.hedvig.app.service.audioplayer

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.FloatRange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.app.service.audioplayer.AudioPlayerState.Ready.ReadyState
import com.hedvig.app.util.getProgressPercentage
import com.hedvig.app.util.hasReachedTheEnd
import com.hedvig.app.util.seekToPercent
import d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import java.io.Closeable

private const val ONE_SIXTIETH_OF_A_SECOND: Long = 1_000 / 60

class AudioPlayerImpl(
    signedAudioURL: String,
    lifecycleOwner: LifecycleOwner,
) : AudioPlayer, Closeable {
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    // Channel to conflate the requests to seek to drop the old ones and keep only the last request
    private val seekRequestChannel = Channel<Float>(Channel.CONFLATED)

    private val _audioPlayerState: MutableStateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Preparing)
    override val audioPlayerState: StateFlow<AudioPlayerState> = _audioPlayerState.asStateFlow()

    private val mediaPlayerMutex = Mutex()
    private var mediaPlayer: MediaPlayer? = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setDataSource(signedAudioURL)
        setOnErrorListener { _, what, extra ->
            d { "AudipPlayer failed with code: $what and extras code: $extra" }
            _audioPlayerState.update { AudioPlayerState.Failed }
            true
        }
        setOnPreparedListener { _audioPlayerState.update { AudioPlayerState.Ready.notStarted() } }
        setOnCompletionListener { _audioPlayerState.update { AudioPlayerState.Ready.done() } }
        prepareAsync()
    }

    init {
        coroutineScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    try {
                        awaitCancellation()
                    } finally {
                        if (mediaPlayer?.isPlaying == true) {
                            mediaPlayer?.pause()
                        }
                        updateAudioPlayerReadyState(ReadyState.Paused)
                    }
                }
                launch {
                    seekRequestChannel
                        .receiveAsFlow()
                        .collectLatest { percentage ->
                            mediaPlayerMutex.withLock {
                                yield()
                                val mediaPlayer = mediaPlayer ?: return@withLock
                                if (audioPlayerState.value.isSeekable.not()) return@withLock
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.pause()
                                }
                                yield()
                                updateAudioPlayerReadyState(ReadyState.Seeking)
                                mediaPlayer.seekToPercent(percentage)
                                yield()
                                updateAudioPlayerReadyState(ReadyState.Playing)
                                mediaPlayer.start()
                            }
                        }
                }
                audioPlayerState
                    .map { audioPlayerState -> (audioPlayerState as? AudioPlayerState.Ready)?.readyState }
                    .map { readyState: ReadyState? -> readyState?.shouldContinuouslyUpdateProgress == true }
                    .distinctUntilChanged()
                    .collectLatest { shouldContinuouslyUpdateProgress: Boolean ->
                        if (shouldContinuouslyUpdateProgress) {
                            coroutineScope {
                                while (isActive) {
                                    delay(ONE_SIXTIETH_OF_A_SECOND)
                                    updateStateWithCurrentAudioPlayerProgress()
                                }
                            }
                        } else {
                            updateStateWithCurrentAudioPlayerProgress()
                        }
                    }
            }
        }
    }

    override fun close() {
        coroutineScope.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun pausePlayer() {
        coroutineScope.launch {
            mediaPlayerMutex.withLock {
                val mediaPlayer = mediaPlayer ?: return@withLock
                if (audioPlayerState.value.isPaused) return@withLock
                updateAudioPlayerReadyState(ReadyState.Paused)
                mediaPlayer.pause()
            }
        }
    }

    override fun startPlayer() {
        coroutineScope.launch {
            mediaPlayerMutex.withLock {
                val mediaPlayer = mediaPlayer ?: return@withLock
                if (mediaPlayer.isPlaying) return@withLock
                if (audioPlayerState.value.isPlayable.not()) return@withLock
                if (mediaPlayer.hasReachedTheEnd()) {
                    updateAudioPlayerReadyState(ReadyState.Seeking)
                    mediaPlayer.seekToPercent(0f)
                }
                updateAudioPlayerReadyState(ReadyState.Playing)
                coroutineContext.job
                ensureActive()
                mediaPlayer.start()
            }
        }
    }

    override fun seekTo(@FloatRange(from = 0.0, to = 1.0) percentage: Float) {
        if (audioPlayerState.value.isSeekable) {
            seekRequestChannel.trySend(percentage)
        } else {
            startPlayer()
        }
    }

    private fun updateStateWithCurrentAudioPlayerProgress() {
        val mediaPlayer = mediaPlayer ?: return
        if (audioPlayerState.value !is AudioPlayerState.Ready) return
        val progress = mediaPlayer.getProgressPercentage()
        updateAudioPlayerProgress(progress)
    }

    private fun updateAudioPlayerProgress(progress: Float) {
        _audioPlayerState.update { oldAudioPlayerState ->
            if (oldAudioPlayerState is AudioPlayerState.Ready) {
                oldAudioPlayerState.copy(progress = progress)
            } else {
                oldAudioPlayerState
            }
        }
    }

    private fun updateAudioPlayerReadyState(readyState: ReadyState) {
        _audioPlayerState.update { oldAudioPlayerState ->
            if (oldAudioPlayerState is AudioPlayerState.Ready) {
                oldAudioPlayerState.copy(readyState = readyState)
            } else {
                oldAudioPlayerState
            }
        }
    }
}
