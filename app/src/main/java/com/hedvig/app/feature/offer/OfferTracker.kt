package com.hedvig.app.feature.offer

import com.hedvig.app.util.jsonObjectOf
import com.mixpanel.android.mpmetrics.MixpanelAPI

class OfferTracker(
    private val mixpanel: MixpanelAPI
) {

    fun openChat() = mixpanel.track("OFFER_OPEN_CHAT")
    fun openOfferLink(displayName: String) = mixpanel.track(
        "OFFER_OPEN_LINK",
        jsonObjectOf("link_label" to displayName)
    )

    fun floatingSign() = mixpanel.track("OFFER_SIGN_BUTTON")
    fun removeDiscount() = mixpanel.track("OFFER_REMOVE_DISCOUNT_BUTTON")
    fun addDiscount() = mixpanel.track("OFFER_ADD_DISCOUNT_BUTTON")
    fun terms() = mixpanel.track("OFFER_TERMS")

    fun chooseStartDate() = mixpanel.track("START_DATE_BTN")
    fun activateToday() = mixpanel.track("ACTIVATE_TODAY_BTN")
    fun activateOnInsuranceEnd() = mixpanel.track("ACTIVATE_INSURANCE_END_BTN")
    fun changeDateContinue() = mixpanel.track("ALERT_CONTINUE")
    fun settings() = mixpanel.track("SETTINGS_ACCESSIBILITY_HINT")
}
