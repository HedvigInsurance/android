package com.hedvig.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import com.hedvig.app.service.DynamicLink
import com.hedvig.app.service.getDynamicLinkFromFirebase
import com.hedvig.app.service.startActivity
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val authTokenService: AuthTokenService by inject()
  private val marketManager: MarketManager by inject()
  private val featureManager: FeatureManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Prevents this activity from showing. Keeps splash screen on forever until we navigate away
    installSplashScreen().setKeepOnScreenCondition { true }
    super.onCreate(savedInstanceState)
    languageService.performOnLaunchLanguageCheck()
    val isBringingToFront = intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0
    val resetTaskIfNeeded = intent.flags and Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED != 0
    if (isBringingToFront && resetTaskIfNeeded) {
      // We've started the app from the home screen after it was put there by pressing the home button, and not going
      // back from the start destination. In this case, do not override the existing backstack.
      // And since `MainActivity` is added at the top of the backstack, we need to simply pop it out and we're good.
      // This fixes the bug in DK where exiting the app, entering the auth info, and clicking on the app in the home
      // screen to come back to the app fails by navigating to the marketing screen again, and failing authentication.
      onBackPressedDispatcher.onBackPressed()
    } else {
      getLoginStatusAndNavigate(intent)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    getLoginStatusAndNavigate(intent)
  }

  private fun getLoginStatusAndNavigate(intent: Intent) {
    lifecycleScope.launch {
      val authStatusAsync = async { authTokenService.authStatus.filterNotNull().first() }
      val mustForceUpdate = async { featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY) }
      val dynamicLinkAsync = async { getDynamicLinkFromFirebase(intent) }
      if (mustForceUpdate.await()) {
        applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
        finish()
        return@launch
      }
      val dynamicLink = dynamicLinkAsync.await()
      when (dynamicLink) {
        DynamicLink.None, DynamicLink.Unknown -> {}
        else -> {
          hAnalytics.deepLinkOpened(dynamicLink.type)
        }
      }
      dynamicLink.startActivity(
        context = this@MainActivity,
        marketManager = marketManager,
        featureManager = featureManager,
        onDefault = { startDefaultActivity(authStatusAsync.await()) },
      )
      finish()
    }
  }

  private fun startDefaultActivity(authStatus: AuthStatus) = when (authStatus) {
    AuthStatus.LoggedOut -> startActivity(MarketingActivity.newInstance(this))
    is AuthStatus.LoggedIn -> {
      // Upcast everyone that were logged in before Norway launch to be in the Swedish market
      if (marketManager.market == null) {
        marketManager.market = Market.SE
      }
      startActivity(Intent(this, LoggedInActivity::class.java))
    }
  }
}
