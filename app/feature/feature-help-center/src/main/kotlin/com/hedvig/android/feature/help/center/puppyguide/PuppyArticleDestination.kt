package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
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
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.logger.logcat
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
              .fillMaxWidth()
              .defaultMinSize(minHeight = 200.dp)
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
        Spacer(Modifier.height(16.dp))
        logcat { "Mariia: uiState.story.rating ${uiState.story.rating}" }
        RatingSection(
          onRatingClick = onRatingClick,
          selectedRating = uiState.story.rating,
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun RatingSection(selectedRating: Int?, onRatingClick: (Int) -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val ratings = listOf(1, 2, 3, 4, 5)
    Row(
      horizontalArrangement = Arrangement.SpaceAround,
      modifier = Modifier,
    ) {
      ratings.forEach { rating ->
        val isSelectedRating = selectedRating == rating
        logcat { "Mariia: isSelectedRating $isSelectedRating" }
        HedvigCard(
          modifier = Modifier.weight(1f),
          onClick = {
            onRatingClick(rating)
          },
          color = if (isSelectedRating) {
            HedvigTheme.colorScheme.signalGreenFill
          } else {
            HedvigTheme.colorScheme.surfacePrimary
          },
        ) {
          HedvigText(
            text = rating.toString(),
            style = HedvigTheme.typography.bodyLarge,
            color = if (isSelectedRating) {
              HedvigTheme.colorScheme.textBlack
            } else {
              HedvigTheme.colorScheme.textSecondaryTranslucent
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
          )
        }
        Spacer(Modifier.width(6.dp))
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(
          stringResource(R.string.PUPPY_GUIDE_RATING_NOT_HELPFUL),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          HedvigText(
            stringResource(R.string.PUPPY_GUIDE_RATING_VERY_HELPFUL),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
          )
        }
      },
      spaceBetween = 8.dp,
    )
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
