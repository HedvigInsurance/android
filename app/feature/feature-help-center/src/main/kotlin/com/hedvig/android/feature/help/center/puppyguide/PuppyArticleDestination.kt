package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.help.center.data.PuppyGuideStory

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
    imageLoader
  )

}

@Composable
private fun PuppyArticleScreen(
  uiState: PuppyArticleUiState,
  navigateUp: () -> Unit,
  onReload: () -> Unit,
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
      imageLoader = imageLoader
    )
  }
}

@Composable
private fun PuppyArticleSuccessScreen(
  uiState: PuppyArticleUiState.Success,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  
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
        {},
        {},
        rememberPreviewImageLoader()
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
