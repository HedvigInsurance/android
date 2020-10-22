package com.hedvig.app.feature.insurance.detail

import android.content.Intent
import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.pager2.KViewPager2
import com.agoda.kakao.pager2.KViewPagerItem
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class ContractDetailScreen : Screen<ContractDetailScreen>() {
    val tabContent = KViewPager2({ withId(R.id.tabContent) }, {
        itemType(::YourInfoTab)
        itemType(::DocumentsTab)
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

    class DocumentsTab(parent: Matcher<View>) : KViewPagerItem<DocumentsTab>(parent) {
        val recycler = KRecyclerView(parent, { withId(R.id.recycler) }, {
            itemType(::Button)
        })

        val agreementUrl = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }

        class Button(parent: Matcher<View>) : KRecyclerItem<Button>(parent) {
            val button = KButton { withMatcher(parent) }
        }
    }
}
