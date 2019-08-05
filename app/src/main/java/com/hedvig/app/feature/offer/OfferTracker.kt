package com.hedvig.app.feature.offer

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class OfferTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    private var hasSigned = false

    fun openChat() = firebaseAnalytics.logEvent("OFFER_OPEN_CHAT", null)
    fun openTerms() = firebaseAnalytics.logEvent("OFFER_PRIVACY_POLICY", null)
    fun floatingSign() = firebaseAnalytics.logEvent("OFFER_SIGN_BUTTON", null)
    fun toolbarSign() = firebaseAnalytics.logEvent("OFFER_BANKID_SIGN_BUTTON", null)
    fun removeDiscount() = firebaseAnalytics.logEvent("OFFER_REMOVE_DISCOUNT_BUTTON", null)
    fun addDiscount() = firebaseAnalytics.logEvent("OFFER_ADD_DISCOUNT_BUTTON", null)
    fun presaleInformation() = firebaseAnalytics.logEvent("OFFER_PRESALE_INFORMATION", null)
    fun terms() = firebaseAnalytics.logEvent("OFFER_TERMS", null)
    fun userDidSign(price: Double) {
        if (!hasSigned) {
            hasSigned = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, Bundle().apply {
                putDouble(FirebaseAnalytics.Param.VALUE, price)
                putString(FirebaseAnalytics.Param.CURRENCY, "SEK")
            })
        }
    }
}
