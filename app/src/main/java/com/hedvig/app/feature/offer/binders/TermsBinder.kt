package com.hedvig.app.feature.offer.binders

import android.view.LayoutInflater
import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.dashboard.ui.contractcoverage.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.TermsAdapter
import kotlinx.android.synthetic.main.offer_terms_area.view.*

class TermsBinder(
    private val root: LinearLayout,
    private val tracker: OfferTracker
) {

    private var previousData: OfferQuery.Data? = null

    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(root.context)
    }

    fun bind(data: OfferQuery.Data) = root.apply {
        if (data == previousData) {
            return@apply
        }
        val quote = data.lastQuoteOfMember.asCompleteQuote

        insurableLimits.adapter = InsurableLimitsAdapter()
        quote?.insurableLimits?.let { limits ->
            (insurableLimits.adapter as InsurableLimitsAdapter).items =
                limits.map { it.fragments.insurableLimitsFragment }
        }

        termsRecyclerView.adapter = TermsAdapter(tracker)
        quote?.insuranceTerms?.let { terms ->
            (termsRecyclerView.adapter as TermsAdapter).items = terms
        }
    }
}
