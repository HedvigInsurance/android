package com.hedvig.app.feature.embark.passages.previousinsurer

sealed class PreviousInsurerItem {
    object Header : PreviousInsurerItem()

    data class Insurer(
        val name: String,
        val icon: String?,
        val id: String,
        val collectionId: String? = null,
    ) : PreviousInsurerItem()
}
