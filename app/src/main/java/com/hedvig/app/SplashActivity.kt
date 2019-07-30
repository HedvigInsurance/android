package com.hedvig.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.offer.OfferActivity
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.service.LoginStatus
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.whenApiVersion
import com.hedvig.app.viewmodel.AnalyticsViewModel
import io.branch.referral.Branch
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashActivity : BaseActivity() {

    private val analyticsViewModel: AnalyticsViewModel by viewModel()
    private val loggedInService: LoginStatusService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        whenApiVersion(Build.VERSION_CODES.M) {
            window.statusBarColor = compatColor(R.color.off_white)
        }
    }

    override fun onStart() {
        super.onStart()

        // Branch init
        Branch.getInstance().initSession({ referringParams, error ->
            error?.let { e ->
                Timber.e("BRANCH SDK ${e.message} code ${e.errorCode}")
            } ?: analyticsViewModel.handleBranchReferringParams(referringParams)
        }, this.intent.data, this)

        disposables += loggedInService
            .getLoginStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ navigateToActivity(it) }, { Timber.e(it) })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //Don't ask me why remove this when we remove branch
        this.intent = intent
        handleFirebaseDynamicLink(intent)
    }

    private fun handleFirebaseDynamicLink(intent: Intent) {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener { pendingDynamicLinkData ->
            if (pendingDynamicLinkData != null && pendingDynamicLinkData.link != null) {
                val link = pendingDynamicLinkData.link
                val referee = link.getQueryParameter("memberId")
                val incentive = link.getQueryParameter("incentive")
                if (referee != null && incentive != null) {
                    getSharedPreferences("referrals", Context.MODE_PRIVATE).edit().putString("referee", referee)
                        .putString("incentive", incentive).apply()

                    val b = Bundle()
                    b.putString("invitedByMemberId", referee)
                    b.putString("incentive", incentive)

                    FirebaseAnalytics.getInstance(this).logEvent("referrals_open", b)
                }

                link.getQueryParameter("code")?.let { referralCode ->
                    startActivity(
                        ReferralsReceiverActivity.newInstance(
                            this,
                            referralCode,
                            "10"
                        )
                    ) //Fixme "10" should not be hard coded
                } ?: run {
                    startActivity(Intent(this, MarketingActivity::class.java))
                }
            } else {
                startActivity(Intent(this, MarketingActivity::class.java))
            }
        }.addOnFailureListener {
            startActivity(Intent(this, MarketingActivity::class.java))
        }
    }

    private fun navigateToActivity(loginStatus: LoginStatus) = when (loginStatus) {
        LoginStatus.ONBOARDING -> {
            handleFirebaseDynamicLink(intent)
        }
        LoginStatus.IN_OFFER -> {
            val intent = Intent(this, OfferActivity::class.java)
            startActivity(intent)
        }
        LoginStatus.LOGGED_IN -> startActivity(Intent(this, LoggedInActivity::class.java))
        LoginStatus.LOGGED_IN_TERMINATED -> startActivity(Intent(this, LoggedInTerminatedActivity::class.java))
    }
}
