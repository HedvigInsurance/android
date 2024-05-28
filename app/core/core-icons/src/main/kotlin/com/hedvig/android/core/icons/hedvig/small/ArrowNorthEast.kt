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
public val HedvigIcons.ArrowNorthEast: ImageVector
  get() {
    if (_arrowNorthEast != null) {
      return _arrowNorthEast!!
    }
    _arrowNorthEast = Builder(
      name = "Arrow pointing northeast",
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
        moveTo(12.1522f, 2.7871f)
        curveTo(12.131f, 2.7885f, 12.1094f, 2.79f, 12.0874f, 2.7914f)
        lineTo(6.0499f, 3.1939f)
        curveTo(5.6366f, 3.2215f, 5.2792f, 2.9088f, 5.2517f, 2.4955f)
        curveTo(5.2241f, 2.0822f, 5.5368f, 1.7248f, 5.9501f, 1.6973f)
        lineTo(12.0347f, 1.2916f)
        curveTo(12.4728f, 1.2623f, 12.8771f, 1.2353f, 13.2059f, 1.2594f)
        curveTo(13.5676f, 1.286f, 13.9683f, 1.381f, 14.2937f, 1.7063f)
        curveTo(14.619f, 2.0317f, 14.714f, 2.4324f, 14.7405f, 2.7941f)
        curveTo(14.7647f, 3.1229f, 14.7376f, 3.5271f, 14.7084f, 3.9653f)
        lineTo(14.3027f, 10.0499f)
        curveTo(14.2752f, 10.4632f, 13.9178f, 10.7759f, 13.5045f, 10.7483f)
        curveTo(13.0912f, 10.7208f, 12.7785f, 10.3634f, 12.8061f, 9.9501f)
        lineTo(13.2086f, 3.9126f)
        curveTo(13.21f, 3.8906f, 13.2115f, 3.869f, 13.2129f, 3.8478f)
        lineTo(2.5303f, 14.5303f)
        curveTo(2.2374f, 14.8232f, 1.7626f, 14.8232f, 1.4697f, 14.5303f)
        curveTo(1.1768f, 14.2374f, 1.1768f, 13.7626f, 1.4697f, 13.4697f)
        lineTo(12.1522f, 2.7871f)
        close()
      }
    }
      .build()
    return _arrowNorthEast!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _arrowNorthEast: ImageVector? = null
