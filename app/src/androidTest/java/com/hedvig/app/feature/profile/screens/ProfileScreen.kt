package com.hedvig.app.feature.profile.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.R
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

class ProfileScreen : Screen<ProfileScreen>() {
/*    override val layoutId = R.layout.profile_fragment
    override val viewClass = PerilFragment::class.java*/

    val recycler = KRecyclerView({ withId(R.id.recycler) },
        { itemType(::Error) })

    class Error(parent: Matcher<View>) : KRecyclerItem<Error>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }
}
