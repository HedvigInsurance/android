package com.hedvig.android.feature.forever.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.ui.ForeverScreen
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.foreverGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  animatedNavigation<TopLevelGraph.FOREVER>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Forever>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.forever },
    ),
  ) {
    animatedComposable<AppDestination.TopLevelDestination.Forever>(
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

@Composable
private fun ForeverDestination(
  viewModel: ForeverViewModel,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  AnimatedContent(targetState = uiState.isLoading, label = "") { loading ->
    when (loading) {
      true -> HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
      false -> ForeverScreen(
        uiState = uiState,
        reload = viewModel::reload,
        onSubmitCode = viewModel::onSubmitCode,
        onCodeChanged = viewModel::onCodeChanged,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
  }
}
