package com.hedvig.android.feature.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import coil3.EventListener
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.CHAT_FILE_DOWNLOAD
import hedvig.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ChatFileThumbnailTest {
  private val fileCardText = runBlocking { getString(Res.string.CHAT_FILE_DOWNLOAD) }

  @Test
  fun `an attachment the image loader cannot decode is shown as the file card`() = runComposeUiTest {
    val outcome = RecordingListener()
    var openedFileCard = 0
    setContent {
      HedvigTheme(darkTheme = false) {
        ChatFileThumbnail(
          url = "data:application/pdf;base64,JVBERi0xLjQKJcOkw7zDtsOfCg==",
          cacheKey = "undecodable",
          imageLoader = imageLoader(outcome),
          onThumbnailClick = {},
          onFileCardClick = { openedFileCard++ },
        )
      }
    }

    waitUntil(timeoutMillis = 10_000) { outcome.error != null }
    onNodeWithText(fileCardText).assertExists().performClick()
    assertThat(openedFileCard).isEqualTo(1)
  }

  @Test
  fun `a decodable attachment keeps the image path`() = runComposeUiTest {
    val outcome = RecordingListener()
    setContent {
      HedvigTheme(darkTheme = false) {
        ChatFileThumbnail(
          url = "data:image/png;base64,$ONE_PIXEL_PNG",
          cacheKey = "decodable",
          imageLoader = imageLoader(outcome),
          onThumbnailClick = {},
          onFileCardClick = {},
        )
      }
    }

    waitUntil(timeoutMillis = 10_000) { outcome.success != null || outcome.error != null }
    waitForIdle()
    assertThat(outcome.error).isNull()
    assertThat(outcome.success).isNotNull()
    onNodeWithText(fileCardText).assertDoesNotExist()
  }

  private fun imageLoader(listener: EventListener): ImageLoader = ImageLoader
    .Builder(PlatformContext.INSTANCE)
    .memoryCache(null)
    .diskCache(null)
    .eventListener(listener)
    .build()

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
    const val ONE_PIXEL_PNG =
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
  }
}
