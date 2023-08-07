package com.hedvig.android.core.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.g_700_t
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Hedvig
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsuranceCard(
  chips: ImmutableList<String>,
  topText: String,
  bottomText: String,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  fallbackPainter: Painter = ColorPainter(Color.Black.copy(alpha = 0.7f)),
  backgroundImageUrl: String? = null,
) {
  Box(modifier.clip(MaterialTheme.shapes.squircleMedium)) {
    AsyncImage(
      model = backgroundImageUrl,
      contentDescription = null,
      placeholder = fallbackPainter,
      error = fallbackPainter,
      fallback = fallbackPainter,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize(),
    )
    HedvigTheme(darkTheme = true) {
      Column(Modifier.padding(16.dp)) {
        Row(Modifier.heightIn(86.dp)) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f),
          ) {
            for (chipText in chips) {
              Chip(chipText, Modifier.padding(bottom = 8.dp))
            }
          }
          Spacer(Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Hedvig.Hedvig,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp).padding(top = 2.dp),
          )
        }
        Spacer(Modifier.height(8.dp))
        Text(topText)
        Spacer(Modifier.height(4.dp))
        Text(
          text = bottomText,
          color = MaterialTheme.colorScheme.g_700_t,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

@Composable
private fun Chip(
  text: String,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier.clip(shape = MaterialTheme.shapes.squircleExtraSmall),
  ) {
    Box(
      Modifier
        .alpha(DisabledAlpha)
        .background(MaterialTheme.colorScheme.background)
        .matchParentSize(),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Preview
@Composable
private fun PreviewInsuranceCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceCard(
        chips = persistentListOf("Activates 20.03.2024", "Terminates 20.03.2025", "Something in 20.03.2026"),
        topText = "Home Insurance",
        bottomText = "Bellmansgatan 19A ∙ You +1",
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}
