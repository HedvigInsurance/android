package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object TextActionSetScreen : KScreen<TextActionSetScreen>() {
    override val layoutId = R.layout.fragment_text_action_set
    override val viewClass = TextActionFragment::class.java

    val submit = KButton { withId(R.id.textActionSubmit) }
}
