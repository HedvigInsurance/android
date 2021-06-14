package com.hedvig.app.util

import com.agoda.kakao.common.views.KBaseView
import com.agoda.kakao.text.KButton

class KMaterialDatePicker : KBaseView<KMaterialDatePicker>({ isRoot() }) {

    init {
        inRoot { isDialog() }
    }

    val positiveButton = KButton { withId(com.google.android.material.R.id.confirm_button) }
        .also { it.inRoot { isDialog() } }

    val negativeButton = KButton { withId(com.google.android.material.R.id.cancel_button) }
        .also { it.inRoot { isDialog() } }
}
