package com.hedvig.app.feature.whatsnew

import com.hedvig.app.feature.dismissiblepager.DismissablePageTracker
import com.mixpanel.android.mpmetrics.MixpanelAPI

class WhatsNewTracker(
    private val mixpanel: MixpanelAPI
) : DismissablePageTracker {
    override fun clickProceed() = mixpanel.track("NEWS_PROCEED")
}
