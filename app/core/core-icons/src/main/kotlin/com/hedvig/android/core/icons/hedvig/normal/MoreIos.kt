package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.MoreIos: ImageVector
  get() {
    if (_moreIos != null) {
      return _moreIos!!
    }
    _moreIos = materialIcon(name = "More") {
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(6.0f, 11.6508f)
        curveTo(6.0f, 12.7553f, 5.1046f, 13.6508f, 4.0f, 13.6508f)
        curveTo(2.8954f, 13.6508f, 2.0f, 12.7553f, 2.0f, 11.6508f)
        curveTo(2.0f, 10.5462f, 2.8954f, 9.6508f, 4.0f, 9.6508f)
        curveTo(5.1046f, 9.6508f, 6.0f, 10.5462f, 6.0f, 11.6508f)
        close()
      }
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(14.0f, 11.6508f)
        curveTo(14.0f, 12.7553f, 13.1046f, 13.6508f, 12.0f, 13.6508f)
        curveTo(10.8954f, 13.6508f, 10.0f, 12.7553f, 10.0f, 11.6508f)
        curveTo(10.0f, 10.5462f, 10.8954f, 9.6508f, 12.0f, 9.6508f)
        curveTo(13.1046f, 9.6508f, 14.0f, 10.5462f, 14.0f, 11.6508f)
        close()
      }
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(22.0f, 11.6508f)
        curveTo(22.0f, 12.7553f, 21.1046f, 13.6508f, 20.0f, 13.6508f)
        curveTo(18.8954f, 13.6508f, 18.0f, 12.7553f, 18.0f, 11.6508f)
        curveTo(18.0f, 10.5462f, 18.8954f, 9.6508f, 20.0f, 9.6508f)
        curveTo(21.1046f, 9.6508f, 22.0f, 10.5462f, 22.0f, 11.6508f)
        close()
      }
    }
    return _moreIos!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _moreIos: ImageVector? = null
