package com.hedvig.android.core.icons.hedvig.small.hedvig

import androidx.compose.ui.graphics.Color
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
public val HedvigIcons.Sound: ImageVector
  get() {
    if (_sound != null) {
      return _sound!!
    }
    _sound = Builder(
      name = "Sound",
      defaultWidth = 16.0.dp,
      defaultHeight = 16.0.dp,
      viewportWidth = 16.0f,
      viewportHeight = 16.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = NonZero,
      ) {
        moveTo(3.0f, 3.0f)
        curveTo(3.0f, 2.4477f, 3.4477f, 2.0f, 4.0f, 2.0f)
        curveTo(4.5523f, 2.0f, 5.0f, 2.4477f, 5.0f, 3.0f)
        verticalLineTo(13.0f)
        curveTo(5.0f, 13.5523f, 4.5523f, 14.0f, 4.0f, 14.0f)
        curveTo(3.4477f, 14.0f, 3.0f, 13.5523f, 3.0f, 13.0f)
        verticalLineTo(3.0f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = NonZero,
      ) {
        moveTo(7.0f, 9.0f)
        curveTo(7.0f, 8.4477f, 7.4477f, 8.0f, 8.0f, 8.0f)
        curveTo(8.5523f, 8.0f, 9.0f, 8.4477f, 9.0f, 9.0f)
        verticalLineTo(13.0f)
        curveTo(9.0f, 13.5523f, 8.5523f, 14.0f, 8.0f, 14.0f)
        curveTo(7.4477f, 14.0f, 7.0f, 13.5523f, 7.0f, 13.0f)
        verticalLineTo(9.0f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = NonZero,
      ) {
        moveTo(12.0f, 4.0f)
        curveTo(11.4477f, 4.0f, 11.0f, 4.4477f, 11.0f, 5.0f)
        verticalLineTo(13.0f)
        curveTo(11.0f, 13.5523f, 11.4477f, 14.0f, 12.0f, 14.0f)
        curveTo(12.5523f, 14.0f, 13.0f, 13.5523f, 13.0f, 13.0f)
        verticalLineTo(5.0f)
        curveTo(13.0f, 4.4477f, 12.5523f, 4.0f, 12.0f, 4.0f)
        close()
      }
    }
      .build()
    return _sound!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _sound: ImageVector? = null
