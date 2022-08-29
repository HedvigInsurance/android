package com.hedvig.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.databinding.ActivitySplashBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import com.hedvig.app.service.getDynamicLinkFromFirebase
import com.hedvig.app.service.startActivity
import com.hedvig.app.util.extensions.avdDoOnEnd
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity(R.layout.activity_splash) {
  private val loggedInService: LoginStatusService by inject()
  private val marketManager: MarketManager by inject()
  private val featureManager: FeatureManager by inject()
  private val binding by viewBinding(ActivitySplashBinding::bind)
  private val viewModel: SplashViewModel by viewModel()
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    languageService.performOneTimeLanguageMigration()

    window.compatSetDecorFitsSystemWindows(false)
  }

  override fun onStart() {
    super.onStart()
    getLoginStatusAndNavigate(intent)
  }

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
          viewModel.onDynamicLinkOpened(dynamicLink)
          dynamicLink.startActivity(
            context = this@SplashActivity,
            marketManager = marketManager,
            featureManager = featureManager,
            onDefault = { startDefaultActivity(loginStatus) },
          )
        }
        is LoginStatus.InOffer -> startDefaultActivity(loginStatus)
      }
    }
  }

  @SuppressLint("ApplySharedPref")
  private fun startDefaultActivity(loginStatus: LoginStatus) = when (loginStatus) {
    LoginStatus.Onboarding -> {
      runSplashAnimation {
        startActivity(MarketingActivity.newInstance(this))
      }
    }
    is LoginStatus.InOffer -> {
      if (marketManager.market == null) {
        marketManager.market = Market.SE
      }
      runSplashAnimation {
        startActivity(
          OfferActivity.newInstance(
            context = this,
            quoteCartId = loginStatus.quoteCartId,
            shouldShowOnNextAppStart = true,
          ),
        )
      }
    }
    LoginStatus.LoggedIn -> {
      // Upcast everyone that were logged in before Norway launch to be in the Swedish market
      if (marketManager.market == null) {
        marketManager.market = Market.SE
      }
      runSplashAnimation {
        startActivity(Intent(this, LoggedInActivity::class.java))
      }
    }
  }

  private inline fun runSplashAnimation(crossinline andThen: () -> Unit) {
    binding.splashAnimation.avdDoOnEnd { andThen() }
    binding.splashAnimation.avdStart()
  }
}
