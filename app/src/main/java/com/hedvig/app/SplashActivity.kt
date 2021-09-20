package com.hedvig.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.databinding.ActivitySplashBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.DynamicLinkHandler
import com.hedvig.app.util.extensions.avdDoOnEnd
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashActivity : BaseActivity(R.layout.activity_splash) {
    private val loggedInService: LoginStatusService by inject()
    private val marketManager: MarketManager by inject()
    private val binding by viewBinding(ActivitySplashBinding::bind)
    private lateinit var dynamicLinkHandler: DynamicLinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.compatSetDecorFitsSystemWindows(false)

        dynamicLinkHandler = DynamicLinkHandler(
            this,
            marketManager
        ) {
            startDefaultActivity(it)
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            val response = loggedInService.getLoginStatus()
            navigateToActivity(response, intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getLoginStatusAndNavigate(intent)
    }

    @SuppressLint("ApplySharedPref")
    private fun startDefaultActivity(loginStatus: LoginStatus?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val market = sharedPreferences.getString(Market.MARKET_SHARED_PREF, null)
        when (loginStatus) {
            LoginStatus.ONBOARDING -> {
                runSplashAnimation {
                    startActivity(MarketingActivity.newInstance(this))
                }
            }
            LoginStatus.IN_OFFER -> {
                if (market == null) {
                    sharedPreferences.edit()
                        .putString(Market.MARKET_SHARED_PREF, Market.SE.name)
                        .commit()
                }
                runSplashAnimation {
                    startActivity(Intent(this, OfferActivity::class.java))
                }
            }
            LoginStatus.LOGGED_IN -> {
                // Upcast everyone that were logged in before Norway launch to be in the Swedish market
                if (market == null) {
                    sharedPreferences.edit()
                        .putString(Market.MARKET_SHARED_PREF, Market.SE.name)
                        .commit()
                }
                runSplashAnimation {
                    startActivity(Intent(this, LoggedInActivity::class.java))
                }
            }
            else -> getLoginStatusAndNavigate(intent)
        }
    }

    private fun getLoginStatusAndNavigate(intent: Intent) {
        CoroutineScope(IO).launch {
            val response = loggedInService.getLoginStatus()
            navigateToActivity(response, intent)
        }
    }

    private fun navigateToActivity(loginStatus: LoginStatus, intent: Intent) = when (loginStatus) {
        LoginStatus.ONBOARDING, LoginStatus.LOGGED_IN -> {
            dynamicLinkHandler.handleIntent(intent, loginStatus)
        }
        else -> startDefaultActivity(loginStatus)
    }

    private inline fun runSplashAnimation(crossinline andThen: () -> Unit) {
        binding.splashAnimation.avdDoOnEnd { andThen() }
        binding.splashAnimation.avdStart()
    }
}
