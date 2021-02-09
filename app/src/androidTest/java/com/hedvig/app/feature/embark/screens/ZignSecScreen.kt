package com.hedvig.app.feature.embark.screens

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R

class ZignSecScreen: Screen<ZignSecScreen>() {

    val webView = KView { withId(R.id.danishBankIdContainer) }
}
