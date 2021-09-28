package com.hedvig.app

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class ScreenTracker(
    private val trackingFacade: TrackingFacade
) {
    private lateinit var previousScreen: String

    fun screenView(name: String) {
        if (::previousScreen.isInitialized && name == previousScreen) {
            return
        }

        previousScreen = name
        trackingFacade.track(
            "screen_view",
            jsonObjectOf(
                "name" to name
            ),
        )
    }
}
