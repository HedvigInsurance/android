package com.hedvig.android.feature.chat.cbm.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.cbm.CbmChatViewModel
import com.hedvig.android.feature.chat.cbm.inbox.InboxDestination
import com.hedvig.android.feature.chat.cbm.inbox.InboxViewModel
import com.hedvig.android.feature.chat.navigation.ChatDestination
import com.hedvig.android.feature.chat.navigation.ChatDestinations
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.cbmChatGraph(
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  navigator: Navigator,
) {
  navigation<ChatDestination>(
    startDestination = createRoutePattern<ChatDestinations.Inbox>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.chat },
    ),
  ) {
    composable<ChatDestinations.Inbox> { backStackEntry ->
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
    composable<ChatDestinations.Chat> {
      val viewModel = koinViewModel<CbmChatViewModel> { parametersOf(this.conversationId) }
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column {
          Text("Chat id:${this@composable.conversationId}")
          Text(uiState.toString())
        }
      }
    }
  }
}
