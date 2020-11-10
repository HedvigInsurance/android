package com.hedvig.app.feature.insurance.ui.detail.coverage

sealed class PerilModel {
    data class Icon(val link: String) : PerilModel()
    data class Title(val text: String) : PerilModel()
    data class Description(val text: String) : PerilModel()
    sealed class PerilList : PerilModel() {
        data class Covered(val text: String) : PerilList()
        data class Exception(val text: String) : PerilList()
    }

    sealed class Header : PerilModel() {
        object CoveredHeader : Header()
        object ExceptionHeader : Header()
        object InfoHeader : Header()
    }

    data class Paragraph(val text: String) : PerilModel()
}
