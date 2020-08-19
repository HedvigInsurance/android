package com.hedvig.app.feature.home.screens

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R

class HonestyPledgeSheetScreen : Screen<HonestyPledgeSheetScreen>() {
    val claim =
        KButton { withId(R.id.bottomSheetHonestyPledgeButton) }
}
