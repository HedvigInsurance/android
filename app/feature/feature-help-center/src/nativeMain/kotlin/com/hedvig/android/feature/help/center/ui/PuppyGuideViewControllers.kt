package com.hedvig.android.feature.help.center.ui

import com.hedvig.android.design.system.hedvig.HedvigComposeUIViewController
import com.hedvig.android.design.system.hedvig.IosDiHolder
import com.hedvig.android.design.system.hedvig.api.IosSwipeBackController
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleViewModel
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName") // Used from iOS
fun PuppyGuideViewController(
  onNavigateUp: () -> Unit,
  onNavigateToArticle: (storyName: String) -> Unit,
  swipeBackController: IosSwipeBackController,
  onScrollOffsetChanged: (Float) -> Unit,
): UIViewController {
  return HedvigComposeUIViewController(swipeBackController) {
    val viewModel = metroViewModel<PuppyGuideViewModel>()
    PuppyGuideDestination(
      viewModel = viewModel,
      onNavigateUp = onNavigateUp,
      imageLoader = IosDiHolder.imageLoader,
      onNavigateToArticle = { story -> onNavigateToArticle(story.name) },
      onScrollOffsetChanged = onScrollOffsetChanged,
    )
  }
}

@Suppress("unused", "FunctionName") // Used from iOS
fun PuppyArticleViewController(
  storyName: String,
  navigateUp: () -> Unit,
  swipeBackController: IosSwipeBackController,
  onScrollOffsetChanged: (Float) -> Unit,
): UIViewController {
  return HedvigComposeUIViewController(swipeBackController) {
    val viewModel = assistedMetroViewModel<PuppyArticleViewModel, PuppyArticleViewModel.Factory> {
      create(storyName)
    }
    PuppyArticleDestination(
      viewModel = viewModel,
      navigateUp = navigateUp,
      imageLoader = IosDiHolder.imageLoader,
      onScrollOffsetChanged = onScrollOffsetChanged,
    )
  }
}
