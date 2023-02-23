package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_black
import com.hedvig.android.core.designsystem.theme.hedvig_black12percent
import com.hedvig.android.core.designsystem.theme.hedvig_off_white
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.util.compose.rememberBlurHashPainter

/*
 * Note: This Composable uses hardcoded colors due to difficulties with
 * declaring a particular component to be in dark theme instead of the
 * default. When we update `HedvigTheme` to be Compose-first instead of
 * XML-Theme first, we can reconfigure the theme for this composable to
 * be `dark` no matter what the system value is.
 */
@Composable
fun CrossSell(
  data: CrossSellData,
  imageLoader: ImageLoader,
  onCardClick: () -> Unit,
  onCtaClick: (label: String) -> Unit,
) {
  Card(
    border = BorderStroke(1.dp, hedvig_black12percent),
    modifier = Modifier
      .padding(
        horizontal = 16.dp,
        vertical = 8.dp,
      )
      .height(200.dp)
      .clickable(
        onClick = onCardClick,
      ),
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(data.backgroundUrl)
        .crossfade(true)
        .build(),
      contentDescription = null,
      imageLoader = imageLoader,
      placeholder = rememberBlurHashPainter(data.backgroundBlurHash, 64, 32),
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x7F000000),
              Color(0x00000000),
            ),
          ),
        )
        .padding(16.dp),
    ) {
      Column {
        Text(
          text = data.title,
          style = MaterialTheme.typography.subtitle1,
          color = hedvig_off_white,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = data.description,
          style = MaterialTheme.typography.subtitle2,
          color = hedvig_off_white,
        )
      }
      CompositionLocalProvider(
        LocalRippleTheme provides DarkRippleTheme,
      ) {
        Button(
          onClick = { onCtaClick(data.callToAction) },
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.buttonColors(
            backgroundColor = hedvig_off_white,
            contentColor = hedvig_black,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = data.callToAction,
          )
        }
      }
    }
  }
}

private object DarkRippleTheme : RippleTheme {
  // Color sourced from
  // https://cs.android.com/android/platform/superproject/+/master:prebuilts/sdk/current/support/v7/appcompat/res/values/values.xml;l=59
  @Composable
  override fun defaultColor() = Color(0x1f000000)

  @Composable
  override fun rippleAlpha() = RippleTheme.defaultRippleAlpha(
    contentColor = LocalContentColor.current,
    lightTheme = false,
  )
}

private val previewData = CrossSellData(
  title = "Accident Insurance",
  description = "179 kr/mo.",
  callToAction = "Calculate price",
  action = CrossSellData.Action.Chat,
  backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
  backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
  crossSellType = "ACCIDENT",
  typeOfContract = "SE_ACCIDENT",
  about = "If you or a family member is injured in an accident insurance, Hedvig is able to compensate" +
    " you for a hospital stay, rehabilitation, therapy and dental injuries. \n\n" +
    "In case of a permanent injury that affect your your quality of life and ability to work, an " +
    "accident insurance can complement the support from the social welfare system and your employer.",
  perils = emptyList(),
  terms = emptyList(),
  highlights = listOf(
    CrossSellData.Highlight(
      title = "Covers dental injuries",
      description = "Up to 100 000 SEK per damage.",
    ),
    CrossSellData.Highlight(
      title = "Compensates permanent injuries",
      description = "A fixed amount up to 2 000 000 SEK is payed out in " +
        "the event of a permanent injury.",
    ),
    CrossSellData.Highlight(
      title = "Rehabilitation and therapy is covered",
      description = "After accidents and sudden events, such as the death of a close family member.",
    ),
  ),
  faq = emptyList(),
  insurableLimits = emptyList(),
)

@Preview(
  name = "Cross-Sell Card",
  group = "Insurance Tab",
  showBackground = true,
)
@Composable
fun CrossSellPreview() {
  HedvigTheme {
    CrossSell(
      data = previewData,
      imageLoader = rememberPreviewImageLoader(),
      onCardClick = {},
      onCtaClick = {},
    )
  }
}
