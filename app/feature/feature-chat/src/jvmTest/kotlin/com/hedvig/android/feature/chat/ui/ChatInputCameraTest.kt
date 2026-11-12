package com.hedvig.android.feature.chat.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.Res
import hedvig.resources.TALKBACK_ADD_DOCUMENTS
import hedvig.resources.TALKBACK_ADD_IMAGES
import hedvig.resources.TALKBACK_TAKE_PHOTO
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ChatInputCameraTest {
  private val takePhoto = runBlocking { getString(Res.string.TALKBACK_TAKE_PHOTO) }
  private val addImages = runBlocking { getString(Res.string.TALKBACK_ADD_IMAGES) }
  private val addDocuments = runBlocking { getString(Res.string.TALKBACK_ADD_DOCUMENTS) }

  @Test
  fun `the expanded options offer no camera button when the platform cannot take photos`() = runComposeUiTest {
    setContent {
      HedvigTheme(darkTheme = false) {
        ChatInput(
          onSendMessage = {},
          onSendPhoto = {},
          onSendMedia = {},
          appPackageId = "test",
          showUploading = false,
        )
      }
    }

    onNodeWithContentDescription(addImages).assertExists()
    onNodeWithContentDescription(addDocuments).assertExists()
    onNodeWithContentDescription(takePhoto).assertDoesNotExist()
  }
}
