package com.hedvig.app.feature.home.screens

import com.hedvig.app.R
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

class HowClaimsWorkScreen : Screen<HowClaimsWorkScreen>() {
    val button = KButton { withId(R.id.proceed) }
    val title = KTextView { withId(R.id.title) }
}
