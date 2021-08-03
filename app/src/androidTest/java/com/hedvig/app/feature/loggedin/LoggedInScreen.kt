package com.hedvig.app.feature.loggedin

import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import io.github.kakaocup.kakao.bottomnav.KBottomNavigationView
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.screen.Screen

class LoggedInScreen : Screen<LoggedInScreen>() {
    val root = KView { withId(R.id.loggedInRoot) }
    val bottomTabs = KBottomNavigationView { withId(R.id.bottomNavigation) }

    val openChat = KView { withId(R.id.openChat) }

    val chat = KIntent {
        hasComponent(ChatActivity::class.java.name)
    }
}
