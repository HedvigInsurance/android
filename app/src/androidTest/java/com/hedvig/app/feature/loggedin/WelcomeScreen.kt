package com.hedvig.app.feature.loggedin

import com.hedvig.app.R
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.screen.Screen

class WelcomeScreen : Screen<WelcomeScreen>() {
    val close = KImageView { withId(R.id.close) }
}
