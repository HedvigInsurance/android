package com.hedvig.app.feature.home.screens

import com.hedvig.app.R
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView

class CommonClaimScreen : Screen<CommonClaimScreen>() {
    val firstMessage =
        KTextView { withId(R.id.commonClaimFirstMessage) }
}
