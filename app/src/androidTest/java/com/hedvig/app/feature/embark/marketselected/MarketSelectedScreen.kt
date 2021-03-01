package com.hedvig.app.feature.embark.marketselected

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R

class MarketSelectedScreen: Screen<MarketSelectedScreen>() {

    val loginButton = KButton { withId(R.id.logIn) }
    val legalText = KTextView { withId(R.id.legal) }
}
