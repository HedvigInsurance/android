package com.hedvig.android.feature.help.center

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredDestination
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredViewModel
import com.hedvig.android.feature.help.center.commonclaim.FirstVetDestination
import com.hedvig.android.feature.help.center.commonclaim.emergency.EmergencyDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.InnerHelpCenterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.InnerHelpCenterDestination.FirstVet
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.InnerHelpCenterDestination.QuickLinkSickAbroad
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations.ChooseInsuranceToEditCoInsured
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations.Emergency
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionViewModel
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicViewModel
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.helpCenterGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onNavigateToQuickLink: (NavBackStackEntry, QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: (NavBackStackEntry) -> Unit,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<HelpCenterDestination>(
    startDestination = HelpCenterDestinations.HelpCenter::class,
  ) {
    navdestination<HelpCenterDestinations.HelpCenter>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenter),
    ) { backStackEntry ->
      val viewModel = koinViewModel<HelpCenterViewModel>()
      HelpCenterHomeDestination(
        viewModel = viewModel,
        onNavigateToTopic = { topic ->
          navigateToTopic(topic, navigator, backStackEntry)
        },
        onNavigateToQuestion = { question ->
          navigateToQuestion(question, navigator, backStackEntry)
        },
        onNavigateToQuickLink = { destination ->
          when (destination) {
            is QuickLinkDestination.OuterDestination -> {
              onNavigateToQuickLink(backStackEntry, destination)
            }

            is QuickLinkDestination.InnerHelpCenterDestination -> {
              when (destination) {
                is FirstVet -> {
                  with(navigator) {
                    backStackEntry.navigate(HelpCenterDestinations.FirstVet(destination.sections))
                  }
                }

                is QuickLinkSickAbroad -> {
                  with(navigator) {
                    backStackEntry.navigate(
                      Emergency(
                        destination.emergencyNumber,
                        destination.emergencyUrl,
                      ),
                    )
                  }
                }

                ChooseInsuranceForEditCoInsured -> {
                  with(navigator) {
                    backStackEntry.navigate(
                      ChooseInsuranceToEditCoInsured,
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
          onNavigateToNewConversation(backStackEntry)
        },
        onNavigateUp = navigator::navigateUp,
      )
    }

    navdestination<ChooseInsuranceToEditCoInsured> { backStackEntry ->
      val viewModel = koinViewModel<ChooseInsuranceForEditCoInsuredViewModel>()
      ChooseInsuranceForEditCoInsuredDestination(
        viewModel = viewModel,
        navigateUp = {
          navigator.navigateUp()
        },
        navigateToNextStep = { destination ->
          onNavigateToQuickLink(backStackEntry, destination)
        },
      )
    }

    navdestination<HelpCenterDestinations.Topic>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenterCommonTopic),
    ) { backStackEntry ->
      val showNavigateToInboxViewModel = koinViewModel<ShowNavigateToInboxViewModel>()
      val helpCenterTopicViewModel = koinViewModel<HelpCenterTopicViewModel> {
        parametersOf(topicId)
      }
      logcat { "Mariia: topicId: $topicId" }
      HelpCenterTopicDestination(
        showNavigateToInboxViewModel = showNavigateToInboxViewModel,
        helpCenterTopicViewModel = helpCenterTopicViewModel,
        onNavigateToQuestion = { question ->
          navigateToQuestion(question, navigator, backStackEntry)
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        onNavigateToInbox = { onNavigateToInbox(backStackEntry) },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
      )
    }
    navdestination<HelpCenterDestinations.Question>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.helpCenterQuestion),
    ) { backStackEntry ->
      val showNavigateToInboxViewModel = koinViewModel<ShowNavigateToInboxViewModel>()
      val helpCenterQuestionViewModel = koinViewModel<HelpCenterQuestionViewModel> {
        parametersOf(questionId)
      }
      logcat { "Mariia: questionId: $questionId" }
      HelpCenterQuestionDestination(
        showNavigateToInboxViewModel = showNavigateToInboxViewModel,
        onNavigateToInbox = { onNavigateToInbox(backStackEntry) },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
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

private fun navigateToTopic(topicId: String, navigator: Navigator, backStackEntry: NavBackStackEntry) {
  val destination = HelpCenterDestinations.Topic(
    topicId = topicId,
  )
  with(navigator) { backStackEntry.navigate(destination) }
}

private fun navigateToQuestion(questionId: String, navigator: Navigator, backStackEntry: NavBackStackEntry) {
  val destination = HelpCenterDestinations.Question(
    questionId = questionId,
  )
  with(navigator) { backStackEntry.navigate(destination) }
}
