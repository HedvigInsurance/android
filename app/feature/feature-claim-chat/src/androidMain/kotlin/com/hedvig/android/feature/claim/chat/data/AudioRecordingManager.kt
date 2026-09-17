package com.hedvig.android.feature.claim.chat.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.fileupload.AndroidFile
import com.hedvig.android.core.fileupload.CommonFile
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class AndroidAudioRecordingManager(
  private val clock: Clock,
) : AudioRecordingManager {
  private var recorder: MediaRecorder? = null
  private var timer: Timer? = null
  private var player: MediaPlayer? = null
  private var currentFilePath: String? = null

  /**
   * Bumped by [cleanup]. Both state callbacks reach us late and from another thread, the amplitude sampler on the
   * timer and the playback one from [MediaPlayer.prepare], so each captures the generation it was armed in and
   * checks it before reporting. That makes a teardown final: work already in flight cannot report a recording
   * after the caller has thrown it away.
   */
  @Volatile
  private var generation: Int = 0

  override fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit) {
    if (recorder != null) return // Already recording

    val armedGeneration = generation
    recorder = MediaRecorder().apply {
      setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
      setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      setAudioSamplingRate(96_000)
      setAudioEncodingBitRate(128_000)
      val filePath = File.createTempFile(
        "claim_android_recording_${UUID.randomUUID()}",
        ".mp4",
      ).absolutePath
      currentFilePath = filePath
      setOutputFile(filePath)
      setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      prepare()
      start()

      val startTime = clock.now()
      var amplitudes = emptyList<Int>()
      val samplesPerSecond = 20

      onStateUpdate(
        AudioRecordingStepState.AudioRecording.Recording(
          amplitudes = emptyList(),
          startedAt = startTime,
          filePath = filePath,
        ),
      )

      timer = Timer()
      timer?.schedule(
        timerTask {
          if (generation != armedGeneration) return@timerTask
          recorder?.maxAmplitude?.let { amplitude ->
            if (amplitude == 0) return@let
            amplitudes = amplitudes.plus(amplitude).takeLast((1.5 * samplesPerSecond).toInt())
            onStateUpdate(
              AudioRecordingStepState.AudioRecording.Recording(
                amplitudes = amplitudes.toList(),
                startedAt = startTime,
                filePath = filePath,
              ),
            )
          }
        },
        0L,
        1000L / samplesPerSecond,
      )
    }
  }

  override fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit) {
    val filePath = currentFilePath ?: return
    val armedGeneration = generation

    cleanupRecorder()

    val file = File(filePath)
    if (!file.exists()) {
      onStateUpdate(
        AudioRecordingStepState.AudioRecording.Playback(
          audioPath = AudioPath.FilePath(filePath),
          isPlaying = false,
          isPrepared = false,
          hasError = true,
        ),
      )
      return
    }

    player = MediaPlayer().apply {
      setDataSource(filePath)
      setOnPreparedListener {
        if (generation != armedGeneration) return@setOnPreparedListener
        onStateUpdate(
          AudioRecordingStepState.AudioRecording.Playback(
            audioPath = AudioPath.FilePath(filePath),
            isPlaying = false,
            isPrepared = true,
            hasError = false,
          ),
        )
      }
      setOnCompletionListener {
        // Playback completed
      }
      prepare()
    }
  }

  override fun getRecordedFile(): CommonFile? {
    val filePath = currentFilePath ?: return null
    val file = File(filePath)
    if (!file.exists()) return null

    return AndroidFile.fromFile(file, description = "Audio recording")
  }

  override fun cleanup() {
    generation++
    cleanupRecorder()
    cleanupPlayer()
  }

  override fun reset() {
    cleanup()
    currentFilePath = null
  }

  private fun cleanupRecorder() {
    timer?.cancel()
    timer = null

    recorder?.stop()
    recorder?.release()
    recorder = null
  }

  private fun cleanupPlayer() {
    player?.stop()
    player?.release()
    player = null
  }

  private inline fun timerTask(crossinline run: () -> Unit) = object : TimerTask() {
    override fun run() {
      run()
    }
  }
}
