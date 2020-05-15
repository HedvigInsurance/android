package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class EmbarkScreen : Screen<EmbarkScreen>() {
    val spinner = KView { withId(R.id.loadingSpinner) }
    val messages =
        KRecyclerView({ withId(R.id.messages) }, { itemType(::MessageRow) })

    val response = KTextView { withId(R.id.response) }

    val selectActions =
        KRecyclerView({ withId(R.id.actions) }, { itemType(::SelectAction) })

    val textActionInput = KEditText { withId(R.id.textActionInput) }
    val textActionSubmit = KButton { withId(R.id.textActionSubmit) }

    class MessageRow(parent: Matcher<View>) : KRecyclerItem<MessageRow>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class SelectAction(parent: Matcher<View>) : KRecyclerItem<SelectAction>(parent) {
        val button = KButton { withMatcher(parent) }
    }
}
