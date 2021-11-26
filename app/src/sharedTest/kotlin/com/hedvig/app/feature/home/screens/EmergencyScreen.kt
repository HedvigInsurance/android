package com.hedvig.app.feature.home.screens

import com.hedvig.app.R
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.toolbar.KToolbar

class EmergencyScreen : Screen<EmergencyScreen>() {
    val title =
        KToolbar { withId(R.id.toolbar) }
}
