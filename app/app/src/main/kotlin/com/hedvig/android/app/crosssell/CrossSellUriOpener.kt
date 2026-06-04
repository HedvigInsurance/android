package com.hedvig.android.app.crosssell

import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Opens a cross-sell URL by first appending the member's authorization code (when available) so the
 * web flow can resume the session, then delegating to [uriHandler]. Kept separate from
 * [com.hedvig.android.app.urihandler.DeepLinkFirstUriHandler] because appending an auth code is a
 * cross-sell concern, not part of the [UriHandler] contract.
 */
internal class CrossSellUriOpener(
  private val getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  private val uriHandler: UriHandler,
  private val scope: CoroutineScope,
) {
  fun open(url: String) {
    scope.launch {
      val code = getMemberAuthorizationCodeUseCase.invoke()
      val finalUrl = if (code != null) {
        url.toUri().buildUpon().appendQueryParameter("authorization_code", code).build().toString()
      } else {
        url
      }
      uriHandler.openUri(finalUrl)
    }
  }
}
