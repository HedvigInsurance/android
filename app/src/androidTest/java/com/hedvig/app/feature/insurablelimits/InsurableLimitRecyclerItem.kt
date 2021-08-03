package com.hedvig.app.feature.insurablelimits

import android.view.View
import com.hedvig.app.R
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class InsurableLimitRecyclerItem(parent: Matcher<View>) : KRecyclerItem<InsurableLimitRecyclerItem>(parent) {
    val label = KTextView(parent) { withId(R.id.label) }
    val content = KTextView(parent) { withId(R.id.content) }
}
