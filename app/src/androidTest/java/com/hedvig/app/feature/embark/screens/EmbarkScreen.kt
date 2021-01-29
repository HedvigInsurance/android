package com.hedvig.app.feature.embark.screens

import android.content.Intent
import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class EmbarkScreen : Screen<EmbarkScreen>() {
    val spinner = KView { withId(R.id.loadingSpinner) }
    val messages = KRecyclerView({ withId(R.id.messages) }, { itemType(::MessageRow) })
    val textActionSet = KRecyclerView({ withId(R.id.inputRecycler) },
        { itemType(::TextAction) })

    val response = KTextView { withId(R.id.response) }

    val selectActions = KRecyclerView({ withId(R.id.actions) }, { itemType(::SelectAction) })

    val textActionSingleInput = KEditText { withId(R.id.input) }

    val textActionSubmit = KButton { withId(R.id.textActionSubmit) }

    val numberInput = KTextInputLayout { withId(R.id.inputContainer) }
    val submit = KButton { withId(R.id.submit) }

    val upgradeApp = KButton { withId(R.id.upgradeApp) }
    val playStoreIntent = KIntent {
        hasAction(Intent.ACTION_VIEW)
        hasData {
            hasScheme("market")
        }
    }

    class MessageRow(parent: Matcher<View>) : KRecyclerItem<MessageRow>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class TextAction(parent: Matcher<View>) : KRecyclerItem<TextAction>(parent) {
        val input = KEditText(parent) { withId(R.id.input) }
    }

    class SelectAction(parent: Matcher<View>) : KRecyclerItem<SelectAction>(parent) {
        val button = KButton { withMatcher(parent) }
    }

    val continueButton = KButton { withId(R.id.continueButton) }

    val previousInsurerButton = KButton { withId(R.id.currentInsurerContainer) }
    val previousInsurerButtonLabel = KTextView { withId(R.id.currentInsurerLabel) }
}
