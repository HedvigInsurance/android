package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.ImageLoader
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.navigation.Navigator
import com.hedvig.odyssey.datadog.DatadogProvider
import com.hedvig.odyssey.remote.actions.CHAT_URL
import com.hedvig.odyssey.remote.actions.CLOSE_URL
import com.hedvig.odyssey.remote.scopes.ScopeValues
import com.hedvig.odyssey.remote.scopes.keys.CommonClaimIdScopeValueKey
import com.hedvig.odyssey.ui.OdysseyRoot
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val accessTokenProvider: AccessTokenProvider by inject()
  private val datadogProvider: DatadogProvider by inject()
  private val imageLoader: ImageLoader by inject()
  private val navigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    val odysseyUrl = intent.getStringExtra(ODYSSEY_URL_KEY) ?: error("ODYSSEY_URL_KEY needs to be passed in")
    val commonClaimId = intent.getStringExtra(COMMON_CLAIM_ID)

    val scopeValues = ScopeValues()
    scopeValues.setValue(
      CommonClaimIdScopeValueKey,
      commonClaimId,
    )

    setContent {
      OdysseyRoot(
        apiUrl = odysseyUrl,
        accessTokenProvider = object : com.hedvig.odyssey.auth.AccessTokenProvider {
          override suspend fun provide(): String? {
            return accessTokenProvider.provide()
          }
        },
        datadogProvider = datadogProvider,
        imageLoader = imageLoader,
        initialUrl = ROOT_URL,
        scopeValues = scopeValues,
        onExternalNavigation = ::onExternalNavigation,
      )
    }
  }

  private fun onExternalNavigation(url: String) {
    when (url) {
      CLOSE_URL -> finish()
      CHAT_URL -> {
        finish()
        navigator.navigateToChat(this@ClaimsFlowActivity)
      }
    }
  }

  companion object {
    private const val ROOT_URL = "/automation-claim"
    private const val ODYSSEY_URL_KEY = "com.hedvig.android.odyssey.ODYSSEY_URL_KEY"
    private const val COMMON_CLAIM_ID = "COMMON_CLAIM_ID"

    fun newInstance(
      context: Context,
      odysseyUrl: String,
      commonClaimId: String?,
    ): Intent {
      return Intent(context, ClaimsFlowActivity::class.java)
        .putExtra(ODYSSEY_URL_KEY, odysseyUrl)
        .putExtra(COMMON_CLAIM_ID, commonClaimId)
    }
  }
}
