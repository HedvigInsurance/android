package com.hedvig.app.feature.home.screens

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.toolbar.KToolbar
import com.hedvig.app.R

class EmergencyScreen : Screen<EmergencyScreen>() {
    val title =
        KToolbar { withId(R.id.toolbar) }
}
