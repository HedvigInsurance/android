package com.hedvig.android.auth.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.navigation.Navigator
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class AuthenticatedActivity : AppCompatActivity() {
  private val authTokenService: AuthTokenService by inject()
  private val navigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        authTokenService.authStatus
          .filterIsInstance<AuthStatus.LoggedOut>()
          .first()
        navigator.navigateToMarketingActivity(this@AuthenticatedActivity)
      }
    }
  }
}
