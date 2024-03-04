package com.hedvig.android.audio.player.audioplayer

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.audio_player_data.AudioPlayer
import com.example.audio_player_data.AudioPlayerState
import com.example.audio_player_data.PlayableAudioSource
import com.hedvig.android.audio.player.internal.getProgressPercentage
import com.hedvig.android.audio.player.internal.hasReachedTheEnd
import com.hedvig.android.audio.player.internal.seekToPercent
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelChildren
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

private const val ONE_SIXTIETH_OF_A_SECOND: Long = 1_000 / 60

private class AudioPlayerImpl(
  private val dataSourceUrl: String,
  private val lifecycleOwner: LifecycleOwner,
) : AudioPlayer {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  private val seekRequestChannel = Channel<ProgressPercentage>(Channel.CONFLATED)

  private val _audioPlayerState: MutableStateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Preparing)
  override val audioPlayerState: StateFlow<AudioPlayerState> = _audioPlayerState.asStateFlow()

  private val mediaPlayerMutex = Mutex()
  private var mediaPlayer: MediaPlayer? = null

  override fun initialize() {
    initializeMediaPlayer()
    coroutineScope.launch {
      lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        launch {
          try {
            awaitCancellation()
          } finally {
            try {
              if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
              }
            } catch (ignored: IllegalStateException) {
              // ignore if the audio player has already been disposed
            }
            updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Paused)
          }
        }
        launch {
          seekRequestChannel
            .receiveAsFlow()
            .collectLatest { progressPercentage ->
              mediaPlayerMutex.withLock {
                yield()
                val mediaPlayer = mediaPlayer ?: return@withLock
                if (audioPlayerState.value.isSeekable.not()) return@withLock
                if (mediaPlayer.isPlaying) {
                  mediaPlayer.pause()
                }
                yield()
                updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Seeking)
                mediaPlayer.seekToPercent(progressPercentage)
                yield()
                updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Playing)
                mediaPlayer.start()
              }
            }
        }
        audioPlayerState
          .map { audioPlayerState -> (audioPlayerState as? AudioPlayerState.Ready)?.readyState }
          .map { readyState: AudioPlayerState.Ready.ReadyState? ->
            readyState?.shouldContinuouslyUpdateProgress == true
          }
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
    coroutineScope.coroutineContext.cancelChildren()
    closeMediaPlayer()
  }

  private fun initializeMediaPlayer() {
    _audioPlayerState.update { AudioPlayerState.Preparing }
    mediaPlayer = MediaPlayer().apply {
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build(),
      )
      setDataSource(dataSourceUrl)
      setOnErrorListener { _, what, extra ->
        logcat { "AudioPlayer failed with code: $what and extras code: $extra" }
        _audioPlayerState.update { AudioPlayerState.Failed }
        true
      }
      setOnPreparedListener { _audioPlayerState.update { AudioPlayerState.Ready.notStarted() } }
      setOnCompletionListener { _audioPlayerState.update { AudioPlayerState.Ready.done() } }
      prepareAsync()
    }
  }

  private fun closeMediaPlayer() {
    try {
      mediaPlayer?.stop()
    } catch (ignored: IllegalStateException) {
    }
    mediaPlayer?.release()
    mediaPlayer = null
  }

  override fun pausePlayer() {
    coroutineScope.launch {
      mediaPlayerMutex.withLock {
        val mediaPlayer = mediaPlayer ?: return@withLock
        if (audioPlayerState.value.isPaused) return@withLock
        updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Paused)
        mediaPlayer.pause()
      }
    }
  }

  override fun retryLoadingAudio() {
    coroutineScope.launch {
      mediaPlayerMutex.withLock {
        if (_audioPlayerState.value !is AudioPlayerState.Failed) return@withLock
        closeMediaPlayer()
        initializeMediaPlayer()
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
          updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Seeking)
          mediaPlayer.seekToPercent(ProgressPercentage(0f))
        }
        updateAudioPlayerReadyState(AudioPlayerState.Ready.ReadyState.Playing)
        coroutineContext.job
        ensureActive()
        mediaPlayer.start()
      }
    }
  }

  override fun seekTo(progressPercentage: ProgressPercentage) {
    if (audioPlayerState.value.isSeekable) {
      seekRequestChannel.trySend(progressPercentage)
    } else {
      startPlayer()
    }
  }

  private fun updateStateWithCurrentAudioPlayerProgress() {
    val mediaPlayer = mediaPlayer ?: return
    if (audioPlayerState.value !is AudioPlayerState.Ready) return
    val progressPercentage = mediaPlayer.getProgressPercentage()
    updateAudioPlayerProgress(progressPercentage)
  }

  private fun updateAudioPlayerProgress(progressPercentage: ProgressPercentage) {
    _audioPlayerState.update { oldAudioPlayerState ->
      if (oldAudioPlayerState is AudioPlayerState.Ready) {
        oldAudioPlayerState.copy(progressPercentage = progressPercentage)
      } else {
        oldAudioPlayerState
      }
    }
  }

  private fun updateAudioPlayerReadyState(readyState: AudioPlayerState.Ready.ReadyState) {
    _audioPlayerState.update { oldAudioPlayerState ->
      if (oldAudioPlayerState is AudioPlayerState.Ready) {
        oldAudioPlayerState.copy(readyState = readyState)
      } else {
        oldAudioPlayerState
      }
    }
  }
}

@Composable
fun rememberAudioPlayer(playableAudioSource: PlayableAudioSource): AudioPlayer {
  val lifecycleOwner = LocalLifecycleOwner.current
  val audioPlayer: AudioPlayer = remember(playableAudioSource, lifecycleOwner) {
    AudioPlayerImpl(playableAudioSource.dataSourceUrl, lifecycleOwner)
  }
  DisposableEffect(audioPlayer) {
    audioPlayer.initialize()
    onDispose {
      audioPlayer.close()
    }
  }
  return audioPlayer
}
