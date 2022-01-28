package com.hedvig.app.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Instant

@Composable
fun currentTimeAsState(
    updateIntervalInSeconds: Long = 1L,
    clock: Clock = Clock.systemUTC(),
): State<Instant> {
    return produceState(initialValue = Instant.now(clock)) {
        while (isActive) {
            delay(updateIntervalInSeconds * 1_000)
            value = Instant.now(clock)
        }
    }
}
