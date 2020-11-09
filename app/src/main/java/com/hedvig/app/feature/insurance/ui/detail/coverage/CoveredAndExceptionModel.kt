package com.hedvig.app.feature.insurance.ui.detail.coverage

sealed class CoveredAndExceptionModel {
    data class Icon(val link: String) : CoveredAndExceptionModel()
    data class Title(val text: String) : CoveredAndExceptionModel()
    data class Description(val text: String) : CoveredAndExceptionModel()
    sealed class CommonDenominator : CoveredAndExceptionModel() {
        data class Covered(val text: String) : CommonDenominator()
        data class Exception(val text: String) : CommonDenominator()
    }

    sealed class Header : CoveredAndExceptionModel() {
        object CoveredHeader : Header()
        object ExceptionHeader : Header()
        object InfoHeader : Header()
    }

    data class Paragraph(val text: String) : CoveredAndExceptionModel()
}
