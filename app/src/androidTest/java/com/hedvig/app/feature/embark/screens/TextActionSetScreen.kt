package com.hedvig.app.feature.embark.screens

import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.hedvig.app.util.withParentIndex
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton

object TextActionSetScreen : KScreen<TextActionSetScreen>() {
    override val layoutId = R.layout.fragment_text_action_set
    override val viewClass = TextActionFragment::class.java

    fun input(index: Int, function: KTextInputLayout.() -> Unit) = KTextInputLayout {
        withParent { withId(R.id.input_container) }
        withParentIndex(index)
    }.invoke(function)

    val submit = KButton { withId(R.id.textActionSubmit) }
}
