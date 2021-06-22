package com.hedvig.app.feature.insurablelimits

sealed class InsurableLimitItem {
    sealed class Header : InsurableLimitItem() {
        object Details : Header()
        object MoreInfo : Header()
    }

    data class InsurableLimit(
        val label: String,
        val limit: String,
        val description: String,
    ) : InsurableLimitItem()
}
