package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.util.apollo.stringRes

fun InsuranceQuery.Contract.toModelItems(
    includeMovingFlowItems: Boolean
): List<YourInfoModel> = when {
    currentAgreement.asSwedishApartmentAgreement != null -> currentAgreement.asSwedishApartmentAgreement!!.let {
        listOfNotNull(
            YourInfoModel.Home.Apartment(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.saType.stringRes(),
                it.squareMeters
            ),
            if (includeMovingFlowItems) {
                YourInfoModel.ChangeAddressButton
            } else {
                null
            },
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asSwedishHouseAgreement != null -> currentAgreement.asSwedishHouseAgreement!!.let {
        listOfNotNull(
            YourInfoModel.Home.House(
                street = it.address.fragments.addressFragment.street,
                postalCode = it.address.fragments.addressFragment.postalCode,
                type = R.string.SWEDISH_HOUSE_LOB,
                size = it.squareMeters,
                ancillaryArea = it.ancillaryArea,
                yearOfConstruction = it.yearOfConstruction,
                numberOfBathrooms = it.numberOfBathrooms,
                isPartlySubleted = it.isSubleted,
                extraBuildings = it.extraBuildings.mapNotNull { eb ->
                    eb?.asExtraBuildingCore?.let { ebc ->
                        Triple(ebc.displayName, ebc.area, ebc.hasWaterConnected)
                    }
                }
            ),
            if (includeMovingFlowItems) {
                YourInfoModel.ChangeAddressButton
            } else {
                null
            },
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asNorwegianHomeContentAgreement != null -> currentAgreement.asNorwegianHomeContentAgreement!!.let {
        listOf(
            YourInfoModel.Home.Apartment(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.nhcType?.stringRes(),
                it.squareMeters
            ),
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asDanishHomeContentAgreement != null -> currentAgreement.asDanishHomeContentAgreement!!.let {
        listOf(
            YourInfoModel.Home.Apartment(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.dhcType?.stringRes(),
                it.squareMeters
            ),
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asNorwegianTravelAgreement != null -> listOf(
        YourInfoModel.Coinsured(currentAgreement.asNorwegianTravelAgreement!!.numberCoInsured)
    )
    currentAgreement.asDanishTravelAgreement != null -> listOf(
        YourInfoModel.Coinsured(currentAgreement.asDanishTravelAgreement!!.numberCoInsured)
    )
    currentAgreement.asDanishAccidentAgreement != null -> listOf(
        YourInfoModel.Coinsured(currentAgreement.asDanishAccidentAgreement!!.numberCoInsured)
    )
    else -> throw IllegalArgumentException("No agreement matched when creating contract items for $currentAgreement")
}
