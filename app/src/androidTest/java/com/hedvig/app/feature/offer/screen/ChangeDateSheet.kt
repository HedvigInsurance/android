package com.hedvig.app.feature.offer.screen

import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton

object ChangeDateSheet : KScreen<ChangeDateSheet>() {
    override val layoutId = R.layout.dialog_change_start_date
    override val viewClass = ChangeDateBottomSheet::class.java
    val submit = KButton { withId(R.id.chooseDateButton) }
    val changeDateContainer = KView {
        withId(R.id.change_date_container)
    }
}
