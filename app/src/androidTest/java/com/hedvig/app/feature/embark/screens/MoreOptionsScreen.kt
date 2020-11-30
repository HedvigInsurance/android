package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class MoreOptionsScreen : Screen<MoreOptionsScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) },
        {
            itemType(::Reload)
            itemType(::Id)
        })

    class Reload(parent: Matcher<View>) : KRecyclerItem<Reload>(parent) {
        val reload = KTextView(parent) { withId(R.id.memberId) }
    }

    class Id(parent: Matcher<View>) : KRecyclerItem<Id>(parent) {
        val id = KTextView(parent) { withId(R.id.memberId) }
    }
}
