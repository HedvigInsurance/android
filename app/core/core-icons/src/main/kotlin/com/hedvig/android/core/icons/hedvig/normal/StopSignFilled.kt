package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.StopSignFilled: ImageVector
  get() {
    if (_stopSignFilled != null) {
      return _stopSignFilled!!
    }
    _stopSignFilled = materialIcon(name = "Stop sign filled") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0f, 2.25f)
        curveTo(6.6152f, 2.25f, 2.25f, 6.6152f, 2.25f, 12.0f)
        curveTo(2.25f, 17.3848f, 6.6152f, 21.75f, 12.0f, 21.75f)
        curveTo(17.3848f, 21.75f, 21.75f, 17.3848f, 21.75f, 12.0f)
        curveTo(21.75f, 6.6152f, 17.3848f, 2.25f, 12.0f, 2.25f)
        close()
        moveTo(6.0f, 12.0f)
        curveTo(6.0f, 11.0572f, 6.0f, 10.5858f, 6.2929f, 10.2929f)
        curveTo(6.5858f, 10.0f, 7.0572f, 10.0f, 8.0f, 10.0f)
        horizontalLineTo(16.0f)
        curveTo(16.9428f, 10.0f, 17.4142f, 10.0f, 17.7071f, 10.2929f)
        curveTo(18.0f, 10.5858f, 18.0f, 11.0572f, 18.0f, 12.0f)
        curveTo(18.0f, 12.9428f, 18.0f, 13.4142f, 17.7071f, 13.7071f)
        curveTo(17.4142f, 14.0f, 16.9428f, 14.0f, 16.0f, 14.0f)
        horizontalLineTo(8.0f)
        curveTo(7.0572f, 14.0f, 6.5858f, 14.0f, 6.2929f, 13.7071f)
        curveTo(6.0f, 13.4142f, 6.0f, 12.9428f, 6.0f, 12.0f)
        close()
      }
    }
    return _stopSignFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _stopSignFilled: ImageVector? = null
