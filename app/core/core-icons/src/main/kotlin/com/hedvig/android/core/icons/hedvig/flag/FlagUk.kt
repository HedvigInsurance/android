package com.hedvig.android.core.icons.hedvig.flag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

private var _flagUk: ImageVector? = null

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.FlagUk: ImageVector
  get() {
    if (_flagUk != null) {
      return _flagUk!!
    }
    _flagUk = ImageVector.Builder(
      name = "Flag of UK",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF30577E)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(2f, 4f)
        horizontalLineTo(22f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 6f)
        verticalLineTo(18f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 20f)
        horizontalLineTo(2f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 18f)
        verticalLineTo(6f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 4f)
        close()
      }
      group {
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11.584f, 12.624f)
          lineTo(11.792f, 12.7627f)
          lineTo(11.9307f, 12.5547f)
          lineTo(12.4854f, 11.7226f)
          lineTo(12.624f, 11.5146f)
          lineTo(12.416f, 11.376f)
          lineTo(0.416031f, 3.37596f)
          lineTo(0.208019f, 3.23728f)
          lineTo(0.0693439f, 3.44529f)
          lineTo(-0.485356f, 4.27734f)
          lineTo(-0.624031f, 4.48536f)
          lineTo(-0.416019f, 4.62403f)
          lineTo(11.584f, 12.624f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11.584f, 11.376f)
          lineTo(11.376f, 11.5146f)
          lineTo(11.5146f, 11.7226f)
          lineTo(12.0693f, 12.5547f)
          lineTo(12.208f, 12.7627f)
          lineTo(12.416f, 12.624f)
          lineTo(24.416f, 4.62403f)
          lineTo(24.624f, 4.48536f)
          lineTo(24.4854f, 4.27734f)
          lineTo(23.9307f, 3.44529f)
          lineTo(23.792f, 3.23728f)
          lineTo(23.584f, 3.37596f)
          lineTo(11.584f, 11.376f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(-0.416019f, 19.376f)
          lineTo(-0.624031f, 19.5146f)
          lineTo(-0.485356f, 19.7226f)
          lineTo(0.0693439f, 20.5547f)
          lineTo(0.208019f, 20.7627f)
          lineTo(0.416031f, 20.624f)
          lineTo(12.416f, 12.624f)
          lineTo(12.624f, 12.4854f)
          lineTo(12.4854f, 12.2773f)
          lineTo(11.9307f, 11.4453f)
          lineTo(11.792f, 11.2373f)
          lineTo(11.584f, 11.376f)
          lineTo(-0.416019f, 19.376f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(23.584f, 20.624f)
          lineTo(23.792f, 20.7627f)
          lineTo(23.9307f, 20.5547f)
          lineTo(24.4854f, 19.7226f)
          lineTo(24.624f, 19.5146f)
          lineTo(24.416f, 19.376f)
          lineTo(12.416f, 11.376f)
          lineTo(12.208f, 11.2373f)
          lineTo(12.0693f, 11.4453f)
          lineTo(11.5146f, 12.2773f)
          lineTo(11.376f, 12.4854f)
          lineTo(11.584f, 12.624f)
          lineTo(23.584f, 20.624f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(11.7227f, 12.416f)
          lineTo(-0.277344f, 4.41602f)
          lineTo(0.277356f, 3.58397f)
          lineTo(12.2774f, 11.584f)
          lineTo(11.7227f, 12.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(-0.277344f, 4.41602f)
          lineTo(0.000005126f, 2.19722f)
          lineTo(13.6641f, 11.3066f)
          lineTo(11.7227f, 12.416f)
          lineTo(-0.277344f, 4.41602f)
          close()
          moveTo(12.2774f, 11.584f)
          lineTo(11.7227f, 12.416f)
          lineTo(-0.277344f, 4.41602f)
          lineTo(0.277355f, 3.58397f)
          lineTo(12.2774f, 11.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(11.7227f, 11.584f)
          lineTo(23.7227f, 3.58397f)
          lineTo(24.2774f, 4.41602f)
          lineTo(12.2774f, 12.416f)
          lineTo(11.7227f, 11.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(23.7227f, 3.58397f)
          lineTo(25.6641f, 4.69337f)
          lineTo(12f, 13.8028f)
          lineTo(11.7227f, 11.584f)
          lineTo(23.7227f, 3.58397f)
          close()
          moveTo(12.2774f, 12.416f)
          lineTo(11.7227f, 11.584f)
          lineTo(23.7227f, 3.58397f)
          lineTo(24.2774f, 4.41602f)
          lineTo(12.2774f, 12.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(-0.277344f, 19.584f)
          lineTo(11.7227f, 11.584f)
          lineTo(12.2774f, 12.416f)
          lineTo(0.277356f, 20.416f)
          lineTo(-0.277344f, 19.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(12f, 10.1972f)
          lineTo(12.2774f, 12.416f)
          lineTo(0.277358f, 20.416f)
          lineTo(-1.66409f, 19.3066f)
          lineTo(12f, 10.1972f)
          close()
          moveTo(0.277358f, 20.416f)
          lineTo(-0.277343f, 19.584f)
          lineTo(11.7227f, 11.584f)
          lineTo(12.2774f, 12.416f)
          lineTo(0.277358f, 20.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(23.7227f, 20.416f)
          lineTo(11.7227f, 12.416f)
          lineTo(12.2774f, 11.584f)
          lineTo(24.2774f, 19.584f)
          lineTo(23.7227f, 20.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(10.3359f, 12.6934f)
          lineTo(12.2774f, 11.584f)
          lineTo(24.2774f, 19.584f)
          lineTo(24f, 21.8028f)
          lineTo(10.3359f, 12.6934f)
          close()
          moveTo(24.2774f, 19.584f)
          lineTo(23.7227f, 20.416f)
          lineTo(11.7227f, 12.416f)
          lineTo(12.2774f, 11.584f)
          lineTo(24.2774f, 19.584f)
          close()
        }
      }
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(0f, 10f)
        horizontalLineTo(24f)
        verticalLineTo(14f)
        horizontalLineTo(0f)
        verticalLineTo(10f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(23.5f, 10f)
        verticalLineTo(14f)
        horizontalLineTo(24.5f)
        verticalLineTo(10f)
        horizontalLineTo(23.5f)
        close()
        moveTo(0.5f, 14f)
        verticalLineTo(10f)
        horizontalLineTo(-0.5f)
        verticalLineTo(14f)
        horizontalLineTo(0.5f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(10f, 4f)
        horizontalLineTo(14f)
        verticalLineTo(20f)
        horizontalLineTo(10f)
        verticalLineTo(4f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(10f, 4.5f)
        horizontalLineTo(14f)
        verticalLineTo(3.5f)
        horizontalLineTo(10f)
        verticalLineTo(4.5f)
        close()
        moveTo(14f, 19.5f)
        horizontalLineTo(10f)
        verticalLineTo(20.5f)
        horizontalLineTo(14f)
        verticalLineTo(19.5f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFF513A)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(11f, 4f)
        horizontalLineTo(13f)
        verticalLineTo(20f)
        horizontalLineTo(11f)
        verticalLineTo(4f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(11f, 4.5f)
        horizontalLineTo(13f)
        verticalLineTo(3.5f)
        horizontalLineTo(11f)
        verticalLineTo(4.5f)
        close()
        moveTo(13f, 19.5f)
        horizontalLineTo(11f)
        verticalLineTo(20.5f)
        horizontalLineTo(13f)
        verticalLineTo(19.5f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFF513A)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(0f, 11f)
        horizontalLineTo(24f)
        verticalLineTo(13f)
        horizontalLineTo(0f)
        verticalLineTo(11f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(23.5f, 11f)
        verticalLineTo(13f)
        horizontalLineTo(24.5f)
        verticalLineTo(11f)
        horizontalLineTo(23.5f)
        close()
        moveTo(0.5f, 13f)
        verticalLineTo(11f)
        horizontalLineTo(-0.5f)
        verticalLineTo(13f)
        horizontalLineTo(0.5f)
        close()
      }
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color(0xFF121212)),
        strokeAlpha = 0.07f,
        strokeLineWidth = 0.5f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(2f, 4.25f)
        horizontalLineTo(22f)
        arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.75f, 6f)
        verticalLineTo(18f)
        arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 19.75f)
        horizontalLineTo(2f)
        arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.25f, 18f)
        verticalLineTo(6f)
        arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 4.25f)
        close()
      }
    }.build()
    return _flagUk!!
  }
