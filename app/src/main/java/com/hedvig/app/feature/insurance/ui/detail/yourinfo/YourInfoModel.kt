package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement

sealed class YourInfoModel {
    data class Home(
        val street: String,
        val postalCode: String,
        val type: Int?,
        val size: Int
    ) : YourInfoModel()

    data class Coinsured(val amount: Int) : YourInfoModel()

    object Change : YourInfoModel()

    object ChangeAddressButton : YourInfoModel()

    data class PendingAddressChange(val upcomingAgreement: UpcomingAgreement?) : YourInfoModel()
}
