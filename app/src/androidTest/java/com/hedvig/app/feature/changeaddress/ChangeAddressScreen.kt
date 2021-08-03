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

    val address = KTextView { withId(R.id.coinsured_label) }
    val postalCode = KTextView { withId(R.id.postal_code_label) }
    val type = KTextView { withId(R.id.type_label) }
    val livingSpace = KTextView { withId(R.id.living_space_label) }
    val date = KTextView { withId(R.id.date_label) }
}
