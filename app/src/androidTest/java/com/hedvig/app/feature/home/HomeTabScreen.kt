package com.hedvig.app.feature.home

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class HomeTabScreen : Screen<HomeTabScreen>() {
    val recycler =
        KRecyclerView({ withId(R.id.recycler) },
            {
                itemType(::BigTextItem)
            })

    class BigTextItem(parent: Matcher<View>) : KRecyclerItem<BigTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}
