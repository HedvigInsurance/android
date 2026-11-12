package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/** The cache that the chat's video players read and write video data through. */
internal expect interface ChatVideoCache

/** The app's single video cache, injected into the chat. */
internal expect class ChatSimpleVideoCache : ChatVideoCache

/** The video players of the chat's messages, kept across recompositions and released together. */
@Stable
internal expect class ChatVideoPlayers {
  fun releaseAll()
}

@Composable
internal expect fun rememberChatVideoPlayers(): ChatVideoPlayers

@Composable
internal expect fun ChatVideoMessage(
  videoPlayers: ChatVideoPlayers,
  messageId: String,
  url: String,
  videoCache: ChatVideoCache,
  openUrl: (String) -> Unit,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier,
)

@Composable
internal expect fun rememberPreviewChatVideoCache(): ChatVideoCache
