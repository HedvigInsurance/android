package com.hedvig.app.common

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import org.hamcrest.Matcher

class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
    val retry = KButton(parent) { withId(R.id.retry) }
}
