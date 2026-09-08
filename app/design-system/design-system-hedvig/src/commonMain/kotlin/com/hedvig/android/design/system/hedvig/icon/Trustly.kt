package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor

/**
 * A single monochrome path filled with the content color, so the mark stays legible in both themes
 * even where it is drawn without a tint.
 */
val HedvigIcons.Trustly: ImageVector
  @Composable
  get() {
    val color = LocalContentColor.current
    return remember(color) { trustlyImageVector(color) }
  }

private fun trustlyImageVector(color: Color): ImageVector = ImageVector.Builder(
  name = "Trustly",
  defaultWidth = 40.dp,
  defaultHeight = 32.dp,
  viewportWidth = 40f,
  viewportHeight = 32f,
).apply {
  path(fill = SolidColor(color)) {
    moveTo(0f, 14.018f)
    horizontalLineTo(13.992f)
    verticalLineTo(2.107f)
    lineTo(25.164f, 14.018f)
    lineTo(13.992f, 25.91f)
    verticalLineTo(32f)
    horizontalLineTo(27.553f)
    verticalLineTo(14.018f)
    horizontalLineTo(40f)
    verticalLineTo(0f)
    horizontalLineTo(0f)
    verticalLineTo(14.018f)
    close()
  }
}.build()

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Trustly,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
