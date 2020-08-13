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
    }

    sealed class BodyText : HomeModel() {
        object Pending : BodyText()
        object ActiveInFuture : BodyText()
    }
}
