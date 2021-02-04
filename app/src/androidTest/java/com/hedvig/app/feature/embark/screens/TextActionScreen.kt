package com.hedvig.app.feature.embark.screens

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object TextActionScreen : KScreen<TextActionScreen>() {
    override val layoutId = R.layout.fragment_embark_text_action
    override val viewClass = TextActionFragment::class.java

    val input = KTextInputLayout { withId(R.id.filledTextField) }
    val submit = KButton { withId(R.id.textActionSubmit) }
}
