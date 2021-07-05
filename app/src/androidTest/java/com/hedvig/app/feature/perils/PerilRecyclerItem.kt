package com.hedvig.app.feature.perils

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class PerilRecyclerItem(parent: Matcher<View>) : KRecyclerItem<PerilRecyclerItem>(parent) {
    val label = KTextView(parent) { withId(R.id.label) }
}
