package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.util.apollo.stringRes

class YourInfoListItemBuilder {

    fun createYourInfoList(
        contract: InsuranceQuery.Contract,
        upcomingAgreementResult: YourInfoModel.PendingAddressChange?
    ): MutableList<YourInfoModel> {
        val list = mutableListOf<YourInfoModel>()
        contract.currentAgreement.asSwedishApartmentAgreement?.let {
            upcomingAgreementResult?.let(list::add)
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.saType.stringRes(),
                it.squareMeters
            ).let(list::add)
            YourInfoModel.ChangeAddressButton.let(list::add)
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asSwedishHouseAgreement?.let {
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                R.string.SWEDISH_HOUSE_LOB,
                it.squareMeters
            ).let(list::add)
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asNorwegianHomeContentAgreement?.let {
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.nhcType?.stringRes(),
                it.squareMeters
            ).let(list::add)
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asDanishHomeContentAgreement?.let {
            YourInfoModel.Home(
                it.address.fragments.addressFragment.street,
                it.address.fragments.addressFragment.postalCode,
                it.dhcType?.stringRes(),
                it.squareMeters
            ).let(list::add)
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asNorwegianTravelAgreement?.let {
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asDanishTravelAgreement?.let {
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }
        contract.currentAgreement.asDanishAccidentAgreement?.let {
            YourInfoModel.Coinsured(it.numberCoInsured).let(list::add)
            YourInfoModel.Change.let(list::add)
        }

        return list
    }
}
