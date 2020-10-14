package com.hedvig.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.service.LoginStatus
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.extensions.avdDoOnEnd
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.getMarket
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashActivity : BaseActivity(R.layout.activity_splash) {
    private val loggedInService: LoginStatusService by inject()
    private val marketProvider: MarketProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root.setEdgeToEdgeSystemUiFlags(true)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            val response = loggedInService.getLoginStatus()
            navigateToActivity(response)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFirebaseDynamicLink(intent, null)
    }

    private fun handleFirebaseDynamicLink(intent: Intent, loginStatus: LoginStatus?) {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                if (pendingDynamicLinkData != null && pendingDynamicLinkData.link != null) {
                    val link = pendingDynamicLinkData.link
                    when (link?.pathSegments?.getOrNull(0)) {
                        "referrals" -> handleReferralsDeepLink(link, loginStatus)
                        "direct-debit" -> handleDirectDebitDeepLink(loginStatus)
                        "forever" -> handleForeverDeepLink(loginStatus)
                        else -> startDefaultActivity(loginStatus)
                    }
                } else {
                    startDefaultActivity(loginStatus)
                }
            }.addOnFailureListener {
                startDefaultActivity(loginStatus)
            }
    }

    private fun handleForeverDeepLink(loginStatus: LoginStatus?) {
        if (loginStatus != LoginStatus.LOGGED_IN) {
            startDefaultActivity(loginStatus)
            return
        }

        runSplashAnimation {
            startActivity(LoggedInActivity.newInstance(this, initialTab = LoggedInTabs.REFERRALS))
        }
    }

    private fun handleDirectDebitDeepLink(loginStatus: LoginStatus?) {
        if (loginStatus != LoginStatus.LOGGED_IN) {
            startDefaultActivity(loginStatus)
            return
        }

        runSplashAnimation {
            marketProvider.market?.connectPayin(this)?.let { connectPayinIntent ->
                startActivities(
                    arrayOf(
                        Intent(this, LoggedInActivity::class.java),
                        connectPayinIntent
                    )
                )
            }
        }
    }

    private fun handleReferralsDeepLink(link: Uri, loginStatus: LoginStatus?) {
        if (loginStatus != LoginStatus.ONBOARDING) {
            startDefaultActivity(loginStatus)
            return
        }
        when (getMarket()) {
            null -> {
                runSplashAnimation {
                    startActivity(MarketingActivity.newInstance(this))
                }
            }
            Market.SE -> {
                link.getQueryParameter("code")?.let { referralCode ->
                    runSplashAnimation {
                        startActivity(
                            ReferralsReceiverActivity.newInstance(
                                this,
                                referralCode,
                                "10"
                            )
                        ) //Fixme "10" should not be hard coded
                    }

                } ?: startDefaultActivity(loginStatus)
            }
            else -> {
                runSplashAnimation {
                    startActivity(Intent(this, MarketingActivity::class.java))
                }
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun startDefaultActivity(loginStatus: LoginStatus?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val market = sharedPreferences.getString(Market.MARKET_SHARED_PREF, null)
        when (loginStatus) {
            LoginStatus.ONBOARDING -> {
                if (market == null) {
                    runSplashAnimation {
                        startActivity(MarketingActivity.newInstance(this))
                    }
                } else {
                    runSplashAnimation {
                        startActivity(MarketingActivity.newInstance(this))
                    }
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
            else -> {
                CoroutineScope(IO).launch {
                    val response = loggedInService.getLoginStatus()
                    navigateToActivity(response)
                }
            }
        }
    }

    private fun navigateToActivity(loginStatus: LoginStatus) = when (loginStatus) {
        LoginStatus.ONBOARDING, LoginStatus.LOGGED_IN -> {
            handleFirebaseDynamicLink(intent, loginStatus)
        }
        else -> startDefaultActivity(loginStatus)
    }

    private inline fun runSplashAnimation(crossinline andThen: () -> Unit) {
        splashAnimation.avdDoOnEnd { andThen() }
        splashAnimation.avdStart()
    }
}
