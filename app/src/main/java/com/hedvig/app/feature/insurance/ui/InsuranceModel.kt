package com.hedvig.app.feature.insurance.ui

import com.hedvig.app.feature.crossselling.ui.CrossSellData

sealed class InsuranceModel {
  object Header : InsuranceModel()

  data class Contract(
    val contractCardViewState: ContractCardViewState,
  ) : InsuranceModel()

  data class CrossSellHeader(
    val showNotificationBadge: Boolean = false,
  ) : InsuranceModel()

  data class CrossSellCard(
    val inner: CrossSellData,
  ) : InsuranceModel()

  data class Error(val message: String?) : InsuranceModel()

  object TerminatedContractsHeader : InsuranceModel()
  data class TerminatedContracts(val quantity: Int) : InsuranceModel()
}
