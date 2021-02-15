package com.hedvig.onboarding.embark.passages.previousinsurer

sealed class PreviousInsurerItem {
    object Header : PreviousInsurerItem()

    data class Insurer(
        val name: String,
        val icon: String
    ) : PreviousInsurerItem()
}
