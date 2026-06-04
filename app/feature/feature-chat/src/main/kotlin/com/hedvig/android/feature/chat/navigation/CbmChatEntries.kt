package com.hedvig.android.feature.chat.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.CbmChatDestination
import com.hedvig.android.feature.chat.CbmChatViewModel
import com.hedvig.android.feature.chat.inbox.InboxDestination
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.cbmChatEntries(
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (claimId: String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  backstack: Backstack,
) {
  entry<InboxKey> {
    val viewModel: InboxViewModel = metroViewModel()
    InboxDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onConversationClick = dropUnlessResumed { conversationId ->
        backstack.add(ChatKey(conversationId))
      },
    )
  }
  entry<ChatKey> { key ->
    val conversationId = key.conversationId
    val viewModel = assistedMetroViewModel<CbmChatViewModel, CbmChatViewModel.Factory> {
      create(conversationId)
    }
    CbmChatDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = hedvigBuildConstants.appPackageId,
      openUrl = openUrl,
      onNavigateToClaimDetails = onNavigateToClaimDetails,
      onNavigateToImageViewer = onNavigateToImageViewer,
      onNavigateUp = backstack::navigateUp,
    )
  }
}
