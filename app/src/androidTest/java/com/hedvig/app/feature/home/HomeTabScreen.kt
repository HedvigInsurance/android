package com.hedvig.app.feature.home

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class HomeTabScreen : Screen<HomeTabScreen>() {
    val recycler =
        KRecyclerView({ withId(R.id.recycler) },
            {
                itemType(::BigTextItem)
                itemType(::BodyTextItem)
                itemType(::StartClaimItem)
                itemType(::CommonClaimTitleItem)
                itemType(::ErrorItem)
            })

    class BigTextItem(parent: Matcher<View>) : KRecyclerItem<BigTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class BodyTextItem(parent: Matcher<View>) : KRecyclerItem<BodyTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class StartClaimItem(parent: Matcher<View>) : KRecyclerItem<StartClaimItem>(parent) {
        val button = KButton { withMatcher(parent) }
    }

    class CommonClaimTitleItem(parent: Matcher<View>) :
        KRecyclerItem<CommonClaimTitleItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
        val retry = KButton { withId(R.id.retry) }
    }
}
