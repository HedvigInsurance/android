package com.hedvig.app.feature.perils

import android.view.View
import com.hedvig.app.R
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class PerilRecyclerItem(parent: Matcher<View>) : KRecyclerItem<PerilRecyclerItem>(parent) {
    val label = KTextView(parent) { withId(R.id.label) }
}
