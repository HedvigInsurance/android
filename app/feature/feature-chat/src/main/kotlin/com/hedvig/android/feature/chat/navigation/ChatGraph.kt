package com.hedvig.android.feature.chat.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.ui.ChatDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.chatGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  navigator: Navigator,
) {
  composable<AppDestination.Chat>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.chat },
    ),
    enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up) },
    exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down) },
  ) {

    val viewModel: ChatViewModel = koinViewModel { parametersOf(chatContext) }
    ChatDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = hedvigBuildConstants.appId,
      openUrl = openUrl,
      onNavigateUp = navigator::navigateUp,
    )
  }
}
