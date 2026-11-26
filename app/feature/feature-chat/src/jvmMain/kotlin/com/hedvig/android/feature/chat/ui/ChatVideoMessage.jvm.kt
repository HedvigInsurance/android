package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hedvig.android.feature.chat.AttachedFileMessage
import dev.zacsweers.metro.Inject

internal actual interface ChatVideoCache

/** The desktop app plays no video in the chat, so nothing is cached. */
@Inject
internal actual class ChatSimpleVideoCache : ChatVideoCache

@Stable
internal actual class ChatVideoPlayers {
  actual fun releaseAll() {}
}

@Composable
internal actual fun rememberChatVideoPlayers(): ChatVideoPlayers = remember { ChatVideoPlayers() }

/** The desktop app does not play videos inline: the message is the attached file card that opens the video's URL. */
@Composable
internal actual fun ChatVideoMessage(
  videoPlayers: ChatVideoPlayers,
  messageId: String,
  url: String,
  videoCache: ChatVideoCache,
  openUrl: (String) -> Unit,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier,
) {
  AttachedFileMessage(onClick = { openUrl(url) }, modifier = modifier)
}

@Composable
internal actual fun rememberPreviewChatVideoCache(): ChatVideoCache = remember { ChatSimpleVideoCache() }
