package com.hedvig.app.feature.chat.ui

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.hedvig.app.R
import com.hedvig.app.databinding.AudioRecorderViewBinding
import com.hedvig.app.feature.chat.service.ChatTracker
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class AudioRecorderView : FrameLayout {
    private val binding by viewBinding(AudioRecorderViewBinding::bind)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    private var elapsedTime: Disposable? = null

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private lateinit var requestPermission: () -> Unit
    private lateinit var uploadRecording: (String) -> Unit
    private lateinit var tracker: ChatTracker

    init {
        inflate(context, R.layout.audio_recorder_view, this)

        binding.apply {
            redo.root.setText(R.string.AUDIO_INPUT_REDO)
            playback.root.setText(R.string.AUDIO_INPUT_PLAY)
            upload.root.setText(R.string.AUDIO_INPUT_SAVE)
            startRecording.setHapticClickListener {
                tracker.recordClaim()
                if (context.hasPermissions(Manifest.permission.RECORD_AUDIO)) {
                    triggerStartRecording()
                } else {
                    requestPermission()
                }
            }

            stopRecording.setHapticClickListener {
                tracker.stopRecording()
                triggerStopRecording()
            }

            redo.root.setHapticClickListener {
                tracker.redoClaim()
                triggerRedo()
            }

            playback.root.setHapticClickListener {
                tracker.playClaim()
                triggerPlayback()
            }

            upload.root.setHapticClickListener {
                tracker.uploadClaim()
                triggerUpload()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    fun initialize(
        requestPermission: () -> Unit,
        uploadRecording: (String) -> Unit,
        tracker: ChatTracker
    ) {
        this.requestPermission = requestPermission
        this.uploadRecording = uploadRecording
        this.tracker = tracker
    }

    fun permissionGranted() {
        triggerStartRecording()
    }

    private fun triggerStopRecording() {
        binding.apply {
            recordingContainer.remove()
            elapsedTime?.dispose()
            elapsedTime = null
            stopRecording.remove()
            stopRecording.pauseAnimation()
            recorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    e(e)
                }
                release()
            }
            recorder = null
            player?.apply {
                try {
                    stop()
                    reset()
                } catch (e: RuntimeException) {
                    e(e)
                }
                release()
            }
            player = null

            optionsContainer.show()
        }
    }

    private fun triggerStartRecording() {
        val filePath = getFilePath() ?: run {
            e { "External cache dir is null" }
            return@triggerStartRecording
        }
        binding.startRecording.remove()

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                e(e)
            }

            startStopwatch(R.string.AUDIO_INPUT_RECORDING)

            start()

        }

        binding.stopRecording.apply {
            show()
            progress = 0f
            playAnimation()
        }
    }

    private fun triggerRedo() {
        binding.apply {
            optionsContainer.remove()
            recordingContainer.show()
            triggerStartRecording()
        }
    }

    private fun triggerPlayback() {
        binding.apply {
            optionsContainer.remove()
            recordingContainer.show()
            stopRecording.progress = 0f
            stopRecording.playAnimation()
            stopRecording.show()
        }

        player = MediaPlayer().apply {
            try {
                val filePath = getFilePath() ?: run {
                    e { "External cache dir is null" }
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
            } catch (iOException: IOException) {
                e { "$iOException IOException on trigger playback" }
                context.makeToast(R.string.CHAT_AUDIO__PLAYBACK_FAILED)
            }
        }
    }

    private fun startStopwatch(@StringRes textKey: Int) {
        elapsedTime = Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ time ->
                binding.recordingLabel.text = resources.getString(textKey, time)
            }, { e(it) })
    }

    private fun triggerUpload() {
        binding.apply {
            optionsContainer.remove()
            loadingSpinner.loadingSpinner.show()
        }
        val filePath = getFilePath() ?: run {
            e { "External cache dir is null" }
            return@triggerUpload
        }
        uploadRecording(filePath)
    }

    private fun getFilePath(): String? {
        val filePath = context.externalCacheDir?.absolutePath ?: return null
        return "$filePath/claim.aac"
    }
}
