package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tries to resolve a URI to an in-app [com.hedvig.android.navigation.common.HedvigNavKey] via [matcher] and navigate to
 * it via [backstackController]. Falls back to the [delegate] (e.g. the system browser) when the URI is not one of our
 * deep links.
 */
internal class DeepLinkFirstUriHandler(
  private val matcher: HedvigDeepLinkMatcher,
  private val backstackController: BackstackController,
  private val delegate: UriHandler,
  private val getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  private val scope: CoroutineScope,
) : UriHandler {
  override fun openUri(uri: String) {
    val destination = matcher.match(uri)
    if (destination != null) {
      logcat { "DeepLinkFirstUriHandler navigating to $destination for uri:$uri" }
      backstackController.navigateToDeepLink(destination)
    } else {
      logcat { "DeepLinkFirstUriHandler falling back to default UriHandler for uri:$uri" }
      delegate.openUri(uri)
    }
  }

  fun openCrossSellUri(url: String) {
    scope.launch {
      val code = getMemberAuthorizationCodeUseCase.invoke()
      val finalUrl = if (code != null) {
        url.toUri().buildUpon().appendQueryParameter("authorization_code", code).build().toString()
      } else {
        url
      }
      openUri(finalUrl)
    }
  }
}
