package com.hedvig.app.feature.documents

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KButton
import org.hamcrest.Matcher

class DocumentRecyclerItem(parent: Matcher<View>) : KRecyclerItem<DocumentRecyclerItem>(parent) {
    val button = KButton { withMatcher(parent) }
}
