package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase

sealed class YourInfoModel {
    object Change : YourInfoModel()

    object ChangeAddressButton : YourInfoModel()

    data class PendingAddressChange(
        val upcomingAgreement: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
    ) : YourInfoModel()
}
