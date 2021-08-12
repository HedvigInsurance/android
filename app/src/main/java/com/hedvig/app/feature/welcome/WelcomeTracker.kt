package com.hedvig.app.feature.welcome

import com.hedvig.app.feature.dismissiblepager.DismissablePageTracker
import com.hedvig.app.feature.tracking.TrackingFacade

class WelcomeTracker(
    private val trackingFacade: TrackingFacade,
) : DismissablePageTracker {
    override fun clickProceed() = trackingFacade.track("NEWS_PROCEED")
}
