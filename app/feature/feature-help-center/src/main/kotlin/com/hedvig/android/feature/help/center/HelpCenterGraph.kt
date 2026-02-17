package com.hedvig.android.feature.help.center

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.feature.help.center.commonclaim.FirstVetDestination
import com.hedvig.android.feature.help.center.commonclaim.emergency.EmergencyDestination
import com.hedvig.android.feature.help.center.data.InnerHelpCenterDestination
import com.hedvig.android.feature.help.center.data.InnerHelpCenterDestination.FirstVet
import com.hedvig.android.feature.help.center.data.InnerHelpCenterDestination.QuickLinkSickAbroad
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations.Emergency
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionViewModel
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.helpCenterGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onNavigateToQuickLink: (QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
) {
  navgraph<HelpCenterDestination>(
    startDestination = HelpCenterDestinations.HelpCenter::class,
  ) {
    navdestination<HelpCenterDestinations.HelpCenter>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenter),
    ) {
      val viewModel = koinViewModel<HelpCenterViewModel>()
      HelpCenterHomeDestination(
        viewModel = viewModel,
        onNavigateToTopic = dropUnlessResumed { topic ->
          navigateToTopic(topic, navigator)
        },
        onNavigateToQuestion = dropUnlessResumed { question ->
          navigateToQuestion(question, navigator)
        },
        onNavigateToQuickLink = dropUnlessResumed { destination ->
          when (destination) {
            is QuickLinkDestination.OuterDestination -> {
              onNavigateToQuickLink(destination)
            }

            is InnerHelpCenterDestination -> {
              when (destination) {
                is FirstVet -> {
                  navigator.navigate(HelpCenterDestinations.FirstVet(destination.sections))
                }

                is QuickLinkSickAbroad -> {
                  navigator.navigate(
                    Emergency(
                      destination.emergencyNumber,
                      destination.emergencyUrl,
                      destination.preferredPartnerImageHeight,
                    ),
                  )
                }
              }
            }
          }
        },
        onNavigateToInbox = dropUnlessResumed {
          onNavigateToInbox()
        },
        onNavigateToNewConversation = dropUnlessResumed {
          onNavigateToNewConversation()
        },
        onNavigateUp = navigator::navigateUp,
      )
    }

    navdestination<HelpCenterDestinations.Topic>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenterCommonTopic),
    ) {
      val showNavigateToInboxViewModel = koinViewModel<ShowNavigateToInboxViewModel>()
      val helpCenterTopicViewModel = koinViewModel<HelpCenterTopicViewModel> {
        parametersOf(topicId)
      }
      HelpCenterTopicDestination(
        showNavigateToInboxViewModel = showNavigateToInboxViewModel,
        helpCenterTopicViewModel = helpCenterTopicViewModel,
        onNavigateToQuestion = dropUnlessResumed { question ->
          navigateToQuestion(question, navigator)
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      )
    }
    navdestination<HelpCenterDestinations.Question>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenterQuestion),
    ) {
      val showNavigateToInboxViewModel = koinViewModel<ShowNavigateToInboxViewModel>()
      val helpCenterQuestionViewModel = koinViewModel<HelpCenterQuestionViewModel> {
        parametersOf(questionId)
      }
      HelpCenterQuestionDestination(
        showNavigateToInboxViewModel = showNavigateToInboxViewModel,
        onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        helpCenterQuestionViewModel = helpCenterQuestionViewModel,
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
    navdestination<Emergency> {
      EmergencyDestination(
        emergencyNumber = emergencyNumber,
        emergencyUrl = emergencyUrl,
        preferredPartnerImageHeight = preferredPartnerImageHeight,
        navigateUp = navigator::navigateUp,
        openUrl = openUrl,
        tryToDialPhone = tryToDialPhone,
      )
    }
  }
}

private fun navigateToTopic(topicId: String, navigator: Navigator) {
  val destination = HelpCenterDestinations.Topic(
    topicId = topicId,
  )
  navigator.navigate(destination)
}

private fun navigateToQuestion(questionId: String, navigator: Navigator) {
  val destination = HelpCenterDestinations.Question(
    questionId = questionId,
  )
  navigator.navigate(destination)
}
