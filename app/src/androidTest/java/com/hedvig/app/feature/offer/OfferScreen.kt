package com.hedvig.app.feature.offer

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class OfferScreen : Screen<OfferScreen>() {
    val scroll = KRecyclerView({ withId(R.id.offerScroll) }, {
        itemType(::HeaderItem)
    })

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
    }
}
