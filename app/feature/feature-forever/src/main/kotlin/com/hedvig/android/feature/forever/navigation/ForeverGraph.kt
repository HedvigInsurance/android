package com.hedvig.android.feature.forever.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.ui.ForeverDestination
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.foreverGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  composable<ForeverDestination>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.forever },
    ),
  ) {
    val viewModel: ForeverViewModel = koinViewModel()
    ForeverDestination(
      viewModel = viewModel,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
  }
}
