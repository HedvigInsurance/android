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
        itemType(::CoverageTab)
    })

    class CoverageTab(parent: Matcher<View>) : KViewPagerItem<CoverageTab>(parent) {
        val recycler = KRecyclerView(parent, { withId(R.id.recycler) }, {
            itemType(::Header)
            itemType(::Peril)
            itemType(::Row)
        })

        class Header(parent: Matcher<View>) : KRecyclerItem<Header>(parent) {
            val text = KTextView { withMatcher(parent) }
        }

        class Peril(parent: Matcher<View>) : KRecyclerItem<Peril>(parent)

        class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
            val label = KTextView(parent) { withId(R.id.label) }
            val content = KTextView(parent) { withId(R.id.content) }
        }

        class PerilBottomSheetScreen : Screen<PerilBottomSheetScreen>() {
            val body = KTextView { withId(R.id.body) }
        }
    }

    class YourInfoTab(parent: Matcher<View>) : KViewPagerItem<YourInfoTab>(parent) {
        val recycler = KRecyclerView(parent, { withId(R.id.recycler) }, {
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
