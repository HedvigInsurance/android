package com.hedvig.android.feature.product.inapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.product.inapp.data.CrossSellProductInfo
import hedvig.resources.Res
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

internal data class CrossSellProductHeader(
  val title: String,
  val subtitle: String,
  val pillowImageSrc: String,
  val discountText: String?,
)

@Composable
internal fun CrossSellProductDestination(
  viewModel: CrossSellProductViewModel,
  header: CrossSellProductHeader,
  imageLoader: ImageLoader,
  close: () -> Unit,
  openStoreUrl: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CrossSellProductScreen(
    uiState = uiState,
    header = header,
    imageLoader = imageLoader,
    close = close,
    retry = { viewModel.emit(CrossSellProductEvent.Retry) },
    openStoreUrl = openStoreUrl,
  )
}

@Composable
private fun CrossSellProductScreen(
  uiState: CrossSellProductUiState,
  header: CrossSellProductHeader,
  imageLoader: ImageLoader,
  close: () -> Unit,
  retry: () -> Unit,
  openStoreUrl: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
      ProductHeader(header = header, imageLoader = imageLoader, close = close)
      when (uiState) {
        CrossSellProductUiState.Loading -> {
          HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f).fillMaxWidth())
        }

        CrossSellProductUiState.Failure -> {
          HedvigErrorSection(onButtonClick = retry, modifier = Modifier.weight(1f))
        }

        is CrossSellProductUiState.Content -> {
          ProductContent(
            productInfo = uiState.productInfo,
            discountText = header.discountText,
            imageLoader = imageLoader,
            modifier = Modifier.weight(1f),
          )
        }
      }
      // TODO: Add "Discover your offer" / "Upptäck ditt erbjudande" to Lokalise
      HedvigButton(
        text = "Discover your offer",
        onClick = openStoreUrl,
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      )
    }
  }
}

@Composable
private fun ProductHeader(header: CrossSellProductHeader, imageLoader: ImageLoader, close: () -> Unit) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.Top,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerXLarge)
    AsyncImage(
      model = header.pillowImageSrc,
      contentDescription = EmptyContentDescription,
      placeholder = placeholder,
      error = placeholder,
      fallback = placeholder,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = Modifier.size(48.dp),
    )
    Column(Modifier.weight(1f)) {
      HedvigText(text = header.title, modifier = Modifier.semantics { heading() })
      HedvigText(
        text = header.subtitle,
        color = HedvigTheme.colorScheme.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    IconButton(onClick = close, modifier = Modifier.size(24.dp)) {
      Icon(
        imageVector = HedvigIcons.Close,
        contentDescription = stringResource(Res.string.general_close_button),
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@Composable
private fun ProductContent(
  productInfo: CrossSellProductInfo,
  discountText: String?,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(8.dp))
    ProductHero(image = productInfo.heroImage, discountText = discountText, imageLoader = imageLoader)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
      productInfo.heroIntro?.let { intro ->
        TitleAndDescription(title = intro.title, description = intro.description)
      }
      for (usp in productInfo.usps) {
        TitleAndDescription(title = usp.title, description = usp.description)
      }
      if (productInfo.carousel.isNotEmpty()) {
        CrossSellStoryCarousel(
          items = productInfo.carousel,
          imageLoader = imageLoader,
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(STORY_CARD_ASPECT_RATIO),
        )
      }
    }
    Spacer(Modifier.height(8.dp))
  }
}

@Composable
private fun ProductHero(image: ImageAsset, discountText: String?, imageLoader: ImageLoader) {
  val shape = HedvigTheme.shapes.cornerXLarge
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(HERO_ASPECT_RATIO)
      .clip(shape),
  ) {
    val placeholder = crossSellPainterFallback(shape = shape)
    AsyncImage(
      model = image.src,
      contentDescription = image.description ?: EmptyContentDescription,
      placeholder = placeholder,
      error = placeholder,
      fallback = placeholder,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize(),
    )
    Box(
      Modifier
        .matchParentSize()
        .background(HedvigTheme.colorScheme.surfacePrimaryTransparent),
    )
    if (discountText != null) {
      HighlightLabel(
        labelText = discountText,
        size = HighLightSize.Small,
        color = HighlightColor.Green(HighlightShade.LIGHT),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp),
      )
    }
  }
}

@Composable
private fun TitleAndDescription(title: String, description: String) {
  Column(Modifier.fillMaxWidth()) {
    HedvigText(title)
    HedvigText(description, color = HedvigTheme.colorScheme.textSecondary)
  }
}

private const val HERO_ASPECT_RATIO = 358f / 423f
private const val STORY_CARD_ASPECT_RATIO = 358f / 498f

@HedvigPreview
@Composable
private fun PreviewCrossSellProductScreen(
  @PreviewParameter(CrossSellProductUiStateProvider::class) uiState: CrossSellProductUiState,
) {
  HedvigTheme {
    CrossSellProductScreen(
      uiState = uiState,
      header = CrossSellProductHeader(
        title = "Accident Insurance",
        subtitle = "Help when you need it the most",
        pillowImageSrc = "",
        discountText = "-20%",
      ),
      imageLoader = rememberPreviewImageLoader(),
      close = {},
      retry = {},
      openStoreUrl = {},
    )
  }
}

private class CrossSellProductUiStateProvider : PreviewParameterProvider<CrossSellProductUiState> {
  override val values = sequenceOf(
    CrossSellProductUiState.Content(
      CrossSellProductInfo(
        heroImage = ImageAsset(id = "hero", src = "", description = null),
        heroIntro = CrossSellProductInfo.HeroIntro(
          title = "Compensation",
          description = "Up to 1 000 000 kr for permanent injuries and loss of work ability.",
        ),
        usps = listOf(
          CrossSellProductInfo.Usp(
            title = "Wide protection",
            description = "The insurance is valid worldwide. It also covers many extreme sports.",
          ),
          CrossSellProductInfo.Usp(title = "No lock-in period", description = "Cancel whenever you want."),
        ),
        carousel = listOf(
          CrossSellProductInfo.CarouselItem(
            title = "Help directly in the app",
            subtitle = "Our service team is only a click away",
            image = ImageAsset(id = "slide", src = "", description = null),
            videoUrl = null,
            loop = false,
          ),
          CrossSellProductInfo.CarouselItem(
            title = "Second slide",
            subtitle = null,
            image = ImageAsset(id = "slide2", src = "", description = null),
            videoUrl = null,
            loop = false,
          ),
        ),
      ),
    ),
    CrossSellProductUiState.Loading,
    CrossSellProductUiState.Failure,
  )
}
