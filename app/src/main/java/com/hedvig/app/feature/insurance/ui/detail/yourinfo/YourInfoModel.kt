package com.hedvig.app.feature.insurance.ui.detail.yourinfo

sealed class YourInfoModel {
    sealed class Header : YourInfoModel() {
        object Details : Header()
        object Coinsured : Header()
    }

    data class Row(
        val label: String,
        val content: String
    ) : YourInfoModel()
}
