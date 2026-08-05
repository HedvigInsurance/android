package com.hedvig.android.design.system.hedvig.icon.colored

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

val HedvigIcons.ColoredFirstVetNoCircle: ImageVector
  get() {
    if (_FirstVetLogo != null) {
      return _FirstVetLogo!!
    }
    _FirstVetLogo = ImageVector.Builder(
      name = "FirstVetLogo",
      defaultWidth = 24.dp,
      defaultHeight = 19.dp,
      viewportWidth = 24f,
      viewportHeight = 19f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF0062FF)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(23.301f, 2.357f)
        lineTo(19.411f, 4.422f)
        curveTo(19.402f, 4.343f, 19.395f, 4.263f, 19.386f, 4.185f)
        curveTo(19.146f, 2.127f, 17.488f, 0.518f, 15.434f, 0.29f)
        curveTo(13.697f, 0.096f, 11.932f, 0f, 10.166f, 0f)
        horizontalLineTo(9.532f)
        curveTo(7.767f, 0f, 6.001f, 0.096f, 4.266f, 0.29f)
        curveTo(2.212f, 0.518f, 0.553f, 2.127f, 0.311f, 4.182f)
        curveTo(-0.104f, 7.715f, -0.104f, 11.283f, 0.311f, 14.815f)
        curveTo(0.553f, 16.871f, 2.212f, 18.482f, 4.266f, 18.709f)
        curveTo(6.001f, 18.902f, 7.767f, 19f, 9.532f, 19f)
        horizontalLineTo(10.166f)
        curveTo(11.93f, 19f, 13.697f, 18.904f, 15.432f, 18.709f)
        curveTo(17.485f, 18.482f, 19.144f, 16.871f, 19.386f, 14.815f)
        curveTo(19.395f, 14.735f, 19.402f, 14.657f, 19.411f, 14.576f)
        lineTo(23.298f, 16.641f)
        curveTo(23.617f, 16.808f, 24f, 16.58f, 24f, 16.221f)
        verticalLineTo(2.775f)
        curveTo(24f, 2.417f, 23.617f, 2.187f, 23.301f, 2.357f)
        close()
        moveTo(15.777f, 10.49f)
        curveTo(15.64f, 11.265f, 14.898f, 11.783f, 14.12f, 11.645f)
        lineTo(11.305f, 11.149f)
        lineTo(10.561f, 15.358f)
        lineTo(9.153f, 15.11f)
        curveTo(8.375f, 14.974f, 7.855f, 14.234f, 7.994f, 13.459f)
        lineTo(8.491f, 10.653f)
        lineTo(4.268f, 9.911f)
        lineTo(4.517f, 8.508f)
        curveTo(4.653f, 7.733f, 5.395f, 7.214f, 6.173f, 7.353f)
        lineTo(8.989f, 7.847f)
        lineTo(9.733f, 3.637f)
        lineTo(11.141f, 3.885f)
        curveTo(11.919f, 4.022f, 12.439f, 4.761f, 12.3f, 5.536f)
        lineTo(11.805f, 8.343f)
        lineTo(16.028f, 9.084f)
        lineTo(15.777f, 10.49f)
        close()
      }
    }.build()

    return _FirstVetLogo!!
  }

@Suppress("ObjectPropertyName")
private var _FirstVetLogo: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ColoredFirstVetNoCircle,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}
