package com.hedvig.android.feature.help.center

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleViewModel
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideViewModel
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionViewModel
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.helpCenterGraph(
  navigator: Navigator,
  onNavigateUp: () -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  navgraph(
    startDestination = HelpCenterDestinations.HelpCenter::class,
  ) {
    navdestination<HelpCenterDestinations.HelpCenter> {
      val viewModel = metroViewModel<HelpCenterViewModel>()
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
        onNavigateUp = onNavigateUp,
        onNavigateToPuppyGuide = dropUnlessResumed {
          navigator.navigate(HelpCenterDestinations.PuppyGuide)
        },
      )
    }

    navdestination<HelpCenterDestinations.Topic> {
      val showNavigateToInboxViewModel = metroViewModel<ShowNavigateToInboxViewModel>()
      val topicId = topicId
      val helpCenterTopicViewModel =
        assistedMetroViewModel<HelpCenterTopicViewModel, HelpCenterTopicViewModel.Factory> {
          create(topicId)
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
    navdestination<HelpCenterDestinations.Question> {
      val showNavigateToInboxViewModel = metroViewModel<ShowNavigateToInboxViewModel>()
      val questionId = questionId
      val helpCenterQuestionViewModel =
        assistedMetroViewModel<HelpCenterQuestionViewModel, HelpCenterQuestionViewModel.Factory> {
          create(questionId)
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
    navdestination<HelpCenterDestinations.FirstVet> {
      FirstVetDestination(
        sections = sections,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
        openUrl = openUrl,
      )
    }
    navdestination<Emergency> {
      EmergencyDestination(
        deflect = deflectData,
        navigateUp = navigator::navigateUp,
        openUrl = openUrl,
        tryToDialPhone = tryToDialPhone,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        imageLoader = imageLoader,
      )
    }

    navdestination<HelpCenterDestinations.PuppyGuide> {
      val viewModel = metroViewModel<PuppyGuideViewModel>()
      PuppyGuideDestination(
        viewModel,
        onNavigateUp = navigator::navigateUp,
        onNavigateToArticle = { story ->
          navigator.navigate(
            HelpCenterDestinations.PuppyGuideArticle(
              story.name,
            ),
          )
        },
        imageLoader = imageLoader,
      )
    }

    navdestination<HelpCenterDestinations.PuppyGuideArticle> {
      val storyName = storyName
      val viewModel =
        assistedMetroViewModel<PuppyArticleViewModel, PuppyArticleViewModel.Factory> {
          create(storyName)
        }
      PuppyArticleDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        imageLoader = imageLoader,
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
