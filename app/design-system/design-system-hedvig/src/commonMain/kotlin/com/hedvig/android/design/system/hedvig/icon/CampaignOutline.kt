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
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.CampaignOutline: ImageVector
  get() {
    if (_CampaignOutline != null) {
      return _CampaignOutline!!
    }
    _CampaignOutline = ImageVector.Builder(
      name = "CampaignOutline",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
      ) {
        moveTo(10.205f, 4.005f)
        curveTo(10.854f, 2.358f, 13.185f, 2.358f, 13.834f, 4.005f)
        lineTo(15.038f, 7.057f)
        curveTo(15.389f, 7.946f, 16.093f, 8.65f, 16.982f, 9.001f)
        lineTo(20.034f, 10.205f)
        curveTo(21.681f, 10.854f, 21.681f, 13.185f, 20.034f, 13.834f)
        lineTo(16.982f, 15.038f)
        curveTo(16.093f, 15.389f, 15.389f, 16.093f, 15.038f, 16.982f)
        lineTo(13.834f, 20.034f)
        curveTo(13.185f, 21.681f, 10.854f, 21.681f, 10.205f, 20.034f)
        lineTo(9.001f, 16.982f)
        curveTo(8.65f, 16.093f, 7.946f, 15.389f, 7.057f, 15.038f)
        lineTo(4.005f, 13.834f)
        curveTo(2.358f, 13.185f, 2.358f, 10.854f, 4.005f, 10.205f)
        lineTo(7.057f, 9.001f)
        curveTo(7.946f, 8.65f, 8.65f, 7.946f, 9.001f, 7.057f)
        lineTo(10.205f, 4.005f)
        close()
      }
    }.build()

    return _CampaignOutline!!
  }

@Suppress("ObjectPropertyName")
private var _CampaignOutline: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.CampaignOutline,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
