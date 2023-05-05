package com.hedvig.android.core.designsystem.newtheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import java.lang.Float.min

// 27 magic number, matches the figma file close enough
private val squircleRadius = 27.dp

val SquircleShape = object : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val path = Path().apply {
      val atMostHalfSizeCornerRadius = min(
        with(density) { squircleRadius.toPx() },
        min(size.height / 2, size.height / 2),
      )
      drawSquircle(size, atMostHalfSizeCornerRadius)
      close()
    }
    return Outline.Generic(path)
  }

  private fun Path.drawSquircle(size: Size, cornerRadius: Float) {
    moveTo(x = 0f, y = cornerRadius)
    cubicTo(
      x1 = 0f,
      y1 = 0f,
      x2 = 0f,
      y2 = 0f,
      x3 = cornerRadius,
      y3 = 0f,
    )
    lineTo(x = size.width - cornerRadius, y = 0f)
    cubicTo(
      x1 = size.width,
      y1 = 0f,
      x2 = size.width,
      y2 = 0f,
      x3 = size.width,
      y3 = cornerRadius,
    )
    lineTo(x = size.width, y = size.height - cornerRadius)
    cubicTo(
      x1 = size.width,
      y1 = size.height,
      x2 = size.width,
      y2 = size.height,
      x3 = size.width - cornerRadius,
      y3 = size.height,
    )
    lineTo(x = cornerRadius, y = size.height)
    cubicTo(
      x1 = 0f,
      y1 = size.height,
      x2 = 0f,
      y2 = size.height,
      x3 = 0f,
      y3 = size.height - cornerRadius,
    )
  }
}

@Preview
@Composable
private fun PreviewSquircleShape() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier, Alignment.Center) {
        Spacer(
          modifier = Modifier
            .padding(10.dp)
            .background(Color.Red, SquircleShape)
            .width(100.dp)
            .height(50.dp),
        )
      }
    }
  }
}
