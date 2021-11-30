package com.hedvig.app.feature.settings

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity

enum class Market {
    SE,
    NO,
    DK,
    FR;

    /**
     * Members paying to Hedvig
     */
    fun connectPayin(context: Context, isPostSign: Boolean = false) = when (this) {
        SE -> TrustlyConnectPayinActivity.newInstance(
            context,
            isPostSign
        )
        NO, DK, FR -> AdyenConnectPayinActivity.newInstance(
            context,
            AdyenCurrency.fromMarket(this),
            isPostSign
        )
    }

    /**
     * Hedvig paying to member
     */
    fun connectPayout(context: Context) = when (this) {
        NO -> AdyenConnectPayoutActivity.newInstance(context, AdyenCurrency.fromMarket(this))
        else -> null
    }

    val flag: Int
        get() = when (this) {
            SE -> R.drawable.ic_flag_se
            NO -> R.drawable.ic_flag_no
            DK -> R.drawable.ic_flag_dk
            FR -> R.drawable.ic_flag_fr
        }

    val label: Int
        get() = when (this) {
            SE -> R.string.market_sweden
            NO -> R.string.market_norway
            DK -> R.string.market_denmark
            FR -> R.string.market_france
        }

    fun openAuth(context: Context, fragmentManager: FragmentManager) {
        when (this) {
            SE -> {
                LoginDialog().show(fragmentManager, AuthenticateDialog.TAG)
            }
            NO, DK -> {
                context.startActivity(SimpleSignAuthenticationActivity.newInstance(context, this))
            }
            FR -> {
                TODO("Open generic auth")
            }
        }
    }

    fun onboarding(context: Context, seEmbarkOnboarding: Boolean) = when (this) {
        SE -> {
            if (seEmbarkOnboarding) {
                ChoosePlanActivity.newInstance(context)
            } else {
                ChatActivity.newInstance(context)
                    .apply { putExtra(ChatActivity.EXTRA_SHOW_RESTART, true) }
            }
        }
        NO -> {
            ChoosePlanActivity.newInstance(context)
        }
        DK -> {
            WebOnboardingActivity.newInstance(context)
        }
        FR -> {
            WebOnboardingActivity.newInstance(context)
        }
    }

    fun getPriceCaption(data: ProfileQuery.Data) = when (this) {
        SE -> when (data.bankAccount?.directDebitStatus) {
            DirectDebitStatus.ACTIVE -> R.string.Direct_Debit_Connected
            DirectDebitStatus.NEEDS_SETUP,
            DirectDebitStatus.PENDING,
            DirectDebitStatus.UNKNOWN__,
            null,
            -> R.string.Direct_Debit_Not_Connected
        }
        DK,
        NO -> if (data.activePaymentMethods == null) {
            R.string.Card_Not_Connected
        } else {
            R.string.Card_Connected
        }
        FR -> TODO()
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}
