package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Campaign: ImageVector
  get() {
    val current = _campaign
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Campaign",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.50756 3.72951 C10.4066 1.44987 13.6329 1.44986 14.532 3.72951 L15.7359 6.78219 C16.0104 7.47818 16.5614 8.02911 17.2574 8.3036 L20.31 9.50756 C22.5897 10.4066 22.5897 13.6329 20.31 14.532 L17.2574 15.7359 C16.5614 16.0104 16.0104 16.5614 15.7359 17.2574 L14.532 20.31 C13.6329 22.5897 10.4066 22.5897 9.50756 20.31 L8.3036 17.2574 C8.02911 16.5614 7.47818 16.0104 6.78219 15.7359 L3.72951 14.532 C1.44987 13.6329 1.44986 10.4066 3.72951 9.50756 L6.78219 8.3036 C7.47818 8.02911 8.02911 7.47818 8.3036 6.78219 L9.50756 3.72951Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 9.50756 3.72951
        moveTo(x = 9.50756f, y = 3.72951f)
        // C 10.4066 1.44987 13.6329 1.44986 14.532 3.72951
        curveTo(
          x1 = 10.4066f,
          y1 = 1.44987f,
          x2 = 13.6329f,
          y2 = 1.44986f,
          x3 = 14.532f,
          y3 = 3.72951f,
        )
        // L 15.7359 6.78219
        lineTo(x = 15.7359f, y = 6.78219f)
        // C 16.0104 7.47818 16.5614 8.02911 17.2574 8.3036
        curveTo(
          x1 = 16.0104f,
          y1 = 7.47818f,
          x2 = 16.5614f,
          y2 = 8.02911f,
          x3 = 17.2574f,
          y3 = 8.3036f,
        )
        // L 20.31 9.50756
        lineTo(x = 20.31f, y = 9.50756f)
        // C 22.5897 10.4066 22.5897 13.6329 20.31 14.532
        curveTo(
          x1 = 22.5897f,
          y1 = 10.4066f,
          x2 = 22.5897f,
          y2 = 13.6329f,
          x3 = 20.31f,
          y3 = 14.532f,
        )
        // L 17.2574 15.7359
        lineTo(x = 17.2574f, y = 15.7359f)
        // C 16.5614 16.0104 16.0104 16.5614 15.7359 17.2574
        curveTo(
          x1 = 16.5614f,
          y1 = 16.0104f,
          x2 = 16.0104f,
          y2 = 16.5614f,
          x3 = 15.7359f,
          y3 = 17.2574f,
        )
        // L 14.532 20.31
        lineTo(x = 14.532f, y = 20.31f)
        // C 13.6329 22.5897 10.4066 22.5897 9.50756 20.31
        curveTo(
          x1 = 13.6329f,
          y1 = 22.5897f,
          x2 = 10.4066f,
          y2 = 22.5897f,
          x3 = 9.50756f,
          y3 = 20.31f,
        )
        // L 8.3036 17.2574
        lineTo(x = 8.3036f, y = 17.2574f)
        // C 8.02911 16.5614 7.47818 16.0104 6.78219 15.7359
        curveTo(
          x1 = 8.02911f,
          y1 = 16.5614f,
          x2 = 7.47818f,
          y2 = 16.0104f,
          x3 = 6.78219f,
          y3 = 15.7359f,
        )
        // L 3.72951 14.532
        lineTo(x = 3.72951f, y = 14.532f)
        // C 1.44987 13.6329 1.44986 10.4066 3.72951 9.50756
        curveTo(
          x1 = 1.44987f,
          y1 = 13.6329f,
          x2 = 1.44986f,
          y2 = 10.4066f,
          x3 = 3.72951f,
          y3 = 9.50756f,
        )
        // L 6.78219 8.3036
        lineTo(x = 6.78219f, y = 8.3036f)
        // C 7.47818 8.02911 8.02911 7.47818 8.3036 6.78219
        curveTo(
          x1 = 7.47818f,
          y1 = 8.02911f,
          x2 = 8.02911f,
          y2 = 7.47818f,
          x3 = 8.3036f,
          y3 = 6.78219f,
        )
        // L 9.50756 3.72951z
        lineTo(x = 9.50756f, y = 3.72951f)
        close()
      }
    }.build().also { _campaign = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Campaign,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _campaign: ImageVector? = null
