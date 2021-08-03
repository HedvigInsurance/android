package com.hedvig.app.feature.onboarding.screens

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object MoreOptionsScreen : KScreen<MoreOptionsScreen>() {

    override val layoutId = R.layout.activity_more_options
    override val viewClass = MoreOptionsActivity::class.java

    val recycler = KRecyclerView(
        { withId(R.id.recycler) },
        {
            itemType(MoreOptionsScreen::Row)
        }
    )

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val info = KTextView(parent) { withId(R.id.info) }
    }
}
