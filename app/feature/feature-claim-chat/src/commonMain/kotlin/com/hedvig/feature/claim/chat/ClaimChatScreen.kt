package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ktor.http.parametersOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ClaimChatDestination(
  isDevelopmentFlow: Boolean,
  messageId: String?
) {
  val claimChatViewModel = koinViewModel<ClaimChatViewModel>{
    parametersOf(
      isDevelopmentFlow, messageId
    )
  }
  ClaimChatScreen(claimChatViewModel)
}

@Composable
internal fun ClaimChatScreen(claimChatViewModel: ClaimChatViewModel) {
  val uiState by claimChatViewModel.state.collectAsState()

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    ConversationScreen(
      uiState,
      onAction = claimChatViewModel::processUserAction,
    )
  }
}
