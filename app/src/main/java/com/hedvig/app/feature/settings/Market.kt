package com.hedvig.app.feature.settings

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.feature.zignsec.ZignSecAuthenticationActivity

enum class Market {
    SE,
    NO,
    DK;

    fun connectPayin(context: Context, isPostSign: Boolean = false) = when (this) {
        SE -> TrustlyConnectPayinActivity.newInstance(
            context,
            isPostSign
        )
        NO, DK -> AdyenConnectPayinActivity.newInstance(
            context,
            AdyenCurrency.fromMarket(this),
            isPostSign
        )
    }

    val flag: Int
        get() = when (this) {
            SE -> R.drawable.ic_flag_se
            NO -> R.drawable.ic_flag_no
            DK -> R.drawable.ic_flag_dk
        }

    val label: Int
        get() = when (this) {
            SE -> R.string.market_sweden
            NO -> R.string.market_norway
            DK -> R.string.market_denmark
        }

    fun openAuth(context: Context, fragmentManager: FragmentManager) {
        when (this) {
            SE -> {
                AuthenticateDialog().show(fragmentManager, AuthenticateDialog.TAG)
            }
            NO, DK -> {
                context.startActivity(ZignSecAuthenticationActivity.newInstance(context))
            }
        }
    }

    fun onboarding(context: Context) = when (this) {
        SE -> ChatActivity.newInstance(context)
            .apply { putExtra(ChatActivity.EXTRA_SHOW_RESTART, true) }
        NO -> {
            ChoosePlanActivity.newInstance(context)
        }
        DK -> {
            WebOnboardingActivity.newInstance(context)
        }
    }

    fun connectPayout(context: Context) = when (this) {
        NO -> AdyenConnectPayoutActivity.newInstance(context, AdyenCurrency.fromMarket(this))
        else -> null
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}
