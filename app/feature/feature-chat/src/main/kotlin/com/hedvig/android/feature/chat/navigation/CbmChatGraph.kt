package com.hedvig.android.feature.chat.navigation

import androidx.media3.datasource.cache.SimpleCache
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.CbmChatDestination
import com.hedvig.android.feature.chat.CbmChatViewModel
import com.hedvig.android.feature.chat.inbox.InboxDestination
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateUp
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.cbmChatGraph(
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (claimId: String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  backStack: MutableList<HedvigNavKey>,
) {
  navdestination<InboxKey> {
    val viewModel: InboxViewModel = metroViewModel()
    InboxDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onConversationClick = dropUnlessResumed { conversationId ->
        backStack.add(ChatKey(conversationId))
      },
    )
  }
  navdestination<ChatKey> {
    val conversationId = this.conversationId
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
      onNavigateUp = backStack::navigateUp,
      simpleVideoCache = simpleVideoCache,
    )
  }
}
