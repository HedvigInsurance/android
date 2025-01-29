package com.hedvig.android.feature.chat.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.CbmChatDestination
import com.hedvig.android.feature.chat.CbmChatViewModel
import com.hedvig.android.feature.chat.inbox.InboxDestination
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.cbmChatGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (claimId: String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigator: Navigator,
) {
  navgraph<ChatDestination>(
    startDestination = ChatDestinations.Inbox::class,
  ) {
    navdestination<ChatDestinations.Inbox>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.inbox },
        navDeepLink { uriPattern = hedvigDeepLinkContainer.chat },
      ),
    ) { backStackEntry ->
      val viewModel: InboxViewModel = koinViewModel()
      InboxDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onConversationClick = { conversationId ->
          with(navigator) {
            backStackEntry.navigate(ChatDestinations.Chat(conversationId))
          }
        },
      )
    }
    navdestination<ChatDestinations.Chat>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.conversation },
      ),
    ) {
      val viewModel = koinViewModel<CbmChatViewModel> { parametersOf(this.conversationId) }
      CbmChatDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
        appPackageId = hedvigBuildConstants.appId,
        openUrl = openUrl,
        onNavigateToClaimDetails = onNavigateToClaimDetails,
        onNavigateToImageViewer = onNavigateToImageViewer,
        onNavigateUp = navigator::navigateUp,
      )
    }
  }
}
