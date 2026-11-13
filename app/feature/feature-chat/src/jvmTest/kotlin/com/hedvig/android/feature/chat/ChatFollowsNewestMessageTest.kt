package com.hedvig.android.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.ImageLoader
import coil3.PlatformContext
import com.benasher44.uuid.Uuid
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.LatestChatMessage
import com.hedvig.android.feature.chat.data.ConversationInfo.Info
import com.hedvig.android.feature.chat.model.CbmChatMessage
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.ui.rememberPreviewChatVideoCache
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The chat list sits at the newest message. A message arrives, and the list must show it at the bottom.
 *
 * [CbmChatViewModel] stops its presenter five seconds after the screen stops observing it, and a restarted presenter
 * hands the screen a new [LazyPagingItems]. These tests stand in for the presenter with a [key] around
 * [collectAsLazyPagingItems], so bumping `presenterRun` does what a restart does.
 */
@OptIn(ExperimentalTestApi::class)
class ChatFollowsNewestMessageTest {
  @Before
  fun setUp() {
    // Paging delivers loaded items on Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `a message arriving while the presenter keeps running is scrolled to`() = runComposeUiTest {
    val chat = FakeChat()
    setContent { chat.Screen() }
    waitUntil(timeoutMillis = 10_000) {
      onAllNodesWithText(chat.textOf(HISTORY_SIZE)).fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithText(chat.textOf(HISTORY_SIZE)).assertIsDisplayed()

    chat.receive(HISTORY_SIZE + 1)
    mainClock.advanceTimeBy(2_000)
    waitForIdle()

    onNodeWithText(chat.textOf(HISTORY_SIZE + 1)).assertIsDisplayed()
  }

  @Test
  fun `a message arriving after the presenter restarted is scrolled to`() = runComposeUiTest {
    val chat = FakeChat()
    setContent { chat.Screen() }
    waitUntil(timeoutMillis = 10_000) {
      onAllNodesWithText(chat.textOf(HISTORY_SIZE)).fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithText(chat.textOf(HISTORY_SIZE)).assertIsDisplayed()

    chat.restartPresenter()
    waitForIdle()
    chat.latest = chat.latestOf(HISTORY_SIZE)
    waitForIdle()
    onNodeWithText(chat.textOf(HISTORY_SIZE)).assertIsDisplayed()

    chat.receive(HISTORY_SIZE + 1)
    mainClock.advanceTimeBy(2_000)
    waitForIdle()

    onNodeWithText(chat.textOf(HISTORY_SIZE + 1)).assertIsDisplayed()
  }

  private class FakeChat {
    private val pagingData = MutableStateFlow(PagingData.from(messagesUpTo(HISTORY_SIZE)))
    private var presenterRun by mutableIntStateOf(0)
    var latest: LatestChatMessage? by mutableStateOf(latestOf(HISTORY_SIZE))

    /** A restarted presenter starts with no latest message until its database query answers. */
    fun restartPresenter() {
      latest = null
      presenterRun++
    }

    fun receive(newest: Int) {
      pagingData.value = PagingData.from(messagesUpTo(newest))
      latest = latestOf(newest)
    }

    fun textOf(index: Int) = "Message $index"

    fun latestOf(index: Int) = LatestChatMessage(idOf(index), Sender.HEDVIG)

    @androidx.compose.runtime.Composable
    fun Screen() {
      HedvigTheme(darkTheme = false) {
        val messages = key(presenterRun) {
          // Like the presenter's combined flow, this is not a SharedFlow, so each run starts empty and then loads.
          remember { pagingData.map { it } }.collectAsLazyPagingItems()
        }
        val imageLoader = remember { ImageLoader.Builder(PlatformContext.INSTANCE).build() }
        CbmChatLoadedScreen(
          uiState = loaded(messages, latest),
          imageLoader = imageLoader,
          simpleVideoCache = rememberPreviewChatVideoCache(),
          appPackageId = "",
          openUrl = {},
          onNavigateToImageViewer = { _, _ -> },
          onRetrySendChatMessage = {},
          onSendMessage = {},
          onSendPhoto = {},
          onSendMedia = {},
          onCloseBannerClick = {},
          onCrossSellShown = { _, _ -> },
          onDismissCrossSellClick = {},
          onCrossSellClick = {},
          errorSnackbarState = null,
        )
      }
    }

    private fun messagesUpTo(newest: Int): List<CbmUiChatMessage> = (newest downTo 1).map { index ->
      CbmUiChatMessage(
        CbmChatMessage.ChatMessageText(
          id = idOf(index).toString(),
          sender = Sender.HEDVIG,
          sentAt = Instant.fromEpochSeconds(index * 60L),
          banner = null,
          text = textOf(index),
          action = null,
        ),
        isLastDeliveredMessage = false,
      )
    }

    private fun idOf(index: Int): Uuid = Uuid.fromString("00000000-0000-0000-0000-%012d".format(index))

    private fun loaded(messages: LazyPagingItems<CbmUiChatMessage>, latest: LatestChatMessage?) = Loaded(
      backendConversationInfo = Info("conversation", null, Instant.fromEpochSeconds(0), false),
      messages = messages,
      latestMessage = latest,
      bannerText = null,
      enableMessageSenderLabeling = false,
      enableInlineMediaPlayer = false,
      showUploading = false,
      showFileTooBigErrorToast = false,
      showFileFailedToBeSentToast = false,
      crossSell = null,
    )
  }

  private companion object {
    const val HISTORY_SIZE = 40
  }
}
