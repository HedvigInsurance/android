package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A [UriHandler] decorator that appends the member's authorization code to URLs which opt in via the
 * `requiresAuthorization=true` query param, so the web flow can resume the member's session. URLs without the param
 * are passed straight through to [delegate] untouched.
 *
 * The param-driven contract mirrors iOS: the backend tags links that need a session (cross-sells, conversation links)
 * with `requiresAuthorization=true`, and the client decides at open-time whether to fetch and append the code. Because
 * [UriHandler.openUri] is synchronous but fetching the code is suspending, the fetch is launched on [scope].
 *
 * While that fetch runs nothing is on screen yet, so a second tap on the same link would open it twice. [tryOpenUri]
 * therefore launches each URL once at a time and tells the caller whether this call was the one that launched it,
 * which is what a click that should be counted once needs to know. All calls happen on the main thread, where
 * [scope] also completes the fetch, so the in-flight set needs no locking.
 */
internal class AuthorizationCodeUriHandler(
  private val getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  private val delegate: UriHandler,
  private val scope: CoroutineScope,
) : UriHandler {
  private val inFlight = mutableSetOf<String>()

  override fun openUri(uri: String) {
    tryOpenUri(uri)
  }

  /**
   * Opens [uri], unless a launch of that same [uri] is still waiting for its authorization code. Returns true when
   * this call is the one that opens it, false when it was dropped as a repeat.
   */
  fun tryOpenUri(uri: String): Boolean {
    if (!uri.requiresAuthorization()) {
      delegate.openUri(uri)
      return true
    }
    if (!inFlight.add(uri)) return false
    scope.launch {
      try {
        val code = getMemberAuthorizationCodeUseCase.invoke()
        val finalUrl = if (code != null) {
          uri.toUri().buildUpon().appendQueryParameter("authorization_code", code).build().toString()
        } else {
          uri
        }
        delegate.openUri(finalUrl)
      } finally {
        inFlight.remove(uri)
      }
    }
    return true
  }
}

private fun String.requiresAuthorization(): Boolean = toUri().getQueryParameter("requiresAuthorization") == "true"
