package com.hedvig.android.core.designsystem.newtheme

import android.graphics.PointF
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
    }
    return Outline.Generic(path)
  }

  private fun Path.drawSquircle(size: Size, cornerRadius: Float) {
    val topLeft = PointF(0f, 0f)
    val topRight = PointF(size.width, 0f)
    val bottomLeft = PointF(0f, size.height)
    val bottomRight = PointF(size.width, size.height)

    // The points are enumerated as a chain of points going around the shape clockwise
    // Top side, left and right points.
    val p0 = PointF(0f + cornerRadius, 0f)
    val p1 = PointF(size.width - cornerRadius, 0f)

    // Right side, top and bottom points.
    val p2 = PointF(size.width, 0f + cornerRadius)
    val p3 = PointF(size.width, size.height - cornerRadius)

    // Bottom side, right and left points.
    val p4 = PointF(size.width - cornerRadius, size.height)
    val p5 = PointF(0f + cornerRadius, size.height)

    // Right side, bottom and top points.
    val p6 = PointF(0f, size.height - cornerRadius)
    val p7 = PointF(0f, 0f + cornerRadius)

    moveTo(p0)
    lineTo(p1)
    cubicTo(p2, topRight, topRight)
    lineTo(p3)
    cubicTo(p4, bottomRight, bottomRight)
    lineTo(p5)
    cubicTo(p6, bottomLeft, bottomLeft)
    lineTo(p7)
    cubicTo(p0, topLeft, topLeft)
  }

  private fun Path.moveTo(to: PointF) {
    moveTo(x = to.x, y = to.y)
  }

  private fun Path.lineTo(to: PointF) {
    lineTo(x = to.x, y = to.y)
  }

  private fun Path.cubicTo(to: PointF, controlPoint1: PointF, controlPoint2: PointF) {
    cubicTo(
      x1 = controlPoint1.x,
      y1 = controlPoint1.y,
      x2 = controlPoint2.x,
      y2 = controlPoint2.y,
      x3 = to.x,
      y3 = to.y,
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
