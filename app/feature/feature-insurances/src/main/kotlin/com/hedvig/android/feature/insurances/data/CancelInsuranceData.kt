package com.hedvig.android.feature.insurances.data

import com.hedvig.android.data.contract.ContractGroup
import kotlinx.datetime.LocalDate

data class CancelInsuranceData(
  val contractId: String,
  val contractDisplayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
  val activateFrom: LocalDate,
)
