package com.hedvig.app.feature.referrals

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R

class ReferralScreen : Screen<ReferralScreen>() {
    val moreInfo = KButton { withId(R.id.referralMoreInfo) }
    val invites = KView { withId(R.id.invites) }
}
