package com.hedvig.app.feature.home.ui

sealed class HomeModel {
    sealed class BigText : HomeModel() {
        data class Pending(
            val name: String
        ) : BigText()
    }
}
