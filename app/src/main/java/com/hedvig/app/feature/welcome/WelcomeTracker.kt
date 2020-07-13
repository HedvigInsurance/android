package com.hedvig.app.feature.welcome

import com.hedvig.app.feature.dismissiblepager.DismissablePageTracker
import com.mixpanel.android.mpmetrics.MixpanelAPI

class WelcomeTracker(
    private val mixpanel: MixpanelAPI
) : DismissablePageTracker {
    override fun clickProceed() = mixpanel.track("NEWS_PROCEED")
}
