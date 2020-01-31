package com.hedvig.app.feature.offer.binders

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isApartmentOwner
import kotlinx.android.synthetic.main.feature_bubbles.view.*
import org.threeten.bp.LocalDate

class FeatureBubbleBinder(
    private val root: ConstraintLayout,
    private val tracker: OfferTracker,
    fragmentManager: FragmentManager
) {

    init {
        root.dateButton.setHapticClickListener {
            tracker.chooseStartDate()
            ChangeDateBottomSheet.newInstance()
                .show(fragmentManager, ChangeDateBottomSheet.TAG)
        }
    }

    fun bind(data: OfferQuery.Data) = root.apply {
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
        bindChooseStartDateButton(data)
    }

    private fun bindChooseStartDateButton(data: OfferQuery.Data) = root.apply {
        val startDate = (data.lastQuoteOfMember as? OfferQuery.AsCompleteQuote)?.startDate
        val previousInsurer = data.insurance.previousInsurer
        root.dateButton.text = buildSpannedString {
            append(resources.getString(R.string.START_DATE_BTN))
            append(" ")

            if (startDate == null || startDate == LocalDate.now()) {
                bold { append(resources.getString(R.string.START_DATE_TODAY)) }
            } else {
                val month = startDate.month.toString().substring(0, 3).toLowerCase()
                bold { append("${startDate.dayOfMonth} $month ${startDate.year}") }
            }

            if (previousInsurer != null && startDate == null) {
                clear()
                append(resources.getString(R.string.START_DATE_BTN))
                append(" ")
                bold { append(resources.getString(R.string.START_DATE_EXPIRES)) }
            }
        }
    }
}
