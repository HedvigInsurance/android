package com.hedvig.android.feature.chat

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import coil3.EventListener
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.chat.model.CbmChatMessage.ChatMessageFile
import com.hedvig.android.feature.chat.model.CbmChatMessage.ChatMessageFile.MimeType
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.ui.ChatSimpleVideoCache
import com.hedvig.android.feature.chat.ui.ChatVideoPlayers
import hedvig.resources.CHAT_FILE_DOWNLOAD
import hedvig.resources.Res
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ChatBubbleFileThumbnailTest {
  private val fileCardText = runBlocking { getString(Res.string.CHAT_FILE_DOWNLOAD) }

  @Test
  fun `a pdf whose thumbnail cannot be decoded is shown as the file card that opens the url`() = runComposeUiTest {
    val outcome = RecordingListener()
    val openedUrls = mutableListOf<String>()
    setChatBubble(MimeType.PDF, UNDECODABLE_PDF, outcome, openUrl = { openedUrls += it })

    waitUntil(timeoutMillis = 10_000) { outcome.error != null }
    onNodeWithText(fileCardText, useUnmergedTree = true).assertExists().performClick()
    assertThat(openedUrls).containsExactly(UNDECODABLE_PDF)
  }

  @Test
  fun `a pdf whose thumbnail decodes keeps the image path`() = runComposeUiTest {
    val outcome = RecordingListener()
    setChatBubble(MimeType.PDF, DECODABLE_PNG, outcome)

    waitUntil(timeoutMillis = 10_000) { outcome.success != null || outcome.error != null }
    waitForIdle()
    assertThat(outcome.error).isNull()
    assertThat(outcome.success).isNotNull()
    onNodeWithText(fileCardText, useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun `an image whose request fails is not turned into a file card`() = runComposeUiTest {
    val outcome = RecordingListener()
    setChatBubble(MimeType.IMAGE, UNDECODABLE_IMAGE, outcome)

    waitUntil(timeoutMillis = 10_000) { outcome.error != null }
    waitForIdle()
    onNodeWithText(fileCardText, useUnmergedTree = true).assertDoesNotExist()
  }

  private fun ComposeUiTest.setChatBubble(
    mimeType: MimeType,
    url: String,
    listener: EventListener,
    openUrl: (String) -> Unit = {},
  ) {
    val imageLoader = ImageLoader
      .Builder(PlatformContext.INSTANCE)
      .memoryCache(null)
      .diskCache(null)
      .eventListener(listener)
      .build()
    val message = ChatMessageFile(
      id = "message-id",
      sender = Sender.HEDVIG,
      sentAt = Instant.fromEpochMilliseconds(0),
      banner = null,
      url = url,
      mimeType = mimeType,
    )
    setContent {
      HedvigTheme(darkTheme = false) {
        ChatBubble(
          uiChatMessage = CbmUiChatMessage(message, isLastDeliveredMessage = false),
          chatItemIndex = 0,
          enableMessageSenderLabeling = false,
          imageLoader = imageLoader,
          enableInlineMediaPlayer = false,
          simpleVideoCache = ChatSimpleVideoCache(),
          videoPlayers = ChatVideoPlayers(),
          openUrl = openUrl,
          onNavigateToImageViewer = { _, _ -> },
          onRetrySendChatMessage = {},
          onGoFullWidth = {},
          onGoDefaultWidth = {},
          showingFullWidth = false,
        )
      }
    }
  }

  private class RecordingListener : EventListener() {
    @Volatile var success: SuccessResult? = null

    @Volatile var error: ErrorResult? = null

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
      success = result
    }

    override fun onError(request: ImageRequest, result: ErrorResult) {
      error = result
    }
  }

  private companion object {
    const val UNDECODABLE_PDF = "data:application/pdf;base64,JVBERi0xLjQKJcOkw7zDtsOfCg=="
    const val UNDECODABLE_IMAGE = "data:image/png;base64,bm90IGFuIGltYWdl"
    const val DECODABLE_PNG =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
  }
}
