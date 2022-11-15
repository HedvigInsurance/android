package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.ImageLoader
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.Navigator
import com.hedvig.common.remote.actions.CHAT_URL
import com.hedvig.common.remote.actions.CLOSE_URL
import com.hedvig.common.ui.OdysseyRoot
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val languageService: LanguageService by inject()
  private val imageLoader: ImageLoader by inject()
  private val navigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val token = authenticationTokenService.authenticationToken
    requireNotNull(token)
    val odysseyUrl = intent.getStringExtra(ODYSSEY_URL_KEY) ?: error("ODYSSEY_URL_KEY needs to be passed in")
    val locale = languageService.getLocale().toString()

    setContent {
      OdysseyRoot(
        apiUrl = odysseyUrl,
        authorizationToken = token,
        locale = locale,
        imageLoader = imageLoader,
        initialUrl = ROOT_URL,
        onExternalNavigation = { url ->
          when (url) {
            CLOSE_URL -> finish()
            CHAT_URL -> navigator.navigateToChat(this)
          }
        },
      )
    }
  }

  companion object {
    private const val ROOT_URL = "/automation-claim"
    private const val ODYSSEY_URL_KEY = "com.hedvig.android.odyssey.ODYSSEY_URL_KEY"

    fun newInstance(
      context: Context,
      odysseyUrl: String,
    ): Intent {
      return Intent(context, ClaimsFlowActivity::class.java)
        .putExtra(ODYSSEY_URL_KEY, odysseyUrl)
    }
  }
}
