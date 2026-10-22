package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AuthorizationCodeUriHandlerTest {
  private class FakeGetMemberAuthorizationCodeUseCase : GetMemberAuthorizationCodeUseCase {
    /** The pending fetch; the test completes it to let the launch proceed. */
    var fetch = CompletableDeferred<String?>()

    override suspend fun invoke(): String? = fetch.await()
  }

  private class RecordingUriHandler : UriHandler {
    val opened = mutableListOf<String>()

    override fun openUri(uri: String) {
      opened += uri
    }
  }

  private val storeUrl =
    "https://dev.hedvigit.com/api/redirect/recommend?next=%2Fse%2Faccident&requiresAuthorization=true"

  @Test
  fun `a second tap on the same link while its code is being fetched is dropped`() = runTest {
    val useCase = FakeGetMemberAuthorizationCodeUseCase()
    val delegate = RecordingUriHandler()
    val handler = AuthorizationCodeUriHandler(useCase, delegate, backgroundScope)

    assertThat(handler.tryOpenUri(storeUrl)).isTrue()
    runCurrent()
    assertThat(handler.tryOpenUri(storeUrl)).isFalse()
    assertThat(delegate.opened).isEmpty()

    useCase.fetch.complete("code-1")
    runCurrent()

    assertThat(delegate.opened).containsExactly("$storeUrl&authorization_code=code-1")
  }

  @Test
  fun `once the link has opened, the next tap launches it again`() = runTest {
    val useCase = FakeGetMemberAuthorizationCodeUseCase()
    val delegate = RecordingUriHandler()
    val handler = AuthorizationCodeUriHandler(useCase, delegate, backgroundScope)
    handler.tryOpenUri(storeUrl)
    useCase.fetch.complete("code-1")
    runCurrent()

    useCase.fetch = CompletableDeferred()
    assertThat(handler.tryOpenUri(storeUrl)).isTrue()
    useCase.fetch.complete("code-2")
    runCurrent()

    assertThat(delegate.opened).containsExactly(
      "$storeUrl&authorization_code=code-1",
      "$storeUrl&authorization_code=code-2",
    )
  }

  @Test
  fun `a different link is not held back by one still fetching its code`() = runTest {
    val useCase = FakeGetMemberAuthorizationCodeUseCase()
    val delegate = RecordingUriHandler()
    val handler = AuthorizationCodeUriHandler(useCase, delegate, backgroundScope)
    val otherUrl = "https://dev.hedvigit.com/api/redirect/recommend?next=%2Fse%2Fpet&requiresAuthorization=true"
    handler.tryOpenUri(storeUrl)

    assertThat(handler.tryOpenUri(otherUrl)).isTrue()
    useCase.fetch.complete("code-1")
    runCurrent()

    assertThat(delegate.opened).containsExactly(
      "$storeUrl&authorization_code=code-1",
      "$otherUrl&authorization_code=code-1",
    )
  }

  @Test
  fun `a link that fails to get a code still opens, once, and frees the link for the next tap`() = runTest {
    val useCase = FakeGetMemberAuthorizationCodeUseCase()
    val delegate = RecordingUriHandler()
    val handler = AuthorizationCodeUriHandler(useCase, delegate, backgroundScope)
    handler.tryOpenUri(storeUrl)
    assertThat(handler.tryOpenUri(storeUrl)).isFalse()

    useCase.fetch.complete(null)
    runCurrent()

    assertThat(delegate.opened).containsExactly(storeUrl)
    useCase.fetch = CompletableDeferred()
    assertThat(handler.tryOpenUri(storeUrl)).isTrue()
  }

  @Test
  fun `a link without the authorization param opens at once and is never dropped`() = runTest {
    val useCase = FakeGetMemberAuthorizationCodeUseCase()
    val delegate = RecordingUriHandler()
    val handler = AuthorizationCodeUriHandler(useCase, delegate, backgroundScope)
    val plainUrl = "https://www.hedvig.com/se/forsakringar/djurforsakring"

    assertThat(handler.tryOpenUri(plainUrl)).isTrue()
    assertThat(handler.tryOpenUri(plainUrl)).isTrue()

    assertThat(delegate.opened).containsExactly(plainUrl, plainUrl)
  }
}
