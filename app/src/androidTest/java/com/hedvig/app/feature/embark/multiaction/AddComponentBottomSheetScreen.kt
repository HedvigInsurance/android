package com.hedvig.app.feature.embark.multiaction

import android.widget.ListView
import androidx.test.espresso.DataInteraction
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.list.KAbsListView
import io.github.kakaocup.kakao.list.KAdapterItem
import io.github.kakaocup.kakao.text.KTextView

object AddComponentBottomSheetScreen : KScreen<AddComponentBottomSheetScreen>() {
    override val layoutId = R.layout.add_component_bottom_sheet
    override val viewClass = AddComponentBottomSheet::class.java

    val dropDownMenu = KTextView { withId(R.id.dropdownLayout) }
    val numberInput = KEditText { withId(R.id.numberInput) }
    val list = KAbsListView(
        builder = { isInstanceOf(ListView::class.java) },
        itemTypeBuilder = { itemType(::Item) }
    )

    class Item(i: DataInteraction) : KAdapterItem<Item>(i) {
        val text = KTextView(i) { withId(R.id.dropdown_popup) }
    }
}
