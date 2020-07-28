package com.hedvig.app.feature.offer

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.dialog.KAlertDialog
import com.agoda.kakao.picker.date.KDatePickerDialog
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class OfferScreen : Screen<OfferScreen>() {

    val scroll = KRecyclerView({ withId(R.id.offerScroll) }, {
        itemType(::HeaderItem)
        itemType(::SwitcherItem)
    })

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
    }

    class SwitcherItem(parent: Matcher<View>) : KRecyclerItem<SwitcherItem>(parent) {
        val title = KTextView(parent) { withId(R.id.switchTitle) }
    }
}

class ChangeDateSheet : Screen<ChangeDateSheet>() {
    val autoSetDate = KButton { withId(R.id.autoSetDateText) }
    val pickDate = KView { withId(R.id.datePickButton) }
    val datePicker = KDatePickerDialog()
    val submit = KButton { withId(R.id.chooseDateButton) }
    val confirm = KAlertDialog()
}
