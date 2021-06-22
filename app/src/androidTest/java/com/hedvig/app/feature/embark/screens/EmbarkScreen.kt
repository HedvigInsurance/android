package com.hedvig.app.feature.embark.screens

import android.view.View
import com.agoda.kakao.dialog.KAlertDialog
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import org.hamcrest.Matcher

class EmbarkScreen : Screen<EmbarkScreen>() {
    val messages = KRecyclerView({ withId(R.id.messages) }, { itemType(::MessageRow) })

    val response = KTextView { withId(R.id.response) }

    val selectActions = KRecyclerView({ withId(R.id.actions) }, { itemType(::SelectAction) })

    val textActionSingleInput = KEditText { withId(R.id.input) }

    val textActionSubmit = KButton { withId(R.id.textActionSubmit) }

    val upgradeApp = KButton { withId(R.id.upgradeApp) }

    val offerActivityIntent = KIntent { hasComponent(OfferActivity::class.java.name) }
    val webOfferIntent = KIntent { hasComponent(WebOnboardingActivity::class.java.name) }

    class MessageRow(parent: Matcher<View>) : KRecyclerItem<MessageRow>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class SelectAction(parent: Matcher<View>) : KRecyclerItem<SelectAction>(parent) {
        val button = KButton { withMatcher(parent) }
    }

    val continueButton = KButton { withId(R.id.continueButton) }

    val previousInsurerButton = KButton { withId(R.id.currentInsurerContainer) }
    val previousInsurerButtonLabel = KTextView { withId(R.id.currentInsurerLabel) }
    val errorDialog = KAlertDialog()
}
