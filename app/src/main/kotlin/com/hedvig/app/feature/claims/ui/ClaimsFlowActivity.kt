package com.hedvig.app.feature.claims.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.common.nav.OdysseyNavigationComposable
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val authenticationTokenService: AuthenticationTokenService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val token = authenticationTokenService.authenticationToken
    requireNotNull(token)

    setContent {
      HedvigTheme {
        OdysseyNavigationComposable(
          apiUrl = API_URL,
          authorizationToken = token,
          context = this,
          navigationRootUrl = ROOT_URL,
        )
      }
    }
  }

  companion object {
    private const val API_URL = "http://10.0.2.2:9000"
    private const val ROOT_URL = "/audio-claim"

    fun newInstance(context: Context) = Intent(context, ClaimsFlowActivity::class.java)
  }
}
