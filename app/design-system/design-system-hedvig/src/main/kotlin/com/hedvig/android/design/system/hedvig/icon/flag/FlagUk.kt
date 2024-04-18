package com.hedvig.android.design.system.hedvig.icon.flag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("ktlint:standard:backing-property-naming")
private var _flagUk: ImageVector? = null

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.FlagUk: ImageVector
  get() {
    if (_flagUk != null) {
      return _flagUk!!
    }
    _flagUk = ImageVector.Builder(
      name = "Flaguk",
      defaultWidth = 24.0.dp,
      defaultHeight = 16.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 16.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF30577E)),
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = Butt,
        strokeLineJoin = Miter,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero,
      ) {
        moveTo(2.0f, 0.0f)
        lineTo(22.0f, 0.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 24.0f, 2.0f)
        lineTo(24.0f, 14.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 22.0f, 16.0f)
        lineTo(2.0f, 16.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 0.0f, 14.0f)
        lineTo(0.0f, 2.0f)
        arcTo(2.0f, 2.0f, 0.0f, false, true, 2.0f, 0.0f)
        close()
      }
      group {
        // region fix
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeLineWidth = 0.5f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(11.584f, 8.624f)
          lineTo(11.792f, 8.7627f)
          lineTo(11.9307f, 8.5547f)
          lineTo(12.4854f, 7.7226f)
          lineTo(12.624f, 7.5146f)
          lineTo(12.416f, 7.376f)
          lineTo(0.416f, -0.624f)
          lineTo(0.208f, -0.7627f)
          lineTo(0.0693f, -0.5547f)
          lineTo(-0.4854f, 0.2773f)
          lineTo(-0.624f, 0.4854f)
          lineTo(-0.416f, 0.624f)
          lineTo(11.584f, 8.624f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeLineWidth = 0.5f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(11.584f, 7.376f)
          lineTo(11.376f, 7.5146f)
          lineTo(11.5146f, 7.7226f)
          lineTo(12.0693f, 8.5547f)
          lineTo(12.208f, 8.7627f)
          lineTo(12.416f, 8.624f)
          lineTo(24.416f, 0.624f)
          lineTo(24.624f, 0.4854f)
          lineTo(24.4854f, 0.2773f)
          lineTo(23.9307f, -0.5547f)
          lineTo(23.792f, -0.7627f)
          lineTo(23.584f, -0.624f)
          lineTo(11.584f, 7.376f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeLineWidth = 0.5f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(-0.416f, 15.376f)
          lineTo(-0.624f, 15.5146f)
          lineTo(-0.4854f, 15.7226f)
          lineTo(0.0693f, 16.5547f)
          lineTo(0.208f, 16.7627f)
          lineTo(0.416f, 16.624f)
          lineTo(12.416f, 8.624f)
          lineTo(12.624f, 8.4854f)
          lineTo(12.4854f, 8.2773f)
          lineTo(11.9307f, 7.4453f)
          lineTo(11.792f, 7.2373f)
          lineTo(11.584f, 7.376f)
          lineTo(-0.416f, 15.376f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeLineWidth = 0.5f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(23.584f, 16.624f)
          lineTo(23.792f, 16.7627f)
          lineTo(23.9307f, 16.5547f)
          lineTo(24.4854f, 15.7226f)
          lineTo(24.624f, 15.5146f)
          lineTo(24.416f, 15.376f)
          lineTo(12.416f, 7.376f)
          lineTo(12.208f, 7.2373f)
          lineTo(12.0693f, 7.4453f)
          lineTo(11.5146f, 8.2773f)
          lineTo(11.376f, 8.4854f)
          lineTo(11.584f, 8.624f)
          lineTo(23.584f, 16.624f)
          close()
        }
//        path(
//          fill = SolidColor(Color(0xFFFF513A)),
//          stroke = null,
//          strokeLineWidth = 0.0f,
//          strokeLineCap = Butt,
//          strokeLineJoin = Miter,
//          strokeLineMiter = 4.0f,
//          pathFillType = EvenOdd,
//        ) {
//          moveTo(11.7227f, 8.416f)
//          lineTo(-0.2773f, 0.416f)
//          lineTo(0.2774f, -0.416f)
//          lineTo(12.2774f, 7.584f)
//          lineTo(11.7227f, 8.416f)
//          close()
//        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = EvenOdd,
        ) {
          moveTo(-0.2773f, 0.416f)
          lineTo(0.0f, -1.8028f)
          lineTo(13.6641f, 7.3066f)
          lineTo(11.7227f, 8.416f)
          lineTo(-0.2773f, 0.416f)
          close()
          moveTo(12.2774f, 7.584f)
          lineTo(11.7227f, 8.416f)
          lineTo(-0.2773f, 0.416f)
          lineTo(0.2774f, -0.416f)
          lineTo(12.2774f, 7.584f)
          close()
        }
//        path(
//          fill = SolidColor(Color(0xFFFF513A)),
//          stroke = null,
//          strokeLineWidth = 0.0f,
//          strokeLineCap = Butt,
//          strokeLineJoin = Miter,
//          strokeLineMiter = 4.0f,
//          pathFillType = EvenOdd,
//        ) {
//          moveTo(11.7227f, 7.584f)
//          lineTo(23.7227f, -0.416f)
//          lineTo(24.2774f, 0.416f)
//          lineTo(12.2774f, 8.416f)
//          lineTo(11.7227f, 7.584f)
//          close()
//        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = EvenOdd,
        ) {
          moveTo(23.7227f, -0.416f)
          lineTo(25.6641f, 0.6934f)
          lineTo(12.0f, 9.8028f)
          lineTo(11.7227f, 7.584f)
          lineTo(23.7227f, -0.416f)
          close()
          moveTo(12.2774f, 8.416f)
          lineTo(11.7227f, 7.584f)
          lineTo(23.7227f, -0.416f)
          lineTo(24.2774f, 0.416f)
          lineTo(12.2774f, 8.416f)
          close()
        }
//        path(
//          fill = SolidColor(Color(0xFFFF513A)),
//          stroke = null,
//          strokeLineWidth = 0.0f,
//          strokeLineCap = Butt,
//          strokeLineJoin = Miter,
//          strokeLineMiter = 4.0f,
//          pathFillType = EvenOdd,
//        ) {
//          moveTo(-0.2773f, 15.584f)
//          lineTo(11.7227f, 7.584f)
//          lineTo(12.2774f, 8.416f)
//          lineTo(0.2774f, 16.416f)
//          lineTo(-0.2773f, 15.584f)
//          close()
//        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = EvenOdd,
        ) {
          moveTo(12.0f, 6.1972f)
          lineTo(12.2774f, 8.416f)
          lineTo(0.2774f, 16.416f)
          lineTo(-1.6641f, 15.3066f)
          lineTo(12.0f, 6.1972f)
          close()
          moveTo(0.2774f, 16.416f)
          lineTo(-0.2773f, 15.584f)
          lineTo(11.7227f, 7.584f)
          lineTo(12.2774f, 8.416f)
          lineTo(0.2774f, 16.416f)
          close()
        }
//        path(
//          fill = SolidColor(Color(0xFFFF513A)),
//          stroke = null,
//          strokeLineWidth = 0.0f,
//          strokeLineCap = Butt,
//          strokeLineJoin = Miter,
//          strokeLineMiter = 4.0f,
//          pathFillType = EvenOdd,
//        ) {
//          moveTo(23.7227f, 16.416f)
//          lineTo(11.7227f, 8.416f)
//          lineTo(12.2774f, 7.584f)
//          lineTo(24.2774f, 15.584f)
//          lineTo(23.7227f, 16.416f)
//          close()
//        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = EvenOdd,
        ) {
          moveTo(10.3359f, 8.6934f)
          lineTo(12.2774f, 7.584f)
          lineTo(24.2774f, 15.584f)
          lineTo(24.0f, 17.8028f)
          lineTo(10.3359f, 8.6934f)
          close()
          moveTo(24.2774f, 15.584f)
          lineTo(23.7227f, 16.416f)
          lineTo(11.7227f, 8.416f)
          lineTo(12.2774f, 7.584f)
          lineTo(24.2774f, 15.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(0.0f, 6.0f)
          horizontalLineTo(24.0f)
          verticalLineTo(10.0f)
          horizontalLineTo(0.0f)
          verticalLineTo(6.0f)
          close()
        }
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          stroke = null,
          fillAlpha = 0.07f,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(23.5f, 6.0f)
          verticalLineTo(10.0f)
          horizontalLineTo(24.5f)
          verticalLineTo(6.0f)
          horizontalLineTo(23.5f)
          close()
          moveTo(0.5f, 10.0f)
          verticalLineTo(6.0f)
          horizontalLineTo(-0.5f)
          verticalLineTo(10.0f)
          horizontalLineTo(0.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(10.0f, 0.0f)
          horizontalLineTo(14.0f)
          verticalLineTo(16.0f)
          horizontalLineTo(10.0f)
          verticalLineTo(0.0f)
          close()
        }
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          stroke = null,
          fillAlpha = 0.07f,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(10.0f, 0.5f)
          horizontalLineTo(14.0f)
          verticalLineTo(-0.5f)
          horizontalLineTo(10.0f)
          verticalLineTo(0.5f)
          close()
          moveTo(14.0f, 15.5f)
          horizontalLineTo(10.0f)
          verticalLineTo(16.5f)
          horizontalLineTo(14.0f)
          verticalLineTo(15.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(11.0f, 0.0f)
          horizontalLineTo(13.0f)
          verticalLineTo(16.0f)
          horizontalLineTo(11.0f)
          verticalLineTo(0.0f)
          close()
        }
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          stroke = null,
          fillAlpha = 0.07f,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(11.0f, 0.5f)
          horizontalLineTo(13.0f)
          verticalLineTo(-0.5f)
          horizontalLineTo(11.0f)
          verticalLineTo(0.5f)
          close()
          moveTo(13.0f, 15.5f)
          horizontalLineTo(11.0f)
          verticalLineTo(16.5f)
          horizontalLineTo(13.0f)
          verticalLineTo(15.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          stroke = null,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(0.0f, 7.0f)
          horizontalLineTo(24.0f)
          verticalLineTo(9.0f)
          horizontalLineTo(0.0f)
          verticalLineTo(7.0f)
          close()
        }
      }
      group {
        path(
          fill = SolidColor(Color(0xFF121212)),
          stroke = null,
          fillAlpha = 0.07f,
          strokeLineWidth = 0.0f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(23.5f, 7.0f)
          verticalLineTo(9.0f)
          horizontalLineTo(24.5f)
          verticalLineTo(7.0f)
          horizontalLineTo(23.5f)
          close()
          moveTo(0.5f, 9.0f)
          verticalLineTo(7.0f)
          horizontalLineTo(-0.5f)
          verticalLineTo(9.0f)
          horizontalLineTo(0.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0x00000000)),
          stroke = SolidColor(Color(0xFF121212)),
          strokeAlpha = 0.07f,
          strokeLineWidth = 0.5f,
          strokeLineCap = Butt,
          strokeLineJoin = Miter,
          strokeLineMiter = 4.0f,
          pathFillType = NonZero,
        ) {
          moveTo(2.0f, 0.25f)
          lineTo(22.0f, 0.25f)
          arcTo(1.75f, 1.75f, 0.0f, false, true, 23.75f, 2.0f)
          lineTo(23.75f, 14.0f)
          arcTo(1.75f, 1.75f, 0.0f, false, true, 22.0f, 15.75f)
          lineTo(2.0f, 15.75f)
          arcTo(1.75f, 1.75f, 0.0f, false, true, 0.25f, 14.0f)
          lineTo(0.25f, 2.0f)
          arcTo(1.75f, 1.75f, 0.0f, false, true, 2.0f, 0.25f)
          close()
        }
      }
    }
      .build()
    return _flagUk!!
  }
