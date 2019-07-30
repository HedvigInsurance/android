package com.hedvig.app.feature.chat

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.audio_recorder_view.view.*
import kotlinx.android.synthetic.main.loading_spinner.view.*
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class AudioRecorderView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle)

    private var elapsedTime: Disposable? = null

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private lateinit var requestPermission: () -> Unit
    private lateinit var uploadRecording: (String) -> Unit

    init {
        inflate(context, R.layout.audio_recorder_view, this)

        (redo as TextView).text = resources.getString(R.string.AUDIO_INPUT_REDO)
        (playback as TextView).text = resources.getString(R.string.AUDIO_INPUT_PLAY)
        (upload as TextView).text = resources.getString(R.string.AUDIO_INPUT_SAVE)

        startRecording.setHapticClickListener {
            if (context.hasPermissions(Manifest.permission.RECORD_AUDIO)) {
                triggerStartRecording()
            } else {
                requestPermission()
            }
        }

        stopRecording.setHapticClickListener {
            triggerStopRecording()
        }

        redo.setHapticClickListener {
            triggerRedo()
        }

        playback.setHapticClickListener {
            triggerPlayback()
        }

        upload.setHapticClickListener {
            triggerUpload()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    fun initialize(requestPermission: () -> Unit, uploadRecording: (String) -> Unit) {
        this.requestPermission = requestPermission
        this.uploadRecording = uploadRecording
    }

    fun permissionGranted() {
        triggerStartRecording()
    }

    private fun triggerStopRecording() {
        recordingContainer.remove()
        elapsedTime?.dispose()
        elapsedTime = null
        stopRecording.remove()
        stopRecording.pauseAnimation()
        recorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                Timber.e(e)
            }
            release()
        }
        recorder = null
        player?.apply {
            try {
                stop()
                reset()
            } catch (e: RuntimeException) {
                Timber.e(e)
            }
            release()
        }
        player = null

        optionsContainer.show()
    }

    private fun triggerStartRecording() {
        val filePath = getFilePath() ?: run {
            Timber.e("External cache dir is null")
            return@triggerStartRecording
        }
        startRecording.remove()

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e(e)
            }

            startStopwatch(R.string.AUDIO_INPUT_RECORDING)

            start()

        }

        stopRecording.show()
        stopRecording.progress = 0f
        stopRecording.playAnimation()
    }

    private fun triggerRedo() {
        optionsContainer.remove()
        recordingContainer.show()
        triggerStartRecording()
    }

    private fun triggerPlayback() {
        optionsContainer.remove()
        recordingContainer.show()
        stopRecording.progress = 0f
        stopRecording.playAnimation()
        stopRecording.show()

        player = MediaPlayer().apply {
            val filePath = getFilePath() ?: run {
                Timber.e("External cache dir is null")
                return@triggerPlayback
            }

            setDataSource(filePath)
            setOnPreparedListener {
                startStopwatch(R.string.AUDIO_INPUT_PLAYBACK_PROGRESS)
                start()
            }
            setOnCompletionListener {
                triggerStopRecording()
            }
            prepareAsync()

        }
    }

    private fun startStopwatch(@StringRes textKey: Int) {
        elapsedTime = Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ time ->
                recordingLabel.text = interpolateTextKey(
                    resources.getString(textKey),
                    "SECONDS" to time
                )
            }, { Timber.e(it) })
    }

    private fun triggerUpload() {
        optionsContainer.remove()
        loadingSpinner.show()
        val filePath = getFilePath() ?: run {
            Timber.e("External cache dir is null")
            return@triggerUpload
        }
        uploadRecording(filePath)
    }

    private fun getFilePath(): String? {
        val filePath = context.externalCacheDir?.absolutePath ?: return null
        return "$filePath/claim.aac"
    }
}
