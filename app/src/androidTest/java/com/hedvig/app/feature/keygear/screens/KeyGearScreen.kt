package com.hedvig.app.feature.keygear.screens

import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.tab.KeyGearFragment
import com.kaspersky.kaspresso.screens.KScreen

object KeyGearScreen : KScreen<KeyGearScreen>() {
    override val layoutId = R.layout.generic_error
    override val viewClass = KeyGearFragment::class.java

    val reload = KButton { withId(R.id.retry) }
    val header = KTextView { withId(R.id.header) }
}
