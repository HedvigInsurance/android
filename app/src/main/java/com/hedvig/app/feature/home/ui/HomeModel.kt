package com.hedvig.app.feature.home.ui

import java.time.LocalDate

sealed class HomeModel {
    sealed class BigText : HomeModel() {
        data class Pending(
            val name: String
        ) : BigText()

        data class ActiveInFuture(
            val name: String,
            val inception: LocalDate
        ) : BigText()

        data class Terminated(
            val name: String
        ) : BigText()
    }

    object StartClaimOutlined : HomeModel()
}
