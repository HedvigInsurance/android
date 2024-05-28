package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.RestartOneArrow: ImageVector
  get() {
    if (_restartOneArrow != null) {
      return _restartOneArrow!!
    }
    _restartOneArrow = materialIcon(name = "Restart with one arrow") {
      group {
        materialPath(pathFillType = PathFillType.EvenOdd) {
          moveTo(18.8061f, 7.3357f)
          curveTo(16.8822f, 4.5328f, 13.3356f, 3.1076f, 9.867f, 4.037f)
          curveTo(5.4659f, 5.2162f, 2.8541f, 9.74f, 4.0333f, 14.1411f)
          curveTo(5.2126f, 18.5422f, 9.7364f, 21.154f, 14.1375f, 19.9747f)
          curveTo(17.6483f, 19.034f, 20.0222f, 15.964f, 20.2384f, 12.5245f)
          curveTo(20.2643f, 12.1111f, 20.6205f, 11.797f, 21.0339f, 11.823f)
          curveTo(21.4473f, 11.849f, 21.7614f, 12.2052f, 21.7354f, 12.6186f)
          curveTo(21.4801f, 16.6815f, 18.6774f, 20.3112f, 14.5257f, 21.4236f)
          curveTo(9.3244f, 22.8173f, 3.9781f, 19.7306f, 2.5845f, 14.5293f)
          curveTo(1.1908f, 9.328f, 4.2775f, 3.9818f, 9.4788f, 2.5881f)
          curveTo(13.5716f, 1.4914f, 17.7523f, 3.169f, 20.028f, 6.4653f)
          curveTo(20.054f, 6.343f, 20.0745f, 6.201f, 20.0889f, 6.0346f)
          curveTo(20.1433f, 5.4035f, 20.1006f, 4.5599f, 20.0359f, 3.3303f)
          curveTo(20.0141f, 2.9166f, 20.3318f, 2.5636f, 20.7454f, 2.5419f)
          curveTo(21.1591f, 2.5201f, 21.512f, 2.8378f, 21.5338f, 3.2514f)
          lineTo(21.5368f, 3.3078f)
          curveTo(21.5978f, 4.4672f, 21.6478f, 5.4159f, 21.5833f, 6.1634f)
          curveTo(21.516f, 6.9446f, 21.3156f, 7.6333f, 20.772f, 8.1769f)
          curveTo(20.2283f, 8.7206f, 19.5396f, 8.9209f, 18.7585f, 8.9883f)
          curveTo(18.0109f, 9.0527f, 17.0621f, 9.0028f, 15.9028f, 8.9417f)
          lineTo(15.8465f, 8.9388f)
          curveTo(15.4328f, 8.917f, 15.1151f, 8.564f, 15.1369f, 8.1504f)
          curveTo(15.1587f, 7.7368f, 15.5117f, 7.4191f, 15.9253f, 7.4408f)
          curveTo(17.1549f, 7.5056f, 17.9985f, 7.5483f, 18.6296f, 7.4938f)
          curveTo(18.7254f, 7.4856f, 18.813f, 7.4753f, 18.8935f, 7.4631f)
          lineTo(18.8061f, 7.3357f)
          close()
        }
      }
    }
    return _restartOneArrow!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _restartOneArrow: ImageVector? = null
