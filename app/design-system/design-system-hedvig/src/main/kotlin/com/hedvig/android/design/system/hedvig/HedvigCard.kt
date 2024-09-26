package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ChipType.GENERAL
import com.hedvig.android.design.system.hedvig.ChipType.TIER
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Frosted
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.DARK
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.placeholder
import com.hedvig.android.placeholder.shimmer

@Composable
fun HedvigCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
  Surface(
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .then(
        if (onClick != null) {
          Modifier.clickable(onClick = onClick)
        } else {
          Modifier
        },
      ),
  ) {
    content()
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsuranceCard(
  chips: List<ChipUiData>,
  topText: String,
  bottomText: String,
  imageLoader: ImageLoader,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  fallbackPainter: Painter = ColorPainter(Color.Black.copy(alpha = 0.7f)),
  backgroundImageUrl: String? = null,
) {
  Box(modifier.clip(HedvigTheme.shapes.cornerXLarge)) {
    if (isLoading) {
      Image(
        painter = ColorPainter(Color.Black.copy(alpha = 0.3f)),
        modifier = Modifier
          .matchParentSize()
          .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer()),
        contentDescription = null,
      )
    } else {
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
    }
    HedvigTheme {
      Column(Modifier.padding(16.dp)) {
        Row(Modifier.heightIn(86.dp)) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.weight(1f),
          ) {
            if (!isLoading) {
              for (chip in chips) {
                Chip(chip, Modifier.padding(bottom = 8.dp))
              }
            }
          }
          Spacer(Modifier.width(8.dp))
          Icon(
            imageVector = HedvigIcons.HelipadOutline,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.fillWhite,
            modifier = Modifier
              .size(24.dp),
          )
        }
        Spacer(Modifier.height(8.dp))
        HedvigText(
          topText,
          color = HedvigTheme.colorScheme.textWhite,
          modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer()),
        )
        Spacer(Modifier.height(4.dp))
        HedvigTheme(darkTheme = true) {
          HedvigText(
            text = bottomText,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.shimmer()),
          )
        }
      }
    }
  }
}

@Composable
private fun Chip(chip: ChipUiData, modifier: Modifier = Modifier) {
  HedvigTheme(darkTheme = false) {
    HighlightLabel(
      modifier = modifier,
      size = Small,
      labelText = chip.chipText,
      color = when (chip.chipType) {
        GENERAL -> Frosted(MEDIUM)
        TIER -> Frosted(DARK)
      },
    )
  }
}

@Composable
fun InsuranceCardPlaceholder(imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  InsuranceCard(
    chips = listOf(),
    topText = "",
    bottomText = "",
    imageLoader = imageLoader,
    isLoading = true,
    modifier = modifier,
  )
}

data class ChipUiData(
  val chipText: String,
  val chipType: ChipType,
)

enum class ChipType {
  GENERAL,
  TIER,
}

@HedvigPreview
@Composable
private fun PreviewInsuranceCard(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isLoading: Boolean,
) {
  HedvigTheme {
    Surface {
      InsuranceCard(
        chips = listOf(
          ChipUiData("Bas", TIER),
          ChipUiData("Activates 20.03.2024", GENERAL),
        ),
        topText = "Home Insurance",
        bottomText = "Bellmansgatan 19A ∙ You +1",
        imageLoader = rememberPreviewImageLoader(),
        isLoading = isLoading,
      )
    }
  }
}
