package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Waiting: ImageVector
  get() {
    if (_waiting != null) {
      return _waiting!!
    }
    _waiting = materialIcon(name = "Waiting") {
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
        moveTo(12.0f, 7.25f)
        curveTo(12.4142f, 7.25f, 12.75f, 7.5858f, 12.75f, 8.0f)
        verticalLineTo(11.25f)
        lineTo(16.0f, 11.25f)
        curveTo(16.4142f, 11.25f, 16.75f, 11.5858f, 16.75f, 12.0f)
        curveTo(16.75f, 12.4142f, 16.4142f, 12.75f, 16.0f, 12.75f)
        lineTo(12.0f, 12.75f)
        curveTo(11.8011f, 12.75f, 11.6103f, 12.671f, 11.4697f, 12.5303f)
        curveTo(11.329f, 12.3897f, 11.25f, 12.1989f, 11.25f, 12.0f)
        verticalLineTo(8.0f)
        curveTo(11.25f, 7.5858f, 11.5858f, 7.25f, 12.0f, 7.25f)
        close()
      }
    }
    return _waiting!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _waiting: ImageVector? = null
