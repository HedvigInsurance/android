package com.hedvig.app.feature.embark.passages.previousinsurer

sealed class PreviousInsurerItem {
    data class Header(
        val text: String
    ) : PreviousInsurerItem()

    data class Insurer(
        val name: String,
        val icon: String
    ) : PreviousInsurerItem()
}
