package com.hedvig.app.feature.insurance.ui.detail.yourinfo

sealed class YourInfoModel {
    sealed class Header : YourInfoModel() {
        object Details : Header()
        object Coinsured : Header()
        object Change : Header()
    }

    data class Row(
        val label: String,
        val content: String
    ) : YourInfoModel()

    data class Paragraph(val text: String) : YourInfoModel()

    object ChangeParagraph: YourInfoModel()

    object OpenChatButton : YourInfoModel()
}
