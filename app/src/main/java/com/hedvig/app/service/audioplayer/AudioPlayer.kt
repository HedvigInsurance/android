package com.hedvig.app.service.audioplayer

import com.hedvig.app.util.ProgressPercentage
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

interface AudioPlayer : Closeable {
    val audioPlayerState: StateFlow<AudioPlayerState>

    fun initialize()
    fun startPlayer()
    fun pausePlayer()
    fun seekTo(progressPercentage: ProgressPercentage)
}
