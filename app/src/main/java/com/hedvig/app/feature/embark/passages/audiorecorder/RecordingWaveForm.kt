package com.hedvig.app.feature.embark.passages.audiorecorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

private val color = Color.Black

@Composable
fun RecordingWaveForm(
    amplitudes: List<Int>,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Canvas(
        Modifier
            .height(200.dp)
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { size = it }
    ) {
        val maxAmplitudes = size.width / 10
        amplitudes
            .takeLast(maxAmplitudes)
            .forEachIndexed { index, amplitude ->
                val x = index * 10f
                val vCenter = 200.dp.toPx() / 2
                val y = (amplitude / 16f) + 3.dp.toPx()
                drawLine(
                    color,
                    Offset(x, vCenter + y),
                    Offset(x, vCenter - y),
                    2.dp.toPx(),
                )
            }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordingWaveFormPreview() {
    RecordingWaveForm(
        listOf(
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
        )
    )
}
