package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_black12percent
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.util.compose.rememberBlurHashPainter
import slimber.log.d

@Composable
fun CrossSellCard(
  data: CrossSellData,
  imageLoader: ImageLoader,
  onCardClick: () -> Unit,
  onCtaClick: () -> Unit,
) {
  HedvigTheme(darkTheme = true) {
    HedvigCard(
      onClick = onCardClick,
      border = BorderStroke(1.dp, hedvig_black12percent),
      colors = CardDefaults.outlinedCardColors(
        containerColor = Color(0x00000000),
      ),
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .fillMaxWidth()
        .requiredHeight(200.dp),
    ) {
      LaunchedEffect(data.backgroundUrl) {
        d { "Stelios data.backgroundUrl${data.backgroundUrl}" }
      }
      Box(
        propagateMinConstraints = true,
        modifier = Modifier.fillMaxSize(),
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
        )
        Column(
          verticalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
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
              style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = data.description,
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          Button(
            onClick = onCtaClick,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(stringResource(hedvig.resources.R.string.cross_selling_card_se_accident_cta))
          }
        }
      }
    }
  }
}

private val previewData = CrossSellData(
  id = "123",
  title = "Accident Insurance",
  description = "179 kr/mo.",
  backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
  backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
  about = "If you or a family member is injured in an accident insurance, Hedvig is able to compensate" +
    " you for a hospital stay, rehabilitation, therapy and dental injuries. \n\n" +
    "In case of a permanent injury that affect your your quality of life and ability to work, an " +
    "accident insurance can complement the support from the social welfare system and your employer.",
  storeUrl = "",
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

@HedvigPreview
@Composable
private fun PreviewCrossSellCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      CrossSellCard(
        data = previewData,
        imageLoader = rememberPreviewImageLoader(),
        onCardClick = {},
        onCtaClick = {},
      )
    }
  }
}
