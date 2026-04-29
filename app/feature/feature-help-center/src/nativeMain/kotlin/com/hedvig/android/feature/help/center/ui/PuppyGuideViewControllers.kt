package com.hedvig.android.feature.help.center.ui

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleViewModel
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName") // Used from iOS
fun PuppyGuideViewController(
  onNavigateUp: () -> Unit,
  onNavigateToArticle: (storyName: String) -> Unit,
): UIViewController {
  return ComposeUIViewController {
    HedvigTheme {
      val imageLoader = koinInject<ImageLoader>()
      val viewModel = koinViewModel<PuppyGuideViewModel>()
      PuppyGuideDestination(
        viewModel = viewModel,
        onNavigateUp = onNavigateUp,
        imageLoader = imageLoader,
        onNavigateToArticle = { story -> onNavigateToArticle(story.name) },
      )
    }
  }
}

@Suppress("unused", "FunctionName") // Used from iOS
fun PuppyArticleViewController(
  storyName: String,
  navigateUp: () -> Unit,
): UIViewController {
  return ComposeUIViewController {
    HedvigTheme {
      val imageLoader = koinInject<ImageLoader>()
      val viewModel = koinViewModel<PuppyArticleViewModel> { parametersOf(storyName) }
      PuppyArticleDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}
