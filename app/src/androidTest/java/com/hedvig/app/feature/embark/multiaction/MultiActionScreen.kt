package com.hedvig.app.feature.embark.multiaction

import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionFragment
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object MultiActionScreen : KScreen<MultiActionScreen>() {
    override val layoutId = R.layout.number_action_fragment
    override val viewClass = MultiActionFragment::class.java

    val multiActionList = KRecyclerView(
        { withId(R.id.componentContainer) },
        {
            itemType(MultiActionScreen::AddBuildingButton)
        }
    )

    class AddBuildingButton(parent: Matcher<View>) : KRecyclerItem<AddBuildingButton>(parent) {
        val button = KImageView(parent) { withId(R.id.addButton)}
    }

}
