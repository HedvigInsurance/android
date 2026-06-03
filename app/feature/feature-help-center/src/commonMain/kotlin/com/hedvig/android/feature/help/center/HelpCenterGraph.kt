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
import com.hedvig.android.feature.help.center.navigation.EmergencyKey
import com.hedvig.android.feature.help.center.navigation.FirstVetKey
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.help.center.navigation.HelpCenterQuestionKey
import com.hedvig.android.feature.help.center.navigation.HelpCenterTopicKey
import com.hedvig.android.feature.help.center.navigation.PuppyGuideArticleKey
import com.hedvig.android.feature.help.center.navigation.PuppyGuideKey
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyArticleViewModel
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideDestination
import com.hedvig.android.feature.help.center.puppyguide.PuppyGuideViewModel
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionViewModel
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.helpCenterGraph(
  backStack: MutableList<HedvigNavKey>,
  onNavigateUp: () -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination.OuterDestination) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  entry<HelpCenterKey> {
    val viewModel = metroViewModel<HelpCenterViewModel>()
    HelpCenterHomeDestination(
      viewModel = viewModel,
      onNavigateToTopic = dropUnlessResumed { topic ->
        navigateToTopic(topic, backStack)
      },
      onNavigateToQuestion = dropUnlessResumed { question ->
        navigateToQuestion(question, backStack)
      },
      onNavigateToQuickLink = dropUnlessResumed { destination ->
        when (destination) {
          is QuickLinkDestination.OuterDestination -> {
            onNavigateToQuickLink(destination)
          }

          is InnerHelpCenterDestination -> {
            when (destination) {
              is FirstVet -> {
                backStack.add(FirstVetKey(destination.sections))
              }

              is QuickLinkSickAbroad -> {
                backStack.add(EmergencyKey(destination.deflectData))
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
        backStack.add(PuppyGuideKey)
      },
    )
  }

  entry<HelpCenterTopicKey> { key ->
    val showNavigateToInboxViewModel = metroViewModel<ShowNavigateToInboxViewModel>()
    val topicId = key.topicId
    val helpCenterTopicViewModel =
      assistedMetroViewModel<HelpCenterTopicViewModel, HelpCenterTopicViewModel.Factory> {
        create(topicId)
      }
    HelpCenterTopicDestination(
      showNavigateToInboxViewModel = showNavigateToInboxViewModel,
      helpCenterTopicViewModel = helpCenterTopicViewModel,
      onNavigateToQuestion = dropUnlessResumed { question ->
        navigateToQuestion(question, backStack)
      },
      onNavigateUp = backStack::navigateUp,
      onNavigateBack = backStack::popBackStack,
      onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }
  entry<HelpCenterQuestionKey> { key ->
    val showNavigateToInboxViewModel = metroViewModel<ShowNavigateToInboxViewModel>()
    val questionId = key.questionId
    val helpCenterQuestionViewModel =
      assistedMetroViewModel<HelpCenterQuestionViewModel, HelpCenterQuestionViewModel.Factory> {
        create(questionId)
      }
    HelpCenterQuestionDestination(
      showNavigateToInboxViewModel = showNavigateToInboxViewModel,
      onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      onNavigateUp = backStack::navigateUp,
      onNavigateBack = backStack::popBackStack,
      helpCenterQuestionViewModel = helpCenterQuestionViewModel,
    )
  }
  entry<FirstVetKey> { key ->
    FirstVetDestination(
      sections = key.sections,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
      openUrl = openUrl,
    )
  }
  entry<EmergencyKey> { key ->
    EmergencyDestination(
      deflect = key.deflectData,
      navigateUp = backStack::navigateUp,
      openUrl = openUrl,
      tryToDialPhone = tryToDialPhone,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      imageLoader = imageLoader,
    )
  }

  entry<PuppyGuideKey> {
    val viewModel = metroViewModel<PuppyGuideViewModel>()
    PuppyGuideDestination(
      viewModel,
      onNavigateUp = backStack::navigateUp,
      onNavigateToArticle = { story ->
        backStack.add(PuppyGuideArticleKey(story.name))
      },
      imageLoader = imageLoader,
    )
  }

  entry<PuppyGuideArticleKey> { key ->
    val storyName = key.storyName
    val viewModel =
      assistedMetroViewModel<PuppyArticleViewModel, PuppyArticleViewModel.Factory> {
        create(storyName)
      }
    PuppyArticleDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      imageLoader = imageLoader,
    )
  }
}

private fun navigateToTopic(topicId: String, backStack: MutableList<HedvigNavKey>) {
  backStack.add(HelpCenterTopicKey(topicId = topicId))
}

private fun navigateToQuestion(questionId: String, backStack: MutableList<HedvigNavKey>) {
  backStack.add(HelpCenterQuestionKey(questionId = questionId))
}
