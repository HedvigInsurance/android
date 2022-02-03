package com.hedvig.app

import androidx.lifecycle.ViewModel
import com.hedvig.app.service.DynamicLink
import com.hedvig.hanalytics.HAnalytics

class SplashViewModel(
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    fun onDynamicLinkOpened(link: DynamicLink) {
        when (link) {
            DynamicLink.None, DynamicLink.Unknown -> {}
            else -> {
                hAnalytics.deepLinkOpened(link.type)
            }
        }
    }
}
