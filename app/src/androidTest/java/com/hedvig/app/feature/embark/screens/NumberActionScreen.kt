package com.hedvig.app.feature.embark.screens

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object NumberActionScreen : KScreen<NumberActionScreen>() {
    override val layoutId = R.layout.number_action_set_fragment
    override val viewClass = NumberActionFragment::class.java

    val input = KTextInputLayout { withId(R.id.textField) }
    val submit = KButton { withId(R.id.submit) }
}
