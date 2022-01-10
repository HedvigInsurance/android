package com.hedvig.app.service.audioplayer

import com.hedvig.app.util.ProgressPercentage
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val audioPlayerState: StateFlow<AudioPlayerState>

    fun pausePlayer()
    fun startPlayer()
    fun seekTo(progressPercentage: ProgressPercentage)
}
