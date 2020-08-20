package com.hedvig.app.feature.home.screens

import android.view.View
import com.agoda.kakao.image.KImageView
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
                itemType(HomeTabScreen::BigTextItem)
                itemType(HomeTabScreen::BodyTextItem)
                itemType(HomeTabScreen::StartClaimItem)
                itemType(HomeTabScreen::CommonClaimTitleItem)
                itemType(HomeTabScreen::CommonClaimItem)
                itemType(HomeTabScreen::ErrorItem)
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

    class CommonClaimItem(parent: Matcher<View>) : KRecyclerItem<CommonClaimItem>(parent) {
        val icon = KImageView(parent) { withId(R.id.icon) }
        val text = KTextView(parent) { withId(R.id.label) }
    }

    class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }
}
