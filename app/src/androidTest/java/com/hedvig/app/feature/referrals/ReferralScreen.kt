package com.hedvig.app.feature.referrals

import android.content.Intent
import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KSnackbar
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class ReferralScreen : Screen<ReferralScreen>() {
    val moreInfo = KButton { withId(R.id.referralMoreInfo) }
    val share = KButton { withId(R.id.share) }
    val shareIntent = KIntent {
        hasAction(Intent.ACTION_CHOOSER)
    }

    val codeCopied = KSnackbar()

    val recycler = KRecyclerView({ withId(R.id.invites) }, itemTypeBuilder = {
        itemType(::HeaderItem)
        itemType(::CodeItem)
        itemType(::InvitesHeaderItem)
        itemType(::ReferralItem)
        itemType(::ErrorItem)
    })

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
