package com.hedvig.app.feature.embark.previousinsurer

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.previousinsurer.InsurerProviderBottomSheet
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object PreviousInsurerBottomSheetScreen : KScreen<PreviousInsurerBottomSheetScreen>() {
    override val layoutId = null
    override val viewClass = InsurerProviderBottomSheet::class.java

    val recycler = KRecyclerView(
        {
            withId(R.id.recycler)
        },
        {
            itemType(::PreviousInsurer)
        }
    )

    class PreviousInsurer(parent: Matcher<View>) : KRecyclerItem<PreviousInsurer>(parent) {
        val text = KTextView(parent) { withId(R.id.text) }
    }
}
