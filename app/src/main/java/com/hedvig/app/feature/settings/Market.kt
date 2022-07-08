package com.hedvig.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.graphql.type.DirectDebitStatus
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity

enum class Market {
  SE,
  NO,
  DK,
  FR;

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

  fun openOnboarding(context: Context) {
    val webPath = Language.fromSettings(context, this).webPath()
    val url = context.getString(R.string.WEB_BASE_URL) + "/" + webPath + "/new-member"
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(browserIntent)
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
