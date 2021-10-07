package com.hedvig.app.feature.offer

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class OfferTracker(
    private val trackingFacade: TrackingFacade
) {
    fun signQuotes(quoteIds: List<String>) = trackingFacade.track(
        "QUOTES_SIGNED",
        jsonObjectOf("quoteIds" to quoteIds),
    )

    fun openChat() = trackingFacade.track("OFFER_OPEN_CHAT")
    fun openOfferLink(displayName: String) = trackingFacade.track(
        "OFFER_OPEN_LINK",
        jsonObjectOf("link_label" to displayName)
    )

    fun removeDiscount() = trackingFacade.track("OFFER_REMOVE_DISCOUNT_BUTTON")
    fun addDiscount() = trackingFacade.track("OFFER_ADD_DISCOUNT_BUTTON")
    fun terms() = trackingFacade.track("OFFER_TERMS")

    fun chooseStartDate() = trackingFacade.track("START_DATE_BTN")
    fun activateOnInsuranceEnd() = trackingFacade.track("ACTIVATE_INSURANCE_END_BTN")
    fun changeDateContinue() = trackingFacade.track("ALERT_CONTINUE")
    fun settings() = trackingFacade.track("SETTINGS_ACCESSIBILITY_HINT")
    fun checkoutHeader(method: String) = checkout(method, "header")
    fun checkoutFloating(method: String) = checkout(method, "floating")
    private fun checkout(method: String, location: String) = trackingFacade.track(
        "button_click",
        jsonObjectOf(
            "localization_key" to method,
            "location" to location,
        )
    )
}
