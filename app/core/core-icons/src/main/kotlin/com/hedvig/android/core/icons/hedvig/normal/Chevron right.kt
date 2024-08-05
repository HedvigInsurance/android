package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ChevronRight: ImageVector
  get() {
    if (_chevronRight != null) {
      return _chevronRight!!
    }
    _chevronRight = materialIcon(name = "Chevron right") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(7.2927f, 21.4939f)
        curveTo(7.0199f, 21.1821f, 7.0515f, 20.7083f, 7.3632f, 20.4356f)
        lineTo(14.2086f, 14.4459f)
        curveTo(15.0267f, 13.7301f, 15.5792f, 13.2443f, 15.9372f, 12.8296f)
        curveTo(16.2812f, 12.4311f, 16.3557f, 12.1983f, 16.3557f, 12.0f)
        curveTo(16.3557f, 11.8017f, 16.2812f, 11.5688f, 15.9372f, 11.1703f)
        curveTo(15.5792f, 10.7557f, 15.0267f, 10.2699f, 14.2086f, 9.5541f)
        lineTo(7.3632f, 3.5644f)
        curveTo(7.0515f, 3.2917f, 7.0199f, 2.8178f, 7.2927f, 2.5061f)
        curveTo(7.5655f, 2.1944f, 8.0393f, 2.1628f, 8.351f, 2.4356f)
        lineTo(15.2385f, 8.4621f)
        curveTo(16.0037f, 9.1316f, 16.6374f, 9.6861f, 17.0726f, 10.1901f)
        curveTo(17.5295f, 10.7194f, 17.8557f, 11.2907f, 17.8557f, 12.0f)
        curveTo(17.8557f, 12.7092f, 17.5295f, 13.2806f, 17.0726f, 13.8099f)
        curveTo(16.6375f, 14.3139f, 16.0037f, 14.8684f, 15.2385f, 15.5378f)
        lineTo(8.351f, 21.5644f)
        curveTo(8.0393f, 21.8372f, 7.5655f, 21.8056f, 7.2927f, 21.4939f)
        close()
      }
    }
    return _chevronRight!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chevronRight: ImageVector? = null
