package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Info: ImageVector
  get() {
    if (_info != null) {
      return _info!!
    }
    _info = materialIcon(name = "Info-1") {
      materialPath(pathFillType = PathFillType.NonZero) {
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
        moveTo(13.0f, 7.7873f)
        curveTo(13.0f, 8.3396f, 12.5523f, 8.7873f, 12.0f, 8.7873f)
        curveTo(11.4477f, 8.7873f, 11.0f, 8.3396f, 11.0f, 7.7873f)
        curveTo(11.0f, 7.235f, 11.4477f, 6.7873f, 12.0f, 6.7873f)
        curveTo(12.5523f, 6.7873f, 13.0f, 7.235f, 13.0f, 7.7873f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0255f, 10.3744f)
        curveTo(12.4397f, 10.3744f, 12.7755f, 10.7102f, 12.7755f, 11.1244f)
        lineTo(12.7755f, 16.2527f)
        curveTo(12.7755f, 16.6669f, 12.4397f, 17.0027f, 12.0255f, 17.0027f)
        curveTo(11.6113f, 17.0027f, 11.2755f, 16.6669f, 11.2755f, 16.2527f)
        lineTo(11.2755f, 11.1244f)
        curveTo(11.2755f, 10.7102f, 11.6113f, 10.3744f, 12.0255f, 10.3744f)
        close()
      }
    }
    return _info!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _info: ImageVector? = null
