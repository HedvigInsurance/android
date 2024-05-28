package com.hedvig.android.core.icons.hedvig.colored.hedvig

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
public val HedvigIcons.FirstVet: ImageVector
  get() {
    if (_firstvet != null) {
      return _firstvet!!
    }
    _firstvet = Builder(
      name = "FirstVet",
      defaultWidth = 32.0.dp,
      defaultHeight = 32.0.dp,
      viewportWidth = 32.0f,
      viewportHeight = 32.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF0061FF)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(32.0f, 16.0f)
        curveTo(32.0f, 24.8366f, 24.8366f, 32.0f, 16.0f, 32.0f)
        curveTo(7.1634f, 32.0f, 0.0f, 24.8366f, 0.0f, 16.0f)
        curveTo(0.0f, 7.1634f, 7.1634f, 0.0f, 16.0f, 0.0f)
        curveTo(24.8366f, 0.0f, 32.0f, 7.1634f, 32.0f, 16.0f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(24.4754f, 10.7368f)
        lineTo(21.5583f, 12.258f)
        curveTo(21.5516f, 12.2004f, 21.5465f, 12.1411f, 21.5398f, 12.0835f)
        curveTo(21.3599f, 10.5673f, 20.1157f, 9.3819f, 18.5756f, 9.214f)
        curveTo(17.2726f, 9.0708f, 15.9494f, 9.0f, 14.6244f, 9.0f)
        horizontalLineTo(14.1486f)
        curveTo(12.8254f, 9.0f, 11.5005f, 9.0708f, 10.1992f, 9.214f)
        curveTo(8.6591f, 9.3819f, 7.4149f, 10.5673f, 7.2333f, 12.0818f)
        curveTo(6.9222f, 14.6846f, 6.9222f, 17.3137f, 7.2333f, 19.9165f)
        curveTo(7.4149f, 21.4311f, 8.6591f, 22.6181f, 10.1992f, 22.786f)
        curveTo(11.5005f, 22.9275f, 12.8254f, 23.0f, 14.1486f, 23.0f)
        horizontalLineTo(14.6244f)
        curveTo(15.9477f, 23.0f, 17.2726f, 22.9292f, 18.5739f, 22.786f)
        curveTo(20.114f, 22.6181f, 21.3582f, 21.4311f, 21.5398f, 19.9165f)
        curveTo(21.5465f, 19.8573f, 21.5516f, 19.7996f, 21.5583f, 19.7404f)
        lineTo(24.4737f, 21.2615f)
        curveTo(24.7125f, 21.385f, 25.0f, 21.2171f, 25.0f, 20.952f)
        verticalLineTo(11.0447f)
        curveTo(25.0f, 10.7813f, 24.7125f, 10.6117f, 24.4754f, 10.7368f)
        close()
        moveTo(18.8328f, 16.7293f)
        curveTo(18.7303f, 17.3006f, 18.1738f, 17.6825f, 17.5904f, 17.5804f)
        lineTo(15.4786f, 17.2149f)
        lineTo(14.9204f, 20.3166f)
        lineTo(13.8645f, 20.1338f)
        curveTo(13.2811f, 20.0334f, 12.891f, 19.4885f, 12.9953f, 18.9172f)
        lineTo(13.3685f, 16.8495f)
        lineTo(10.2008f, 16.3029f)
        lineTo(10.3875f, 15.2691f)
        curveTo(10.49f, 14.6978f, 11.0466f, 14.3158f, 11.63f, 14.4179f)
        lineTo(13.7418f, 14.7817f)
        lineTo(14.3f, 11.6802f)
        lineTo(15.3559f, 11.8629f)
        curveTo(15.9393f, 11.9633f, 16.3293f, 12.5082f, 16.2251f, 13.0795f)
        lineTo(15.8535f, 15.1472f)
        lineTo(19.0212f, 15.6938f)
        lineTo(18.8328f, 16.7293f)
        close()
      }
    }
      .build()
    return _firstvet!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _firstvet: ImageVector? = null
