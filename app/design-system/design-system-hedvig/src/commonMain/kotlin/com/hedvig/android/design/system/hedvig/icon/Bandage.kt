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
val HedvigIcons.Bandage: ImageVector
  get() {
    if (_Bandage != null) {
      return _Bandage!!
    }
    _Bandage = ImageVector.Builder(
      name = "Bandage",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(fill = SolidColor(Color(0xFF121212))) {
        moveTo(5.175f, 2.388f)
        curveTo(6.335f, 1.718f, 7.765f, 1.718f, 8.925f, 2.388f)
        curveTo(9.374f, 2.647f, 9.785f, 3.068f, 10.409f, 3.692f)
        lineTo(20.308f, 13.591f)
        curveTo(20.931f, 14.214f, 21.353f, 14.625f, 21.612f, 15.075f)
        curveTo(22.282f, 16.235f, 22.281f, 17.664f, 21.612f, 18.824f)
        curveTo(21.353f, 19.274f, 20.931f, 19.685f, 20.308f, 20.309f)
        curveTo(19.685f, 20.932f, 19.274f, 21.352f, 18.825f, 21.612f)
        curveTo(17.664f, 22.281f, 16.235f, 22.281f, 15.075f, 21.612f)
        curveTo(14.626f, 21.352f, 14.215f, 20.932f, 13.591f, 20.309f)
        lineTo(3.692f, 10.408f)
        curveTo(3.068f, 9.785f, 2.647f, 9.374f, 2.388f, 8.925f)
        curveTo(1.718f, 7.765f, 1.718f, 6.335f, 2.388f, 5.175f)
        curveTo(2.647f, 4.726f, 3.068f, 4.315f, 3.692f, 3.692f)
        curveTo(4.315f, 3.068f, 4.726f, 2.647f, 5.175f, 2.388f)
        close()
        moveTo(8.175f, 3.688f)
        curveTo(7.479f, 3.286f, 6.621f, 3.286f, 5.925f, 3.688f)
        curveTo(5.69f, 3.823f, 5.444f, 4.061f, 4.752f, 4.752f)
        curveTo(4.061f, 5.444f, 3.824f, 5.69f, 3.688f, 5.925f)
        curveTo(3.286f, 6.621f, 3.286f, 7.479f, 3.688f, 8.175f)
        curveTo(3.824f, 8.41f, 4.061f, 8.656f, 4.752f, 9.348f)
        lineTo(14.652f, 19.247f)
        curveTo(15.343f, 19.939f, 15.59f, 20.176f, 15.825f, 20.312f)
        curveTo(16.521f, 20.714f, 17.378f, 20.714f, 18.075f, 20.312f)
        curveTo(18.31f, 20.176f, 18.556f, 19.939f, 19.248f, 19.247f)
        curveTo(19.939f, 18.556f, 20.176f, 18.309f, 20.312f, 18.074f)
        curveTo(20.714f, 17.378f, 20.714f, 16.521f, 20.312f, 15.825f)
        curveTo(20.176f, 15.59f, 19.939f, 15.343f, 19.248f, 14.652f)
        lineTo(9.348f, 4.752f)
        curveTo(8.657f, 4.061f, 8.41f, 3.823f, 8.175f, 3.688f)
        close()
        moveTo(11.47f, 12.813f)
        curveTo(11.763f, 12.52f, 12.238f, 12.52f, 12.531f, 12.813f)
        curveTo(12.823f, 13.106f, 12.823f, 13.58f, 12.531f, 13.873f)
        curveTo(12.238f, 14.166f, 11.763f, 14.166f, 11.47f, 13.873f)
        curveTo(11.177f, 13.58f, 11.177f, 13.106f, 11.47f, 12.813f)
        close()
        moveTo(9.915f, 11.258f)
        curveTo(10.207f, 10.965f, 10.682f, 10.965f, 10.975f, 11.258f)
        curveTo(11.268f, 11.551f, 11.268f, 12.026f, 10.975f, 12.319f)
        curveTo(10.682f, 12.611f, 10.208f, 12.611f, 9.915f, 12.319f)
        curveTo(9.622f, 12.026f, 9.622f, 11.551f, 9.915f, 11.258f)
        close()
        moveTo(13.025f, 11.258f)
        curveTo(13.318f, 10.965f, 13.793f, 10.965f, 14.086f, 11.258f)
        curveTo(14.379f, 11.551f, 14.379f, 12.026f, 14.086f, 12.319f)
        curveTo(13.793f, 12.611f, 13.318f, 12.611f, 13.025f, 12.319f)
        curveTo(12.732f, 12.026f, 12.733f, 11.551f, 13.025f, 11.258f)
        close()
        moveTo(11.469f, 9.702f)
        curveTo(11.762f, 9.409f, 12.238f, 9.409f, 12.531f, 9.702f)
        curveTo(12.823f, 9.995f, 12.823f, 10.47f, 12.531f, 10.763f)
        curveTo(12.238f, 11.056f, 11.762f, 11.056f, 11.469f, 10.763f)
        curveTo(11.177f, 10.47f, 11.177f, 9.995f, 11.469f, 9.702f)
        close()
      }
    }.build()

    return _Bandage!!
  }

@Suppress("ObjectPropertyName")
private var _Bandage: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Bandage,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
