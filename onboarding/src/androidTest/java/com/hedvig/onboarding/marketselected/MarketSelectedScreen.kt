package com.hedvig.onboarding.marketselected

import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.MarketSelectedFragment
import com.kaspersky.kaspresso.screens.KScreen

object MarketSelectedScreen : KScreen<MarketSelectedScreen>() {

    override val layoutId = R.layout.fragment_market_selected
    override val viewClass = MarketSelectedFragment::class.java

    val loginButton = KButton { withId(R.id.logIn) }
}
