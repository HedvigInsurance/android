package com.hedvig.android.feature.help.center

import android.content.res.Resources
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.feature.help.center.commonclaim.CommonClaimDestination
import com.hedvig.android.feature.help.center.commonclaim.emergency.EmergencyDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.helpCenterGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onNavigateToQuickLink: (NavBackStackEntry, QuickLinkDestination) -> Unit,
  openChat: (NavBackStackEntry, AppDestination.Chat.ChatContext?) -> Unit,
) {
  navigation<HelpCenterDestination>(
    startDestination = createRoutePattern<HelpCenterDestinations.HelpCenter>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenter },
    ),
  ) {
    composable<HelpCenterDestinations.HelpCenter> { backStackEntry ->
      val viewModel = koinViewModel<HelpCenterViewModel>()
      val resources = LocalContext.current.resources
      HelpCenterHomeDestination(
        viewModel = viewModel,
        onNavigateToTopic = { topic ->
          navigateToTopic(resources, topic, navigator, backStackEntry)
        },
        onNavigateToQuestion = { question ->
          navigateToQuestion(resources, question, navigator, backStackEntry)
        },
        onNavigateToQuickLink = { destination ->
          onNavigateToQuickLink(backStackEntry, destination)
        },
        onNavigateToCommonClaim = { commonClaim ->
          when (commonClaim) {
            is CommonClaim.Emergency -> {
              with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Emergency(commonClaim)) }
            }

            is CommonClaim.Generic -> {
              with(navigator) { backStackEntry.navigate(HelpCenterDestinations.CommonClaim(commonClaim)) }
            }
          }
        },
        openChat = {
          openChat(backStackEntry, null)
        },
        onNavigateUp = navigator::navigateUp,
      )
    }
    composable<HelpCenterDestinations.Topic> { backStackEntry ->
      val resources = LocalContext.current.resources
      HelpCenterTopicDestination(
        topic = topic,
        onNavigateToQuestion = { question ->
          navigateToQuestion(resources, question, navigator, backStackEntry)
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        openChat = {
          openChat(backStackEntry, topic.chatContext)
        },
      )
    }
    composable<HelpCenterDestinations.Question> { backStackEntry ->
      val resources = LocalContext.current.resources
      HelpCenterQuestionDestination(
        questionId = question,
        onNavigateToQuestion = { question ->
          navigateToQuestion(resources, question, navigator, backStackEntry)
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        openChat = {
          openChat(backStackEntry, question.chatContext)
        },
      )
    }
    composable<HelpCenterDestinations.CommonClaim> {
      CommonClaimDestination(
        commonClaim = commonClaim,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
    composable<HelpCenterDestinations.Emergency> {
      EmergencyDestination(
        emergencyData = emergency,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}

private fun navigateToTopic(
  resources: Resources,
  topic: Topic,
  navigator: Navigator,
  backStackEntry: NavBackStackEntry,
) {
  val destination = HelpCenterDestinations.Topic(
    displayName = resources.getString(topic.titleRes),
    topic = topic,
  )
  with(navigator) { backStackEntry.navigate(destination) }
}

private fun navigateToQuestion(
  resources: Resources,
  question: Question,
  navigator: Navigator,
  backStackEntry: NavBackStackEntry,
) {
  val destination = HelpCenterDestinations.Question(
    displayName = resources.getString(question.questionRes),
    question = question,
  )
  with(navigator) { backStackEntry.navigate(destination) }
}
