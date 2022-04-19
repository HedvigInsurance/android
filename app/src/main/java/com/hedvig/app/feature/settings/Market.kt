package com.hedvig.app.feature.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
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

    fun openOnboarding(context: Context) = when (this) {
        SE -> {
            context.startActivity(ChoosePlanActivity.newInstance(context))
        }
        NO -> {
            context.startActivity(ChoosePlanActivity.newInstance(context))
        }
        DK -> {
            context.startActivity(ChoosePlanActivity.newInstance(context))
        }
        FR -> {
            context.startActivity(WebOnboardingActivity.newInstance(context))
        }
    }

    @StringRes
    fun getPriceCaption(
        directDebitStatus: DirectDebitStatus?,
        activePaymentMethodsFragment: ActivePaymentMethodsFragment?,
    ): Int = when (this) {
        SE -> when (directDebitStatus) {
            DirectDebitStatus.ACTIVE -> R.string.Direct_Debit_Connected
            DirectDebitStatus.NEEDS_SETUP,
            DirectDebitStatus.PENDING,
            DirectDebitStatus.UNKNOWN__,
            null,
            -> R.string.Direct_Debit_Not_Connected
        }
        DK,
        NO,
        -> when {
            activePaymentMethodsFragment?.asStoredCardDetails != null -> {
                R.string.Card_Connected
            }
            activePaymentMethodsFragment?.asStoredThirdPartyDetails != null -> {
                R.string.Third_Party_Connected
            }
            activePaymentMethodsFragment == null -> R.string.Card_Not_Connected
            else -> R.string.Card_Not_Connected
        }
        FR -> TODO()
    }

    fun isCompatible(language: Language) = when (this) {
        SE -> language == Language.EN_SE || language == Language.SV_SE
        NO -> language == Language.EN_NO || language == Language.NB_NO
        DK -> language == Language.EN_DK || language == Language.DA_DK
        FR -> language == Language.EN_FR || language == Language.FR_FR
    }

    fun defaultLanguage() = when (this) {
        SE -> Language.EN_SE
        NO -> Language.EN_NO
        DK -> Language.EN_DK
        FR -> Language.EN_FR
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}
