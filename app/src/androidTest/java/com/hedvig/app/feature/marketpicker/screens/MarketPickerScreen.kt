package com.hedvig.app.feature.marketpicker.screens

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R
import java.util.regex.Matcher

class MarketPickerScreen : Screen<MarketPickerScreen>() {
    val marketRecyclerView: KRecyclerView = KRecyclerView(
        { withId(R.id.marketList) },
        itemTypeBuilder = { itemType(::Item) })

    class Item(parent: Matcher<View>) : KRecyclerItem<Item>(parent) {
        val radioButton = KView { withId(R.id.title) }
    }
}
