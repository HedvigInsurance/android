package com.hedvig.app.feature.embark.screens

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object TextActionScreen : KScreen<TextActionScreen>() {
    override val layoutId = R.layout.fragment_embark_text_action
    override val viewClass = TextActionFragment::class.java

    val input = KTextInputLayout { withId(R.id.textField) }
    val submitButton = KButton { withId(R.id.textActionSubmit) }
    val loginButton = KView { withText(R.string.common_signin_button_text) }
    val appInfoButton = KView { withText(R.string.onboarding_contextual_menu_app_info_label) }
    val settingsButton = KView { withText(R.string.profile_appSettingsSection_title) }
    val restartButton = KView { withText(R.string.EMBARK_RESTART_BUTTON) }
    val okButton = KView { withText(R.string.ALERT_OK) }
}
