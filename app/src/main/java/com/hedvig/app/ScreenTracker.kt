package com.hedvig.app

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ScreenTracker(
    private val mixpanel: MixpanelAPI
) {
    private lateinit var previousScreen: String

    fun screenView(name: String) {
        if (::previousScreen.isInitialized && name == previousScreen) {
            return
        }

        previousScreen = name
        mixpanel.track("screen_view_$name")
    }
}
