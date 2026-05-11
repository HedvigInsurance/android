package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import hedvig.resources.PUPPY_GUIDE_INFO
import hedvig.resources.PUPPY_GUIDE_LABEL_READ
import hedvig.resources.PUPPY_GUIDE_TITLE
import hedvig.resources.Res
import hedvig.resources.hundar_badar_pet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PuppyGuideDestination(
  viewModel: PuppyGuideViewModel,
  onNavigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PuppyGuideScreen(
    uiState,
    onNavigateToArticle = onNavigateToArticle,
    onNavigateUp = onNavigateUp,
    reload = {
      viewModel.emit(PuppyGuideEvent.Reload)
    },
    imageLoader = imageLoader,
  )
}

@Composable
private fun PuppyGuideScreen(
  uiState: PuppyGuideUiState,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  onNavigateUp: () -> Unit,
  reload: () -> Unit,
  imageLoader: ImageLoader,
) {
  when (uiState) {
    PuppyGuideUiState.Failure -> HedvigScaffold(
      navigateUp = onNavigateUp,
    ) {
      HedvigErrorSection(
        onButtonClick = reload,
        modifier = Modifier.weight(1f),
      )
    }

    PuppyGuideUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    is PuppyGuideUiState.Success -> PuppyGuideSuccessScreen(
      uiState,
      onNavigateUp = onNavigateUp,
      onNavigateToArticle = onNavigateToArticle,
      imageLoader = imageLoader,
    )
  }
}

@Composable
private fun PuppyGuideSuccessScreen(
  uiState: PuppyGuideUiState.Success,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  onNavigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  val categories = uiState.stories.flatMap { it.categories }.toSet().toList()
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      Modifier.fillMaxSize(),
    ) {
      PuppyTopAppBar(
        title = "",
        onBack = onNavigateUp,
      )
      val horizontalInsetsPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal)
        .asPaddingValues()
      val bottomInsetsPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
      val sectionContentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsetsPadding

      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = bottomInsetsPadding,
      ) {
        item {
          Column(
            modifier = Modifier.padding(sectionContentPadding),
          ) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Image(
                painter = painterResource(Res.drawable.hundar_badar_pet),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                  .height(300.dp)
                  .clip(HedvigTheme.shapes.cornerXLarge),
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HedvigText(stringResource(Res.string.PUPPY_GUIDE_TITLE))
            Spacer(modifier = Modifier.height(8.dp))
            HedvigText(
              stringResource(Res.string.PUPPY_GUIDE_INFO),
              color = HedvigTheme.colorScheme.textSecondary,
            )
            Spacer(modifier = Modifier.height(48.dp))
          }
        }

        stickyHeader {
          Surface(
            color = HedvigTheme.colorScheme.backgroundPrimary,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column {
              GuideCategoriesRow(
                categories = categories,
                contentPadding = sectionContentPadding,
                onCategoryClick = onClick@{ category ->
                  val index = categories.indexOf(category)
                  if (index == -1) return@onClick
                  scope.launch {
                    listState.animateScrollToItem(
                      index + 2,
                      // Negative offset to scroll less and avoid sticky header covering the title
                      scrollOffset = -200, // todo: wtf
                    )
                  }
                },
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }

        items(categories) { cat ->
          CategoryWithArticlesSection(
            category = cat,
            stories = uiState.stories.filter { it.categories.contains(cat) },
            onNavigateToArticle = onNavigateToArticle,
            imageLoader = imageLoader,
            contentPadding = sectionContentPadding,
          )
        }
      }
    }
  }
}

@Composable
private fun GuideCategoriesRow(
  categories: List<String>,
  contentPadding: PaddingValues,
  onCategoryClick: (String) -> Unit,
) {
  LazyRow(
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(categories) { category ->
      HedvigButton(
        text = category,
        enabled = true,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        onClick = {
          onCategoryClick(category)
        },
      )
    }
  }
}

@Composable
private fun CategoryWithArticlesSection(
  category: String,
  stories: List<PuppyGuideStory>,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  imageLoader: ImageLoader,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      category,
      fontStyle = HedvigTheme.typography.headlineSmall.fontStyle,
      fontSize = HedvigTheme.typography.headlineSmall.fontSize,
      fontFamily = HedvigTheme.typography.headlineSmall.fontFamily,
      modifier = Modifier.padding(contentPadding),
    )
    Spacer(Modifier.height(12.dp))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(24.dp),
      contentPadding = contentPadding,
    ) {
      val size = 148.dp
      items(stories) { story ->
        ArticleItem(
          story = story,
          onNavigateToArticle = onNavigateToArticle,
          imageLoader = imageLoader,
          size = size,
        )
      }
    }
    Spacer(Modifier.height(48.dp))
  }
}

@Composable
private fun ArticleItem(
  story: PuppyGuideStory,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  imageLoader: ImageLoader,
  size: Dp,
  modifier: Modifier = Modifier,
  shape: Shape = HedvigTheme.shapes.cornerMedium,
) {
  Column(
    modifier
      .width(size)
      .clip(shape)
      .clickable(
        onClick = {
          onNavigateToArticle(story)
        },
      ),
  ) {
    Box(
      contentAlignment = Alignment.TopEnd,
    ) {
      val fallbackPainter: Painter = ColorPainter(Color.Black.copy(alpha = 0.7f))
      AsyncImage(
        model = story.image,
        contentDescription = EmptyContentDescription, // todo
        placeholder = fallbackPainter,
        error = fallbackPainter,
        fallback = fallbackPainter,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size)
          .clip(shape),
      )
      if (story.isRead || story.rating != null) {
        HighlightLabel(
          modifier = modifier.padding(
            end = 12.dp,
            top = 12.dp,
          ),
          labelText = stringResource(Res.string.PUPPY_GUIDE_LABEL_READ),
          size = HighlightLabelDefaults.HighLightSize.Small,
          color = HighlightLabelDefaults.HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
        )
      }
    }

    Spacer(Modifier.height(8.dp))
    HedvigText(
      story.title,
      style = HedvigTheme.typography.label,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis, // todo: not by a11y req
    )
    HedvigText(
      story.subtitle,
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PuppyArticleScreenAnimations(
  @PreviewParameter(PuppyGuideUiStatePreviewProvider::class) uiState: PuppyGuideUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PuppyGuideScreen(
        uiState,
        {},
        {},
        reload = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

private class PuppyGuideUiStatePreviewProvider :
  CollectionPreviewParameterProvider<PuppyGuideUiState>(
    listOf(
      PuppyGuideUiState.Success(
        stories = listOf(
          PuppyGuideStory(
            categories = listOf("Food"),
            content = "some long long long long long long long long long long long long" +
              " long long long long long long long long long long long long content",
            image = "",
            name = "",
            rating = 5,
            isRead = true,
            subtitle = "5 min read",
            title = "Puppy food food food food food food food ",
          ),
          PuppyGuideStory(
            categories = listOf("Training"),
            content = "some long long long long long long long long long long long long" +
              " long long long long long long long long long long long long content",
            image = "",
            name = "",
            rating = 5,
            isRead = false,
            subtitle = "4 min read",
            title = "Puppy training",
          ),
        ),
      ),
      PuppyGuideUiState.Loading,
      PuppyGuideUiState.Failure,
    ),
  )
