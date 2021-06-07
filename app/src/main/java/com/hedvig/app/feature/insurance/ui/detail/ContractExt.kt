package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.util.FeatureFlag
import com.hedvig.app.util.apollo.stringRes

fun InsuranceQuery.Contract.toModelItems(): List<YourInfoModel> = when {
    currentAgreement.asSwedishApartmentAgreement != null -> currentAgreement.asSwedishApartmentAgreement!!.let {
        listOfNotNull(
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.saType.stringRes(),
                it.squareMeters
            ),
            if (FeatureFlag.MOVING_FLOW.enabled) YourInfoModel.ChangeAddressButton else null,
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asSwedishHouseAgreement != null -> currentAgreement.asSwedishHouseAgreement!!.let {
        listOfNotNull(
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                R.string.SWEDISH_HOUSE_LOB,
                it.squareMeters
            ),
            if (FeatureFlag.MOVING_FLOW.enabled) YourInfoModel.ChangeAddressButton else null,
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asNorwegianHomeContentAgreement != null -> currentAgreement.asNorwegianHomeContentAgreement!!.let {
        listOf(
            YourInfoModel.Home(
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
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.dhcType?.stringRes(),
                it.squareMeters
            ),
            YourInfoModel.Coinsured(it.numberCoInsured)
        )
    }
    currentAgreement.asNorwegianTravelAgreement != null -> listOf(YourInfoModel.Coinsured(currentAgreement.asNorwegianTravelAgreement!!.numberCoInsured))
    currentAgreement.asDanishTravelAgreement != null -> listOf(YourInfoModel.Coinsured(currentAgreement.asDanishTravelAgreement!!.numberCoInsured))
    currentAgreement.asDanishAccidentAgreement != null -> listOf(YourInfoModel.Coinsured(currentAgreement.asDanishAccidentAgreement!!.numberCoInsured))
    else -> throw IllegalArgumentException("No agreement matched when creating contract items for $currentAgreement")
}
