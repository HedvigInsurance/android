package com.hedvig.app.feature.home.screens

import com.hedvig.app.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen

class LoggedInScreen : Screen<LoggedInScreen>() {
    val tooltip = KView { withText(R.string.home_tab_chat_hint_text) }
}
