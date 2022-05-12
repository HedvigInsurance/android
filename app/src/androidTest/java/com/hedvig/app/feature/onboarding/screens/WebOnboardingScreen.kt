package com.hedvig.app.feature.onboarding.screens

import com.hedvig.app.R
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.web.KWebView

object WebOnboardingScreen : KScreen<WebOnboardingScreen>() {
    override val layoutId = R.layout.activity_web_onboarding
    override val viewClass = WebOnboardingActivity::class.java

    val webView = KWebView { withId(R.id.webOnboarding) }
}
