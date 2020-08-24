package com.hedvig.app.feature.dashboard.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class DashboardScreen : Screen<DashboardScreen>() {
    val recycler =
        KRecyclerView({ withId(R.id.root) }, {
            itemType(DashboardScreen::TitleTextItem)
        })

    class TitleTextItem(parent: Matcher<View>) : KRecyclerItem<TitleTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}
