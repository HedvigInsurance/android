package com.hedvig.app.util.navigation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.hedvig.android.market.Market
import com.hedvig.app.authenticate.BankIdLoginDialog
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity

fun Market?.getConnectPayoutActivity(context: Context): Intent? {
  return when (this) {
    Market.NO -> AdyenConnectPayoutActivity.newInstance(context, AdyenCurrency.fromMarket(this))
    else -> null
  }
}

fun Market.openAuth(context: Context, fragmentManager: FragmentManager) {
  when (this) {
    Market.SE -> {
      BankIdLoginDialog().show(fragmentManager, BankIdLoginDialog.TAG)
    }
    Market.NO, Market.DK -> {
      context.startActivity(SimpleSignAuthenticationActivity.newInstance(context, this))
    }
    Market.FR -> {
      TODO("Open generic auth")
    }
  }
}
