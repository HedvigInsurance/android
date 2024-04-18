package com.hedvig.android.core.icons.hedvig.nav.hedvig

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
public val HedvigIcons.Home: ImageVector
  get() {
    if (_home != null) {
      return _home!!
    }
    _home = Builder(
      name = "Home tab",
      defaultWidth = 25.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 25.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 0.595f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(4.25f, 12.0f)
        curveTo(4.25f, 7.4436f, 7.9436f, 3.75f, 12.5f, 3.75f)
        curveTo(17.0563f, 3.75f, 20.75f, 7.4436f, 20.75f, 12.0f)
        curveTo(20.75f, 16.5563f, 17.0563f, 20.25f, 12.5f, 20.25f)
        curveTo(7.9436f, 20.25f, 4.25f, 16.5563f, 4.25f, 12.0f)
        close()
        moveTo(12.5f, 2.25f)
        curveTo(7.1152f, 2.25f, 2.75f, 6.6152f, 2.75f, 12.0f)
        curveTo(2.75f, 17.3848f, 7.1152f, 21.75f, 12.5f, 21.75f)
        curveTo(17.8848f, 21.75f, 22.25f, 17.3848f, 22.25f, 12.0f)
        curveTo(22.25f, 6.6152f, 17.8848f, 2.25f, 12.5f, 2.25f)
        close()
        moveTo(10.25f, 7.0f)
        horizontalLineTo(8.75f)
        lineTo(8.75f, 17.0f)
        horizontalLineTo(10.25f)
        verticalLineTo(12.75f)
        horizontalLineTo(14.75f)
        verticalLineTo(17.0f)
        horizontalLineTo(16.25f)
        lineTo(16.25f, 7.0f)
        horizontalLineTo(14.75f)
        verticalLineTo(11.25f)
        horizontalLineTo(10.25f)
        verticalLineTo(7.0f)
        close()
      }
    }
      .build()
    return _home!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _home: ImageVector? = null
