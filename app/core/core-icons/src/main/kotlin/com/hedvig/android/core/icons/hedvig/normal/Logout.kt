package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Logout: ImageVector
  get() {
    if (_logout != null) {
      return _logout!!
    }
    _logout = materialIcon(name = "Logout") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0002f, 1.25f)
        curveTo(12.4145f, 1.25f, 12.7502f, 1.5858f, 12.7502f, 2.0f)
        lineTo(12.7502f, 12.0f)
        curveTo(12.7502f, 12.4142f, 12.4145f, 12.75f, 12.0002f, 12.75f)
        curveTo(11.586f, 12.75f, 11.2502f, 12.4142f, 11.2502f, 12.0f)
        lineTo(11.2502f, 2.0f)
        curveTo(11.2502f, 1.5858f, 11.586f, 1.25f, 12.0002f, 1.25f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(8.6719f, 3.6017f)
        curveTo(8.8562f, 3.9727f, 8.705f, 4.4228f, 8.334f, 4.6072f)
        curveTo(5.6157f, 5.958f, 3.7502f, 8.7619f, 3.7502f, 12.0f)
        curveTo(3.7502f, 16.5564f, 7.4439f, 20.25f, 12.0002f, 20.25f)
        curveTo(16.5566f, 20.25f, 20.2502f, 16.5564f, 20.2502f, 12.0f)
        curveTo(20.2502f, 8.7619f, 18.3848f, 5.958f, 15.6665f, 4.6072f)
        curveTo(15.2955f, 4.4228f, 15.1443f, 3.9727f, 15.3286f, 3.6017f)
        curveTo(15.5129f, 3.2308f, 15.9631f, 3.0796f, 16.334f, 3.2639f)
        curveTo(19.5426f, 4.8584f, 21.7502f, 8.1707f, 21.7502f, 12.0f)
        curveTo(21.7502f, 17.3848f, 17.385f, 21.75f, 12.0002f, 21.75f)
        curveTo(6.6155f, 21.75f, 2.2502f, 17.3848f, 2.2502f, 12.0f)
        curveTo(2.2502f, 8.1707f, 4.4579f, 4.8584f, 7.6665f, 3.2639f)
        curveTo(8.0374f, 3.0796f, 8.4875f, 3.2308f, 8.6719f, 3.6017f)
        close()
      }
    }
    return _logout!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _logout: ImageVector? = null
