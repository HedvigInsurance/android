package com.hedvig.app.testdata.feature.home

import com.hedvig.app.testdata.feature.home.builders.HomeDataBuilder

val HOME_DATA_PENDING = HomeDataBuilder(listOf(HomeDataBuilder.Status.PENDING)).build()

val HOME_DATA_ACTIVE_IN_FUTURE =
    HomeDataBuilder(listOf(HomeDataBuilder.Status.ACTIVE_IN_FUTURE)).build()

val HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE = HomeDataBuilder(
    listOf(
        HomeDataBuilder.Status.ACTIVE_IN_FUTURE,
        HomeDataBuilder.Status.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
    )
).build()

val HOME_DATA_ACTIVE_IN_FUTURE_NO_INCEPTION = HomeDataBuilder(
    listOf(
        HomeDataBuilder.Status.ACTIVE_IN_FUTURE_INVALID
    )
).build()

val HOME_DATA_TERMINATED =
    HomeDataBuilder(listOf(HomeDataBuilder.Status.TERMINATED)).build()

val HOME_DATA_PENDING_NO_FIRST_NAME =
    HomeDataBuilder(listOf(HomeDataBuilder.Status.PENDING), firstName = null).build()

val HOME_DATA_ACTIVE = HomeDataBuilder(listOf(HomeDataBuilder.Status.ACTIVE)).build()
