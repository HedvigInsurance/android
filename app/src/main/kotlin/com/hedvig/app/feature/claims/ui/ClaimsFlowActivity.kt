package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hedvig.android.language.LanguageService
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.util.extensions.startChat
import com.hedvig.common.remote.actions.CHAT_URL
import com.hedvig.common.remote.actions.CLOSE_URL
import com.hedvig.common.ui.OdysseyRoot
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val odysseyUrl get() = getString(R.string.ODYSSEY_URL)
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val token = authenticationTokenService.authenticationToken
    requireNotNull(token)

    setContent {
      OdysseyRoot(
        apiUrl = odysseyUrl,
        authorizationToken = token,
        locale = languageService.getLocale().toString(),
        initialUrl = ROOT_URL,
        onExternalNavigation = { url ->
          when (url) {
            CLOSE_URL -> finish()
            CHAT_URL -> startChat()
          }
        },
      )
    }
  }

  companion object {
    private const val ROOT_URL = "/audio-claim"

    fun newInstance(context: Context) = Intent(context, ClaimsFlowActivity::class.java)
  }
}
