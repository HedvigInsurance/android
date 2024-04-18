package com.hedvig.android.design.system.hedvig.icon.flag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.FlagSweden: ImageVector
  get() {
    if (_flagSweden != null) {
      return _flagSweden!!
    }
    _flagSweden = Builder(
      name = "Flag of Sweden",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF59BFFA)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(2.0f, 4.0f)
        lineTo(22.0f, 4.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 24.0f, 6.0f)
        lineTo(24.0f, 18.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 22.0f, 20.0f)
        lineTo(2.0f, 20.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 0.0f, 18.0f)
        lineTo(0.0f, 6.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 2.0f, 4.0f)
        close()
      }
      path(
        fill = SolidColor(Color(0x00000000)),
        stroke = SolidColor(Color(0xFF121212)),
        strokeAlpha = 0.07f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(2.0f, 4.25f)
        lineTo(22.0f, 4.25f)
        arcTo(1.75f, 1.75f, 0.0f, false, true, 23.75f, 6.0f)
        lineTo(23.75f, 18.0f)
        arcTo(1.75f, 1.75f, 0.0f, false, true, 22.0f, 19.75f)
        lineTo(2.0f, 19.75f)
        arcTo(1.75f, 1.75f, 0.0f, false, true, 0.25f, 18.0f)
        lineTo(0.25f, 6.0f)
        arcTo(1.75f, 1.75f, 0.0f, false, true, 2.0f, 4.25f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFFF266)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(0.0f, 10.5f)
        horizontalLineTo(24.0f)
        verticalLineTo(13.5f)
        horizontalLineTo(0.0f)
        verticalLineTo(10.5f)
        close()
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          fillAlpha = 0.07f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          pathFillType = NonZero,
        ) {
          moveTo(23.5f, 10.5f)
          verticalLineTo(13.5f)
          horizontalLineTo(24.5f)
          verticalLineTo(10.5f)
          horizontalLineTo(23.5f)
          close()
          moveTo(0.5f, 13.5f)
          verticalLineTo(10.5f)
          horizontalLineTo(-0.5f)
          verticalLineTo(13.5f)
          horizontalLineTo(0.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFFF266)),
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          pathFillType = NonZero,
        ) {
          moveTo(8.0f, 4.0f)
          horizontalLineTo(11.0f)
          verticalLineTo(20.0f)
          horizontalLineTo(8.0f)
          verticalLineTo(4.0f)
          close()
        }
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          fillAlpha = 0.07f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          pathFillType = NonZero,
        ) {
          moveTo(8.0f, 4.5f)
          horizontalLineTo(11.0f)
          verticalLineTo(4f)
          horizontalLineTo(8.0f)
          verticalLineTo(4.5f)
          close()
          moveTo(11.0f, 19.5f)
          horizontalLineTo(8.0f)
          verticalLineTo(20f)
          horizontalLineTo(11.0f)
          verticalLineTo(19.5f)
          close()
        }
      }
    }
      .build()
    return _flagSweden!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _flagSweden: ImageVector? = null
