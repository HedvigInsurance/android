package com.hedvig.android.design.system.hedvig.icon.colored

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

val HedvigIcons.Swish: ImageVector
  get() {
    if (_Swish != null) {
      return _Swish!!
    }
    _Swish = ImageVector.Builder(
      name = "Swish",
      defaultWidth = 420.dp,
      defaultHeight = 420.dp,
      viewportWidth = 420f,
      viewportHeight = 420f,
    ).apply {
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFFEF2131),
            1f to Color(0xFFFECF2C),
          ),
          start = Offset(237.8f, 289.7f),
          end = Offset(177.74f, 104.45f),
        ),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(119.3f, 399.2f)
        curveToRelative(84.3f, 40.3f, 188.3f, 20.4f, 251.2f, -54.5f)
        curveToRelative(74.5f, -88.8f, 62.9f, -221.1f, -25.8f, -295.5f)
        lineToRelative(-59f, 70.3f)
        curveToRelative(69.3f, 58.2f, 78.4f, 161.5f, 20.2f, 230.9f)
        curveToRelative(-46.4f, 55.3f, -122.8f, 73.7f, -186.5f, 48.9f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFFFBC52C),
            0.3f to Color(0xFFF87130),
            0.6f to Color(0xFFEF52E2),
            1f to Color(0xFF661EEC),
          ),
          start = Offset(379.9f, 129.59f),
          end = Offset(243f, 399.77f),
        ),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(119.3f, 399.2f)
        curveToRelative(84.3f, 40.3f, 188.3f, 20.4f, 251.2f, -54.5f)
        curveToRelative(7.7f, -9.2f, 14.5f, -18.8f, 20.3f, -28.8f)
        curveToRelative(9.9f, -61.7f, -11.9f, -126.9f, -63.2f, -169.9f)
        curveToRelative(-13f, -10.9f, -27.2f, -19.8f, -41.9f, -26.5f)
        curveToRelative(69.3f, 58.2f, 78.4f, 161.5f, 20.2f, 230.9f)
        curveToRelative(-46.4f, 55.3f, -122.8f, 73.7f, -186.5f, 48.9f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFF78F6D8),
            0.3f to Color(0xFF77D1F6),
            0.6f to Color(0xFF70A4F3),
            1f to Color(0xFF661EEC),
          ),
          start = Offset(118.21f, 92.5f),
          end = Offset(178.27f, 277.75f),
        ),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(300.3f, 20.4f)
        curveTo(216f, -19.9f, 111.9f, 0f, 49.1f, 74.9f)
        curveToRelative(-74.5f, 88.8f, -62.9f, 221.1f, 25.8f, 295.5f)
        lineToRelative(59f, -70.3f)
        curveToRelative(-69.3f, -58.2f, -78.4f, -161.5f, -20.2f, -230.9f)
        curveTo(160.2f, 14f, 236.6f, -4.5f, 300.3f, 20.4f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFF536EED),
            0.2f to Color(0xFF54C3EC),
            0.6f to Color(0xFF64D769),
            1f to Color(0xFFFECF2C),
          ),
          start = Offset(95.13f, 220.03f),
          end = Offset(232.03f, -50.15f),
        ),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(300.3f, 20.4f)
        curveTo(216f, -19.9f, 111.9f, 0f, 49.1f, 74.9f)
        curveToRelative(-7.7f, 9.2f, -14.5f, 18.8f, -20.3f, 28.8f)
        curveToRelative(-9.9f, 61.7f, 11.9f, 126.9f, 63.2f, 169.9f)
        curveToRelative(13f, 10.9f, 27.2f, 19.8f, 41.9f, 26.5f)
        curveToRelative(-69.3f, -58.2f, -78.4f, -161.5f, -20.2f, -230.9f)
        curveTo(160.2f, 14f, 236.6f, -4.5f, 300.3f, 20.4f)
      }
    }.build()

    return _Swish!!
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Swish,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName")
private var _Swish: ImageVector? = null
