package com.hedvig.app.feature.embark.screens

import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionFragment
import com.hedvig.app.util.withParentIndex
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton

object NumberActionScreen : KScreen<NumberActionScreen>() {
    override val layoutId = R.layout.number_action_set_fragment
    override val viewClass = NumberActionFragment::class.java

    fun input(index: Int, function: KTextInputLayout.() -> Unit) = KTextInputLayout {
        withParent { withId(R.id.input_container) }
        withParentIndex(index)
    }.invoke(function)

    val input = KTextInputLayout { withId(R.id.textField) }
    val submit = KButton { withId(R.id.submit) }
}
