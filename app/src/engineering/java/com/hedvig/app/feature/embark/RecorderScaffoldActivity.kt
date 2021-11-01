package com.hedvig.app.feature.embark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderFragment
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderParameters
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows

class RecorderScaffoldActivity : AppCompatActivity(R.layout.fragment_container_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)

        supportFragmentManager
            .commit(allowStateLoss = true) {
                replace(
                    R.id.container,
                    AudioRecorderFragment.newInstance(
                        AudioRecorderParameters(
                            messages = listOf("Hello"),
                            key = "test",
                            label = "recordLabel",
                            link = "nextPassage"
                        )
                    )
                )
            }
    }
}
