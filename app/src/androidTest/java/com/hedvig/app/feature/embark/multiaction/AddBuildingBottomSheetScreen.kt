package com.hedvig.app.feature.embark.multiaction

import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.kaspersky.kaspresso.screens.KScreen

object AddBuildingBottomSheetScreen : KScreen<AddBuildingBottomSheetScreen>() {
    override val layoutId = R.layout.dialog_add_building
    override val viewClass = AddComponentBottomSheet::class.java

    val dropDownMenu = KTextView { withId(R.id.dropdownLayout) }
    val dropDownInput = KEditText { withId(R.id.dropdownInput) }

    val numberLayout = KTextInputLayout { withId(R.id.numberLayout) }
    val numberInput = KEditText { withId(R.id.numberInput) }
}
