package com.hedvig.app.feature.offer.binders

import android.view.LayoutInflater
import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.additional_buildings_row.view.*
import kotlinx.android.synthetic.main.offer_fact_area.view.*
import org.threeten.bp.LocalDate

class FactAreaBinder(
    private val root: LinearLayout
) {
    init {
        root.expandableContentView.initialize()
    }

    private var previousData: OfferQuery.AsCompleteQuote? = null

    fun bind(data: OfferQuery.AsCompleteQuote) = root.apply {
        if (data == previousData) {
            return@apply
        }

        when {
            data.quoteDetails.asSwedishHouseQuoteDetails != null -> bindHouse(data.quoteDetails.asSwedishHouseQuoteDetails)
            data.quoteDetails.asSwedishApartmentQuoteDetails != null -> bindApartment(data.quoteDetails.asSwedishApartmentQuoteDetails)
        }

        root.expandableContentView.contentSizeChanged()

        previousData = data
    }

    private fun bindApartment(data: OfferQuery.AsSwedishApartmentQuoteDetails) = root.apply {
        removeHouseViews()
        bindCommon(data.livingSpace, data.householdSize)
    }

    private fun bindHouse(data: OfferQuery.AsSwedishHouseQuoteDetails) = root.apply {
        ancillarySpaceLabel.show()
        ancillarySpace.show()
        ancillarySpace.text =
            resources.getString(R.string.HOUSE_INFO_BIYTA_SQUAREMETERS, data.ancillarySpace)

        yearOfConstructionLabel.show()
        yearOfConstruction.show()
        yearOfConstruction.text = data.yearOfConstruction.toString()

        bathroomsLabel.show()
        bathrooms.show()
        bathrooms.text = data.numberOfBathrooms.toString()

        subletedLabel.show()
        subleted.show()
        subleted.text = if (data.isSubleted) {
            resources.getString(R.string.HOUSE_INFO_SUBLETED_TRUE)
        } else {
            resources.getString(R.string.HOUSE_INFO_SUBLETED_FALSE)
        }

        data.extraBuildings.let { extraBuildings ->
            if (extraBuildings.isEmpty()) {
                removeExtraBuildingViews()
                return@let
            }
            bindExtraBuildings(extraBuildings)
        }
    }

    private fun bindExtraBuildings(extraBuildings: List<OfferQuery.ExtraBuilding>) = root.apply {
        additionalBuildingsTitle.show()
        additionalBuildingsSeparator.show()

        extraBuildings.forEach { eb ->
            val extraBuilding = eb.asExtraBuildingCore ?: return@forEach
            val row = LayoutInflater
                .from(additionalBuildingsContainer.context)
                .inflate(R.layout.additional_buildings_row, additionalBuildingsContainer, false)
            row.title.text = extraBuilding.displayName

            var bodyText =
                resources.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS, extraBuilding.area)
            if (extraBuilding.hasWaterConnected) {
                bodyText += ", " + resources.getString(R.string.HOUSE_INFO_CONNECTED_WATER)
            }
            row.body.text = bodyText
            additionalBuildingsContainer.addView(row)
        }

        additionalBuildingsContainer.show()
    }

    private fun removeExtraBuildingViews() = root.apply {
        additionalBuildingsTitle.remove()
        additionalBuildingsContainer.remove()
    }

    private fun removeHouseViews() = root.apply {
        ancillarySpaceLabel.remove()
        ancillarySpace.remove()

        yearOfConstructionLabel.remove()
        yearOfConstruction.remove()

        bathroomsLabel.remove()
        bathrooms.remove()

        subletedLabel.remove()
        subleted.remove()
    }

    private fun bindCommon(dataLivingSpace: Int, personsInHousehold: Int) = root.apply {
        livingSpace.text =
            resources.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS, dataLivingSpace)
        coinsured.text = personsInHousehold.toString()
        offerExpirationDate.text = resources.getString(
            R.string.OFFER_INFO_OFFER_EXPIRES,
            LocalDate.now().plusMonths(1).toString()
        )
    }
}
