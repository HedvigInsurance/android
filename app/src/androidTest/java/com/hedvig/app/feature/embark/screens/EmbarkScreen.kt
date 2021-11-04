package com.hedvig.app.feature.embark.screens

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import io.github.kakaocup.kakao.dialog.KAlertDialog
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class EmbarkScreen : Screen<EmbarkScreen>() {
    val messages = KRecyclerView({ withId(R.id.messages) }, { itemType(::MessageRow) })

    val response = KTextView { withId(R.id.response) }

    val singleSelectAction = KButton { withId(R.id.singleActionButton) }

    val textActionSingleInput = KEditText { withId(R.id.input) }

    val textActionSubmit = KButton { withId(R.id.textActionSubmit) }

    val upgradeApp = KButton { withId(R.id.upgradeApp) }

    val offerActivityIntent = KIntent { hasComponent(OfferActivity::class.java.name) }
    val webOfferIntent = KIntent { hasComponent(WebOnboardingActivity::class.java.name) }

    class MessageRow(parent: Matcher<View>) : KRecyclerItem<MessageRow>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    val continueButton = KButton { withId(R.id.continueButton) }

    val previousInsurerButton = KButton { withId(R.id.currentInsurerContainer) }
    val previousInsurerButtonLabel = KTextView { withId(R.id.currentInsurerLabel) }
    val errorDialog = KAlertDialog()
}
