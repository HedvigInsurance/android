package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object MoreOptionsScreen : KScreen<MoreOptionsScreen>() {

    override val layoutId = R.layout.activity_more_options
    override val viewClass = MoreOptionsActivity::class.java

    val recycler = KRecyclerView(
        { withId(R.id.recycler) },
        {
            itemType(::Row)
        }
    )

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val info = KTextView(parent) { withId(R.id.info) }
    }
}
