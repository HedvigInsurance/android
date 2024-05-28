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
public val HedvigIcons.Checkmark: ImageVector
  get() {
    if (_tick != null) {
      return _tick!!
    }
    _tick = Builder(
      name = "Checkmark",
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
        moveTo(15.5117f, 3.4517f)
        curveTo(15.8146f, 3.7343f, 15.8309f, 4.2089f, 15.5483f, 4.5117f)
        lineTo(7.6771f, 12.9452f)
        curveTo(6.5899f, 14.11f, 4.7435f, 14.11f, 3.6563f, 12.9452f)
        lineTo(0.4517f, 9.5117f)
        curveTo(0.1691f, 9.2089f, 0.1855f, 8.7343f, 0.4883f, 8.4517f)
        curveTo(0.7911f, 8.1691f, 1.2657f, 8.1854f, 1.5483f, 8.4883f)
        lineTo(4.7529f, 11.9217f)
        curveTo(5.247f, 12.4512f, 6.0863f, 12.4512f, 6.5805f, 11.9217f)
        lineTo(14.4517f, 3.4883f)
        curveTo(14.7343f, 3.1855f, 15.2089f, 3.1691f, 15.5117f, 3.4517f)
        close()
      }
    }
      .build()
    return _tick!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _tick: ImageVector? = null
