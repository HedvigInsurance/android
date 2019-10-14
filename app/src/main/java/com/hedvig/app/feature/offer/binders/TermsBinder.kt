package com.hedvig.app.feature.offer.binders

import androidx.constraintlayout.widget.ConstraintLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isHouse
import com.hedvig.app.util.isStudentInsurance
import kotlinx.android.synthetic.main.offer_section_terms.view.*

class TermsBinder(
    private val root: ConstraintLayout
) {
    private var previousData: OfferQuery.Insurance? = null
    fun bind(data: OfferQuery.Insurance) = root.apply {
        if (previousData == data) {
            return@apply
        }

        data.type?.let { insuranceType ->
            if (insuranceType.isHouse) {
                noLimit.text = resources.getString(R.string.OFFER_HOUSE_TRUST_HOUSE)
                maxCompensation.text = interpolateTextKey(
                    resources.getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                    "MAX_COMPENSATION" to resources.getString(R.string.HOUSE_INFO_COMPENSATION_GADGETS)
                )
                deductibleExceptions.show()
            } else {
                noLimit.text = resources.getString(R.string.OFFER_TERMS_NO_COVERAGE_LIMIT)
                maxCompensation.text = interpolateTextKey(
                    resources.getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                    "MAX_COMPENSATION" to if (insuranceType.isStudentInsurance) {
                        resources.getString(R.string.MAX_COMPENSATION_STUDENT)
                    } else {
                        resources.getString(R.string.MAX_COMPENSATION)
                    }
                )
                deductibleExceptions.remove()
            }
            deductible.text = interpolateTextKey(
                resources.getString(R.string.OFFER_TERMS_DEDUCTIBLE),
                "DEDUCTIBLE" to resources.getString(R.string.DEDUCTIBLE)
            )
        }
    }
}
