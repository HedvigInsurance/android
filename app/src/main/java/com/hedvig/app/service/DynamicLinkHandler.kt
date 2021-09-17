package com.hedvig.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class DynamicLinkHandler(
    val context: Context,
    val marketManager: MarketManager,
    val startDefaultActivity: (LoginStatus?) -> Unit
) {

    enum class DynamicLink(val path: String) {
        REFERRALS("referrals"),
        DIRECT_DEBIT("direct-debit"),
        FOREVER("forever"),
        INSURANCE("insurance")
    }

    fun handleIntent(
        intent: Intent,
        loginStatus: LoginStatus?
    ) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                // This will actually be null in some cases
                if (pendingDynamicLinkData != null && pendingDynamicLinkData.link != null) {
                    val link = pendingDynamicLinkData.link
                    when (link?.pathSegments?.getOrNull(0)) {
                        DynamicLink.REFERRALS.path -> handleReferralsDeepLink(link)
                        DynamicLink.DIRECT_DEBIT.path -> handleDirectDebitDeepLink()
                        DynamicLink.FOREVER.path -> startLoggedInActivityWithInitialTab(LoggedInTabs.REFERRALS)
                        DynamicLink.INSURANCE.path -> startLoggedInActivityWithInitialTab(LoggedInTabs.INSURANCE)
                        else -> startDefaultActivity(loginStatus)
                    }
                } else {
                    startDefaultActivity(loginStatus)
                }
            }.addOnFailureListener {
                startDefaultActivity(loginStatus)
            }
    }

    private fun handleReferralsDeepLink(link: Uri) {
        when (marketManager.market) {
            null -> context.startActivity(MarketingActivity.newInstance(context))
            Market.SE -> {
                link.getQueryParameter("code")?.let { referralCode ->
                    startActivity(
                        context,
                        ReferralsReceiverActivity.newInstance(
                            context,
                            referralCode,
                            "10"
                        ),
                        null
                    ) // Fixme "10" should not be hard coded
                }
            }
            else -> context.startActivity(Intent(context, MarketingActivity::class.java))
        }
    }

    private fun handleDirectDebitDeepLink() {
        marketManager.market?.connectPayin(context)?.let { connectPayinIntent ->
            context.startActivities(
                arrayOf(
                    Intent(context, LoggedInActivity::class.java),
                    connectPayinIntent
                )
            )
        }
    }

    private fun startLoggedInActivityWithInitialTab(tab: LoggedInTabs) {
        context.startActivity(LoggedInActivity.newInstance(context, initialTab = tab))
    }
}
