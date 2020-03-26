package com.hedvig.app.feature.offer.binders

import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.extensions.toContractType
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.offer_terms_area.view.*

class TermsBinder(
    private val root: LinearLayout,
    private val tracker: OfferTracker
) {

    private var previousInsuranceData: OfferQuery.Insurance? = null
    private var previousQuoteData: OfferQuery.AsCompleteQuote? = null

    fun bind(insurance: OfferQuery.Insurance, quote: OfferQuery.AsCompleteQuote) = root.apply {
        /*if (previousInsuranceData == insurance && previousQuoteData == quote) {
            return@apply
        }

        quote.toContractType().let { contractType ->
            when (contractType) {
                TypeOfContract.SE_HOUSE -> {
                    noLimit.text = resources.getString(R.string.OFFER_HOUSE_TRUST_HOUSE)
                    maxCompensation.text = interpolateTextKey(
                        resources.getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                        "MAX_COMPENSATION" to resources.getString(R.string.HOUSE_INFO_COMPENSATION_GADGETS)
                    )
                    deductibleExceptions.show()
                }
                TypeOfContract.SE_APARTMENT_BRF,
                TypeOfContract.SE_APARTMENT_RENT-> {
                    noLimit.text = resources.getString(R.string.OFFER_TERMS_NO_COVERAGE_LIMIT)
                    maxCompensation.text = interpolateTextKey(
                        resources.getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                        "MAX_COMPENSATION" to
                            resources.getString(R.string.MAX_COMPENSATION)
                    )
                    deductibleExceptions.remove()
                }
                TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                    noLimit.text = resources.getString(R.string.OFFER_TERMS_NO_COVERAGE_LIMIT)
                    maxCompensation.text = interpolateTextKey(
                        resources.getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                        "MAX_COMPENSATION" to
                            resources.getString(R.string.MAX_COMPENSATION_STUDENT)
                    )
                    deductibleExceptions.remove()
                }
                TypeOfContract.NO_HOME_CONTENT_OWN,
                TypeOfContract.NO_HOME_CONTENT_RENT,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                TypeOfContract.NO_TRAVEL,
                TypeOfContract.NO_TRAVEL_YOUTH,
                TypeOfContract.UNKNOWN__ -> {/* currently not supported */}
            }
            deductible.text = interpolateTextKey(
                resources.getString(R.string.OFFER_TERMS_DEDUCTIBLE),
                "DEDUCTIBLE" to resources.getString(R.string.DEDUCTIBLE)
            )
        }
        insurance.presaleInformationUrl?.let { piu ->
            presaleInformation.setHapticClickListener {
                tracker.presaleInformation()
                startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(piu)), null)
            }
        }

        insurance.policyUrl?.let { pu ->
            terms.setHapticClickListener {
                tracker.terms()
                startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(pu)), null)
            }
        }*/
    }
}
