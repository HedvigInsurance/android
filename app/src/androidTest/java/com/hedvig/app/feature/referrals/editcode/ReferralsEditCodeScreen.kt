package com.hedvig.app.feature.referrals.editcode

import android.widget.ImageButton
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.dialog.KAlertDialog
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R

class ReferralsEditCodeScreen : Screen<ReferralsEditCodeScreen>() {
    val editLayout = KTextInputLayout { withId(R.id.codeContainer) }
    val save = KButton { withId(R.id.save) }

    val confirmDismiss = KAlertDialog()
    val up = KView {
        withParent { withId(R.id.toolbar) }
        isInstanceOf(ImageButton::class.java)
    }
}
