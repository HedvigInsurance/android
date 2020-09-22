package com.hedvig.app.feature.loggedin

import com.agoda.kakao.image.KImageView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R

class WelcomeScreen : Screen<WelcomeScreen>() {
    val close = KImageView { withId(R.id.close) }
}
