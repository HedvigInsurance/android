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
 */
internal class AuthorizationCodeUriHandler(
  private val getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  private val delegate: UriHandler,
  private val scope: CoroutineScope,
) : UriHandler {
  override fun openUri(uri: String) {
    if (!uri.requiresAuthorization()) {
      delegate.openUri(uri)
      return
    }
    scope.launch {
      val code = getMemberAuthorizationCodeUseCase.invoke()
      val finalUrl = if (code != null) {
        uri.toUri().buildUpon().appendQueryParameter("authorization_code", code).build().toString()
      } else {
        uri
      }
      delegate.openUri(finalUrl)
    }
  }
}

private fun String.requiresAuthorization(): Boolean = toUri().getQueryParameter("requiresAuthorization") == "true"
