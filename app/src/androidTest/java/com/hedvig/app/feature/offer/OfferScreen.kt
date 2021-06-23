package com.hedvig.app.feature.offer

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.dialog.KAlertDialog
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.util.KMaterialDatePicker
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

class OfferScreen : Screen<OfferScreen>() {

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
        val startDateLabel = KTextView(parent) { withId(R.id.startDateLabel) }
    }

    class SwitcherItem(parent: Matcher<View>) : KRecyclerItem<SwitcherItem>(parent) {
        val title = KTextView(parent) { withId(R.id.switchTitle) }
    }

    class Facts(parent: Matcher<View>) : KRecyclerItem<Facts>(parent) {
        val expandableContent = KView(parent) { withId(R.id.expandableContentView) }
    }
}

class ChangeDateBottomSheetScreen : Screen<ChangeDateBottomSheetScreen>() {
    val materialDatePicker = KMaterialDatePicker()
    val submit = KButton { withId(R.id.chooseDateButton) }
    val changeDateContainer = KView {
        withId(R.id.change_date_container)
    }
    val confirmSubmit = KAlertDialog()
}

class ChangeDateView : KScreen<ChangeDateView>() {
    override val layoutId = R.layout.change_date
    override val viewClass = ChangeDateView::class.java
}
