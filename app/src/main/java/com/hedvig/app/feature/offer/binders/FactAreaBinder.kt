package com.hedvig.app.feature.offer.binders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isHouse
import kotlinx.android.synthetic.main.offer_fact_area.view.*
import org.threeten.bp.LocalDate

class FactAreaBinder(
    private val root: LinearLayout
) {
    init {
        root.expandableContentView.initialize()
    }

    private var previousData: OfferQuery.Insurance? = null

    fun bind(data: OfferQuery.Insurance) = root.apply {
        if (data == previousData) {
            return@apply
        }
        address.text = data.address

        if (data.type?.isHouse == true) {
            ancillarySpaceLabel.show()
            ancillarySpace.show()
            ancillarySpace.text = interpolateTextKey(
                resources.getString(R.string.HOUSE_INFO_BIYTA_SQUAREMETERS),
                "HOUSE_INFO_AMOUNT_BIYTA" to data.ancillaryArea
            )

            yearOfConstructionLabel.show()
            yearOfConstruction.show()
            yearOfConstruction.text = data.yearOfConstruction.toString()

            bathroomsLabel.show()
            bathrooms.show()
            bathrooms.text = data.numberOfBathrooms.toString()

            subletedLabel.show()
            subleted.show()
            subleted.text = if (data.isSubleted == true) {
                resources.getString(R.string.HOUSE_INFO_SUBLETED_TRUE)
            } else {
                resources.getString(R.string.HOUSE_INFO_SUBLETED_FALSE)
            }
        } else {
            ancillarySpaceLabel.remove()
            ancillarySpace.remove()

            yearOfConstructionLabel.remove()
            yearOfConstruction.remove()

            bathroomsLabel.remove()
            bathrooms.remove()

            subletedLabel.remove()
            subleted.remove()
        }

        livingSpace.text = interpolateTextKey(
            resources.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS),
            "HOUSE_INFO_AMOUNT_BOYTA" to data.livingSpace
        )
        coinsured.text = data.personsInHousehold.toString()
        offerExpirationDate.text = interpolateTextKey(
            resources.getString(R.string.OFFER_INFO_OFFER_EXPIRES),
            "OFFER_EXPIERY_DATE" to LocalDate.now().plusMonths(1)
        )

        root.expandableContentView.contentSizeChanged()

        previousData = data
    }
}
