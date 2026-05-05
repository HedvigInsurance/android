package com.hedvig.android.feature.help.center

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.helpCenterGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navController: NavController,
  onNavigateToQuickLink: (QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  imageLoader: ImageLoader
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
          navigateToTopic(topic, navController)
        },
        onNavigateToQuestion = dropUnlessResumed { question ->
          navigateToQuestion(question, navController)
        },
        onNavigateToQuickLink = dropUnlessResumed { destination ->
          when (destination) {
            is QuickLinkDestination.OuterDestination -> {
              onNavigateToQuickLink(destination)
            }

            is InnerHelpCenterDestination -> {
              when (destination) {
                is FirstVet -> {
                  navController.navigate(HelpCenterDestinations.FirstVet(destination.sections))
                }

                is QuickLinkSickAbroad -> {
                  navController.navigate(
                    Emergency(
                      destination.deflectData,
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
        onNavigateUp = navController::navigateUp,
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
          navigateToQuestion(question, navController)
        },
        onNavigateUp = navController::navigateUp,
        onNavigateBack = navController::popBackStack,
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
        onNavigateUp = navController::navigateUp,
        onNavigateBack = navController::popBackStack,
        helpCenterQuestionViewModel = helpCenterQuestionViewModel,
      )
    }
    navdestination<HelpCenterDestinations.FirstVet>(
      HelpCenterDestinations.FirstVet,
    ) {
      FirstVetDestination(
        sections = sections,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
      )
    }
    navdestination<Emergency>(HelpCenterDestinations.Emergency) {
      EmergencyDestination(
        deflect = deflectData,
        navigateUp = navController::navigateUp,
        openUrl = openUrl,
        tryToDialPhone = tryToDialPhone,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        imageLoader = imageLoader
      )
    }
  }
}

private fun navigateToTopic(topicId: String, navController: NavController) {
  val destination = HelpCenterDestinations.Topic(
    topicId = topicId,
  )
  navController.navigate(destination)
}

private fun navigateToQuestion(questionId: String, navController: NavController) {
  val destination = HelpCenterDestinations.Question(
    questionId = questionId,
  )
  navController.navigate(destination)
}
