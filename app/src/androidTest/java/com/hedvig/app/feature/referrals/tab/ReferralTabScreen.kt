package com.hedvig.app.feature.referrals.tab

import android.view.View
import com.hedvig.app.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.swiperefresh.KSwipeRefreshLayout
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KSnackbar
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class ReferralTabScreen : Screen<ReferralTabScreen>() {
    val moreInfo = KButton { withId(R.id.referralMoreInfo) }
    val share = KButton { withId(R.id.share) }

    val codeCopied = KSnackbar()

    val swipeToRefresh = KSwipeRefreshLayout { withId(R.id.swipeToRefresh) }

    val recycler = KRecyclerView(
        { withId(R.id.invites) },
        itemTypeBuilder = {
            itemType(ReferralTabScreen::HeaderItem)
            itemType(ReferralTabScreen::CodeItem)
            itemType(ReferralTabScreen::InvitesHeaderItem)
            itemType(ReferralTabScreen::ReferralItem)
            itemType(ReferralTabScreen::ErrorItem)
        }
    )

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val discountPerMonthPlaceholder =
            KImageView(parent) { withId(R.id.discountPerMonthPlaceholder) }
        val newPricePlaceholder = KImageView(parent) { withId(R.id.newPricePlaceholder) }

        val grossPrice = KTextView(parent) { withId(R.id.grossPrice) }

        val discountPerMonth = KTextView(parent) { withId(R.id.discountPerMonth) }
        val newPrice = KTextView(parent) { withId(R.id.newPrice) }

        val discountPerMonthLabel = KTextView(parent) { withId(R.id.discountPerMonthLabel) }
        val newPriceLabel = KTextView(parent) { withId(R.id.newPriceLabel) }

        val emptyHeadline = KTextView(parent) { withId(R.id.emptyHeadline) }
        val emptyBody = KTextView(parent) { withId(R.id.emptyBody) }

        val otherDiscountBox = KView(parent) { withId(R.id.otherDiscountBox) }
    }

    class CodeItem(parent: Matcher<View>) : KRecyclerItem<CodeItem>(parent) {
        val placeholder = KImageView(parent) { withId(R.id.codePlaceholder) }
        val code = KTextView(parent) { withId(R.id.code) }
        val edit = KButton(parent) { withId(R.id.edit) }
    }

    class InvitesHeaderItem(parent: Matcher<View>) : KRecyclerItem<InvitesHeaderItem>(parent)

    class ReferralItem(parent: Matcher<View>) : KRecyclerItem<ReferralItem>(parent) {
        val iconPlaceholder = KImageView(parent) { withId(R.id.iconPlaceholder) }
        val textPlaceholder = KImageView(parent) { withId(R.id.textPlaceholder) }

        val icon = KImageView(parent) { withId(R.id.icon) }
        val name = KTextView(parent) { withId(R.id.name) }
        val referee = KTextView(parent) { withId(R.id.refereeLabel) }
        val status = KTextView(parent) { withId(R.id.status) }
    }

    class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
        val errorTitle = KTextView(parent) { withId(R.id.errorTitle) }
        val errorParagraph = KTextView(parent) { withId(R.id.errorParagraph) }
        val retry = KTextView(parent) { withId(R.id.retry) }
    }
}
