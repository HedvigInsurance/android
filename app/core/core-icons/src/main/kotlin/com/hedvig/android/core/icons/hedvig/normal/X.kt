package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.X: ImageVector
  get() {
    if (_x != null) {
      return _x!!
    }
    _x = materialIcon(name = "X") {
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(19.5303f, 5.5303f)
        curveTo(19.8232f, 5.2374f, 19.8232f, 4.7626f, 19.5303f, 4.4697f)
        curveTo(19.2374f, 4.1768f, 18.7626f, 4.1768f, 18.4697f, 4.4697f)
        lineTo(12.0f, 10.9393f)
        lineTo(5.5303f, 4.4697f)
        curveTo(5.2374f, 4.1768f, 4.7626f, 4.1768f, 4.4697f, 4.4697f)
        curveTo(4.1768f, 4.7626f, 4.1768f, 5.2374f, 4.4697f, 5.5303f)
        lineTo(10.9393f, 12.0f)
        lineTo(4.4697f, 18.4697f)
        curveTo(4.1768f, 18.7626f, 4.1768f, 19.2374f, 4.4697f, 19.5303f)
        curveTo(4.7626f, 19.8232f, 5.2374f, 19.8232f, 5.5303f, 19.5303f)
        lineTo(12.0f, 13.0607f)
        lineTo(18.4697f, 19.5303f)
        curveTo(18.7626f, 19.8232f, 19.2374f, 19.8232f, 19.5303f, 19.5303f)
        curveTo(19.8232f, 19.2374f, 19.8232f, 18.7626f, 19.5303f, 18.4697f)
        lineTo(13.0607f, 12.0f)
        lineTo(19.5303f, 5.5303f)
        close()
      }
    }
    return _x!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _x: ImageVector? = null
