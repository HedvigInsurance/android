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
public val HedvigIcons.InsuranceFilled: ImageVector
  get() {
    if (_insuranceFilled != null) {
      return _insuranceFilled!!
    }
    _insuranceFilled = Builder(
      name = "Insurance tab selected",
      defaultWidth = 25.0.dp,
      defaultHeight = 24.0.dp,
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
        moveTo(11.3799f, 2.2667f)
        curveTo(11.9641f, 2.0513f, 12.4261f, 1.8809f, 12.9187f, 1.8809f)
        curveTo(13.4113f, 1.8809f, 13.8733f, 2.0513f, 14.4575f, 2.2667f)
        lineTo(18.3462f, 3.6968f)
        curveTo(18.947f, 3.9177f, 19.4553f, 4.1046f, 19.8547f, 4.3044f)
        curveTo(20.2794f, 4.5169f, 20.6467f, 4.7745f, 20.924f, 5.1719f)
        curveTo(21.2013f, 5.5693f, 21.3163f, 6.003f, 21.3691f, 6.4749f)
        curveTo(21.4187f, 6.9187f, 21.4187f, 7.4602f, 21.4187f, 8.1004f)
        verticalLineTo(12.0f)
        curveTo(21.4187f, 14.2349f, 20.3584f, 16.1602f, 19.0658f, 17.6803f)
        curveTo(17.7707f, 19.2031f, 16.1901f, 20.3824f, 15.0183f, 21.1432f)
        lineTo(14.9354f, 21.1971f)
        curveTo(14.2771f, 21.6254f, 13.7358f, 21.9776f, 12.9187f, 21.9776f)
        curveTo(12.1016f, 21.9776f, 11.5603f, 21.6254f, 10.902f, 21.1971f)
        lineTo(10.8191f, 21.1432f)
        curveTo(9.6473f, 20.3824f, 8.0667f, 19.2031f, 6.7716f, 17.6803f)
        curveTo(5.479f, 16.1602f, 4.4187f, 14.2349f, 4.4187f, 12.0f)
        verticalLineTo(8.1003f)
        verticalLineTo(8.1003f)
        curveTo(4.4187f, 7.4602f, 4.4187f, 6.9187f, 4.4683f, 6.4749f)
        curveTo(4.5212f, 6.003f, 4.6361f, 5.5693f, 4.9134f, 5.1719f)
        curveTo(5.1907f, 4.7745f, 5.558f, 4.5169f, 5.9827f, 4.3044f)
        curveTo(6.3821f, 4.1046f, 6.8904f, 3.9177f, 7.4912f, 3.6968f)
        lineTo(11.3799f, 2.2667f)
        close()
        moveTo(16.449f, 10.3875f)
        curveTo(16.7419f, 10.0946f, 16.7419f, 9.6197f, 16.449f, 9.3268f)
        curveTo(16.1561f, 9.0339f, 15.6813f, 9.0339f, 15.3884f, 9.3268f)
        lineTo(12.0955f, 12.6197f)
        curveTo(11.9978f, 12.7173f, 11.8396f, 12.7173f, 11.7419f, 12.6197f)
        lineTo(10.449f, 11.3268f)
        curveTo(10.1561f, 11.0339f, 9.6813f, 11.0339f, 9.3884f, 11.3268f)
        curveTo(9.0955f, 11.6197f, 9.0955f, 12.0946f, 9.3884f, 12.3875f)
        lineTo(10.6813f, 13.6804f)
        curveTo(11.3647f, 14.3638f, 12.4727f, 14.3638f, 13.1561f, 13.6804f)
        lineTo(16.449f, 10.3875f)
        close()
      }
    }
      .build()
    return _insuranceFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _insuranceFilled: ImageVector? = null
