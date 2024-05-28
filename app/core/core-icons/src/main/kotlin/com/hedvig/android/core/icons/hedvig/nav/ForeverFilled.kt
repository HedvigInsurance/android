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
public val HedvigIcons.ForeverFilled: ImageVector
  get() {
    if (_foreverFilled != null) {
      return _foreverFilled!!
    }
    _foreverFilled = Builder(
      name = "Forever tab selected",
      defaultWidth = 25.0.dp,
      defaultHeight =
        24.0.dp,
      viewportWidth = 25.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF000000)),
        fillAlpha = 0.927f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(17.5f, 8.375f)
        curveTo(19.2259f, 8.375f, 20.625f, 9.7741f, 20.625f, 11.5f)
        curveTo(20.625f, 13.2259f, 19.2259f, 14.625f, 17.5f, 14.625f)
        curveTo(16.578f, 14.625f, 15.75f, 14.2267f, 15.1768f, 13.5901f)
        lineTo(11.198f, 8.3265f)
        curveTo(11.1847f, 8.3089f, 11.1707f, 8.2917f, 11.156f, 8.2752f)
        curveTo(10.264f, 7.2645f, 8.9562f, 6.625f, 7.5f, 6.625f)
        curveTo(4.8076f, 6.625f, 2.625f, 8.8076f, 2.625f, 11.5f)
        curveTo(2.625f, 14.1924f, 4.8076f, 16.375f, 7.5f, 16.375f)
        curveTo(8.6628f, 16.375f, 9.7327f, 15.9668f, 10.5706f, 15.2866f)
        curveTo(10.9458f, 14.982f, 11.0031f, 14.431f, 10.6985f, 14.0558f)
        curveTo(10.394f, 13.6806f, 9.8429f, 13.6233f, 9.4677f, 13.9279f)
        curveTo(8.9302f, 14.3642f, 8.2467f, 14.625f, 7.5f, 14.625f)
        curveTo(5.7741f, 14.625f, 4.375f, 13.2259f, 4.375f, 11.5f)
        curveTo(4.375f, 9.7741f, 5.7741f, 8.375f, 7.5f, 8.375f)
        curveTo(8.4221f, 8.375f, 9.25f, 8.7733f, 9.8232f, 9.4099f)
        lineTo(13.802f, 14.6735f)
        curveTo(13.8153f, 14.6911f, 13.8293f, 14.7083f, 13.844f, 14.7248f)
        curveTo(14.736f, 15.7355f, 16.0438f, 16.375f, 17.5f, 16.375f)
        curveTo(20.1924f, 16.375f, 22.375f, 14.1924f, 22.375f, 11.5f)
        curveTo(22.375f, 8.8076f, 20.1924f, 6.625f, 17.5f, 6.625f)
        curveTo(16.3084f, 6.625f, 15.2146f, 7.0536f, 14.3679f, 7.7642f)
        curveTo(13.9977f, 8.0748f, 13.9494f, 8.6267f, 14.2601f, 8.9969f)
        curveTo(14.5707f, 9.3671f, 15.1226f, 9.4154f, 15.4928f, 9.1047f)
        curveTo(16.0361f, 8.6488f, 16.7349f, 8.375f, 17.5f, 8.375f)
        close()
      }
    }
      .build()
    return _foreverFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _foreverFilled: ImageVector? = null
