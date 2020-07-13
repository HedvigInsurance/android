package com.hedvig.app.feature.profile.ui.payment

import com.mixpanel.android.mpmetrics.MixpanelAPI

class PaymentTracker(
    private val mixpanel: MixpanelAPI
) {
    fun clickRedeemCode() = mixpanel.track("REFERRAL_ADDCOUPON_HEADLINE")
    fun seePaymentHistory() = mixpanel.track("PAYMENTS_BTN_HISTORY")
    fun connectBankAccount() = mixpanel.track("PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON")
}
