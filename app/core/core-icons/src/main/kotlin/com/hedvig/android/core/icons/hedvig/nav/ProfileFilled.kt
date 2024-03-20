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
public val HedvigIcons.ProfileFilled: ImageVector
  get() {
    if (_profileFilled != null) {
      return _profileFilled!!
    }
    _profileFilled = Builder(
      name = "Profile tab selected",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF000000)),
        fillAlpha = 0.927f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(12.0f, 3.75f)
        curveTo(9.6528f, 3.75f, 7.75f, 5.6528f, 7.75f, 8.0f)
        curveTo(7.75f, 10.3472f, 9.6528f, 12.25f, 12.0f, 12.25f)
        curveTo(14.3472f, 12.25f, 16.25f, 10.3472f, 16.25f, 8.0f)
        curveTo(16.25f, 5.6528f, 14.3472f, 3.75f, 12.0f, 3.75f)
        close()
        moveTo(12.0f, 13.25f)
        curveTo(8.4059f, 13.25f, 5.25f, 15.7021f, 5.25f, 19.0f)
        curveTo(5.25f, 20.0887f, 6.2569f, 20.75f, 7.2f, 20.75f)
        horizontalLineTo(16.8f)
        curveTo(17.7431f, 20.75f, 18.75f, 20.0887f, 18.75f, 19.0f)
        curveTo(18.75f, 15.7021f, 15.5941f, 13.25f, 12.0f, 13.25f)
        close()
      }
    }
      .build()
    return _profileFilled!!
  }

private var _profileFilled: ImageVector? = null
