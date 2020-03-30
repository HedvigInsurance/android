package com.hedvig.app.feature.offer

import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal
import java.util.Currency

class OfferTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val facebookAnalytics: AppEventsLogger
) {

    private var hasSigned = false

    fun openChat() = firebaseAnalytics.logEvent("OFFER_OPEN_CHAT", null)
    fun openTerms() = firebaseAnalytics.logEvent("OFFER_PRIVACY_POLICY", null)
    fun openOfferLink(displayName: String) = firebaseAnalytics.logEvent("OFFER_OPEN_LINK", Bundle().apply { putString("link_label", displayName) })
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
            facebookAnalytics.logPurchase(
                BigDecimal(price),
                Currency.getInstance("SEK")
            )
        }
    }
    fun chooseStartDate() = firebaseAnalytics.logEvent("START_DATE_BTN", null)
    fun activateToday() = firebaseAnalytics.logEvent("ACTIVATE_TODAY_BTN", null)
    fun activateOnInsuranceEnd() = firebaseAnalytics.logEvent("ACTIVATE_INSURANCE_END_BTN", null)
    fun chooseDate() = firebaseAnalytics.logEvent("CHOOSE_DATE_BTN", null)
    fun changeDateContinue() = firebaseAnalytics.logEvent("ALERT_CONTINUE", null)
}
