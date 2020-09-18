package com.hedvig.app.testdata.feature.home

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.feature.home.builders.HomeDataBuilder
import com.hedvig.app.testdata.feature.home.builders.ImportantMessageBuilder

val HOME_DATA_PENDING = HomeDataBuilder(listOf(ContractStatus.PENDING)).build()

val HOME_DATA_ACTIVE_IN_FUTURE =
    HomeDataBuilder(listOf(ContractStatus.ACTIVE_IN_FUTURE)).build()

val HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE = HomeDataBuilder(
    listOf(
        ContractStatus.ACTIVE_IN_FUTURE,
        ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
    )
).build()

val HOME_DATA_ACTIVE_IN_FUTURE_NO_INCEPTION = HomeDataBuilder(
    listOf(
        ContractStatus.ACTIVE_IN_FUTURE_INVALID
    )
).build()

val HOME_DATA_TERMINATED =
    HomeDataBuilder(listOf(ContractStatus.TERMINATED)).build()

val HOME_DATA_PENDING_NO_FIRST_NAME =
    HomeDataBuilder(listOf(ContractStatus.PENDING), firstName = null).build()

val HOME_DATA_ACTIVE = HomeDataBuilder(listOf(ContractStatus.ACTIVE)).build()

val HOME_DATA_TERMINATED_TODAY = HomeDataBuilder(listOf(ContractStatus.TERMINATED_TODAY)).build()

val HOME_DATA_ACTIVE_WITH_PSA = HomeDataBuilder(
    listOf(ContractStatus.ACTIVE), importantMessages = listOf(
        ImportantMessageBuilder().build()
    )
).build()

val HOME_DATA_ACTIVE_WITH_MULTIPLE_PSA = HomeDataBuilder(
    listOf(ContractStatus.ACTIVE), importantMessages = listOf(
        ImportantMessageBuilder().build(),
        ImportantMessageBuilder().build()
    )
).build()
