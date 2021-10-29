package com.hedvig.app.feature.embark.passages.audiorecorder

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import i
import kotlin.math.sqrt

@Composable
fun RecordingAmplitudeIndicator(amplitude: Int) {
    val color = MaterialTheme.colors.primary.copy(alpha = 0.12f)

    val animated by animateIntAsState(
        targetValue = (sqrt(amplitude.toDouble()).toInt() * 10).coerceAtMost(1000),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
        )
    )

    Canvas(modifier = Modifier) {
        drawCircle(
            color = color,
            radius = animated.toFloat(),
        )
    }
}
