package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.CircleWithXFilled: ImageVector // todo get right filled icon frmo figma when it exists
  get() {
    if (_circleWithXFilled != null) {
      return _circleWithXFilled!!
    }
    _circleWithXFilled = materialIcon(name = "Circle with X filled") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0f, 3.75f)
        curveTo(7.4436f, 3.75f, 3.75f, 7.4436f, 3.75f, 12.0f)
        curveTo(3.75f, 16.5563f, 7.4436f, 20.25f, 12.0f, 20.25f)
        curveTo(16.5563f, 20.25f, 20.25f, 16.5563f, 20.25f, 12.0f)
        curveTo(20.25f, 7.4436f, 16.5563f, 3.75f, 12.0f, 3.75f)
        close()
        moveTo(2.25f, 12.0f)
        curveTo(2.25f, 6.6152f, 6.6152f, 2.25f, 12.0f, 2.25f)
        curveTo(17.3848f, 2.25f, 21.75f, 6.6152f, 21.75f, 12.0f)
        curveTo(21.75f, 17.3848f, 17.3848f, 21.75f, 12.0f, 21.75f)
        curveTo(6.6152f, 21.75f, 2.25f, 17.3848f, 2.25f, 12.0f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(15.5303f, 8.4697f)
        curveTo(15.8232f, 8.7626f, 15.8232f, 9.2374f, 15.5303f, 9.5303f)
        lineTo(9.5303f, 15.5303f)
        curveTo(9.2374f, 15.8232f, 8.7626f, 15.8232f, 8.4697f, 15.5303f)
        curveTo(8.1768f, 15.2374f, 8.1768f, 14.7626f, 8.4697f, 14.4697f)
        lineTo(14.4697f, 8.4697f)
        curveTo(14.7626f, 8.1768f, 15.2374f, 8.1768f, 15.5303f, 8.4697f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(15.5303f, 15.5303f)
        curveTo(15.2374f, 15.8232f, 14.7626f, 15.8232f, 14.4697f, 15.5303f)
        lineTo(8.4697f, 9.5303f)
        curveTo(8.1768f, 9.2374f, 8.1768f, 8.7626f, 8.4697f, 8.4697f)
        curveTo(8.7626f, 8.1768f, 9.2374f, 8.1768f, 9.5303f, 8.4697f)
        lineTo(15.5303f, 14.4697f)
        curveTo(15.8232f, 14.7626f, 15.8232f, 15.2374f, 15.5303f, 15.5303f)
        close()
      }
    }
    return _circleWithXFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _circleWithXFilled: ImageVector? = null
