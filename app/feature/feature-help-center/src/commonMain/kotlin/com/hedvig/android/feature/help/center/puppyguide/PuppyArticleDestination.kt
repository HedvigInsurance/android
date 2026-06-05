package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.feature.help.center.ui.MarkdownText
import com.hedvig.android.feature.help.center.ui.rememberPerformRatingHaptic
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.PUPPY_GUIDE_RATING_NOT_HELPFUL
import hedvig.resources.PUPPY_GUIDE_RATING_QUESTION
import hedvig.resources.PUPPY_GUIDE_RATING_VERY_HELPFUL
import hedvig.resources.Res
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PuppyArticleDestination(
  viewModel: PuppyArticleViewModel,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onScrollOffsetChanged: (Float) -> Unit = {},
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
    onScrollOffsetChanged = onScrollOffsetChanged,
    onReachedBottom = {
      viewModel.emit(PuppyArticleEvent.ReachedBottom)
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
  onScrollOffsetChanged: (Float) -> Unit,
  onReachedBottom: () -> Unit,
) {
  when (uiState) {
    PuppyArticleUiState.Failure -> PuppyScaffold(navigateUp = navigateUp) {
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
      onScrollOffsetChanged = onScrollOffsetChanged,
      onReachedBottom = onReachedBottom,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PuppyArticleSuccessScreen(
  uiState: PuppyArticleUiState.Success,
  navigateUp: () -> Unit,
  onRatingClick: (Int) -> Unit,
  imageLoader: ImageLoader,
  onScrollOffsetChanged: (Float) -> Unit,
  onReachedBottom: () -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    Column(Modifier.fillMaxSize()) {
      val consumedWindowInsets = remember { MutableWindowInsets() }
      PuppyTopAppBar(
        onBack = navigateUp,
        modifier = Modifier.onSizeChanged {
          consumedWindowInsets.insets = WindowInsets(top = it.height)
        },
      )
      val horizontalInsetsPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal)
        .asPaddingValues()
      val scrollState = rememberScrollState()
      val density = LocalDensity.current
      LaunchedEffect(scrollState, density, onScrollOffsetChanged) {
        snapshotFlow { with(density) { scrollState.value.toDp().value } }.collect(onScrollOffsetChanged)
      }
      val currentOnReachedBottom by rememberUpdatedState(onReachedBottom)
      LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.maxValue }
          .filter { it != Int.MAX_VALUE }
          .debounce(150.milliseconds)
          .first()
        snapshotFlow { scrollState.value >= scrollState.maxValue }.first { it }
        currentOnReachedBottom()
      }
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontalInsetsPadding)
          .padding(horizontal = 16.dp),
      ) {
        Spacer(
          modifier = Modifier.windowInsetsTopHeight(
            WindowInsets.safeDrawing.exclude(consumedWindowInsets).only(WindowInsetsSides.Top),
          ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth(),
        ) {
          val fallbackPainter: Painter = ColorPainter(Color.Black.copy(alpha = 0.7f))
          val painter = rememberAsyncImagePainter(
            model = uiState.story.image,
            imageLoader = imageLoader,
            error = fallbackPainter,
            fallback = fallbackPainter,
            contentScale = ContentScale.Crop,
          )
          val state by painter.state.collectAsState()
          Image(
            painter = painter,
            contentDescription = EmptyContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 200.dp)
              .clip(HedvigTheme.shapes.cornerMedium)
              .hedvigPlaceholder(
                visible = state is AsyncImagePainter.State.Empty || state is AsyncImagePainter.State.Loading,
                shape = HedvigTheme.shapes.cornerMedium,
                highlight = PlaceholderHighlight.shimmer(),
              ),
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
          MarkdownText(uiState.story.content.replace(Regex("\n\\s*\n"), "\n\n\u200b\n\n"), withArticleStyle = true)
        }
        Spacer(Modifier.height(48.dp))
        HedvigText(stringResource(Res.string.PUPPY_GUIDE_RATING_QUESTION))
        Spacer(Modifier.height(16.dp))
        RatingSection(
          onRatingClick = onRatingClick,
          selectedRating = uiState.story.rating,
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
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
    val performRatingHaptic = rememberPerformRatingHaptic(LocalHapticFeedback.current)
    Row(
      horizontalArrangement = Arrangement.SpaceAround,
      modifier = Modifier,
    ) {
      ratings.forEach { rating ->
        val isSelectedRating = selectedRating == rating
        HedvigCard(
          modifier = Modifier.weight(1f),
          onClick = {
            performRatingHaptic(rating)
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
    Spacer(Modifier.height(8.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(
          stringResource(Res.string.PUPPY_GUIDE_RATING_NOT_HELPFUL),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          HedvigText(
            stringResource(Res.string.PUPPY_GUIDE_RATING_VERY_HELPFUL),
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
        onScrollOffsetChanged = {},
        onReachedBottom = {},
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
