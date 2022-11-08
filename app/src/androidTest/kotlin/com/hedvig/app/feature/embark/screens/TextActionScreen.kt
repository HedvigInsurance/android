package com.hedvig.app.feature.embark.screens

import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton

object TextActionScreen : KScreen<TextActionScreen>() {
  override val layoutId = R.layout.fragment_embark_text_action
  override val viewClass = TextActionFragment::class.java

  val input = KTextInputLayout { withId(R.id.textField) }
  val submitButton = KButton { withId(R.id.textActionSubmit) }
  val loginButton = KView { withText(com.google.android.gms.base.R.string.common_signin_button_text) }
  val appInfoButton = KView { withText(hedvig.resources.R.string.onboarding_contextual_menu_app_info_label) }
  val settingsButton = KView { withText(hedvig.resources.R.string.profile_appSettingsSection_title) }
  val restartButton = KView { withText(hedvig.resources.R.string.EMBARK_RESTART_BUTTON) }
  val okButton = KView { withText(hedvig.resources.R.string.ALERT_OK) }
}
