package com.hedvig.app.feature.offer.binders

import android.net.Uri
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.contractcoverage.InsurableLimitsAdapter
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.offer_limit_row.view.*
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

        quote?.insurableLimits?.indices?.let {
            for (i in it step 2) {
                val limit = quote.insurableLimits[i]
                val secondLimit = quote.insurableLimits.getOrNull(i + 1)
                val limitRow = layoutInflater.inflate(
                    R.layout.offer_limit_row,
                    limitsContainer,
                    false
                ) as LinearLayout

                limitRow.offerLimitDescription1.text =
                    limit.fragments.insurableLimitsFragment.description
                limitRow.offerLimit1.text = limit.fragments.insurableLimitsFragment.limit

                secondLimit?.let {
                    limitRow.offerLimitDescription2.text =
                        it.fragments.insurableLimitsFragment.description
                    limitRow.offerLimit2.text = it.fragments.insurableLimitsFragment.limit
                }
                limitsContainer.addView(limitRow)
            }
        }

        termsButtonContainer.removeAllViews()
        quote?.insuranceTerms?.forEach { terms ->
            val button = layoutInflater.inflate(
                R.layout.offer_terms_area_button,
                termsButtonContainer,
                false
            ) as MaterialButton
            button.text = terms.displayName
            button.setHapticClickListener {
                tracker.openOfferLink(terms.displayName)
                it.context.openUri(Uri.parse(terms.url))
            }
            termsButtonContainer.addView(button)
        }
        previousData = data
    }
}
