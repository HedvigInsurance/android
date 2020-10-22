package com.hedvig.app.feature.insurance.detail

import android.view.View
import com.agoda.kakao.pager2.KViewPager2
import com.agoda.kakao.pager2.KViewPagerItem
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class ContractDetailScreen : Screen<ContractDetailScreen>() {
    val tabContent = KViewPager2({ withId(R.id.tabContent) }, {
        itemType(::YourInfoTab)
    })

    class YourInfoTab(parent: Matcher<View>) : KViewPagerItem<YourInfoTab>(parent) {
        val recycler = KRecyclerView({ withId(R.id.recycler) }, {
            itemType(::Header)
            itemType(::Row)
        })

        class Header(parent: Matcher<View>) : KRecyclerItem<Header>(parent) {
            val text = KTextView { withMatcher(parent) }
        }

        class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
            val label = KTextView(parent) { withId(R.id.label) }
            val content = KTextView(parent) { withId(R.id.content) }
        }
    }
}
