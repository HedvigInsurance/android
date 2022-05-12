package com.hedvig.app.feature.offer.screen

import com.hedvig.app.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.check.KCheckBox

object ChangeDateView : KScreen<ChangeDateView>() {
    override val layoutId = R.layout.change_date
    override val viewClass = ChangeDateView::class.java

    val switches = KCheckBox {
        withId(R.id.auto_set_date_switch)
    }
}
