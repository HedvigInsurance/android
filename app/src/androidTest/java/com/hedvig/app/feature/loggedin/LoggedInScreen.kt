package com.hedvig.app.feature.loggedin

import com.agoda.kakao.bottomnav.KBottomNavigationView
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity

class LoggedInScreen : Screen<LoggedInScreen>() {
    val root = KView { withId(R.id.loggedInRoot) }
    val bottomTabs = KBottomNavigationView { withId(R.id.bottomNavigation) }

    val openChat = KView { withId(R.id.openChat) }

    val chat = KIntent {
        hasComponent(ChatActivity::class.java.name)
    }
}
