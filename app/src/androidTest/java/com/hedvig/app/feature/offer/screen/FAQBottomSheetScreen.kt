package com.hedvig.app.feature.offer.screen

import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.faq.FAQBottomSheet
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KTextView

object FAQBottomSheetScreen : KScreen<FAQBottomSheetScreen>() {
    override val layoutId = R.layout.faq_bottom_sheet
    override val viewClass = FAQBottomSheet::class.java

    val title = KTextView { withId(R.id.title) }
        .also { it.inRoot { isDialog() } }
    val body = KTextView { withId(R.id.body) }
        .also { it.inRoot { isDialog() } }
}
