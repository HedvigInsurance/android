package com.hedvig.app.feature.insurance.ui.detail.yourinfo

sealed class YourInfoModel {
    data class Header(val text: String) : YourInfoModel()

    data class Row(
        val label: String,
        val content: String
    ) : YourInfoModel()

    data class Paragraph(val text: String) : YourInfoModel()

    object Button : YourInfoModel()
}
