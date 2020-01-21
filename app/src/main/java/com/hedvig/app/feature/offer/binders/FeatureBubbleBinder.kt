package com.hedvig.app.feature.offer.binders

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isApartmentOwner
import kotlinx.android.synthetic.main.feature_bubbles.view.*

class FeatureBubbleBinder(
    private val root: ConstraintLayout
) {
    fun bind(data: OfferQuery.Insurance) = root.apply {
        amountInsuredBubbleText.text = interpolateTextKey(
            resources.getString(R.string.OFFER_BUBBLES_INSURED_SUBTITLE),
            "personsInHousehold" to data.personsInHousehold
        )

        if (data.previousInsurer != null) {
            startDateBubbleText.text =
                resources.getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_SWITCHER)
        } else {
            startDateBubbleText.text =
                resources.getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_NEW)
        }

        data.type?.let { t ->
            brfOrTravel.text = if (t.isApartmentOwner) {
                resources.getString(R.string.OFFER_BUBBLES_OWNED_ADDON_TITLE)
            } else {
                resources.getString(R.string.OFFER_BUBBLES_TRAVEL_PROTECTION_TITLE)
            }
        }
        bindChooseStartDateButton()
    }

    private fun bindChooseStartDateButton() = root.apply {
        root.dateButton.text = buildSpannedString {
            append(resources.getString(R.string.OFFER_START_DATE))
            append(" ")
            bold { append(resources.getString(R.string.OFFER_START_DATE_TODAY)) }
        }
    }
}
