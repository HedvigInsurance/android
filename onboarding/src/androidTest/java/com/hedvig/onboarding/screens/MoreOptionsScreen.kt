package com.hedvig.onboarding.screens

import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.feature.zignsec.ZignSecAuthenticationActivity
import com.hedvig.onboarding.R
import com.hedvig.onboarding.moreoptions.MoreOptionsActivity
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object MoreOptionsScreen : KScreen<MoreOptionsScreen>() {

    override val layoutId = R.layout.activity_more_options
    override val viewClass = MoreOptionsActivity::class.java

    val recycler = KRecyclerView({ withId(R.id.recycler) },
        {
            itemType(::Row)
        })

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val info = KTextView(parent) { withId(R.id.info) }
    }

    val loginButton = KButton { withId(R.id.logIn) }

    val authIntent = KIntent {
        hasComponent(ZignSecAuthenticationActivity::class.java.name)
    }
}
