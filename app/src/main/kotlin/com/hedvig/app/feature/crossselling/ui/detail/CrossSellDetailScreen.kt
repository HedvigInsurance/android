package com.hedvig.app.feature.crossselling.ui.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.list.SectionTitle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.scaffold.Scaffold
import com.hedvig.app.R
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.ui.compose.composables.ErrorDialog
import com.hedvig.app.ui.compose.composables.appbar.FadingTopAppBar
import com.hedvig.app.util.compose.rememberBlurHashPainter

@Composable
fun CrossSellDetailScreen(
  onCtaClick: () -> Unit,
  onUpClick: () -> Unit,
  onCoverageClick: () -> Unit,
  onFaqClick: () -> Unit,
  onDismissError: () -> Unit,
  data: CrossSellData,
  errorMessage: String?,
  imageLoader: ImageLoader,
) {
  val scrollState = rememberScrollState()
  val localDensity = LocalDensity.current
  val imageHeight = 260.dp
  val topAppBarBackgroundColorAlpha by remember {
    derivedStateOf {
      val scrollFromTopInDp = with(localDensity) { scrollState.value.toDp() }
      val percentageOfImageScrolledPast = scrollFromTopInDp.coerceAtMost(imageHeight) / imageHeight
      percentageOfImageScrolledPast
    }
  }
  Scaffold(
    bottomBar = {
      LargeContainedButton(
        onClick = onCtaClick,
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
          .padding(16.dp),
      ) {
        Text(text = stringResource(id = hedvig.resources.R.string.cross_selling_card_se_accident_cta))
      }
    },
  ) { paddingValues ->
    Box(
      Modifier
        .fillMaxSize()
        // Since we've applied the insets on the bottomBar itself and that stays at the bottom of the screen, and the
        // height that it takes is passed down inside [paddingValues], we need to inform children that the bottom
        // insets are already consumed.
        .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
      ScrollableContent(
        crossSellData = data,
        onCoverageClick = onCoverageClick,
        onFaqClick = onFaqClick,
        imageHeight = imageHeight,
        imageLoader = imageLoader,
        contentPadding = paddingValues,
        scrollState = scrollState,
      )
      FadingTopAppBar(
        backgroundAlpha = topAppBarBackgroundColorAlpha,
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          .asPaddingValues(),
        navigationIcon = {
          IconButton(onClick = onUpClick) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
          }
        },
      )
    }
  }

  if (errorMessage != null) {
    ErrorDialog(message = errorMessage, onDismiss = onDismissError)
  }
}

@Composable
private fun ScrollableContent(
  crossSellData: CrossSellData,
  onCoverageClick: () -> Unit,
  onFaqClick: () -> Unit,
  imageHeight: Dp,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
  scrollState: ScrollState = rememberScrollState(),
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
      .padding(contentPadding),
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(crossSellData.backgroundUrl)
        .crossfade(true)
        .build(),
      contentDescription = null,
      imageLoader = imageLoader,
      placeholder = rememberBlurHashPainter(crossSellData.backgroundBlurHash, 64, 32),
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .height(imageHeight)
        .fillMaxWidth(),
    )
    Column(Modifier.padding(horizontal = 16.dp)) {
      Spacer(Modifier.height(24.dp))
      Text(
        text = crossSellData.title,
        style = MaterialTheme.typography.h5,
      )
      Spacer(Modifier.height(16.dp))
      crossSellData.highlights.forEach { highlight ->
        Highlight(
          title = highlight.title,
          description = highlight.description,
        )
        Spacer(Modifier.height(24.dp))
      }
      SectionTitle(
        text = stringResource(hedvig.resources.R.string.cross_sell_info_about_title),
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = crossSellData.about,
        style = MaterialTheme.typography.body2,
      )
      Spacer(Modifier.height(24.dp))
      SectionTitle(
        text = stringResource(hedvig.resources.R.string.cross_sell_info_learn_more_title),
      )
    }
    ClickableListItem(
      onClick = onCoverageClick,
      icon = R.drawable.ic_insurance,
      text = stringResource(hedvig.resources.R.string.cross_sell_info_full_coverage_row),
    )
    ClickableListItem(
      onClick = onFaqClick,
      icon = R.drawable.ic_info_toolbar,
      text = stringResource(hedvig.resources.R.string.cross_sell_info_common_questions_row),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellDetailScreen() {
  HedvigTheme {
    Surface {
      CrossSellDetailScreen(
        onCtaClick = {},
        onUpClick = {},
        onCoverageClick = {},
        onFaqClick = {},
        onDismissError = {},
        data = CrossSellData(
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
        ),
        errorMessage = null,
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}
