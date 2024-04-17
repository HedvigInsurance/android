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
val HedvigIcons.CircleFilled: ImageVector
  get() {
    val current = _circleFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.CircleFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 2.5 A9.5 9.5 0 1 0 12 21.5 9.5 9.5 0 1 0 12 2.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 2.5
        moveTo(x = 12.0f, y = 2.5f)
        // A 9.5 9.5 0 1 0 12 21.5
        arcTo(
          horizontalEllipseRadius = 9.5f,
          verticalEllipseRadius = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 12.0f,
          y1 = 21.5f,
        )
        // A 9.5 9.5 0 1 0 12 2.5z
        arcTo(
          horizontalEllipseRadius = 9.5f,
          verticalEllipseRadius = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 12.0f,
          y1 = 2.5f,
        )
        close()
      }
    }.build().also { _circleFilled = it }
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
        imageVector = HedvigIcons.CircleFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _circleFilled: ImageVector? = null
