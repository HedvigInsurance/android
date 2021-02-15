package com.hedvig.onboarding.screens

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object NumberActionScreen : KScreen<NumberActionScreen>() {
    override val layoutId = R.layout.number_action_fragment
    override val viewClass = NumberActionFragment::class.java

    val input = KTextInputLayout { withId(R.id.inputContainer) }
    val submit = KButton { withId(R.id.submit) }
}
