package com.hedvig.app.feature.home.screens

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R

class EmergencyScreen : Screen<EmergencyScreen>() {
    val title =
        KTextView { withId(R.id.emergencyTitle) }
}
