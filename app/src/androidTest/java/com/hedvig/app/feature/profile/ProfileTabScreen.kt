package com.hedvig.app.feature.profile

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

object ProfileTabScreen : Screen<ProfileTabScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::Title)
        itemType(::Row)
        itemType(::Subtitle)
        itemType(::Logout)
    })

    class Title(parent: Matcher<View>) : KRecyclerItem<Title>(parent) {
        val text = KTextView(parent) { withMatcher(parent) }
    }

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val caption = KTextView(parent) { withId(R.id.caption) }
    }

    class Subtitle(parent: Matcher<View>) : KRecyclerItem<Subtitle>(parent) {
        val text = KTextView(parent) { withMatcher(parent) }
    }

    class Logout(parent: Matcher<View>) : KRecyclerItem<Logout>(parent) {

    }
}
