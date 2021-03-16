package com.hedvig.onboarding.screens

import com.agoda.kakao.web.KWebView
import com.hedvig.app.R
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.kaspersky.kaspresso.screens.KScreen

object WebOnboardingScreen : KScreen<WebOnboardingScreen>() {
    override val layoutId = R.layout.activity_web_onboarding
    override val viewClass = WebOnboardingActivity::class.java

    val webView = KWebView { withId(R.id.webOnboarding) }
}
