package com.hedvig.app.common

import android.view.View
import com.hedvig.app.R
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KButton
import org.hamcrest.Matcher

class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
    val retry = KButton(parent) { withId(R.id.retry) }
}
