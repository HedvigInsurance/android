package com.hedvig.app.service.audioplayer

import androidx.annotation.FloatRange
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val audioPlayerState: StateFlow<AudioPlayerState>

    fun pausePlayer()
    fun startPlayer()
    fun seekTo(@FloatRange(from = 0.0, to = 1.0) percentage: Float)
}
