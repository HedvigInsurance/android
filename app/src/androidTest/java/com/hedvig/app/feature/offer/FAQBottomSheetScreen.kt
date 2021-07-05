package com.hedvig.app.feature.offer

import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.faq.FAQBottomSheet
import com.kaspersky.kaspresso.screens.KScreen

object FAQBottomSheetScreen : KScreen<FAQBottomSheetScreen>() {
    override val layoutId = R.layout.faq_bottom_sheet
    override val viewClass = FAQBottomSheet::class.java

    val title = KTextView { withId(R.id.title) }
        .also { it.inRoot { isDialog() } }
    val body = KTextView { withId(R.id.body) }
        .also { it.inRoot { isDialog() } }
}
