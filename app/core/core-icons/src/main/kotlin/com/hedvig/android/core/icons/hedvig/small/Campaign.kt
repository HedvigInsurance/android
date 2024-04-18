package com.hedvig.android.core.icons.hedvig.small.hedvig

import androidx.compose.ui.graphics.Color
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
public val HedvigIcons.Campaign: ImageVector
  get() {
    if (_campaign != null) {
      return _campaign!!
    }
    _campaign = Builder(
      name = "Campaign",
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
        pathFillType = NonZero,
      ) {
        moveTo(5.6067f, 4.1573f)
        curveTo(6.2899f, 2.425f, 6.6315f, 1.5589f, 7.093f, 1.2646f)
        curveTo(7.6462f, 0.9118f, 8.3538f, 0.9118f, 8.907f, 1.2646f)
        curveTo(9.3685f, 1.5589f, 9.7101f, 2.425f, 10.3933f, 4.1573f)
        curveTo(10.532f, 4.5088f, 10.6013f, 4.6846f, 10.7028f, 4.8359f)
        curveTo(10.8251f, 5.0182f, 10.9818f, 5.1749f, 11.1641f, 5.2972f)
        curveTo(11.3154f, 5.3987f, 11.4912f, 5.468f, 11.8427f, 5.6067f)
        curveTo(13.575f, 6.2899f, 14.4411f, 6.6315f, 14.7354f, 7.093f)
        curveTo(15.0882f, 7.6462f, 15.0882f, 8.3538f, 14.7354f, 8.907f)
        curveTo(14.4411f, 9.3685f, 13.575f, 9.7101f, 11.8427f, 10.3933f)
        curveTo(11.4912f, 10.532f, 11.3154f, 10.6013f, 11.1641f, 10.7028f)
        curveTo(10.9818f, 10.8251f, 10.8251f, 10.9818f, 10.7028f, 11.1641f)
        curveTo(10.6013f, 11.3154f, 10.532f, 11.4912f, 10.3933f, 11.8427f)
        curveTo(9.7101f, 13.575f, 9.3685f, 14.4411f, 8.907f, 14.7354f)
        curveTo(8.3538f, 15.0882f, 7.6462f, 15.0882f, 7.093f, 14.7354f)
        curveTo(6.6315f, 14.4411f, 6.2899f, 13.575f, 5.6067f, 11.8427f)
        curveTo(5.468f, 11.4912f, 5.3987f, 11.3154f, 5.2972f, 11.1641f)
        curveTo(5.1749f, 10.9818f, 5.0182f, 10.8251f, 4.8359f, 10.7028f)
        curveTo(4.6846f, 10.6013f, 4.5088f, 10.532f, 4.1573f, 10.3933f)
        curveTo(2.425f, 9.7101f, 1.5589f, 9.3685f, 1.2646f, 8.907f)
        curveTo(0.9118f, 8.3538f, 0.9118f, 7.6462f, 1.2646f, 7.093f)
        curveTo(1.5589f, 6.6315f, 2.425f, 6.2899f, 4.1573f, 5.6067f)
        curveTo(4.5088f, 5.468f, 4.6846f, 5.3987f, 4.8359f, 5.2972f)
        curveTo(5.0182f, 5.1749f, 5.1749f, 5.0182f, 5.2972f, 4.8359f)
        curveTo(5.3987f, 4.6846f, 5.468f, 4.5088f, 5.6067f, 4.1573f)
        close()
      }
    }
      .build()
    return _campaign!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _campaign: ImageVector? = null
