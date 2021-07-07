package com.hedvig.app.feature.offer.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.util.withParentIndex
import com.kaspersky.kaspresso.screens.KScreen
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
        }
    )

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val startDate = KTextView(parent) { withId(R.id.startDate) }
        val startDateLabel = KTextView(parent) { withId(R.id.startDateLabel) }
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
    }

    class WarningCard(parent: Matcher<View>) : KRecyclerItem<WarningCard>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val body = KTextView(parent) { withId(R.id.body) }
    }
}

