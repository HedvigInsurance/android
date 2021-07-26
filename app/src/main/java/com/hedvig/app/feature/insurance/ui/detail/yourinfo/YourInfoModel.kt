package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase

sealed class YourInfoModel {
    sealed class Home : YourInfoModel() {
        data class Apartment(
            val street: String,
            val postalCode: String,
            val type: Int?,
            val size: Int
        ) : Home()

        data class House(
            val street: String,
            val postalCode: String,
            val type: Int?,
            val size: Int,
            val ancillaryArea: Int,
            val yearOfConstruction: Int,
            val numberOfBathrooms: Int,
            val isPartlySubleted: Boolean,
            val extraBuildings: List<Triple<String, Int, Boolean>>
        ) : Home()
    }

    data class Coinsured(val amount: Int) : YourInfoModel()

    object Change : YourInfoModel()

    object ChangeAddressButton : YourInfoModel()

    data class PendingAddressChange(
        val upcomingAgreement: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
    ) : YourInfoModel()
}
