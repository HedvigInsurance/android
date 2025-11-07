package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import hedvig.resources.R

@Composable
internal fun PuppyArticleDestination(
  viewModel: PuppyArticleViewModel,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PuppyArticleScreen(
    uiState,
    navigateUp = navigateUp,
    onReload = {
      viewModel.emit(PuppyArticleEvent.Reload)
    },
    imageLoader = imageLoader,
    onRatingClick = {
      viewModel.emit(PuppyArticleEvent.RatingClick(it))
    },
  )
}

@Composable
private fun PuppyArticleScreen(
  uiState: PuppyArticleUiState,
  navigateUp: () -> Unit,
  onReload: () -> Unit,
  onRatingClick: (Int) -> Unit,
  imageLoader: ImageLoader,
) {
  when (uiState) {
    PuppyArticleUiState.Failure -> HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      HedvigErrorSection(
        onButtonClick = onReload,
        modifier = Modifier.weight(1f),
      )
    }

    PuppyArticleUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    is PuppyArticleUiState.Success -> PuppyArticleSuccessScreen(
      uiState,
      navigateUp = navigateUp,
      imageLoader = imageLoader,
      onRatingClick = onRatingClick,
    )
  }
}

@Composable
private fun PuppyArticleSuccessScreen(
  uiState: PuppyArticleUiState.Success,
  navigateUp: () -> Unit,
  onRatingClick: (Int) -> Unit,
  imageLoader: ImageLoader,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
  ) {
    Column(
      Modifier
        .fillMaxSize(),
    ) {
      TopAppBarWithBack(
        title = "",
        onClick = navigateUp,
      )
      Column(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth(),
        ) {
          val fallbackPainter: Painter = ColorPainter(Color.Black.copy(alpha = 0.7f))
          AsyncImage(
            model = uiState.story.image,
            contentDescription = EmptyContentDescription, // todo
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .height(200.dp)
              .clip(HedvigTheme.shapes.cornerMedium),
          )
        }
        Spacer(Modifier.height(16.dp))
        HedvigText(
          uiState.story.title,
          style = HedvigTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(4.dp))
        HedvigText(
          uiState.story.subtitle,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
        Spacer(Modifier.height(24.dp))
        ProvideTextStyle(
          HedvigTheme.typography.bodySmall
            .copy(color = HedvigTheme.colorScheme.textSecondaryTranslucent),
        ) {
          val headingColor = HedvigTheme.colorScheme.textPrimary
          RichText(
            style = RichTextStyle(
              headingStyle = { _, currentStyle ->
                currentStyle.copy(
                  color = headingColor,
                )
              },
              stringStyle = RichTextStringStyle(
                boldStyle = SpanStyle(
                  color = headingColor,
                ),
              ),
            ),
          ) {
            Markdown(
              content = uiState.story.content,
            )
          }
        }
        Spacer(Modifier.height(48.dp))
        HedvigText(stringResource(R.string.PUPPY_GUIDE_RATING_QUESTION))
        RatingSection(
          onRatingClick = onRatingClick,
        )
      }
    }
  }
}

@Composable
private fun RatingSection(
  onRatingClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {

  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PuppyArticleScreenPreview(
  @PreviewParameter(PuppyArticleUiStatePreviewProvider::class) uiState: PuppyArticleUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PuppyArticleScreen(
        uiState,
        navigateUp = {},
        onReload = {},
        onRatingClick = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}


private class PuppyArticleUiStatePreviewProvider :
  CollectionPreviewParameterProvider<PuppyArticleUiState>(
    listOf(
      PuppyArticleUiState.Success(
        story = PuppyGuideStory(
          categories = listOf("Food"),
          content = "some long long long long long long long long long long long long" +
            " long long long long long long long long long long long long content",
          image = "",
          name = "",
          rating = 5,
          isRead = false,
          subtitle = "5 min read",
          title = "Puppy food",
        ),
      ),
      PuppyArticleUiState.Loading,
      PuppyArticleUiState.Failure,
    ),
  )
