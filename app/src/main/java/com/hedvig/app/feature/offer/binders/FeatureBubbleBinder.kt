package com.hedvig.app.feature.offer.binders

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ChangeDateBottomSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isApartmentOwner
import kotlinx.android.synthetic.main.feature_bubbles.view.*
import org.threeten.bp.LocalDate

class FeatureBubbleBinder(
    private val root: ConstraintLayout
) {
    fun bind(data: OfferQuery.Data, fragmentManager: FragmentManager) = root.apply {
        amountInsuredBubbleText.text = interpolateTextKey(
            resources.getString(R.string.OFFER_BUBBLES_INSURED_SUBTITLE),
            "personsInHousehold" to data.insurance.personsInHousehold
        )

        if (data.insurance.previousInsurer != null) {
            startDateBubbleText.text =
                resources.getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_SWITCHER)
        } else {
            startDateBubbleText.text =
                resources.getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_NEW)
        }

        data.insurance.type?.let { t ->
            brfOrTravel.text = if (t.isApartmentOwner) {
                resources.getString(R.string.OFFER_BUBBLES_OWNED_ADDON_TITLE)
            } else {
                resources.getString(R.string.OFFER_BUBBLES_TRAVEL_PROTECTION_TITLE)
            }
        }

        dateButton.setHapticClickListener {
            ChangeDateBottomSheet.newInstance()
                .show(fragmentManager, ChangeDateBottomSheet.TAG)

        }

        bindChooseStartDateButton((data.lastQuoteOfMember as OfferQuery.AsCompleteQuote).startDate)
    }

    private fun bindChooseStartDateButton(startDate: LocalDate?) = root.apply {
        root.dateButton.text = buildSpannedString {
            append(resources.getString(R.string.OFFER_START_DATE))
            append(" ")
            bold { append(startDate.toString()) }
        }
    }
}
