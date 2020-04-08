package com.hedvig.app.feature.offer.binders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.feature.dashboard.ui.contractcoverage.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.TermsAdapter
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.offer_terms_area.view.*

class TermsBinder(
    private val root: LinearLayout,
    tracker: OfferTracker
) {
    private var previousData: OfferQuery.Data? = null

    init {
        root.insurableLimits.adapter = InsurableLimitsAdapter()
        root.insurableLimits.addItemDecoration((GridSpacingItemDecoration(BASE_MARGIN_DOUBLE)))
        root.termsRecyclerView.adapter = TermsAdapter(tracker)
    }

    fun bind(data: OfferQuery.Data) = root.apply {
        if (data == previousData) {
            return@apply
        }
        val quote = data.lastQuoteOfMember.asCompleteQuote

        quote?.insurableLimits?.let { limits ->
            (insurableLimits.adapter as InsurableLimitsAdapter).items =
                limits.map { it.fragments.insurableLimitsFragment }
        }

        quote?.insuranceTerms?.let { terms ->
            (termsRecyclerView.adapter as TermsAdapter).items = terms
        }
    }
}
