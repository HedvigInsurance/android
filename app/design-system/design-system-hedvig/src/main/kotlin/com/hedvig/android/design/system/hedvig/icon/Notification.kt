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
val HedvigIcons.Notification: ImageVector
  get() {
    val current = _notification
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Notification",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.6746 3.83285 C14.8084 3.96818 15.8703 4.31011 16.5629 5.13666 C17.2573 5.96521 17.6554 6.99419 17.8399 7.95755 L19.9635 15.9812 C20.1759 16.9597 19.4326 17.8404 18.477 17.7425 H5.62904 C4.56723 17.7425 3.82396 16.8619 4.03632 15.9812 L6.15995 7.95755 C6.34555 7.04531 6.74746 6.00019 7.44968 5.15126 C8.13626 4.32124 9.19342 3.974 10.3245 3.8355 C10.5464 3.19089 11.2349 2.75 12 2.75 C12.7641 2.75 13.4518 3.18968 13.6746 3.83285Z M13.75 19.7167 C13.75 20.5961 12.9318 21.25 12 21.25 C11.0682 21.25 10.25 20.5961 10.25 19.7167 C10.25 19.4263 10.511 19.25 10.7556 19.25 H13.2444 C13.489 19.25 13.75 19.4263 13.75 19.7167Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 13.6746 3.83285
        moveTo(x = 13.6746f, y = 3.83285f)
        // C 14.8084 3.96818 15.8703 4.31011 16.5629 5.13666
        curveTo(
          x1 = 14.8084f,
          y1 = 3.96818f,
          x2 = 15.8703f,
          y2 = 4.31011f,
          x3 = 16.5629f,
          y3 = 5.13666f,
        )
        // C 17.2573 5.96521 17.6554 6.99419 17.8399 7.95755
        curveTo(
          x1 = 17.2573f,
          y1 = 5.96521f,
          x2 = 17.6554f,
          y2 = 6.99419f,
          x3 = 17.8399f,
          y3 = 7.95755f,
        )
        // L 19.9635 15.9812
        lineTo(x = 19.9635f, y = 15.9812f)
        // C 20.1759 16.9597 19.4326 17.8404 18.477 17.7425
        curveTo(
          x1 = 20.1759f,
          y1 = 16.9597f,
          x2 = 19.4326f,
          y2 = 17.8404f,
          x3 = 18.477f,
          y3 = 17.7425f,
        )
        // H 5.62904
        horizontalLineTo(x = 5.62904f)
        // C 4.56723 17.7425 3.82396 16.8619 4.03632 15.9812
        curveTo(
          x1 = 4.56723f,
          y1 = 17.7425f,
          x2 = 3.82396f,
          y2 = 16.8619f,
          x3 = 4.03632f,
          y3 = 15.9812f,
        )
        // L 6.15995 7.95755
        lineTo(x = 6.15995f, y = 7.95755f)
        // C 6.34555 7.04531 6.74746 6.00019 7.44968 5.15126
        curveTo(
          x1 = 6.34555f,
          y1 = 7.04531f,
          x2 = 6.74746f,
          y2 = 6.00019f,
          x3 = 7.44968f,
          y3 = 5.15126f,
        )
        // C 8.13626 4.32124 9.19342 3.974 10.3245 3.8355
        curveTo(
          x1 = 8.13626f,
          y1 = 4.32124f,
          x2 = 9.19342f,
          y2 = 3.974f,
          x3 = 10.3245f,
          y3 = 3.8355f,
        )
        // C 10.5464 3.19089 11.2349 2.75 12 2.75
        curveTo(
          x1 = 10.5464f,
          y1 = 3.19089f,
          x2 = 11.2349f,
          y2 = 2.75f,
          x3 = 12.0f,
          y3 = 2.75f,
        )
        // C 12.7641 2.75 13.4518 3.18968 13.6746 3.83285z
        curveTo(
          x1 = 12.7641f,
          y1 = 2.75f,
          x2 = 13.4518f,
          y2 = 3.18968f,
          x3 = 13.6746f,
          y3 = 3.83285f,
        )
        close()
        // M 13.75 19.7167
        moveTo(x = 13.75f, y = 19.7167f)
        // C 13.75 20.5961 12.9318 21.25 12 21.25
        curveTo(
          x1 = 13.75f,
          y1 = 20.5961f,
          x2 = 12.9318f,
          y2 = 21.25f,
          x3 = 12.0f,
          y3 = 21.25f,
        )
        // C 11.0682 21.25 10.25 20.5961 10.25 19.7167
        curveTo(
          x1 = 11.0682f,
          y1 = 21.25f,
          x2 = 10.25f,
          y2 = 20.5961f,
          x3 = 10.25f,
          y3 = 19.7167f,
        )
        // C 10.25 19.4263 10.511 19.25 10.7556 19.25
        curveTo(
          x1 = 10.25f,
          y1 = 19.4263f,
          x2 = 10.511f,
          y2 = 19.25f,
          x3 = 10.7556f,
          y3 = 19.25f,
        )
        // H 13.2444
        horizontalLineTo(x = 13.2444f)
        // C 13.489 19.25 13.75 19.4263 13.75 19.7167z
        curveTo(
          x1 = 13.489f,
          y1 = 19.25f,
          x2 = 13.75f,
          y2 = 19.4263f,
          x3 = 13.75f,
          y3 = 19.7167f,
        )
        close()
      }
    }.build().also { _notification = it }
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
        imageVector = HedvigIcons.Notification,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _notification: ImageVector? = null
