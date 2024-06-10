package com.hedvig.android.feature.forever.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.foreverGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  navigation<ForeverDestination.Graph>(
    startDestination = ForeverDestination.Forever::class,
  ) {
    composable<ForeverDestination.Forever>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.forever },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: ForeverViewModel = koinViewModel()
      ForeverDestination(
        viewModel = viewModel,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
  }
}
