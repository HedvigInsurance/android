package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class MoreOptionsScreen : Screen<MoreOptionsScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) },
        {
            itemType(::Row)
        })

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val info = KTextView(parent) { withId(R.id.info) }
    }

    val loginButton =  KButton { withId(R.id.logIn) }
}
