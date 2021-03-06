package com.hedvig.app.feature.home.screens

import android.content.Intent
import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import org.hamcrest.Matcher

class HomeTabScreen : Screen<HomeTabScreen>() {
    val recycler =
        KRecyclerView(
            { withId(R.id.recycler) },
            {
                itemType(::HomePSAItem)
                itemType(::HowClaimsWork)
                itemType(::UpcomingRenewal)
                itemType(::BigTextItem)
                itemType(::BodyTextItem)
                itemType(::StartClaimItem)
                itemType(::InfoCardItem)
                itemType(::CommonClaimTitleItem)
                itemType(::CommonClaimItem)
                itemType(::ErrorItem)
                itemType(::ChangeAddressItem)
            }
        )

    class HomePSAItem(parent: Matcher<View>) : KRecyclerItem<HomePSAItem>(parent) {
        val text = KTextView(parent) { withId(R.id.body) }
        val button = KImageView(parent) { withId(R.id.arrow) }
        val psaLink = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }

    class HowClaimsWork(parent: Matcher<View>) : KRecyclerItem<HowClaimsWork>(parent) {
        val button = KButton(parent) { withId(R.id.button) }
    }

    class UpcomingRenewal(parent: Matcher<View>) : KRecyclerItem<UpcomingRenewal>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val button = KButton(parent) { withId(R.id.action) }
        val link = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }

    class BigTextItem(parent: Matcher<View>) : KRecyclerItem<BigTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class BodyTextItem(parent: Matcher<View>) : KRecyclerItem<BodyTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class StartClaimItem(parent: Matcher<View>) : KRecyclerItem<StartClaimItem>(parent) {
        val button = KButton { withMatcher(parent) }
    }

    class InfoCardItem(parent: Matcher<View>) : KRecyclerItem<InfoCardItem>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val body = KTextView(parent) { withId(R.id.body) }
        val action = KButton(parent) { withId(R.id.action) }

        val connectPayinTrustly = KIntent {
            hasComponent(TrustlyConnectPayinActivity::class.java.name)
        }

        val connectPayinAdyen =
            KIntent { hasComponent(AdyenConnectPayinActivity::class.java.name) }
    }

    class CommonClaimTitleItem(parent: Matcher<View>) :
        KRecyclerItem<CommonClaimTitleItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class CommonClaimItem(parent: Matcher<View>) : KRecyclerItem<CommonClaimItem>(parent) {
        val text = KTextView(parent) { withId(R.id.label) }
    }

    class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }

    class ChangeAddressItem(parent: Matcher<View>) : KRecyclerItem<ChangeAddressItem>(parent) {
        val changeAddressButton = KTextView(parent) { withMatcher(parent) }
    }
}
