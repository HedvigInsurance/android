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
public val HedvigIcons.Eurobonus: ImageVector
  get() {
    if (_eurobonus != null) {
      return _eurobonus!!
    }
    _eurobonus =
      ImageVector.Builder(
        name = "com.hedvig.android.design.system.hedvig.icon.Eurobonus",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f,
      ).apply {
        path(
          fill = SolidColor(Color(0xFF121212)),
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(9.75f, 3.0f)
          verticalLineTo(4.0f)
          horizontalLineTo(8.25f)
          verticalLineTo(3.0f)
          curveTo(8.25f, 2.0335f, 9.0335f, 1.25f, 10.0f, 1.25f)
          lineTo(14.0f, 1.25f)
          curveTo(14.9665f, 1.25f, 15.75f, 2.0335f, 15.75f, 3.0f)
          verticalLineTo(4.0f)
          lineTo(14.25f, 4.0f)
          verticalLineTo(3.0f)
          curveTo(14.25f, 2.8619f, 14.1381f, 2.75f, 14.0f, 2.75f)
          lineTo(10.0f, 2.75f)
          curveTo(9.8619f, 2.75f, 9.75f, 2.8619f, 9.75f, 3.0f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF121212)),
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(4.25f, 17.052f)
          lineTo(4.25f, 8.698f)
          curveTo(4.25f, 7.7995f, 4.2499f, 7.0503f, 4.3299f, 6.4555f)
          curveTo(4.4143f, 5.8277f, 4.6f, 5.2609f, 5.0555f, 4.8054f)
          curveTo(5.5109f, 4.35f, 6.0777f, 4.1643f, 6.7055f, 4.0799f)
          curveTo(7.3003f, 3.9999f, 8.0495f, 4.0f, 8.948f, 4.0f)
          lineTo(15.052f, 4.0f)
          curveTo(15.9505f, 4.0f, 16.6997f, 3.9999f, 17.2945f, 4.0799f)
          curveTo(17.9223f, 4.1643f, 18.4891f, 4.35f, 18.9445f, 4.8054f)
          curveTo(19.4f, 5.2609f, 19.5857f, 5.8277f, 19.6701f, 6.4555f)
          curveTo(19.7501f, 7.0503f, 19.75f, 7.7995f, 19.75f, 8.698f)
          lineTo(19.75f, 17.052f)
          curveTo(19.75f, 17.9505f, 19.7501f, 18.6997f, 19.6701f, 19.2945f)
          curveTo(19.5857f, 19.9223f, 19.4f, 20.4891f, 18.9445f, 20.9445f)
          curveTo(18.4891f, 21.4f, 17.9223f, 21.5857f, 17.2945f, 21.6701f)
          curveTo(16.6997f, 21.7501f, 15.9505f, 21.75f, 15.052f, 21.75f)
          lineTo(8.948f, 21.75f)
          curveTo(8.0495f, 21.75f, 7.3003f, 21.7501f, 6.7055f, 21.6701f)
          curveTo(6.0777f, 21.5857f, 5.5109f, 21.4f, 5.0555f, 20.9445f)
          curveTo(4.6f, 20.4891f, 4.4143f, 19.9223f, 4.3299f, 19.2945f)
          curveTo(4.2499f, 18.6997f, 4.25f, 17.9505f, 4.25f, 17.052f)
          close()
          moveTo(5.8165f, 19.0946f)
          curveTo(5.8786f, 19.5561f, 5.9858f, 19.7536f, 6.1161f, 19.8839f)
          curveTo(6.2464f, 20.0142f, 6.4439f, 20.1214f, 6.9054f, 20.1835f)
          curveTo(7.3884f, 20.2484f, 8.036f, 20.25f, 9.0f, 20.25f)
          lineTo(15.0f, 20.25f)
          curveTo(15.964f, 20.25f, 16.6116f, 20.2484f, 17.0946f, 20.1835f)
          curveTo(17.5561f, 20.1214f, 17.7536f, 20.0142f, 17.8839f, 19.8839f)
          curveTo(18.0142f, 19.7536f, 18.1214f, 19.5561f, 18.1835f, 19.0946f)
          curveTo(18.2484f, 18.6116f, 18.25f, 17.964f, 18.25f, 17.0f)
          lineTo(18.25f, 8.75f)
          curveTo(18.25f, 7.786f, 18.2484f, 7.1384f, 18.1835f, 6.6554f)
          curveTo(18.1214f, 6.1939f, 18.0142f, 5.9964f, 17.8839f, 5.8661f)
          curveTo(17.7536f, 5.7358f, 17.5561f, 5.6286f, 17.0946f, 5.5665f)
          curveTo(16.6116f, 5.5016f, 15.964f, 5.5f, 15.0f, 5.5f)
          lineTo(9.0f, 5.5f)
          curveTo(8.036f, 5.5f, 7.3884f, 5.5016f, 6.9054f, 5.5665f)
          curveTo(6.4439f, 5.6286f, 6.2464f, 5.7358f, 6.1161f, 5.8661f)
          curveTo(5.9858f, 5.9964f, 5.8786f, 6.1939f, 5.8165f, 6.6554f)
          curveTo(5.7516f, 7.1384f, 5.75f, 7.786f, 5.75f, 8.75f)
          lineTo(5.75f, 17.0f)
          curveTo(5.75f, 17.964f, 5.7516f, 18.6116f, 5.8165f, 19.0946f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF121212)),
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(10.1256f, 13.9359f)
          curveTo(10.1345f, 13.9269f, 10.1436f, 13.9179f, 10.1526f, 13.9089f)
          lineTo(11.7332f, 12.3283f)
          curveTo(11.7422f, 12.3193f, 11.7512f, 12.3102f, 11.7602f, 12.3013f)
          curveTo(12.0233f, 12.0381f, 12.2647f, 11.7967f, 12.4864f, 11.6213f)
          curveTo(12.7283f, 11.4299f, 13.0196f, 11.2562f, 13.3923f, 11.2159f)
          curveTo(13.5175f, 11.2023f, 13.6438f, 11.2023f, 13.769f, 11.2159f)
          curveTo(14.1417f, 11.2562f, 14.433f, 11.4299f, 14.6749f, 11.6213f)
          curveTo(14.8966f, 11.7967f, 15.138f, 12.0381f, 15.4011f, 12.3013f)
          curveTo(15.4101f, 12.3102f, 15.4191f, 12.3193f, 15.4281f, 12.3283f)
          curveTo(15.4372f, 12.3373f, 15.4462f, 12.3464f, 15.4552f, 12.3553f)
          curveTo(15.7183f, 12.6184f, 15.9597f, 12.8598f, 16.1351f, 13.0815f)
          curveTo(16.3266f, 13.3235f, 16.5002f, 13.6147f, 16.5406f, 13.9875f)
          curveTo(16.5541f, 14.1126f, 16.5541f, 14.2389f, 16.5406f, 14.3641f)
          curveTo(16.5002f, 14.7368f, 16.3266f, 15.0281f, 16.1351f, 15.2701f)
          curveTo(15.9597f, 15.4917f, 15.7183f, 15.7331f, 15.4552f, 15.9962f)
          curveTo(15.4462f, 16.0052f, 15.4372f, 16.0142f, 15.4281f, 16.0232f)
          lineTo(13.8475f, 17.6038f)
          curveTo(13.8385f, 17.6129f, 13.8295f, 17.6219f, 13.8205f, 17.6309f)
          curveTo(13.5574f, 17.894f, 13.316f, 18.1354f, 13.0944f, 18.3108f)
          curveTo(12.8524f, 18.5023f, 12.5611f, 18.6759f, 12.1884f, 18.7163f)
          curveTo(12.0632f, 18.7298f, 11.9369f, 18.7298f, 11.8118f, 18.7163f)
          curveTo(11.439f, 18.6759f, 11.1478f, 18.5023f, 10.9058f, 18.3108f)
          curveTo(10.6841f, 18.1354f, 10.4427f, 17.894f, 10.1796f, 17.6309f)
          curveTo(10.1706f, 17.6219f, 10.1616f, 17.6129f, 10.1526f, 17.6038f)
          curveTo(10.1436f, 17.5948f, 10.1345f, 17.5858f, 10.1256f, 17.5768f)
          curveTo(9.8624f, 17.3137f, 9.621f, 17.0723f, 9.4456f, 16.8506f)
          curveTo(9.2542f, 16.6087f, 9.0805f, 16.3174f, 9.0402f, 15.9447f)
          curveTo(9.0266f, 15.8195f, 9.0266f, 15.6932f, 9.0402f, 15.568f)
          curveTo(9.0805f, 15.1953f, 9.2542f, 14.904f, 9.4456f, 14.6621f)
          curveTo(9.621f, 14.4404f, 9.8624f, 14.199f, 10.1256f, 13.9359f)
          close()
          moveTo(10.5299f, 15.7365f)
          curveTo(10.5297f, 15.7366f, 10.53f, 15.7351f, 10.5311f, 15.7318f)
          curveTo(10.5307f, 15.7348f, 10.5301f, 15.7364f, 10.5299f, 15.7365f)
          close()
          moveTo(10.5312f, 15.7315f)
          curveTo(10.536f, 15.7182f, 10.5556f, 15.6767f, 10.622f, 15.5927f)
          curveTo(10.7365f, 15.448f, 10.9141f, 15.2687f, 11.2133f, 14.9695f)
          lineTo(12.7938f, 13.389f)
          curveTo(13.093f, 13.0898f, 13.2723f, 12.9122f, 13.417f, 12.7977f)
          curveTo(13.501f, 12.7313f, 13.5425f, 12.7117f, 13.5558f, 12.7069f)
          curveTo(13.5723f, 12.7053f, 13.589f, 12.7053f, 13.6055f, 12.7069f)
          curveTo(13.6188f, 12.7117f, 13.6603f, 12.7313f, 13.7442f, 12.7977f)
          curveTo(13.889f, 12.9122f, 14.0683f, 13.0898f, 14.3675f, 13.389f)
          curveTo(14.6667f, 13.6882f, 14.8442f, 13.8674f, 14.9588f, 14.0122f)
          curveTo(15.0251f, 14.0961f, 15.0447f, 14.1376f, 15.0495f, 14.151f)
          curveTo(15.0511f, 14.1675f, 15.0511f, 14.1841f, 15.0495f, 14.2006f)
          curveTo(15.0447f, 14.2139f, 15.0251f, 14.2555f, 14.9588f, 14.3394f)
          curveTo(14.8442f, 14.4841f, 14.6667f, 14.6634f, 14.3675f, 14.9626f)
          lineTo(12.7869f, 16.5432f)
          curveTo(12.4877f, 16.8424f, 12.3084f, 17.0199f, 12.1637f, 17.1345f)
          curveTo(12.0798f, 17.2008f, 12.0382f, 17.2204f, 12.0249f, 17.2252f)
          curveTo(12.0084f, 17.2268f, 11.9918f, 17.2268f, 11.9753f, 17.2252f)
          curveTo(11.9619f, 17.2204f, 11.9204f, 17.2008f, 11.8365f, 17.1345f)
          curveTo(11.6917f, 17.0199f, 11.5125f, 16.8424f, 11.2133f, 16.5432f)
          curveTo(10.9141f, 16.244f, 10.7365f, 16.0647f, 10.622f, 15.92f)
          curveTo(10.5556f, 15.836f, 10.536f, 15.7945f, 10.5312f, 15.7812f)
          curveTo(10.5296f, 15.7647f, 10.5296f, 15.748f, 10.5312f, 15.7315f)
          close()
          moveTo(10.5299f, 15.7762f)
          curveTo(10.5301f, 15.7763f, 10.5307f, 15.7779f, 10.5311f, 15.7809f)
          curveTo(10.53f, 15.7776f, 10.5297f, 15.7761f, 10.5299f, 15.7762f)
          close()
          moveTo(11.9802f, 17.2265f)
          curveTo(11.9803f, 17.2267f, 11.9788f, 17.2264f, 11.9755f, 17.2253f)
          curveTo(11.9785f, 17.2258f, 11.9801f, 17.2263f, 11.9802f, 17.2265f)
          close()
          moveTo(12.0199f, 17.2265f)
          curveTo(12.02f, 17.2263f, 12.0216f, 17.2258f, 12.0246f, 17.2253f)
          curveTo(12.0213f, 17.2264f, 12.0198f, 17.2267f, 12.0199f, 17.2265f)
          close()
          moveTo(15.0508f, 14.1956f)
          curveTo(15.051f, 14.1955f, 15.0507f, 14.197f, 15.0496f, 14.2003f)
          curveTo(15.0501f, 14.1973f, 15.0506f, 14.1957f, 15.0508f, 14.1956f)
          close()
          moveTo(15.0508f, 14.1559f)
          curveTo(15.0506f, 14.1558f, 15.0501f, 14.1542f, 15.0496f, 14.1512f)
          curveTo(15.0507f, 14.1545f, 15.051f, 14.156f, 15.0508f, 14.1559f)
          close()
          moveTo(13.6005f, 12.7056f)
          curveTo(13.6004f, 12.7054f, 13.6019f, 12.7057f, 13.6052f, 12.7068f)
          curveTo(13.6022f, 12.7064f, 13.6006f, 12.7058f, 13.6005f, 12.7056f)
          close()
          moveTo(13.5608f, 12.7056f)
          curveTo(13.5607f, 12.7058f, 13.5591f, 12.7064f, 13.5561f, 12.7068f)
          curveTo(13.5594f, 12.7057f, 13.5609f, 12.7054f, 13.5608f, 12.7056f)
          close()
        }
      }.build()
    return _eurobonus!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _eurobonus: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Eurobonus,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
