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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ChatNoCircle: ImageVector
  get() {
    if (_ChatNoCircle != null) {
      return _ChatNoCircle!!
    }
    _ChatNoCircle = ImageVector.Builder(
      name = "ChatNoCircle",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(5.118f, 5f)
        curveTo(3.948f, 5f, 3f, 5.964f, 3f, 7.154f)
        verticalLineTo(16.846f)
        curveTo(3f, 18.036f, 3.948f, 19f, 5.118f, 19f)
        horizontalLineTo(18.882f)
        curveTo(20.052f, 19f, 21f, 18.036f, 21f, 16.846f)
        verticalLineTo(7.154f)
        curveTo(21f, 5.964f, 20.052f, 5f, 18.882f, 5f)
        horizontalLineTo(5.118f)
        close()
        moveTo(12.583f, 13.224f)
        curveTo(12.229f, 13.462f, 11.771f, 13.462f, 11.417f, 13.224f)
        lineTo(5.458f, 9.228f)
        curveTo(5.246f, 9.086f, 5.118f, 8.844f, 5.118f, 8.585f)
        curveTo(5.118f, 7.971f, 5.789f, 7.604f, 6.292f, 7.942f)
        lineTo(11.417f, 11.378f)
        curveTo(11.771f, 11.616f, 12.229f, 11.616f, 12.583f, 11.378f)
        lineTo(17.708f, 7.942f)
        curveTo(18.211f, 7.604f, 18.882f, 7.971f, 18.882f, 8.585f)
        curveTo(18.882f, 8.844f, 18.754f, 9.086f, 18.542f, 9.228f)
        lineTo(12.583f, 13.224f)
        close()
      }
    }.build()

    return _ChatNoCircle!!
  }

@Suppress("ObjectPropertyName")
private var _ChatNoCircle: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ChatNoCircle,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}
