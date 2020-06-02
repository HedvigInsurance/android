package com.hedvig.app.feature.referrals

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R

class ReferralScreen : Screen<ReferralScreen>() {
    val invites = KView { withId(R.id.invites) }
}
