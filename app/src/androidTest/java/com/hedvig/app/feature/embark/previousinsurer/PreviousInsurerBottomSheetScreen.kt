package com.hedvig.app.feature.embark.previousinsurer

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerBottomSheet
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object PreviousInsurerBottomSheetScreen : KScreen<PreviousInsurerBottomSheetScreen>() {
    override val layoutId = null
    override val viewClass = PreviousInsurerBottomSheet::class.java

    val recycler = KRecyclerView({
        withId(R.id.recycler)
    }, {
        itemType(::PreviousInsurer)
    })

    class PreviousInsurer(parent: Matcher<View>) : KRecyclerItem<PreviousInsurer>(parent) {
        val text = KTextView(parent) { withId(R.id.text) }
    }
}
