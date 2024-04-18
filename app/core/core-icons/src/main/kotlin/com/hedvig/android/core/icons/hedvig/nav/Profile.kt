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
public val HedvigIcons.Profile: ImageVector
  get() {
    if (_profile != null) {
      return _profile!!
    }
    _profile = Builder(
      name = "Profile tab",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 0.595f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(5.25f, 19.0f)
        curveTo(5.25f, 15.7021f, 8.4059f, 13.25f, 12.0f, 13.25f)
        curveTo(15.5941f, 13.25f, 18.75f, 15.7021f, 18.75f, 19.0f)
        curveTo(18.75f, 20.0887f, 17.7431f, 20.75f, 16.8f, 20.75f)
        horizontalLineTo(7.2f)
        curveTo(6.2569f, 20.75f, 5.25f, 20.0887f, 5.25f, 19.0f)
        close()
        moveTo(12.0f, 14.75f)
        curveTo(8.9667f, 14.75f, 6.75f, 16.775f, 6.75f, 19.0f)
        curveTo(6.75f, 19.0198f, 6.7577f, 19.0693f, 6.8316f, 19.1309f)
        curveTo(6.9071f, 19.1939f, 7.0349f, 19.25f, 7.2f, 19.25f)
        horizontalLineTo(16.8f)
        curveTo(16.9651f, 19.25f, 17.0929f, 19.1939f, 17.1684f, 19.1309f)
        curveTo(17.2423f, 19.0693f, 17.25f, 19.0198f, 17.25f, 19.0f)
        curveTo(17.25f, 16.775f, 15.0333f, 14.75f, 12.0f, 14.75f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        fillAlpha = 0.595f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(12.0f, 5.25f)
        curveTo(10.4812f, 5.25f, 9.25f, 6.4812f, 9.25f, 8.0f)
        curveTo(9.25f, 9.5188f, 10.4812f, 10.75f, 12.0f, 10.75f)
        curveTo(13.5188f, 10.75f, 14.75f, 9.5188f, 14.75f, 8.0f)
        curveTo(14.75f, 6.4812f, 13.5188f, 5.25f, 12.0f, 5.25f)
        close()
        moveTo(7.75f, 8.0f)
        curveTo(7.75f, 5.6528f, 9.6528f, 3.75f, 12.0f, 3.75f)
        curveTo(14.3472f, 3.75f, 16.25f, 5.6528f, 16.25f, 8.0f)
        curveTo(16.25f, 10.3472f, 14.3472f, 12.25f, 12.0f, 12.25f)
        curveTo(9.6528f, 12.25f, 7.75f, 10.3472f, 7.75f, 8.0f)
        close()
      }
    }
      .build()
    return _profile!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _profile: ImageVector? = null
