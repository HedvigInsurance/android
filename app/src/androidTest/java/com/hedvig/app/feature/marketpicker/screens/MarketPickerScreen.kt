package com.hedvig.app.feature.marketpicker.screens

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.MarketPickerFragment
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object MarketPickerScreen : KScreen<MarketPickerScreen>() {

    override val layoutId = R.layout.fragment_market_picker
    override val viewClass = MarketPickerFragment::class.java

    val picker = KRecyclerView(
        { withId(R.id.picker) },
        {
            itemType(::MarketButton)
            itemType(::ContinueButton)
        }
    )

    val marketPicker = KRecyclerView(
        { withId(R.id.recycler) },
        {
            itemType(::MarketButton)
        }
    )

    class ContinueButton(parent: Matcher<View>) : KRecyclerItem<ContinueButton>(parent) {
        val continueButton = KButton(parent) { withId(R.id.continueButton) }
    }

    class Language(parent: Matcher<View>) : KRecyclerItem<Language>(parent)

    class MarketButton(parent: Matcher<View>) : KRecyclerItem<MarketButton>(parent) {
        val selectedMarket = KTextView(parent) { withId(R.id.selected) }
    }
}
