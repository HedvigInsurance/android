package com.hedvig.app.feature.profile.screens

import android.view.View
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ProfileScreen : KScreen<ProfileScreen>() {
    override val layoutId = R.layout.profile_fragment
    override val viewClass = PerilFragment::class.java

    val recycler = KRecyclerView(
        { withId(R.id.recycler) },
        {
            itemType(::Error)
            itemType(::Title)
        }
    )

    class Error(parent: Matcher<View>) : KRecyclerItem<Error>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }

    class Title(parent: Matcher<View>) : KRecyclerItem<Title>(parent) {
        val header = KTextView { withId(R.id.header) }
    }
}
