package com.hedvig.app.feature.home.screens

import com.hedvig.app.R
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton

class HonestyPledgeSheetScreen : Screen<HonestyPledgeSheetScreen>() {
    val claim =
        KButton { withId(R.id.bottomSheetHonestyPledgeButton) }

    val embark = KIntent {
        hasComponent(EmbarkActivity::class.java.name)
    }
}
