package com.hedvig.app.feature.marketpicker.screens

import android.view.View
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.scroll.KScrollView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class MarketPickerScreen : Screen<MarketPickerScreen>() {
    val marketRecyclerView =
        KRecyclerView({ withId(R.id.marketList) }, itemTypeBuilder = { itemType(::MarketItem) })

    val languageRecyclerView =
        KRecyclerView({ withId(R.id.languageList) }, itemTypeBuilder = { itemType(::LanguageItem) })

    val save = KButton { withId(R.id.save) }

    val scroll = KScrollView { withId(R.id.scrollView) }

    class MarketItem(parent: Matcher<View>) : KRecyclerItem<MarketItem>(parent) {
        val radioButton = KCheckBox(parent) { withId(R.id.radioButton) }
    }

    class LanguageItem(parent: Matcher<View>) : KRecyclerItem<LanguageItem>(parent) {
        val languageText = KTextView(parent) { withId(R.id.language) }
    }
}
