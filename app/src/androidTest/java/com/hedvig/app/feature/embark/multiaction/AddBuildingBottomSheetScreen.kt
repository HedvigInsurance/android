package com.hedvig.app.feature.embark.multiaction

import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAddBuildingBinding
import com.hedvig.app.feature.embark.passages.multiaction.AddBuildingBottomSheet
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionFragment
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object AddBuildingBottomSheetScreen : KScreen<AddBuildingBottomSheetScreen>() {
    override val layoutId = R.layout.dialog_add_building
    override val viewClass = AddBuildingBottomSheet::class.java

    val dropDownMenu = KTextView { withId(R.id.dropdown_menu) }

}
