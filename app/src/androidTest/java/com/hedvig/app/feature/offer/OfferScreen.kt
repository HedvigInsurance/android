package com.hedvig.app.feature.offer

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.util.KMaterialDatePicker
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object OfferScreen : KScreen<OfferScreen>() {
    override val layoutId = R.layout.activity_offer
    override val viewClass = OfferActivity::class.java

    val scroll = KRecyclerView(
        { withId(R.id.offerScroll) },
        {
            itemType(::HeaderItem)
            itemType(::SwitcherItem)
            itemType(::Facts)
        }
    )

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
    }

    class SwitcherItem(parent: Matcher<View>) : KRecyclerItem<SwitcherItem>(parent) {
        val title = KTextView(parent) { withId(R.id.switchTitle) }
    }

    class Facts(parent: Matcher<View>) : KRecyclerItem<Facts>(parent) {
        val expandableContent = KView(parent) { withId(R.id.expandableContentView) }
    }
}

object ChangeDateSheet : KScreen<ChangeDateSheet>() {
    override val layoutId = R.layout.dialog_change_start_date
    override val viewClass = ChangeDateBottomSheet::class.java
    val autoSetDate = KButton { withId(R.id.auto_set_date_switch) }
    val pickDate = KView { withId(R.id.date_pick_layout) }
    val materialDatePicker = KMaterialDatePicker()
    val submit = KButton { withId(R.id.chooseDateButton) }
}
