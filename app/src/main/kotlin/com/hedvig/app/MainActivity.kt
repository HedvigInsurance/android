package com.hedvig.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import com.hedvig.app.service.DynamicLink
import com.hedvig.app.service.getDynamicLinkFromFirebase
import com.hedvig.app.service.startActivity
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val loggedInService: LoginStatusService by inject()
  private val marketManager: MarketManager by inject()
  private val featureManager: FeatureManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Prevents this activity from showing. Keeps splash screen on forever until we navigate away
    installSplashScreen().setKeepOnScreenCondition { true }
    super.onCreate(savedInstanceState)
    languageService.performOnLaunchLanguageCheck()
    getLoginStatusAndNavigate(intent)
  }

  @SuppressLint("MissingSuperCall")
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    getLoginStatusAndNavigate(intent)
  }

  private fun getLoginStatusAndNavigate(intent: Intent) {
    lifecycleScope.launch {
      val loginStatusAsync = async { loggedInService.getLoginStatus() }
      val mustForceUpdate = async { featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY) }
      if (mustForceUpdate.await()) {
        applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
        return@launch
      }
      when (val loginStatus = loginStatusAsync.await()) {
        LoginStatus.Onboarding,
        LoginStatus.LoggedIn,
        -> {
          val dynamicLink = getDynamicLinkFromFirebase(intent)
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
            onDefault = { startDefaultActivity(loginStatus) },
          )
        }

        is LoginStatus.InOffer -> startDefaultActivity(loginStatus)
      }
      finish()
    }
  }

  private fun startDefaultActivity(loginStatus: LoginStatus) = when (loginStatus) {
    LoginStatus.Onboarding -> startActivity(MarketingActivity.newInstance(this))
    is LoginStatus.InOffer -> {
      if (marketManager.market == null) {
        marketManager.market = Market.SE
      }
      startActivity(
        OfferActivity.newInstance(
          context = this,
          quoteCartId = loginStatus.quoteCartId,
        ),
      )
    }

    LoginStatus.LoggedIn -> {
      // Upcast everyone that were logged in before Norway launch to be in the Swedish market
      if (marketManager.market == null) {
        marketManager.market = Market.SE
      }
      startActivity(Intent(this, LoggedInActivity::class.java))
    }
  }
}
