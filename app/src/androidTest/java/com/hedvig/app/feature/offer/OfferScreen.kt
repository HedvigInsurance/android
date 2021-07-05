package com.hedvig.app.feature.offer

import android.view.View
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
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
            itemType(::QuoteDetail)
        }
    )

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
        val startDateLabel = KTextView(parent) { withId(R.id.startDateLabel) }
    }

    class SwitcherItem(parent: Matcher<View>) : KRecyclerItem<SwitcherItem>(parent) {
        val title = KTextView(parent) { withId(R.id.switchTitle) }
    }

    class Facts(parent: Matcher<View>) : KRecyclerItem<Facts>(parent) {
        val expandableContent = KView(parent) { withId(R.id.expandableContentView) }
    }

    class QuoteDetail(parent: Matcher<View>) : KRecyclerItem<QuoteDetail>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}

object ChangeDateSheet : KScreen<ChangeDateSheet>() {
    override val layoutId = R.layout.dialog_change_start_date
    override val viewClass = ChangeDateBottomSheet::class.java

    val submit = KButton { withId(R.id.chooseDateButton) }
    val changeDateContainer = KView {
        withId(R.id.change_date_container)
    }
}

object ChangeDateView : KScreen<ChangeDateView>() {
    override val layoutId = R.layout.change_date
    override val viewClass = ChangeDateView::class.java

    val switches = KCheckBox {
        withId(R.id.auto_set_date_switch)
        isDisplayed()
    }
}
