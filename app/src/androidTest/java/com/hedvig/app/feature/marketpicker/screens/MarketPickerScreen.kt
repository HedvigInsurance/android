package com.hedvig.app.feature.marketpicker.screens

import android.view.View
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R
import org.hamcrest.Matcher

class MarketPickerScreen : Screen<MarketPickerScreen>() {
    val marketRecyclerView =
        KRecyclerView({ withId(R.id.marketList) }, itemTypeBuilder = { itemType(::Item) })

    class Item(parent: Matcher<View>) : KRecyclerItem<Item>(parent) {
        val radioButton = KCheckBox(parent) { withId(R.id.radioButton) }
    }
}
