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
            SCREEN_VIEW,
            jsonObjectOf(
                SCREEN_NAME to name
            ),
        )
    }

    companion object {
        const val SCREEN_VIEW = "screen_view_android"
        const val SCREEN_NAME = "screen_name"
    }
}
