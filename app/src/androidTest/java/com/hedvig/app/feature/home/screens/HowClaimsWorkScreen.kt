package com.hedvig.app.feature.home.screens

import com.agoda.kakao.pager.KViewPager
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R

class HowClaimsWorkScreen : Screen<HowClaimsWorkScreen>() {
    val button = KButton { withId(R.id.proceed) }
    val title = KTextView { withId(R.id.title) }
    val pager = KViewPager { withId(R.id.pager) }
}
