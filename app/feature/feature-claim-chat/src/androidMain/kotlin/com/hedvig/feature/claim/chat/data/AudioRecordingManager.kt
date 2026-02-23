package com.hedvig.feature.claim.chat.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.hedvig.android.core.fileupload.AndroidFile
import com.hedvig.android.core.fileupload.CommonFile
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.time.Clock

internal class AndroidAudioRecordingManager(
  private val clock: Clock,
) : AudioRecordingManager {
  private var recorder: MediaRecorder? = null
  private var timer: Timer? = null
  private var player: MediaPlayer? = null
  private var currentFilePath: String? = null

  override fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit) {
    if (recorder != null) return // Already recording

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

    cleanupRecorder()

    player = MediaPlayer().apply {
      setDataSource(filePath)
      setOnPreparedListener {
        onStateUpdate(
          AudioRecordingStepState.AudioRecording.Playback(
            filePath = filePath,
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
