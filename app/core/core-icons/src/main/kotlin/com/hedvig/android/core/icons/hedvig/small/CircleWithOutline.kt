package com.hedvig.android.core.icons.hedvig.small.hedvig

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.CircleWithOutline: ImageVector
  get() {
    if (_circleWithOutline != null) {
      return _circleWithOutline!!
    }
    _circleWithOutline = Builder(
      name = "Circle with outline",
      defaultWidth = 16.0.dp,
      defaultHeight = 16.0.dp,
      viewportWidth = 16.0f,
      viewportHeight = 16.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0x00000000)),
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = NonZero,
      ) {
        moveTo(8.0f, 8.0f)
        moveToRelative(-6.25f, 0.0f)
        arcToRelative(6.25f, 6.25f, 0.0f, true, true, 12.5f, 0.0f)
        arcToRelative(6.25f, 6.25f, 0.0f, true, true, -12.5f, 0.0f)
      }
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = EvenOdd,
      ) {
        moveTo(8.0f, 13.5f)
        curveTo(11.0376f, 13.5f, 13.5f, 11.0376f, 13.5f, 8.0f)
        curveTo(13.5f, 4.9624f, 11.0376f, 2.5f, 8.0f, 2.5f)
        curveTo(4.9624f, 2.5f, 2.5f, 4.9624f, 2.5f, 8.0f)
        curveTo(2.5f, 11.0376f, 4.9624f, 13.5f, 8.0f, 13.5f)
        close()
        moveTo(8.0f, 15.0f)
        curveTo(11.866f, 15.0f, 15.0f, 11.866f, 15.0f, 8.0f)
        curveTo(15.0f, 4.134f, 11.866f, 1.0f, 8.0f, 1.0f)
        curveTo(4.134f, 1.0f, 1.0f, 4.134f, 1.0f, 8.0f)
        curveTo(1.0f, 11.866f, 4.134f, 15.0f, 8.0f, 15.0f)
        close()
      }
    }
      .build()
    return _circleWithOutline!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _circleWithOutline: ImageVector? = null
