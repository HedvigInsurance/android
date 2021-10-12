package com.hedvig.app.feature.embark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderFragment
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderParameters

class RecorderScaffoldActivity : AppCompatActivity(R.layout.fragment_container_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .commit(allowStateLoss = true) {
                replace(R.id.container, AudioRecorderFragment.newInstance(AudioRecorderParameters(listOf("Hello"))))
            }
    }
}
