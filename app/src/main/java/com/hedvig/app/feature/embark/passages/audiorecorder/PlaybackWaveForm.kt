package com.hedvig.app.feature.embark.passages.audiorecorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlin.math.ceil

@Composable
fun PlaybackWaveForm(
    isPlaying: Boolean,
    play: () -> Unit,
    pause: () -> Unit,
    amplitudes: List<Int>,
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = if (isPlaying) {
                    pause
                } else {
                    play
                },
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                )
            ) {
                Image(
                    painterResource(
                        id = if (isPlaying) {
                            R.drawable.ic_pause
                        } else {
                            R.drawable.ic_play
                        }
                    ),
                    contentDescription = if (isPlaying) {
                        "Pause"
                    } else {
                        "Play"
                    }
                )
            }

            var size by remember { mutableStateOf(IntSize.Zero) }
            val sampledAmplitudes by derivedStateOf {
                val maxAmplitudes = size.width / 10
                val sampleSize = ceil(amplitudes.size / maxAmplitudes.toDouble()).toInt()
                if (sampleSize > 1) {
                    amplitudes
                        .chunked(sampleSize)
                        .map { it.average().toInt() }
                } else {
                    amplitudes
                }
            }
            val playedColor = MaterialTheme.colors.primary
            val notPlayedColor = MaterialTheme.colors.primary.copy(alpha = 0.12f)

            Canvas(
                modifier = Modifier
                    .padding(16.dp)
                    .height(80.dp)
                    .weight(1f, fill = true)
                    .clipToBounds()
                    .onSizeChanged { size = it }
            ) {
                val sampledAmplitudesSize = sampledAmplitudes.size
                sampledAmplitudes
                    .forEachIndexed { index, amplitude ->
                        val percentage = ((index + 1f) / sampledAmplitudesSize).coerceAtMost(1f)
                        val x = index * 10f
                        val vCenter = 80.dp.toPx() / 2
                        val y = (amplitude / 16f) + 3.dp.toPx()
                        drawLine(
                            color = if (percentage <= progress) {
                                playedColor
                            } else {
                                notPlayedColor
                            },
                            start = Offset(x, vCenter + y),
                            end = Offset(x, vCenter - y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaybackWaveFormPreview() {
    HedvigTheme {
        PlaybackWaveForm(
            isPlaying = false,
            play = {},
            pause = {},
            amplitudes = listOf(
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
                100, 200, 150, 250, 0,
                100, 200, 150, 250, 0,
                100, 200, 150, 250, 0,
                100, 200, 150, 250, 0,
                100, 200, 150, 250, 0,
            ),
            progress = 0.5f,
        )
    }
}
