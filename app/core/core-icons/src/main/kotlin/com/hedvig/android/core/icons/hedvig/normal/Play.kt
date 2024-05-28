package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Play: ImageVector
  get() {
    if (_play != null) {
      return _play!!
    }
    _play = materialIcon(name = "Play") {
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(19.6063f, 10.7138f)
        curveTo(20.5773f, 11.2964f, 20.5773f, 12.7036f, 19.6063f, 13.2862f)
        lineTo(9.0218f, 19.637f)
        curveTo(8.022f, 20.2368f, 6.75f, 19.5167f, 6.75f, 18.3507f)
        lineTo(6.75f, 5.6493f)
        curveTo(6.75f, 4.4833f, 8.022f, 3.7632f, 9.0217f, 4.363f)
        lineTo(19.6063f, 10.7138f)
        close()
      }
    }
    return _play!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _play: ImageVector? = null
