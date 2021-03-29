package com.hedvig.app.feature.embark.multiaction

import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.kaspersky.kaspresso.screens.KScreen

object AddBuildingBottomSheetScreen : KScreen<AddBuildingBottomSheetScreen>() {
    override val layoutId = R.layout.dialog_add_building
    override val viewClass = AddComponentBottomSheet::class.java

    val dropDownMenu = KTextView { withId(R.id.dropdown_menu) }

}
