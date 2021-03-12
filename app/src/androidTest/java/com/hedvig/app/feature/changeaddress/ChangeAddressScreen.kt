package com.hedvig.app.feature.changeaddress

import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R

object ChangeAddressScreen : Screen<ChangeAddressScreen>() {
    val title = KTextView { withId(R.id.title) }
    val subtitle = KTextView { withId(R.id.subtitle) }
    val continueButton = KButton { withId(R.id.continueButton) }

    val address = KTextView { withId(R.id.address_label) }
    val postalCode = KTextView { withId(R.id.postal_code_label) }
    val type = KTextView { withId(R.id.type_label) }
    val livingSpace = KTextView { withId(R.id.living_space_label) }
    val date = KTextView { withId(R.id.date_label) }
}
