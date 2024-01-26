package com.hedvig.android.feature.insurances.data

import com.hedvig.android.data.contract.ContractGroup

data class CancelInsuranceData(
  val contractId: String,
  val contractDisplayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
)
