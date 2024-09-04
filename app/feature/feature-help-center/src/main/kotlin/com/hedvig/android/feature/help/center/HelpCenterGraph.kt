package com.hedvig.android.feature.help.center

import android.content.res.Resources
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.help.center.commonclaim.FirstVetDestination
import com.hedvig.android.feature.help.center.commonclaim.emergency.EmergencyDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.helpCenterGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onNavigateToQuickLink: (NavBackStackEntry, QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: (NavBackStackEntry) -> Unit,
  onNavigateToNewConversation: (NavBackStackEntry, AppDestination.Chat.ChatContext?) -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<HelpCenterDestination>(
    startDestination = HelpCenterDestinations.HelpCenter::class,
  ) {
    navdestination<HelpCenterDestinations.HelpCenter>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenter },
      ),
    ) { backStackEntry ->
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
          when (destination) {
            is QuickLinkDestination.OuterDestination -> {
              onNavigateToQuickLink(backStackEntry, destination)
            }

            is QuickLinkDestination.InnerHelpCenterDestination -> {
              when (destination) {
                is QuickLinkDestination.InnerHelpCenterDestination.FirstVet -> {
                  with(navigator) {
                    backStackEntry.navigate(HelpCenterDestinations.FirstVet(destination.sections))
                  }
                }

                is QuickLinkDestination.InnerHelpCenterDestination.QuickLinkSickAbroad -> {
                  with(navigator) {
                    backStackEntry.navigate(
                      HelpCenterDestinations.Emergency(
                        destination.emergencyNumber,
                        destination.emergencyUrl,
                      ),
                    )
                  }
                }
              }
            }
          }
        },
        onNavigateToInbox = {
          onNavigateToInbox(backStackEntry)
        },
        onNavigateToNewConversation = {
          onNavigateToNewConversation(backStackEntry, null)
        },
        onNavigateUp = navigator::navigateUp,
      )
    }
    navdestination<HelpCenterDestinations.Topic>(
      HelpCenterDestinations.Topic,
    ) { backStackEntry ->
      val resources = LocalContext.current.resources
      HelpCenterTopicDestination(
        topic = topic,
        onNavigateToQuestion = { question ->
          navigateToQuestion(resources, question, navigator, backStackEntry)
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        onNavigateToInbox = { onNavigateToInbox(backStackEntry) },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry, topic.chatContext) },
      )
    }
    navdestination<HelpCenterDestinations.Question>(
      HelpCenterDestinations.Question,
    ) { backStackEntry ->
      val resources = LocalContext.current.resources
      HelpCenterQuestionDestination(
        questionId = question,
        onNavigateToQuestion = { question ->
          navigateToQuestion(resources, question, navigator, backStackEntry)
        },
        onNavigateToInbox = { onNavigateToInbox(backStackEntry) },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry, question.chatContext) },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
      )
    }
    navdestination<HelpCenterDestinations.FirstVet>(
      HelpCenterDestinations.FirstVet,
    ) {
      FirstVetDestination(
        sections = sections,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
    navdestination<HelpCenterDestinations.Emergency> {
      EmergencyDestination(
        emergencyNumber = emergencyNumber,
        emergencyUrl = emergencyUrl,
        navigateUp = navigator::navigateUp,
        openUrl = openUrl,
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
