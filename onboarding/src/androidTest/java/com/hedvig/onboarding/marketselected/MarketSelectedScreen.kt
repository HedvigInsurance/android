package com.hedvig.onboarding.marketselected

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R

object MarketSelectedScreen : KScreen<MarketSelectedScreen>() {

    override val layoutId = R.layout.fragment_market_selected
    override val viewClass = MarketSelectedFragment::class.java

    val loginButton = KButton { withId(R.id.logIn) }
}
