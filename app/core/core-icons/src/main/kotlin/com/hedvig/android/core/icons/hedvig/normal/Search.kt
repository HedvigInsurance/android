package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Search: ImageVector
  get() {
    if (_search != null) {
      return _search!!
    }
    _search = materialIcon(name = "Search") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(10.0f, 3.75f)
        curveTo(6.5482f, 3.75f, 3.75f, 6.5482f, 3.75f, 10.0f)
        curveTo(3.75f, 13.4518f, 6.5482f, 16.25f, 10.0f, 16.25f)
        curveTo(13.4518f, 16.25f, 16.25f, 13.4518f, 16.25f, 10.0f)
        curveTo(16.25f, 6.5482f, 13.4518f, 3.75f, 10.0f, 3.75f)
        close()
        moveTo(2.25f, 10.0f)
        curveTo(2.25f, 5.7198f, 5.7198f, 2.25f, 10.0f, 2.25f)
        curveTo(14.2802f, 2.25f, 17.75f, 5.7198f, 17.75f, 10.0f)
        curveTo(17.75f, 14.2802f, 14.2802f, 17.75f, 10.0f, 17.75f)
        curveTo(5.7198f, 17.75f, 2.25f, 14.2802f, 2.25f, 10.0f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(20.4697f, 21.5303f)
        lineTo(14.4697f, 15.5303f)
        lineTo(15.5303f, 14.4697f)
        lineTo(21.5303f, 20.4697f)
        curveTo(21.8232f, 20.7626f, 21.8232f, 21.2374f, 21.5303f, 21.5303f)
        curveTo(21.2374f, 21.8232f, 20.7626f, 21.8232f, 20.4697f, 21.5303f)
        close()
      }
    }
    return _search!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _search: ImageVector? = null
