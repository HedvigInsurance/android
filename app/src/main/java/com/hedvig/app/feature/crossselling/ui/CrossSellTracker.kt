package com.hedvig.app.feature.crossselling.ui

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class CrossSellTracker(
    private val trackingFacade: TrackingFacade,
) {
    private val viewedResults = mutableSetOf<CrossSellingResult>()

    fun view(result: CrossSellingResult) {
        if (viewedResults.contains(result)) {
            return
        }

        viewedResults.add(result)
        when (result) {
            is CrossSellingResult.Success -> {
                trackingFacade.track(
                    "cross_sell_result_viewed",
                    jsonObjectOf(
                        "result" to "success",
                        "bundle_display_name" to result.insuranceType,
                    ),
                )
            }
            CrossSellingResult.Error -> {
                trackingFacade.track(
                    "cross_sell_result_viewed",
                    jsonObjectOf(
                        "result" to "error"
                    )
                )
            }
        }
    }
}
