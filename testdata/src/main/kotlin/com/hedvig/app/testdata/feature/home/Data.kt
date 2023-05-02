package com.hedvig.app.testdata.feature.home

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.feature.home.builders.HomeDataBuilder
import giraffe.type.PayinMethodStatus
import java.time.LocalDate

val HOME_DATA_PENDING = HomeDataBuilder(listOf(ContractStatus.PENDING)).build()

val HOME_DATA_UPCOMING_RENEWAL = HomeDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE, ContractStatus.ACTIVE),
  renewalDate = LocalDate.now().plusDays(1L),
).build()

val HOME_DATA_ACTIVE_IN_FUTURE =
  HomeDataBuilder(listOf(ContractStatus.ACTIVE_IN_FUTURE)).build()

val HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE = HomeDataBuilder(
  listOf(
    ContractStatus.ACTIVE_IN_FUTURE,
    ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE,
  ),
).build()

val HOME_DATA_ACTIVE_IN_FUTURE_NO_INCEPTION = HomeDataBuilder(
  listOf(
    ContractStatus.ACTIVE_IN_FUTURE_INVALID,
  ),
).build()

val HOME_DATA_TERMINATED =
  HomeDataBuilder(listOf(ContractStatus.TERMINATED)).build()

val HOME_DATA_ACTIVE = HomeDataBuilder(listOf(ContractStatus.ACTIVE)).build()

val HOME_DATA_PAYIN_NEEDS_SETUP =
  HomeDataBuilder(listOf(ContractStatus.ACTIVE), payinMethodStatus = PayinMethodStatus.NEEDS_SETUP).build()

val HOME_DATA_TERMINATED_TODAY = HomeDataBuilder(listOf(ContractStatus.TERMINATED_TODAY)).build()
