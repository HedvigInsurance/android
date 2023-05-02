package com.hedvig.app.testdata.dashboard

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder

val INSURANCE_DATA =
  InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
  ).build()

val INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE, ContractStatus.TERMINATED),
).build()
val INSURANCE_DATA_TERMINATED = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.TERMINATED),
).build()

val INSURANCE_DATA_WITH_CROSS_SELL = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
).build()

val INSURANCE_DATA_UPCOMING_AGREEMENT = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
  showUpcomingAgreement = true,
).build()
