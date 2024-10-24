package com.hedvig.android.design.system.hedvig.icon.colored

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ColoredCampaign: ImageVector
  get() {
    if (_campaign != null) {
      return _campaign!!
    }
    _campaign = Builder(
      name = "Campaign",
      defaultWidth = 32.0.dp,
      defaultHeight = 32.0.dp,
      viewportWidth = 32.0f,
      viewportHeight = 32.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFFE2F6C6)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(32.0f, 16.0f)
        curveTo(32.0f, 24.8366f, 24.8366f, 32.0f, 16.0f, 32.0f)
        curveTo(7.1634f, 32.0f, 0.0f, 24.8366f, 0.0f, 16.0f)
        curveTo(0.0f, 7.1634f, 7.1634f, 0.0f, 16.0f, 0.0f)
        curveTo(24.8366f, 0.0f, 32.0f, 7.1634f, 32.0f, 16.0f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF24CC5C)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(13.6067f, 12.1573f)
        curveTo(14.2899f, 10.425f, 14.6315f, 9.5589f, 15.093f, 9.2646f)
        curveTo(15.6462f, 8.9118f, 16.3538f, 8.9118f, 16.907f, 9.2646f)
        curveTo(17.3685f, 9.5589f, 17.7101f, 10.425f, 18.3933f, 12.1573f)
        curveTo(18.532f, 12.5088f, 18.6013f, 12.6846f, 18.7028f, 12.8359f)
        curveTo(18.8251f, 13.0182f, 18.9818f, 13.1749f, 19.1641f, 13.2972f)
        curveTo(19.3154f, 13.3987f, 19.4912f, 13.468f, 19.8427f, 13.6067f)
        curveTo(21.575f, 14.2899f, 22.4411f, 14.6315f, 22.7354f, 15.093f)
        curveTo(23.0882f, 15.6462f, 23.0882f, 16.3538f, 22.7354f, 16.907f)
        curveTo(22.4411f, 17.3685f, 21.575f, 17.7101f, 19.8427f, 18.3933f)
        curveTo(19.4912f, 18.532f, 19.3154f, 18.6013f, 19.1641f, 18.7028f)
        curveTo(18.9818f, 18.8251f, 18.8251f, 18.9818f, 18.7028f, 19.1641f)
        curveTo(18.6013f, 19.3154f, 18.532f, 19.4912f, 18.3933f, 19.8427f)
        curveTo(17.7101f, 21.575f, 17.3685f, 22.4411f, 16.907f, 22.7354f)
        curveTo(16.3538f, 23.0882f, 15.6462f, 23.0882f, 15.093f, 22.7354f)
        curveTo(14.6315f, 22.4411f, 14.2899f, 21.575f, 13.6067f, 19.8427f)
        curveTo(13.468f, 19.4912f, 13.3987f, 19.3154f, 13.2972f, 19.1641f)
        curveTo(13.1749f, 18.9818f, 13.0182f, 18.8251f, 12.8359f, 18.7028f)
        curveTo(12.6846f, 18.6013f, 12.5088f, 18.532f, 12.1573f, 18.3933f)
        curveTo(10.425f, 17.7101f, 9.5589f, 17.3685f, 9.2646f, 16.907f)
        curveTo(8.9118f, 16.3538f, 8.9118f, 15.6462f, 9.2646f, 15.093f)
        curveTo(9.5589f, 14.6315f, 10.425f, 14.2899f, 12.1573f, 13.6067f)
        curveTo(12.5088f, 13.468f, 12.6846f, 13.3987f, 12.8359f, 13.2972f)
        curveTo(13.0182f, 13.1749f, 13.1749f, 13.0182f, 13.2972f, 12.8359f)
        curveTo(13.3987f, 12.6846f, 13.468f, 12.5088f, 13.6067f, 12.1573f)
        close()
      }
    }
      .build()
    return _campaign!!
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ColoredCampaign,
        contentDescription = null,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _campaign: ImageVector? = null
