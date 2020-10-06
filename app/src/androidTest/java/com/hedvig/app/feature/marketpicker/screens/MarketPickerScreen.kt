package com.hedvig.app.feature.marketpicker.screens

import android.view.View
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class MarketPickerScreen : Screen<MarketPickerScreen>() {
    val picker = KRecyclerView({ withId(R.id.picker) }, {
        itemType(::ContinueButton)
        itemType(::Picker)
    })

    class ContinueButton(parent: Matcher<View>) : KRecyclerItem<ContinueButton>(parent) {
        val continueButton = KCheckBox(parent) { withId(R.id.continueButton) }
    }

    class Picker(parent: Matcher<View>) : KRecyclerItem<Picker>(parent) {
        val selectedMarket = KTextView(parent) { withId(R.id.selected) }
    }


}
