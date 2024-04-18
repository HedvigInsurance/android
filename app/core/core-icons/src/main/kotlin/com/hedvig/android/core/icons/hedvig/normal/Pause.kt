package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Pause: ImageVector
  get() {
    if (_pause != null) {
      return _pause!!
    }
    _pause = materialIcon(name = "Pause") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(7.5f, 4.5f)
        curveTo(6.6716f, 4.5f, 6.0f, 5.1716f, 6.0f, 6.0f)
        verticalLineTo(18.0f)
        curveTo(6.0f, 18.8284f, 6.6716f, 19.5f, 7.5f, 19.5f)
        horizontalLineTo(9.0f)
        curveTo(9.8284f, 19.5f, 10.5f, 18.8284f, 10.5f, 18.0f)
        verticalLineTo(6.0f)
        curveTo(10.5f, 5.1716f, 9.8284f, 4.5f, 9.0f, 4.5f)
        horizontalLineTo(7.5f)
        close()
        moveTo(15.0f, 4.5f)
        curveTo(14.1716f, 4.5f, 13.5f, 5.1716f, 13.5f, 6.0f)
        verticalLineTo(18.0f)
        curveTo(13.5f, 18.8284f, 14.1716f, 19.5f, 15.0f, 19.5f)
        horizontalLineTo(16.5f)
        curveTo(17.3284f, 19.5f, 18.0f, 18.8284f, 18.0f, 18.0f)
        verticalLineTo(6.0f)
        curveTo(18.0f, 5.1716f, 17.3284f, 4.5f, 16.5f, 4.5f)
        horizontalLineTo(15.0f)
        close()
      }
    }
    return _pause!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _pause: ImageVector? = null
