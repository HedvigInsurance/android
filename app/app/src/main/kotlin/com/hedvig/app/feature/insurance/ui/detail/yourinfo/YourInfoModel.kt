package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.android.feature.home.legacychangeaddress.GetUpcomingAgreementUseCase

sealed class YourInfoModel {
  object Change : YourInfoModel()

  object ChangeAddressButton : YourInfoModel()

  data class PendingAddressChange(
    val upcomingAgreement: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement,
  ) : YourInfoModel()

  data class CancelInsuranceButton(
    val insuranceId: String,
    val insuranceDisplayName: String,
  ) : YourInfoModel()
}
