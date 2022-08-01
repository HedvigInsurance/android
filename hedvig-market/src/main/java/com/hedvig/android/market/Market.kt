package com.hedvig.android.market

import androidx.annotation.StringRes
import com.hedvig.android.owldroid.graphql.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.graphql.type.DirectDebitStatus

enum class Market {
  SE,
  NO,
  DK,
  FR;

  val flag: Int
    get() = when (this) {
      SE -> hedvig.resources.R.drawable.ic_flag_se
      NO -> hedvig.resources.R.drawable.ic_flag_no
      DK -> hedvig.resources.R.drawable.ic_flag_dk
      FR -> hedvig.resources.R.drawable.ic_flag_fr
    }

  val label: Int
    get() = when (this) {
      SE -> hedvig.resources.R.string.market_sweden
      NO -> hedvig.resources.R.string.market_norway
      DK -> hedvig.resources.R.string.market_denmark
      FR -> hedvig.resources.R.string.market_france
    }

  @StringRes
  fun getPriceCaption(
    directDebitStatus: DirectDebitStatus?,
    activePaymentMethodsFragment: ActivePaymentMethodsFragment?,
  ): Int = when (this) {
    SE -> when (directDebitStatus) {
      DirectDebitStatus.ACTIVE -> hedvig.resources.R.string.Direct_Debit_Connected
      DirectDebitStatus.NEEDS_SETUP,
      DirectDebitStatus.PENDING,
      DirectDebitStatus.UNKNOWN__,
      null,
      -> hedvig.resources.R.string.Direct_Debit_Not_Connected
    }
    DK,
    NO,
    -> when {
      activePaymentMethodsFragment?.asStoredCardDetails != null -> {
        hedvig.resources.R.string.Card_Connected
      }
      activePaymentMethodsFragment?.asStoredThirdPartyDetails != null -> {
        hedvig.resources.R.string.Third_Party_Connected
      }
      activePaymentMethodsFragment == null -> hedvig.resources.R.string.Card_Not_Connected
      else -> hedvig.resources.R.string.Card_Not_Connected
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
