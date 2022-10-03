package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.util.extensions.startChat
import com.hedvig.common.nav.OdysseyNavigationComposable
import com.hedvig.common.remote.actions.CHAT_URL
import com.hedvig.common.remote.actions.CLOSE_URL
import com.hedvig.common.ui.OdysseyTheme
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val odysseyUrl get() = getString(R.string.ODYSSEY_URL)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val token = authenticationTokenService.authenticationToken
    requireNotNull(token)

    setContent {
      OdysseyTheme {
        OdysseyNavigationComposable(
          apiUrl = odysseyUrl,
          authorizationToken = token,
          context = this,
          navigationRootUrl = ROOT_URL,
          onExternalNavigation = { url ->
            when (url) {
              CLOSE_URL -> finish()
              CHAT_URL -> startChat()
            }
          },
        )
      }
    }
  }

  companion object {
    private const val ROOT_URL = "/audio-claim"

    fun newInstance(context: Context) = Intent(context, ClaimsFlowActivity::class.java)
  }
}
