package com.hedvig.android.feature.chat.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.kiwi.navigationcompose.typed.composable

fun NavGraphBuilder.chatGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  composable<AppDestination.Chat>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.chat },
    ),
    enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up) },
    exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down) },
  ) { backstackEntry ->

  }
}
