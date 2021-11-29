package com.hedvig.app.feature.changeaddress

import com.hedvig.app.R
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object ChangeAddressScreen : KScreen<ChangeAddressScreen>() {
    override val layoutId = R.layout.change_address_activity
    override val viewClass = ChangeAddressActivity::class.java

    val title = KTextView { withId(R.id.title) }
    val subtitle = KTextView { withId(R.id.subtitle) }
    val continueButton = KButton { withId(R.id.continueButton) }
}
