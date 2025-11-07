package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import hedvig.resources.R

@Composable
internal fun PuppyGuideDestination(
  viewModel: PuppyGuideViewModel,
  onNavigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    PuppyGuideUiState.Failure -> HedvigScaffold(
      navigateUp = onNavigateUp,
    ) {
      HedvigErrorSection(
        onButtonClick = {
          viewModel.emit(PuppyGuideEvent.Reload)
        },
        modifier = Modifier.weight(1f),
      )
    }

    PuppyGuideUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is PuppyGuideUiState.Success -> PuppyGuideScreen(
      state,
      onNavigateUp = onNavigateUp,
      onNavigateToArticle = onNavigateToArticle,
      imageLoader = imageLoader,
    )
  }
}

@Composable
private fun PuppyGuideScreen(
  uiState: PuppyGuideUiState.Success,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  onNavigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    ) {
      TopAppBarWithBack(
        title = "",
        onClick = onNavigateUp,
      )
      Column(
        Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        Image(
          painter = painterResource(id = com.hedvig.android.feature.help.center.R.drawable.hundar_badar_pet),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          alignment = Alignment.Center,
          modifier = Modifier
            .height(300.dp)
            .clip(HedvigTheme.shapes.cornerXLarge),
        )
        Spacer(modifier = Modifier.height(16.dp))
        HedvigText(stringResource(R.string.PUPPY_GUIDE_TITLE))
        Spacer(modifier = Modifier.height(8.dp))
        HedvigText(
          stringResource(R.string.PUPPY_GUIDE_INFO),
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(modifier = Modifier.height(48.dp))
        val categories = uiState.stories.flatMap { it.categories }.toSet().toList()
        GuideCategoriesRow(
          categories,
          onCategoryClick = {
            // todo
          },
        )
        Spacer(modifier = Modifier.height(48.dp))
        categories.forEach { cat ->
          CategoryWithArticlesSection(
            cat,
            stories = uiState.stories.filter { it.categories.contains(cat) },
            onNavigateToArticle = onNavigateToArticle,
            imageLoader = imageLoader,
          )
        }
      }
    }
  }
}

@Composable
private fun GuideCategoriesRow(categories: List<String>, onCategoryClick: (String) -> Unit) {
  Row(Modifier.horizontalScroll(rememberScrollState())) {
    categories.forEach {
      HedvigButton(
        text = it,
        enabled = true,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        onClick = {
          onCategoryClick(it)
        },
      )
      Spacer(Modifier.width(8.dp))
    }
  }
}

@Composable
private fun CategoryWithArticlesSection(
  category: String,
  stories: List<PuppyGuideStory>,
  onNavigateToArticle: (PuppyGuideStory) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      category,
      fontStyle = HedvigTheme.typography.headlineSmall.fontStyle,
      fontSize = HedvigTheme.typography.headlineSmall.fontSize,
      fontFamily = HedvigTheme.typography.headlineSmall.fontFamily,
    )
    Spacer(Modifier.height(12.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier.horizontalScroll(rememberScrollState())) {
      val size = 148.dp
      stories.forEach { story ->
        Column(
          Modifier
            .width(size)
            .clickable(onClick = {
            onNavigateToArticle(story)
          }),
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
              .clip(HedvigTheme.shapes.cornerMedium),
          )
          Spacer(Modifier.height(8.dp))
          HedvigText(story.title)
        }
      }
    }
    Spacer(Modifier.height(48.dp))
  }
}
