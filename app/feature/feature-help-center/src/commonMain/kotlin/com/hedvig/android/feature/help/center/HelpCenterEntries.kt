package com.hedvig.android.feature.help.center

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.feature.help.center.commonclaim.FirstVetDestination
import com.hedvig.android.feature.help.center.commonclaim.emergency.EmergencyDestination
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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.helpCenterEntries(
  backstack: Backstack,
  onNavigateUp: () -> Unit,
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
        navigateToTopic(topic, backstack)
      },
      onNavigateToQuestion = dropUnlessResumed { question ->
        navigateToQuestion(question, backstack)
      },
      onNavigateToInbox = dropUnlessResumed {
        onNavigateToInbox()
      },
      onNavigateToNewConversation = dropUnlessResumed {
        onNavigateToNewConversation()
      },
      onNavigateUp = onNavigateUp,
      onNavigateToPuppyGuide = dropUnlessResumed {
        backstack.add(PuppyGuideKey)
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
        navigateToQuestion(question, backstack)
      },
      onNavigateUp = backstack::navigateUp,
      onNavigateBack = backstack::popBackstack,
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
      onNavigateUp = backstack::navigateUp,
      onNavigateBack = backstack::popBackstack,
      helpCenterQuestionViewModel = helpCenterQuestionViewModel,
    )
  }
  entry<FirstVetKey> { key ->
    FirstVetDestination(
      sections = key.sections,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      openUrl = openUrl,
    )
  }
  entry<EmergencyKey> { key ->
    EmergencyDestination(
      deflect = key.deflectData,
      navigateUp = backstack::navigateUp,
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
      onNavigateUp = backstack::navigateUp,
      onNavigateToArticle = { story ->
        backstack.add(PuppyGuideArticleKey(story.name))
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
      navigateUp = backstack::navigateUp,
      imageLoader = imageLoader,
    )
  }
}

private fun navigateToTopic(topicId: String, backstack: Backstack) {
  backstack.add(HelpCenterTopicKey(topicId = topicId))
}

private fun navigateToQuestion(questionId: String, backstack: Backstack) {
  backstack.add(HelpCenterQuestionKey(questionId = questionId))
}
