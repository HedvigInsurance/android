package com.hedvig.android.feature.claim.chat.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.fileupload.AndroidFile
import com.hedvig.android.core.fileupload.CommonFile
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class AndroidAudioRecordingManager(
  private val clock: Clock,
  private val applicationScope: ApplicationScope,
) : AudioRecordingManager {
  private var recorder: MediaRecorder? = null
  private var player: MediaPlayer? = null
  private var currentFilePath: String? = null

  /**
   * Samples the recorder while it runs. It lives on [ApplicationScope], which is main confined, so samples reach
   * Compose on the same thread every other call into here arrives on. [cleanupRecorder] cancels it before
   * releasing the recorder, and that order is what stops a sample reading [MediaRecorder.getMaxAmplitude] off a
   * released recorder, or reporting a recording the caller has already thrown away.
   */
  private var amplitudeSamplingJob: Job? = null

  override fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit) {
    if (recorder != null) return // Already recording

    val filePath = File.createTempFile(
      "claim_android_recording_${UUID.randomUUID()}",
      ".mp4",
    ).absolutePath
    currentFilePath = filePath
    recorder = MediaRecorder().apply {
      setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
      setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      setAudioSamplingRate(96_000)
      setAudioEncodingBitRate(128_000)
      setOutputFile(filePath)
      setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      prepare()
      start()
    }

    val startTime = clock.now()
    val samplesPerSecond = 20

    onStateUpdate(
      AudioRecordingStepState.AudioRecording.Recording(
        amplitudes = emptyList(),
        startedAt = startTime,
        filePath = filePath,
      ),
    )

    amplitudeSamplingJob = applicationScope.launch {
      var amplitudes = emptyList<Int>()
      while (isActive) {
        delay(1000L / samplesPerSecond)
        val amplitude = recorder?.maxAmplitude ?: break
        if (amplitude == 0) continue
        amplitudes = amplitudes.plus(amplitude).takeLast((1.5 * samplesPerSecond).toInt())
        onStateUpdate(
          AudioRecordingStepState.AudioRecording.Recording(
            amplitudes = amplitudes,
            startedAt = startTime,
            filePath = filePath,
          ),
        )
      }
    }
  }

  override fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit) {
    val filePath = currentFilePath ?: return

    cleanupRecorder()

    val file = File(filePath)
    if (!file.exists()) {
      onStateUpdate(playbackState(filePath, isPrepared = false))
      return
    }

    // prepare() blocks, so the player is ready by the time it returns and the playback state is settled right
    // here, on the caller's thread.
    val mediaPlayer = MediaPlayer()
    val isPrepared = try {
      mediaPlayer.setDataSource(filePath)
      mediaPlayer.prepare()
      player = mediaPlayer
      true
    } catch (e: IOException) {
      logcat(LogPriority.ERROR, e) { "Failed to prepare playback of the claim chat recording at $filePath" }
      mediaPlayer.release()
      false
    }

    onStateUpdate(playbackState(filePath, isPrepared = isPrepared))
  }

  override fun getRecordedFile(): CommonFile? {
    val filePath = currentFilePath ?: return null
    val file = File(filePath)
    if (!file.exists()) return null

    return AndroidFile.fromFile(file, description = "Audio recording")
  }

  override fun cleanup() {
    cleanupRecorder()
    cleanupPlayer()
  }

  override fun reset() {
    cleanup()
    currentFilePath = null
  }

  private fun cleanupRecorder() {
    amplitudeSamplingJob?.cancel()
    amplitudeSamplingJob = null

    recorder?.stop()
    recorder?.release()
    recorder = null
  }

  private fun cleanupPlayer() {
    player?.stop()
    player?.release()
    player = null
  }

  private fun playbackState(filePath: String, isPrepared: Boolean) = AudioRecordingStepState.AudioRecording.Playback(
    audioPath = AudioPath.FilePath(filePath),
    isPlaying = false,
    isPrepared = isPrepared,
    hasError = !isPrepared,
  )
}
