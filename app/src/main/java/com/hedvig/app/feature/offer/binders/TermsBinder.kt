package com.hedvig.app.feature.offer.binders

import android.view.LayoutInflater
import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.OfferTracker
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

        quote?.insurableLimits?.indices?.let {
            for (i in it step 2) {
                val limit = quote.insurableLimits[i]
                val secondLimit = quote.insurableLimits.getOrNull(i + 1)
                val limitRow = layoutInflater.inflate(
                    R.layout.offer_limit_row,
                    limitsContainer,
                    false
                ) as LinearLayout

                limitRow.offerLimitDescription1.text = limit.description
                limitRow.offerLimit1.text = limit.limit

                secondLimit?.let {
                    limitRow.offerLimitDescription2.text = it.description
                    limitRow.offerLimit2.text = it.limit
                }
                limitsContainer.addView(limitRow)
            }
        }

        termsButtonContainer.removeAllViews()
        // quote?.insuranceTerms?.forEach { terms ->
        //     val button = layoutInflater.inflate(
        //         R.layout.offer_terms_area_button,
        //         termsButtonContainer,
        //         false
        //     ) as MaterialButton
        //     button.text = terms.displayName
        //     button.setHapticClickListener {
        //         tracker.openOfferLink(terms.displayName)
        //         it.context.openUri(Uri.parse(terms.url))
        //     }
        //     termsButtonContainer.addView(button)
        // }
        previousData = data
    }
}
