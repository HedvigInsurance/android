package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.HouseArrow: ImageVector
  get() {
    if (_HouseArrow != null) {
      return _HouseArrow!!
    }
    _HouseArrow = ImageVector.Builder(
      name = "HouseArrow",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(20f, 16.501f)
        verticalLineTo(18.251f)
        curveTo(20f, 19.079f, 19.328f, 19.751f, 18.5f, 19.751f)
        lineTo(5.5f, 19.751f)
        curveTo(4.672f, 19.751f, 4f, 19.079f, 4f, 18.251f)
        lineTo(4f, 10.431f)
        curveTo(4f, 9.999f, 4.187f, 9.587f, 4.512f, 9.302f)
        lineTo(10.683f, 3.903f)
        curveTo(11.437f, 3.243f, 12.563f, 3.243f, 13.317f, 3.903f)
        lineTo(19.511f, 9.555f)
        curveTo(19.823f, 9.839f, 20f, 10.241f, 20f, 10.663f)
        lineTo(20f, 18.001f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12.309f, 17f)
        lineTo(15.873f, 13.435f)
        curveTo(16.114f, 13.195f, 16.114f, 12.805f, 15.873f, 12.565f)
        lineTo(12.309f, 9f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(7.999f, 13f)
        lineTo(15.691f, 13f)
      }
    }.build()

    return _HouseArrow!!
  }

@Suppress("ObjectPropertyName")
private var _HouseArrow: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.HouseArrow,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
