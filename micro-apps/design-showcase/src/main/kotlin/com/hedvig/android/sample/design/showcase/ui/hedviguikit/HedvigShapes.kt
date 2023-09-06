package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.material3.squircleExtraLarge
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.squircleSmall
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun HedvigShapes(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().zoomable(rememberZoomState(20f)),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      for (i in 1..4) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          for (j in 1..5) {
            Box(
              Modifier
                .background(Color(0xFF121212 + (0x323212 * i)), j.toSquircleShape())
                .size(16.dp * i),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Int.toSquircleShape(): Shape {
  return when (this) {
    1 -> MaterialTheme.shapes.squircleExtraSmall
    2 -> MaterialTheme.shapes.squircleSmall
    3 -> MaterialTheme.shapes.squircleMedium
    4 -> MaterialTheme.shapes.squircleLarge
    5 -> MaterialTheme.shapes.squircleExtraLarge
    else -> error("")
  }
}

@Composable
fun ShapePlayground() {
  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(Modifier.zoomable(rememberZoomState(20f)).padding(horizontal = 16.dp)) {
      var cornerRadius by remember { mutableFloatStateOf(12f) }
      var smoothing by remember { mutableFloatStateOf(1f) }

      @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
      val squircleShape: Shape = com.hedvig.android.core.designsystem.material3.FigmaShape(
        cornerRadius.dp,
        smoothing,
      )
      Box(
        modifier = Modifier
          .graphicsLayer(shape = squircleShape)
          .height(64.dp)
          .fillMaxWidth()
          .background(Color(0xFFFBEDC5), squircleShape)
          .border(Dp.Hairline, Color(0xFFFFBF00), squircleShape),
      ) {
        Text(
          "A short message about something that needs attention, an error, info or...",
          fontSize = 14.sp,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
      }
      Slider(cornerRadius, { cornerRadius = it }, valueRange = 0f..50f)
      Text("corner radius:${cornerRadius.dp}")
      Slider(smoothing, { smoothing = it }, valueRange = 0f..1f)
      Text("smoothing:$smoothing")
    }
  }
}

@Preview
@Composable
private fun PreviewShapePlayground() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ShapePlayground()
    }
  }
}
