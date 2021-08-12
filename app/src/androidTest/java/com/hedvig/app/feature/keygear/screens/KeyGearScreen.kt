package com.hedvig.app.feature.keygear.screens

import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.tab.KeyGearFragment
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object KeyGearScreen : KScreen<KeyGearScreen>() {
    override val layoutId = R.layout.generic_error
    override val viewClass = KeyGearFragment::class.java

    val reload = KButton { withId(R.id.retry) }
    val header = KTextView { withId(R.id.header) }
}
