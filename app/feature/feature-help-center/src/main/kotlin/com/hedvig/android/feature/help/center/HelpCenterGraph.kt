package com.hedvig.android.feature.help.center

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation

fun NavGraphBuilder.helpCenterGraph(navigator: Navigator, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
  navigation<HelpCenterDestination>(
    startDestination = createRoutePattern<HelpCenterDestinations.HelpCenter>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenter },
    ),
  ) {
    composable<HelpCenterDestinations.HelpCenter> { backStackEntry ->
      HelpCenterHomeDestination(
        onNavigateToTopic = { topicId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Topic(topicId)) }
        },
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
      )
    }
    composable<HelpCenterDestinations.Topic>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenterCommonTopic },
      ),
    ) { backStackEntry ->
      HelpCenterTopicDestination(
        topicId = id,
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
      )
    }
    composable<HelpCenterDestinations.Question>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenterQuestion },
      ),
    ) { backStackEntry ->
      HelpCenterQuestionDestination(
        questionId = id,
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
      )
    }
  }
}
