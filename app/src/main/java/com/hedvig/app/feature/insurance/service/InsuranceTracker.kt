package com.hedvig.app.feature.insurance.service

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class InsuranceTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun retry() = trackingFacade.track("home_tab.error_button_text")

    fun crossSellCard(typeOfContract: String) = trackingFacade.track(
        "card_clicked",
        jsonObjectOf(
            "type" to "cross_sell",
            "type_of_contract" to typeOfContract,
            "tab" to "insurance",
        ),
    )

    fun crossSellCta(typeOfContract: String, label: String) = trackingFacade.track(
        "button_click",
        jsonObjectOf(
            "text" to label.lowercase(),
            "type_of_contract" to typeOfContract,
            "tab" to "insurance",
        )
    )
}
