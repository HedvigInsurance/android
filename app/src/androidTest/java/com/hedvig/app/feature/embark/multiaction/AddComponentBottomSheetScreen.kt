package com.hedvig.app.feature.embark.multiaction

import android.widget.ListView
import androidx.test.espresso.DataInteraction
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.list.KAbsListView
import com.agoda.kakao.list.KAdapterItem
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.kaspersky.kaspresso.screens.KScreen

object AddComponentBottomSheetScreen : KScreen<AddComponentBottomSheetScreen>() {
    override val layoutId = R.layout.add_component_bottom_sheet
    override val viewClass = AddComponentBottomSheet::class.java

    val dropDownMenu = KTextView { withId(R.id.dropdownLayout) }
    val numberInput = KEditText { withId(R.id.numberInput) }
    val list = KAbsListView(
        builder = { isInstanceOf(ListView::class.java) },
        itemTypeBuilder = { itemType(::Item) })

    class Item(i: DataInteraction) : KAdapterItem<Item>(i) {
        val text = KTextView(i) { withId(R.id.dropdown_popup) }
    }

}
