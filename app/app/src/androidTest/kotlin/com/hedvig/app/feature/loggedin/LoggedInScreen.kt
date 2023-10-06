package com.hedvig.app.feature.loggedin

import com.hedvig.android.feature.chat.ui.ChatActivity
import com.hedvig.app.R
import io.github.kakaocup.kakao.bottomnav.KBottomNavigationView
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.screen.Screen

class LoggedInScreen : Screen<LoggedInScreen>() {
  val root = KView { withId(R.id.chat) }
  val bottomTabs = KBottomNavigationView { withId(R.id.chat) }

  val openChat = KView { withId(R.id.chat) }

  val chat = KIntent {
    hasComponent(ChatActivity::class.java.name)
  }
}
