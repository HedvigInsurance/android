package com.hedvig.android.feature.chat.navigation

import androidx.media3.datasource.cache.SimpleCache
import com.hedvig.android.compose.ui.dropUnlessResumed
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.CbmChatDestination
import com.hedvig.android.feature.chat.CbmChatViewModel
import com.hedvig.android.feature.chat.inbox.InboxDestination
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.cbmChatGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (claimId: String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navController: NavController,
) {
  navgraph<ChatDestination>(
    startDestination = ChatDestinations.Inbox::class,
  ) {
    navdestination<ChatDestinations.Inbox>(
      deepLinks = navDeepLinks(
        hedvigDeepLinkContainer.inbox,
        hedvigDeepLinkContainer.chat,
      ),
    ) {
      val viewModel: InboxViewModel = koinViewModel()
      InboxDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onConversationClick = dropUnlessResumed { conversationId ->
          navController.navigate(ChatDestinations.Chat(conversationId))
        },
      )
    }
    navdestination<ChatDestinations.Chat>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.conversation),
    ) {
      val viewModel = koinViewModel<CbmChatViewModel> { parametersOf(this.conversationId) }
      CbmChatDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
        appPackageId = hedvigBuildConstants.appPackageId,
        openUrl = openUrl,
        onNavigateToClaimDetails = onNavigateToClaimDetails,
        onNavigateToImageViewer = onNavigateToImageViewer,
        onNavigateUp = navController::navigateUp,
        simpleVideoCache = simpleVideoCache,
      )
    }
  }
}
