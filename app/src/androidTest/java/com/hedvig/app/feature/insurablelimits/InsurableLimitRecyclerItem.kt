package com.hedvig.app.feature.insurablelimits

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class InsurableLimitRecyclerItem(parent: Matcher<View>) : KRecyclerItem<InsurableLimitRecyclerItem>(parent) {
    val label = KTextView(parent) { withId(R.id.label) }
    val content = KTextView(parent) { withId(R.id.content) }
}
