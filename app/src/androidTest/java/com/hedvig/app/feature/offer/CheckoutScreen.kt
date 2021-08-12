package com.hedvig.app.feature.offer

import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.checkout.CheckoutActivity
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object CheckoutScreen : KScreen<CheckoutScreen>() {
    override val layoutId = R.layout.activity_checkout
    override val viewClass = CheckoutActivity::class.java

    val title = KTextView { withId(R.id.title) }
    val cost = KTextView { withId(R.id.cost) }
    val originalCost = KTextView { withId(R.id.original_cost) }

    val emailInput = KTextInputLayout { withId(R.id.emailInputContainer) }
    val identityNumberInput = KTextInputLayout { withId(R.id.identityNumberInputContainer) }

    val signButton = KButton { withId(R.id.signButton) }
}
