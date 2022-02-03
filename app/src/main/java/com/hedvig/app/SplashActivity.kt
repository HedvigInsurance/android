package com.hedvig.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.databinding.ActivitySplashBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.getDynamicLinkFromFirebase
import com.hedvig.app.service.startActivity
import com.hedvig.app.util.extensions.avdDoOnEnd
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity(R.layout.activity_splash) {
    private val loggedInService: LoginStatusService by inject()
    private val marketManager: MarketManager by inject()
    private val binding by viewBinding(ActivitySplashBinding::bind)
    private val model: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            when (val loginStatus = loggedInService.getLoginStatus()) {
                LoginStatus.ONBOARDING,
                LoginStatus.LOGGED_IN -> {
                    val dynamicLink = getDynamicLinkFromFirebase(intent)
                    model.onDynamicLinkOpened(dynamicLink)
                    dynamicLink.startActivity(
                        context = this@SplashActivity,
                        marketManager = marketManager,
                        onDefault = { startDefaultActivity(loginStatus) }
                    )
                }
                LoginStatus.IN_OFFER -> startDefaultActivity(loginStatus)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun startDefaultActivity(loginStatus: LoginStatus) = when (loginStatus) {
        LoginStatus.ONBOARDING -> {
            runSplashAnimation {
                startActivity(MarketingActivity.newInstance(this))
            }
        }
        LoginStatus.IN_OFFER -> {
            if (marketManager.market == null) {
                marketManager.market = Market.SE
            }
            runSplashAnimation {
                startActivity(Intent(this, OfferActivity::class.java))
            }
        }
        LoginStatus.LOGGED_IN -> {
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
