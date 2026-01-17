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
val HedvigIcons.PaymentOutline: ImageVector
  get() {
    val current = _paymentOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.PaymentOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M4.75 5 C4.75 3.48122 5.98122 2.25 7.5 2.25 H16.5 C18.0188 2.25 19.25 3.48122 19.25 5 V18.8484 C19.25 20.8642 17.153 22.195 15.3291 21.3367 L14.7585 21.0682 C14.4299 20.9136 14.0504 20.9095 13.7186 21.0569 L13.1169 21.3243 C12.4058 21.6404 11.5942 21.6404 10.8831 21.3243 L10.2814 21.0569 C9.94963 20.9095 9.57008 20.9136 9.24153 21.0682 L8.67094 21.3367 C6.84702 22.195 4.75 20.8642 4.75 18.8484 V5Z M7.5 3.75 C6.80964 3.75 6.25 4.30964 6.25 5 V18.8484 C6.25 19.7647 7.20319 20.3696 8.03225 19.9795 L8.60283 19.7109 C9.32565 19.3708 10.1607 19.3618 10.8907 19.6862 L11.4923 19.9536 C11.8155 20.0973 12.1845 20.0973 12.5077 19.9536 L13.1093 19.6862 C13.8393 19.3618 14.6744 19.3708 15.3972 19.7109 L15.9678 19.9795 C16.7968 20.3696 17.75 19.7647 17.75 18.8484 V5 C17.75 4.30964 17.1904 3.75 16.5 3.75 H7.5Z M8.25 8.25 C8.25 7.83579 8.58579 7.5 9 7.5 H15 C15.4142 7.5 15.75 7.83579 15.75 8.25 C15.75 8.66421 15.4142 9 15 9 H9 C8.58579 9 8.25 8.66421 8.25 8.25Z M8.25 10.75 C8.25 10.3358 8.58579 10 9 10 H13 C13.4142 10 13.75 10.3358 13.75 10.75 C13.75 11.1642 13.4142 11.5 13 11.5 H9 C8.58579 11.5 8.25 11.1642 8.25 10.75Z M8.25 13.25 C8.25 12.8358 8.58579 12.5 9 12.5 H15 C15.4142 12.5 15.75 12.8358 15.75 13.25 C15.75 13.6642 15.4142 14 15 14 H9 C8.58579 14 8.25 13.6642 8.25 13.25Z M8.25 15.75 C8.25 15.3358 8.58579 15 9 15 H13 C13.4142 15 13.75 15.3358 13.75 15.75 C13.75 16.1642 13.4142 16.5 13 16.5 H9 C8.58579 16.5 8.25 16.1642 8.25 15.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 4.75 5
        moveTo(x = 4.75f, y = 5.0f)
        // C 4.75 3.48122 5.98122 2.25 7.5 2.25
        curveTo(
          x1 = 4.75f,
          y1 = 3.48122f,
          x2 = 5.98122f,
          y2 = 2.25f,
          x3 = 7.5f,
          y3 = 2.25f,
        )
        // H 16.5
        horizontalLineTo(x = 16.5f)
        // C 18.0188 2.25 19.25 3.48122 19.25 5
        curveTo(
          x1 = 18.0188f,
          y1 = 2.25f,
          x2 = 19.25f,
          y2 = 3.48122f,
          x3 = 19.25f,
          y3 = 5.0f,
        )
        // V 18.8484
        verticalLineTo(y = 18.8484f)
        // C 19.25 20.8642 17.153 22.195 15.3291 21.3367
        curveTo(
          x1 = 19.25f,
          y1 = 20.8642f,
          x2 = 17.153f,
          y2 = 22.195f,
          x3 = 15.3291f,
          y3 = 21.3367f,
        )
        // L 14.7585 21.0682
        lineTo(x = 14.7585f, y = 21.0682f)
        // C 14.4299 20.9136 14.0504 20.9095 13.7186 21.0569
        curveTo(
          x1 = 14.4299f,
          y1 = 20.9136f,
          x2 = 14.0504f,
          y2 = 20.9095f,
          x3 = 13.7186f,
          y3 = 21.0569f,
        )
        // L 13.1169 21.3243
        lineTo(x = 13.1169f, y = 21.3243f)
        // C 12.4058 21.6404 11.5942 21.6404 10.8831 21.3243
        curveTo(
          x1 = 12.4058f,
          y1 = 21.6404f,
          x2 = 11.5942f,
          y2 = 21.6404f,
          x3 = 10.8831f,
          y3 = 21.3243f,
        )
        // L 10.2814 21.0569
        lineTo(x = 10.2814f, y = 21.0569f)
        // C 9.94963 20.9095 9.57008 20.9136 9.24153 21.0682
        curveTo(
          x1 = 9.94963f,
          y1 = 20.9095f,
          x2 = 9.57008f,
          y2 = 20.9136f,
          x3 = 9.24153f,
          y3 = 21.0682f,
        )
        // L 8.67094 21.3367
        lineTo(x = 8.67094f, y = 21.3367f)
        // C 6.84702 22.195 4.75 20.8642 4.75 18.8484
        curveTo(
          x1 = 6.84702f,
          y1 = 22.195f,
          x2 = 4.75f,
          y2 = 20.8642f,
          x3 = 4.75f,
          y3 = 18.8484f,
        )
        // V 5z
        verticalLineTo(y = 5.0f)
        close()
        // M 7.5 3.75
        moveTo(x = 7.5f, y = 3.75f)
        // C 6.80964 3.75 6.25 4.30964 6.25 5
        curveTo(
          x1 = 6.80964f,
          y1 = 3.75f,
          x2 = 6.25f,
          y2 = 4.30964f,
          x3 = 6.25f,
          y3 = 5.0f,
        )
        // V 18.8484
        verticalLineTo(y = 18.8484f)
        // C 6.25 19.7647 7.20319 20.3696 8.03225 19.9795
        curveTo(
          x1 = 6.25f,
          y1 = 19.7647f,
          x2 = 7.20319f,
          y2 = 20.3696f,
          x3 = 8.03225f,
          y3 = 19.9795f,
        )
        // L 8.60283 19.7109
        lineTo(x = 8.60283f, y = 19.7109f)
        // C 9.32565 19.3708 10.1607 19.3618 10.8907 19.6862
        curveTo(
          x1 = 9.32565f,
          y1 = 19.3708f,
          x2 = 10.1607f,
          y2 = 19.3618f,
          x3 = 10.8907f,
          y3 = 19.6862f,
        )
        // L 11.4923 19.9536
        lineTo(x = 11.4923f, y = 19.9536f)
        // C 11.8155 20.0973 12.1845 20.0973 12.5077 19.9536
        curveTo(
          x1 = 11.8155f,
          y1 = 20.0973f,
          x2 = 12.1845f,
          y2 = 20.0973f,
          x3 = 12.5077f,
          y3 = 19.9536f,
        )
        // L 13.1093 19.6862
        lineTo(x = 13.1093f, y = 19.6862f)
        // C 13.8393 19.3618 14.6744 19.3708 15.3972 19.7109
        curveTo(
          x1 = 13.8393f,
          y1 = 19.3618f,
          x2 = 14.6744f,
          y2 = 19.3708f,
          x3 = 15.3972f,
          y3 = 19.7109f,
        )
        // L 15.9678 19.9795
        lineTo(x = 15.9678f, y = 19.9795f)
        // C 16.7968 20.3696 17.75 19.7647 17.75 18.8484
        curveTo(
          x1 = 16.7968f,
          y1 = 20.3696f,
          x2 = 17.75f,
          y2 = 19.7647f,
          x3 = 17.75f,
          y3 = 18.8484f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // C 17.75 4.30964 17.1904 3.75 16.5 3.75
        curveTo(
          x1 = 17.75f,
          y1 = 4.30964f,
          x2 = 17.1904f,
          y2 = 3.75f,
          x3 = 16.5f,
          y3 = 3.75f,
        )
        // H 7.5z
        horizontalLineTo(x = 7.5f)
        close()
        // M 8.25 8.25
        moveTo(x = 8.25f, y = 8.25f)
        // C 8.25 7.83579 8.58579 7.5 9 7.5
        curveTo(
          x1 = 8.25f,
          y1 = 7.83579f,
          x2 = 8.58579f,
          y2 = 7.5f,
          x3 = 9.0f,
          y3 = 7.5f,
        )
        // H 15
        horizontalLineTo(x = 15.0f)
        // C 15.4142 7.5 15.75 7.83579 15.75 8.25
        curveTo(
          x1 = 15.4142f,
          y1 = 7.5f,
          x2 = 15.75f,
          y2 = 7.83579f,
          x3 = 15.75f,
          y3 = 8.25f,
        )
        // C 15.75 8.66421 15.4142 9 15 9
        curveTo(
          x1 = 15.75f,
          y1 = 8.66421f,
          x2 = 15.4142f,
          y2 = 9.0f,
          x3 = 15.0f,
          y3 = 9.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 9 8.25 8.66421 8.25 8.25z
        curveTo(
          x1 = 8.58579f,
          y1 = 9.0f,
          x2 = 8.25f,
          y2 = 8.66421f,
          x3 = 8.25f,
          y3 = 8.25f,
        )
        close()
        // M 8.25 10.75
        moveTo(x = 8.25f, y = 10.75f)
        // C 8.25 10.3358 8.58579 10 9 10
        curveTo(
          x1 = 8.25f,
          y1 = 10.3358f,
          x2 = 8.58579f,
          y2 = 10.0f,
          x3 = 9.0f,
          y3 = 10.0f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.4142 10 13.75 10.3358 13.75 10.75
        curveTo(
          x1 = 13.4142f,
          y1 = 10.0f,
          x2 = 13.75f,
          y2 = 10.3358f,
          x3 = 13.75f,
          y3 = 10.75f,
        )
        // C 13.75 11.1642 13.4142 11.5 13 11.5
        curveTo(
          x1 = 13.75f,
          y1 = 11.1642f,
          x2 = 13.4142f,
          y2 = 11.5f,
          x3 = 13.0f,
          y3 = 11.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 11.5 8.25 11.1642 8.25 10.75z
        curveTo(
          x1 = 8.58579f,
          y1 = 11.5f,
          x2 = 8.25f,
          y2 = 11.1642f,
          x3 = 8.25f,
          y3 = 10.75f,
        )
        close()
        // M 8.25 13.25
        moveTo(x = 8.25f, y = 13.25f)
        // C 8.25 12.8358 8.58579 12.5 9 12.5
        curveTo(
          x1 = 8.25f,
          y1 = 12.8358f,
          x2 = 8.58579f,
          y2 = 12.5f,
          x3 = 9.0f,
          y3 = 12.5f,
        )
        // H 15
        horizontalLineTo(x = 15.0f)
        // C 15.4142 12.5 15.75 12.8358 15.75 13.25
        curveTo(
          x1 = 15.4142f,
          y1 = 12.5f,
          x2 = 15.75f,
          y2 = 12.8358f,
          x3 = 15.75f,
          y3 = 13.25f,
        )
        // C 15.75 13.6642 15.4142 14 15 14
        curveTo(
          x1 = 15.75f,
          y1 = 13.6642f,
          x2 = 15.4142f,
          y2 = 14.0f,
          x3 = 15.0f,
          y3 = 14.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 14 8.25 13.6642 8.25 13.25z
        curveTo(
          x1 = 8.58579f,
          y1 = 14.0f,
          x2 = 8.25f,
          y2 = 13.6642f,
          x3 = 8.25f,
          y3 = 13.25f,
        )
        close()
        // M 8.25 15.75
        moveTo(x = 8.25f, y = 15.75f)
        // C 8.25 15.3358 8.58579 15 9 15
        curveTo(
          x1 = 8.25f,
          y1 = 15.3358f,
          x2 = 8.58579f,
          y2 = 15.0f,
          x3 = 9.0f,
          y3 = 15.0f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.4142 15 13.75 15.3358 13.75 15.75
        curveTo(
          x1 = 13.4142f,
          y1 = 15.0f,
          x2 = 13.75f,
          y2 = 15.3358f,
          x3 = 13.75f,
          y3 = 15.75f,
        )
        // C 13.75 16.1642 13.4142 16.5 13 16.5
        curveTo(
          x1 = 13.75f,
          y1 = 16.1642f,
          x2 = 13.4142f,
          y2 = 16.5f,
          x3 = 13.0f,
          y3 = 16.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 16.5 8.25 16.1642 8.25 15.75z
        curveTo(
          x1 = 8.58579f,
          y1 = 16.5f,
          x2 = 8.25f,
          y2 = 16.1642f,
          x3 = 8.25f,
          y3 = 15.75f,
        )
        close()
      }
    }.build().also { _paymentOutline = it }
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
        imageVector = HedvigIcons.PaymentOutline,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _paymentOutline: ImageVector? = null
