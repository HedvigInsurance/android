package com.hedvig.app.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun currentTimeAsState(
    updateInterval: Duration = 1.seconds,
    clock: Clock = Clock.systemUTC(),
): State<Instant> {
    return produceState(initialValue = Instant.now(clock)) {
        while (isActive) {
            delay(updateInterval)
            value = Instant.now(clock)
        }
    }
}
