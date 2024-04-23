package com.hedvig.android.core.icons.hedvig.nav

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.PaymentsFilled: ImageVector
  get() {
    if (_paymentsFilled != null) {
      return _paymentsFilled!!
    }
    _paymentsFilled = ImageVector.Builder(
      name = "Payments tab selected",
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
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(5.27002f, 5f)
        curveTo(5.27f, 3.4812f, 6.5012f, 2.25f, 8.02f, 2.25f)
        horizontalLineTo(17.02f)
        curveTo(18.5388f, 2.25f, 19.77f, 3.4812f, 19.77f, 5f)
        verticalLineTo(18.8484f)
        curveTo(19.77f, 20.8642f, 17.673f, 22.195f, 15.8491f, 21.3367f)
        lineTo(15.2785f, 21.0682f)
        curveTo(14.9499f, 20.9136f, 14.5704f, 20.9095f, 14.2386f, 21.0569f)
        lineTo(13.6369f, 21.3243f)
        curveTo(12.9258f, 21.6404f, 12.1142f, 21.6404f, 11.4031f, 21.3243f)
        lineTo(10.8015f, 21.0569f)
        curveTo(10.4696f, 20.9095f, 10.0901f, 20.9136f, 9.7615f, 21.0682f)
        lineTo(9.19096f, 21.3367f)
        curveTo(7.367f, 22.195f, 5.27f, 20.8642f, 5.27f, 18.8484f)
        verticalLineTo(5f)
        close()

        moveTo(16.27f, 8.25f)
        curveTo(16.27f, 7.8358f, 15.9342f, 7.5f, 15.52f, 7.5f)
        horizontalLineTo(9.52002f)
        curveTo(9.1058f, 7.5f, 8.77f, 7.8358f, 8.77f, 8.25f)
        curveTo(8.77f, 8.6642f, 9.1058f, 9f, 9.52f, 9f)
        horizontalLineTo(15.52f)
        curveTo(15.9342f, 9f, 16.27f, 8.6642f, 16.27f, 8.25f)
        close()
        moveTo(14.27f, 10.75f)
        curveTo(14.27f, 10.3358f, 13.9342f, 10f, 13.52f, 10f)
        horizontalLineTo(9.52002f)
        curveTo(9.1058f, 10f, 8.77f, 10.3358f, 8.77f, 10.75f)
        curveTo(8.77f, 11.1642f, 9.1058f, 11.5f, 9.52f, 11.5f)
        horizontalLineTo(13.52f)
        curveTo(13.9342f, 11.5f, 14.27f, 11.1642f, 14.27f, 10.75f)
        close()
        moveTo(16.27f, 13.25f)
        curveTo(16.27f, 12.8358f, 15.9342f, 12.5f, 15.52f, 12.5f)
        horizontalLineTo(9.52002f)
        curveTo(9.1058f, 12.5f, 8.77f, 12.8358f, 8.77f, 13.25f)
        curveTo(8.77f, 13.6642f, 9.1058f, 14f, 9.52f, 14f)
        horizontalLineTo(15.52f)
        curveTo(15.9342f, 14f, 16.27f, 13.6642f, 16.27f, 13.25f)
        close()
        moveTo(14.27f, 15.75f)
        curveTo(14.27f, 15.3358f, 13.9342f, 15f, 13.52f, 15f)
        horizontalLineTo(9.52002f)
        curveTo(9.1058f, 15f, 8.77f, 15.3358f, 8.77f, 15.75f)
        curveTo(8.77f, 16.1642f, 9.1058f, 16.5f, 9.52f, 16.5f)
        horizontalLineTo(13.52f)
        curveTo(13.9342f, 16.5f, 14.27f, 16.1642f, 14.27f, 15.75f)
        close()
      }
    }.build()
    return _paymentsFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _paymentsFilled: ImageVector? = null
