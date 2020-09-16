package com.hedvig.app.feature.home.screens

import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity

class HonestyPledgeSheetScreen : Screen<HonestyPledgeSheetScreen>() {
    val claim =
        KButton { withId(R.id.bottomSheetHonestyPledgeButton) }

    val chat = KIntent {
        hasComponent(ChatActivity::class.java.name)
    }
}
