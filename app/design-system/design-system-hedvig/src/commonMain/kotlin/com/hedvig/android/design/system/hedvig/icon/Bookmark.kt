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
val HedvigIcons.Bookmark: ImageVector
  get() {
    val current = _Bookmark
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Bookmark",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(7.5f, 3.75f)
        curveTo(6.80964f, 3.75f, 6.25f, 4.30964f, 6.25f, 5f)
        verticalLineTo(18.3159f)
        curveTo(6.25f, 19.3076f, 7.34933f, 19.9044f, 8.18092f, 19.3642f)
        lineTo(10.502f, 17.8565f)
        curveTo(11.413f, 17.2647f, 12.587f, 17.2647f, 13.498f, 17.8565f)
        lineTo(15.8191f, 19.3642f)
        curveTo(16.6507f, 19.9044f, 17.75f, 19.3076f, 17.75f, 18.3159f)
        verticalLineTo(5f)
        curveTo(17.75f, 4.30964f, 17.1904f, 3.75f, 16.5f, 3.75f)
        horizontalLineTo(7.5f)
        close()
        moveTo(4.75f, 5f)
        curveTo(4.75f, 3.48122f, 5.98122f, 2.25f, 7.5f, 2.25f)
        horizontalLineTo(16.5f)
        curveTo(18.0188f, 2.25f, 19.25f, 3.48122f, 19.25f, 5f)
        verticalLineTo(18.3159f)
        curveTo(19.25f, 20.4975f, 16.8315f, 21.8105f, 15.002f, 20.6221f)
        lineTo(12.6809f, 19.1144f)
        curveTo(12.2668f, 18.8454f, 11.7332f, 18.8454f, 11.3191f, 19.1144f)
        lineTo(8.99802f, 20.6221f)
        curveTo(7.16853f, 21.8105f, 4.75f, 20.4975f, 4.75f, 18.3159f)
        verticalLineTo(5f)
        close()
      }
    }.build().also { _Bookmark = it }
  }

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _Bookmark: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Bookmark,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
