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
val HedvigIcons.Certified: ImageVector
  get() {
    val current = _certified
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Certified",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.3395 2.52345 C12.5796 1.82552 11.4204 1.82552 10.6605 2.52345 L9.64204 3.45888 C9.25554 3.81385 8.74694 4.00125 8.22546 3.98081 L6.85129 3.92697 C5.826 3.88679 4.93805 4.64104 4.7991 5.67015 L4.61286 7.04946 C4.54218 7.57289 4.27156 8.04738 3.85911 8.37106 L2.77224 9.22398 C1.96131 9.86036 1.76003 11.0159 2.30705 11.8947 L3.0402 13.0725 C3.31842 13.5195 3.41241 14.059 3.30197 14.5754 L3.01097 15.936 C2.79384 16.9511 3.37341 17.9673 4.35044 18.2846 L5.65993 18.7097 C6.15687 18.8711 6.57148 19.2233 6.81474 19.6907 L7.45576 20.9223 C7.93403 21.8412 9.02326 22.2426 9.97314 21.8498 L11.2463 21.3235 C11.7294 21.1237 12.2706 21.1237 12.7537 21.3235 L14.0269 21.8498 C14.9767 22.2426 16.066 21.8412 16.5442 20.9223 L17.1853 19.6907 C17.4285 19.2233 17.8431 18.8711 18.3401 18.7097 L19.6496 18.2846 C20.6266 17.9673 21.2062 16.9511 20.989 15.936 L20.698 14.5754 C20.5876 14.059 20.6816 13.5195 20.9598 13.0725 L21.693 11.8947 C22.24 11.0159 22.0387 9.86036 21.2278 9.22398 L20.1409 8.37106 C19.7284 8.04739 19.4578 7.57289 19.3871 7.04946 L19.2009 5.67015 C19.062 4.64104 18.174 3.88679 17.1487 3.92697 L15.7745 3.98081 C15.2531 4.00125 14.7445 3.81386 14.358 3.45888 L13.3395 2.52345Z M15.5281 11.0325 C15.8222 10.7408 15.8241 10.266 15.5325 9.97187 C15.2408 9.67776 14.7659 9.6758 14.4718 9.96748 L11.1428 13.2691 C11.0453 13.3658 10.8882 13.3658 10.7907 13.2691 L9.52814 12.0169 C9.23404 11.7252 8.75917 11.7272 8.46748 12.0213 C8.1758 12.3154 8.17776 12.7903 8.47186 13.082 L9.73442 14.3341 C10.4167 15.0107 11.5168 15.0107 12.1991 14.3341 L15.5281 11.0325Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 13.3395 2.52345
        moveTo(x = 13.3395f, y = 2.52345f)
        // C 12.5796 1.82552 11.4204 1.82552 10.6605 2.52345
        curveTo(
          x1 = 12.5796f,
          y1 = 1.82552f,
          x2 = 11.4204f,
          y2 = 1.82552f,
          x3 = 10.6605f,
          y3 = 2.52345f,
        )
        // L 9.64204 3.45888
        lineTo(x = 9.64204f, y = 3.45888f)
        // C 9.25554 3.81385 8.74694 4.00125 8.22546 3.98081
        curveTo(
          x1 = 9.25554f,
          y1 = 3.81385f,
          x2 = 8.74694f,
          y2 = 4.00125f,
          x3 = 8.22546f,
          y3 = 3.98081f,
        )
        // L 6.85129 3.92697
        lineTo(x = 6.85129f, y = 3.92697f)
        // C 5.826 3.88679 4.93805 4.64104 4.7991 5.67015
        curveTo(
          x1 = 5.826f,
          y1 = 3.88679f,
          x2 = 4.93805f,
          y2 = 4.64104f,
          x3 = 4.7991f,
          y3 = 5.67015f,
        )
        // L 4.61286 7.04946
        lineTo(x = 4.61286f, y = 7.04946f)
        // C 4.54218 7.57289 4.27156 8.04738 3.85911 8.37106
        curveTo(
          x1 = 4.54218f,
          y1 = 7.57289f,
          x2 = 4.27156f,
          y2 = 8.04738f,
          x3 = 3.85911f,
          y3 = 8.37106f,
        )
        // L 2.77224 9.22398
        lineTo(x = 2.77224f, y = 9.22398f)
        // C 1.96131 9.86036 1.76003 11.0159 2.30705 11.8947
        curveTo(
          x1 = 1.96131f,
          y1 = 9.86036f,
          x2 = 1.76003f,
          y2 = 11.0159f,
          x3 = 2.30705f,
          y3 = 11.8947f,
        )
        // L 3.0402 13.0725
        lineTo(x = 3.0402f, y = 13.0725f)
        // C 3.31842 13.5195 3.41241 14.059 3.30197 14.5754
        curveTo(
          x1 = 3.31842f,
          y1 = 13.5195f,
          x2 = 3.41241f,
          y2 = 14.059f,
          x3 = 3.30197f,
          y3 = 14.5754f,
        )
        // L 3.01097 15.936
        lineTo(x = 3.01097f, y = 15.936f)
        // C 2.79384 16.9511 3.37341 17.9673 4.35044 18.2846
        curveTo(
          x1 = 2.79384f,
          y1 = 16.9511f,
          x2 = 3.37341f,
          y2 = 17.9673f,
          x3 = 4.35044f,
          y3 = 18.2846f,
        )
        // L 5.65993 18.7097
        lineTo(x = 5.65993f, y = 18.7097f)
        // C 6.15687 18.8711 6.57148 19.2233 6.81474 19.6907
        curveTo(
          x1 = 6.15687f,
          y1 = 18.8711f,
          x2 = 6.57148f,
          y2 = 19.2233f,
          x3 = 6.81474f,
          y3 = 19.6907f,
        )
        // L 7.45576 20.9223
        lineTo(x = 7.45576f, y = 20.9223f)
        // C 7.93403 21.8412 9.02326 22.2426 9.97314 21.8498
        curveTo(
          x1 = 7.93403f,
          y1 = 21.8412f,
          x2 = 9.02326f,
          y2 = 22.2426f,
          x3 = 9.97314f,
          y3 = 21.8498f,
        )
        // L 11.2463 21.3235
        lineTo(x = 11.2463f, y = 21.3235f)
        // C 11.7294 21.1237 12.2706 21.1237 12.7537 21.3235
        curveTo(
          x1 = 11.7294f,
          y1 = 21.1237f,
          x2 = 12.2706f,
          y2 = 21.1237f,
          x3 = 12.7537f,
          y3 = 21.3235f,
        )
        // L 14.0269 21.8498
        lineTo(x = 14.0269f, y = 21.8498f)
        // C 14.9767 22.2426 16.066 21.8412 16.5442 20.9223
        curveTo(
          x1 = 14.9767f,
          y1 = 22.2426f,
          x2 = 16.066f,
          y2 = 21.8412f,
          x3 = 16.5442f,
          y3 = 20.9223f,
        )
        // L 17.1853 19.6907
        lineTo(x = 17.1853f, y = 19.6907f)
        // C 17.4285 19.2233 17.8431 18.8711 18.3401 18.7097
        curveTo(
          x1 = 17.4285f,
          y1 = 19.2233f,
          x2 = 17.8431f,
          y2 = 18.8711f,
          x3 = 18.3401f,
          y3 = 18.7097f,
        )
        // L 19.6496 18.2846
        lineTo(x = 19.6496f, y = 18.2846f)
        // C 20.6266 17.9673 21.2062 16.9511 20.989 15.936
        curveTo(
          x1 = 20.6266f,
          y1 = 17.9673f,
          x2 = 21.2062f,
          y2 = 16.9511f,
          x3 = 20.989f,
          y3 = 15.936f,
        )
        // L 20.698 14.5754
        lineTo(x = 20.698f, y = 14.5754f)
        // C 20.5876 14.059 20.6816 13.5195 20.9598 13.0725
        curveTo(
          x1 = 20.5876f,
          y1 = 14.059f,
          x2 = 20.6816f,
          y2 = 13.5195f,
          x3 = 20.9598f,
          y3 = 13.0725f,
        )
        // L 21.693 11.8947
        lineTo(x = 21.693f, y = 11.8947f)
        // C 22.24 11.0159 22.0387 9.86036 21.2278 9.22398
        curveTo(
          x1 = 22.24f,
          y1 = 11.0159f,
          x2 = 22.0387f,
          y2 = 9.86036f,
          x3 = 21.2278f,
          y3 = 9.22398f,
        )
        // L 20.1409 8.37106
        lineTo(x = 20.1409f, y = 8.37106f)
        // C 19.7284 8.04739 19.4578 7.57289 19.3871 7.04946
        curveTo(
          x1 = 19.7284f,
          y1 = 8.04739f,
          x2 = 19.4578f,
          y2 = 7.57289f,
          x3 = 19.3871f,
          y3 = 7.04946f,
        )
        // L 19.2009 5.67015
        lineTo(x = 19.2009f, y = 5.67015f)
        // C 19.062 4.64104 18.174 3.88679 17.1487 3.92697
        curveTo(
          x1 = 19.062f,
          y1 = 4.64104f,
          x2 = 18.174f,
          y2 = 3.88679f,
          x3 = 17.1487f,
          y3 = 3.92697f,
        )
        // L 15.7745 3.98081
        lineTo(x = 15.7745f, y = 3.98081f)
        // C 15.2531 4.00125 14.7445 3.81386 14.358 3.45888
        curveTo(
          x1 = 15.2531f,
          y1 = 4.00125f,
          x2 = 14.7445f,
          y2 = 3.81386f,
          x3 = 14.358f,
          y3 = 3.45888f,
        )
        // L 13.3395 2.52345z
        lineTo(x = 13.3395f, y = 2.52345f)
        close()
        // M 15.5281 11.0325
        moveTo(x = 15.5281f, y = 11.0325f)
        // C 15.8222 10.7408 15.8241 10.266 15.5325 9.97187
        curveTo(
          x1 = 15.8222f,
          y1 = 10.7408f,
          x2 = 15.8241f,
          y2 = 10.266f,
          x3 = 15.5325f,
          y3 = 9.97187f,
        )
        // C 15.2408 9.67776 14.7659 9.6758 14.4718 9.96748
        curveTo(
          x1 = 15.2408f,
          y1 = 9.67776f,
          x2 = 14.7659f,
          y2 = 9.6758f,
          x3 = 14.4718f,
          y3 = 9.96748f,
        )
        // L 11.1428 13.2691
        lineTo(x = 11.1428f, y = 13.2691f)
        // C 11.0453 13.3658 10.8882 13.3658 10.7907 13.2691
        curveTo(
          x1 = 11.0453f,
          y1 = 13.3658f,
          x2 = 10.8882f,
          y2 = 13.3658f,
          x3 = 10.7907f,
          y3 = 13.2691f,
        )
        // L 9.52814 12.0169
        lineTo(x = 9.52814f, y = 12.0169f)
        // C 9.23404 11.7252 8.75917 11.7272 8.46748 12.0213
        curveTo(
          x1 = 9.23404f,
          y1 = 11.7252f,
          x2 = 8.75917f,
          y2 = 11.7272f,
          x3 = 8.46748f,
          y3 = 12.0213f,
        )
        // C 8.1758 12.3154 8.17776 12.7903 8.47186 13.082
        curveTo(
          x1 = 8.1758f,
          y1 = 12.3154f,
          x2 = 8.17776f,
          y2 = 12.7903f,
          x3 = 8.47186f,
          y3 = 13.082f,
        )
        // L 9.73442 14.3341
        lineTo(x = 9.73442f, y = 14.3341f)
        // C 10.4167 15.0107 11.5168 15.0107 12.1991 14.3341
        curveTo(
          x1 = 10.4167f,
          y1 = 15.0107f,
          x2 = 11.5168f,
          y2 = 15.0107f,
          x3 = 12.1991f,
          y3 = 14.3341f,
        )
        // L 15.5281 11.0325z
        lineTo(x = 15.5281f, y = 11.0325f)
        close()
      }
    }.build().also { _certified = it }
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
        imageVector = HedvigIcons.Certified,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _certified: ImageVector? = null
