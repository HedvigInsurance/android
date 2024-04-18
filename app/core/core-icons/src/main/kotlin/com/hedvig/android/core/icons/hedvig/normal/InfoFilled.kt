package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.InfoFilled: ImageVector
  get() {
    if (_infoFilled != null) {
      return _infoFilled!!
    }
    _infoFilled = materialIcon(name = "Info filled") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0f, 2.25f)
        curveTo(6.6152f, 2.25f, 2.25f, 6.6152f, 2.25f, 12.0f)
        curveTo(2.25f, 17.3848f, 6.6152f, 21.75f, 12.0f, 21.75f)
        curveTo(17.3848f, 21.75f, 21.75f, 17.3848f, 21.75f, 12.0f)
        curveTo(21.75f, 6.6152f, 17.3848f, 2.25f, 12.0f, 2.25f)
        close()
        moveTo(13.0f, 7.7873f)
        curveTo(13.0f, 8.3396f, 12.5523f, 8.7873f, 12.0f, 8.7873f)
        curveTo(11.4477f, 8.7873f, 11.0f, 8.3396f, 11.0f, 7.7873f)
        curveTo(11.0f, 7.235f, 11.4477f, 6.7873f, 12.0f, 6.7873f)
        curveTo(12.5523f, 6.7873f, 13.0f, 7.235f, 13.0f, 7.7873f)
        close()
        moveTo(12.7755f, 11.1244f)
        curveTo(12.7755f, 10.7102f, 12.4397f, 10.3744f, 12.0255f, 10.3744f)
        curveTo(11.6113f, 10.3744f, 11.2755f, 10.7102f, 11.2755f, 11.1244f)
        verticalLineTo(16.2527f)
        curveTo(11.2755f, 16.6669f, 11.6113f, 17.0027f, 12.0255f, 17.0027f)
        curveTo(12.4397f, 17.0027f, 12.7755f, 16.6669f, 12.7755f, 16.2527f)
        verticalLineTo(11.1244f)
        close()
      }
    }
    return _infoFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _infoFilled: ImageVector? = null
