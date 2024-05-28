package com.hedvig.android.core.icons.hedvig.small.hedvig

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Lock: ImageVector
  get() {
    if (_lock != null) {
      return _lock!!
    }
    _lock = Builder(
      name = "Lock",
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
        pathFillType = EvenOdd,
      ) {
        moveTo(8.0f, 1.25f)
        curveTo(5.9289f, 1.25f, 4.25f, 2.9289f, 4.25f, 5.0f)
        verticalLineTo(7.0f)
        horizontalLineTo(4.0f)
        curveTo(2.8954f, 7.0f, 2.0f, 7.8954f, 2.0f, 9.0f)
        verticalLineTo(13.0f)
        curveTo(2.0f, 14.1046f, 2.8954f, 15.0f, 4.0f, 15.0f)
        horizontalLineTo(12.0f)
        curveTo(13.1046f, 15.0f, 14.0f, 14.1046f, 14.0f, 13.0f)
        verticalLineTo(9.0f)
        curveTo(14.0f, 7.8954f, 13.1046f, 7.0f, 12.0f, 7.0f)
        horizontalLineTo(11.75f)
        verticalLineTo(5.0f)
        curveTo(11.75f, 2.9289f, 10.0711f, 1.25f, 8.0f, 1.25f)
        close()
        moveTo(10.25f, 7.0f)
        verticalLineTo(5.0f)
        curveTo(10.25f, 3.7574f, 9.2426f, 2.75f, 8.0f, 2.75f)
        curveTo(6.7574f, 2.75f, 5.75f, 3.7574f, 5.75f, 5.0f)
        verticalLineTo(7.0f)
        horizontalLineTo(10.25f)
        close()
      }
    }
      .build()
    return _lock!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _lock: ImageVector? = null
