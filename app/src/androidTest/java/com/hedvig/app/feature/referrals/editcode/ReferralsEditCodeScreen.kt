package com.hedvig.app.feature.referrals.editcode

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R

class ReferralsEditCodeScreen : Screen<ReferralsEditCodeScreen>() {
    val editLayout = KTextInputLayout { withId(R.id.codeContainer) }
    val save = KButton { withId(R.id.save) }
}
