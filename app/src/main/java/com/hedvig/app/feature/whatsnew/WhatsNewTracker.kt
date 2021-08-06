package com.hedvig.app.feature.whatsnew

import com.hedvig.app.feature.dismissiblepager.DismissablePageTracker
import com.hedvig.app.feature.tracking.TrackingFacade

class WhatsNewTracker(
    private val trackingFacade: TrackingFacade,
) : DismissablePageTracker {
    override fun clickProceed() = trackingFacade.track("NEWS_PROCEED")
}
