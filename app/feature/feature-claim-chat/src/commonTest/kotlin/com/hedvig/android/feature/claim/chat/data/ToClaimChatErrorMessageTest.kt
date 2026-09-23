package com.hedvig.android.feature.claim.chat.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import kotlin.test.Test
import kotlinx.io.IOException

class ToClaimChatErrorMessageTest {
  @Test
  fun `a transport failure is retryable`() {
    assertThat(errorMessage(IOException("Socket timeout has expired")).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.ConnectionError)
  }

  @Test
  fun `a failure with no throwable is a plain error`() {
    assertThat(errorMessage(null).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.GeneralError)
  }

  @Test
  fun `a non-IO failure is a plain error`() {
    assertThat(errorMessage(IllegalStateException("boom")).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.GeneralError)
  }

  @Test
  fun `an unreadable local file is not offered as a connection problem`() {
    val unreadable = LocalFileUnreadableException("photo.jpg", IOException("No such file"))

    assertThat(errorMessage(unreadable).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.GeneralError)
  }

  @Test
  fun `an unreadable local file is found even once the engine has wrapped it`() {
    val wrapped = IOException(
      "unexpected end of stream",
      LocalFileUnreadableException("photo.jpg", IOException("No such file")),
    )

    assertThat(errorMessage(wrapped).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.GeneralError)
  }

  @Test
  fun `a cyclic cause chain terminates`() {
    assertThat(errorMessage(SelfCausingException()).toClaimChatErrorMessage())
      .isEqualTo(ClaimChatErrorMessage.ConnectionError)
  }
}

private class SelfCausingException : IOException("loops back on itself") {
  override val cause: Throwable get() = this
}

private fun errorMessage(throwable: Throwable?): ErrorMessage = object : ErrorMessage {
  override val message: String? = throwable?.message
  override val throwable: Throwable? = throwable
}
