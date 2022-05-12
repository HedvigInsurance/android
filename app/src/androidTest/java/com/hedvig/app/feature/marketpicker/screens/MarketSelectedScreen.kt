package com.hedvig.app.feature.marketpicker.screens

import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.MarketSelectedFragment
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton

object MarketSelectedScreen : KScreen<MarketSelectedScreen>() {

    override val layoutId = R.layout.fragment_market_selected
    override val viewClass = MarketSelectedFragment::class.java

    val loginButton = KButton { withId(R.id.logIn) }
}
