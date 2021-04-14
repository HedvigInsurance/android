package com.hedvig.app.feature.embark.screens

import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object TextActionSetScreen : KScreen<TextActionSetScreen>() {
    override val layoutId = R.layout.fragment_text_action_set
    override val viewClass = TextActionFragment::class.java

    val submit = KButton { withId(R.id.textActionSubmit) }
}
