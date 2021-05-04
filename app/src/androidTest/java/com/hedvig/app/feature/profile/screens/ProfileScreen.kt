package com.hedvig.app.feature.profile.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.detail.ContractDetailScreen
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object ProfileScreen : KScreen<ProfileScreen>() {
    override val layoutId = R.layout.profile_fragment
    override val viewClass = PerilFragment::class.java

    val recycler = KRecyclerView({ withId(R.id.recycler) },
        {
            itemType(::Error)
            itemType(::Title)
        })

    class Error(parent: Matcher<View>) : KRecyclerItem<Error>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }

    class Title(parent: Matcher<View>) : KRecyclerItem<Title>(parent) {
        val header = KTextView { withId(R.id.header) }
    }
}
