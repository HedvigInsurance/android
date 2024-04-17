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
val HedvigIcons.Card: ImageVector
  get() {
    val current = _card
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Card",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 8 A2.75 2.75 0 0 0 19 5.25 H5 A2.75 2.75 0 0 0 2.25 8 v8 A2.75 2.75 0 0 0 5 18.75 h14 A2.75 2.75 0 0 0 21.75 16z M19 6.75 A1.25 1.25 0 0 1 20.25 8 v1 H3.75 V8 A1.25 1.25 0 0 1 5 6.75z M3.75 12 v4 A1.25 1.25 0 0 0 5 17.25 h14 A1.25 1.25 0 0 0 20.25 16 v-4z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.75 8
        moveTo(x = 21.75f, y = 8.0f)
        // A 2.75 2.75 0 0 0 19 5.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 19.0f,
          y1 = 5.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 2.75 2.75 0 0 0 2.25 8
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 2.25f,
          y1 = 8.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 2.75 2.75 0 0 0 5 18.75
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 5.0f,
          y1 = 18.75f,
        )
        // h 14
        horizontalLineToRelative(dx = 14.0f)
        // A 2.75 2.75 0 0 0 21.75 16z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 21.75f,
          y1 = 16.0f,
        )
        close()
        // M 19 6.75
        moveTo(x = 19.0f, y = 6.75f)
        // A 1.25 1.25 0 0 1 20.25 8
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 20.25f,
          y1 = 8.0f,
        )
        // v 1
        verticalLineToRelative(dy = 1.0f)
        // H 3.75
        horizontalLineTo(x = 3.75f)
        // V 8
        verticalLineTo(y = 8.0f)
        // A 1.25 1.25 0 0 1 5 6.75z
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 6.75f,
        )
        close()
        // M 3.75 12
        moveTo(x = 3.75f, y = 12.0f)
        // v 4
        verticalLineToRelative(dy = 4.0f)
        // A 1.25 1.25 0 0 0 5 17.25
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 5.0f,
          y1 = 17.25f,
        )
        // h 14
        horizontalLineToRelative(dx = 14.0f)
        // A 1.25 1.25 0 0 0 20.25 16
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 20.25f,
          y1 = 16.0f,
        )
        // v -4z
        verticalLineToRelative(dy = -4.0f)
        close()
      }
    }.build().also { _card = it }
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
        imageVector = Card,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _card: ImageVector? = null
