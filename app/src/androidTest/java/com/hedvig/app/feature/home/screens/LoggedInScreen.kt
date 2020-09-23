package com.hedvig.app.feature.home.screens

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R

class LoggedInScreen : Screen<LoggedInScreen>() {
    val tooltip = KView { withText(R.string.home_tab_chat_hint_text) }
}
