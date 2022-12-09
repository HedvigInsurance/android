package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.ImageLoader
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.Navigator
import com.hedvig.common.remote.actions.CHAT_URL
import com.hedvig.common.remote.actions.CLOSE_URL
import com.hedvig.common.remote.scopes.ScopeValues
import com.hedvig.common.remote.scopes.keys.InitialDataScopeValueKey
import com.hedvig.common.ui.OdysseyRoot
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject

class ClaimsFlowActivity : ComponentActivity() {

  private val authTokenService: AuthTokenService by inject()
  private val languageService: LanguageService by inject()
  private val imageLoader: ImageLoader by inject()
  private val navigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val token = authTokenService.getToken()?.token
    requireNotNull(token)
    val odysseyUrl = intent.getStringExtra(ODYSSEY_URL_KEY) ?: error("ODYSSEY_URL_KEY needs to be passed in")
    val itemType = intent.getParcelableExtra<ItemType>(EXTRA_ITEM_TYPE)?.name

    val locale = languageService.getLocale().toString()

    val scopeValues = ScopeValues()
    if (itemType != null) {
      scopeValues.setValue(InitialDataScopeValueKey, mapOf("itemType" to itemType))
    }

    setContent {
      OdysseyRoot(
        apiUrl = odysseyUrl,
        authorizationToken = token,
        locale = locale,
        imageLoader = imageLoader,
        initialUrl = ROOT_URL,
        onExternalNavigation = ::onExternalNavigation,
        scopeValues = scopeValues,
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
    private const val EXTRA_ITEM_TYPE = "EXTRA_ITEM_TYPE"

    fun newInstance(
      context: Context,
      odysseyUrl: String,
      itemType: ItemType? = null,
    ): Intent {
      return Intent(context, ClaimsFlowActivity::class.java)
        .putExtra(ODYSSEY_URL_KEY, odysseyUrl)
        .putExtra(EXTRA_ITEM_TYPE, itemType)
    }
  }

  @Parcelize
  @JvmInline
  value class ItemType(val name: String) : Parcelable
}
