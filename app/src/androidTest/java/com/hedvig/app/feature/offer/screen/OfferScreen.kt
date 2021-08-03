package com.hedvig.app.feature.offer.screen

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.common.ErrorItem
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.util.withParentIndex
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object OfferScreen : KScreen<OfferScreen>() {
    override val layoutId = R.layout.activity_offer
    override val viewClass = OfferActivity::class.java

    val scroll = KRecyclerView(
        { withId(R.id.offerScroll) },
        {
            itemType(OfferScreen::HeaderItem)
            itemType(OfferScreen::SwitcherItem)
            itemType(OfferScreen::Facts)
            itemType(OfferScreen::QuoteDetail)
            itemType(OfferScreen::FAQ)
            itemType(OfferScreen::InfoCard)
            itemType(OfferScreen::WarningCard)
            itemType(::ErrorItem)
        }
    )

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
        val startDateLabel = KTextView(parent) { withId(R.id.startDateLabel) }
        val sign = KButton(parent) { withId(R.id.sign) }
    }

    class SwitcherItem(parent: Matcher<View>) : KRecyclerItem<SwitcherItem>(parent) {
        val associatedQuote = KTextView(parent) { withId(R.id.associatedQuote) }
        val currentInsurer = KTextView(parent) { withId(R.id.currentInsurer) }
    }

    class Facts(parent: Matcher<View>) : KRecyclerItem<Facts>(parent) {
        val expandableContent = KView(parent) { withId(R.id.expandableContentView) }
    }

    class QuoteDetail(parent: Matcher<View>) : KRecyclerItem<QuoteDetail>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class FAQ(private val parent: Matcher<View>) : KRecyclerItem<FAQ>(parent) {
        val title = KTextView(parent) { withText(R.string.offer_faq_title) }

        fun faqRow(index: Int, function: KTextView.() -> Unit) = KTextView(parent) {
            withParent { withId(R.id.rowContainer) }
            withParentIndex(index)
        }.invoke(function)
    }

    class InfoCard(parent: Matcher<View>) : KRecyclerItem<InfoCard>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val body = KTextView(parent) { withId(R.id.body) }

        fun isShown() {
            title { hasText(R.string.offer_switch_info_card_title) }
            body { hasText(R.string.offer_switch_info_card_body) }
        }
    }

    class WarningCard(parent: Matcher<View>) : KRecyclerItem<WarningCard>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val body = KTextView(parent) { withId(R.id.body) }

        fun isShown() {
            title { hasText(R.string.offer_manual_switch_card_title) }
            body { hasText(R.string.offer_manual_switch_card_body) }
        }
    }
}
