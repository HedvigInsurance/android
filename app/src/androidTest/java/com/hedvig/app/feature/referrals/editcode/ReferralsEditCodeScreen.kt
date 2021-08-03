package com.hedvig.app.feature.referrals.editcode

import android.widget.ImageButton
import com.hedvig.app.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.dialog.KAlertDialog
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton

class ReferralsEditCodeScreen : Screen<ReferralsEditCodeScreen>() {
    val editLayout = KTextInputLayout { withId(R.id.codeContainer) }
    val save = KButton { withId(R.id.save) }

    val confirmDismiss = KAlertDialog()
    val up = KView {
        withParent { withId(R.id.toolbar) }
        isInstanceOf(ImageButton::class.java)
    }
}
