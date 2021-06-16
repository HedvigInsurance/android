package com.hedvig.app.feature.documents

import androidx.annotation.StringRes

sealed class DocumentItems {

    data class Header(@StringRes val stringRes: Int) : DocumentItems()

    data class Document(
        val title: String,
        val subtitle: String?,
        val url: String,
        val type: Type
    ) : DocumentItems() {
        enum class Type {
            TERMS_AND_CONDITIONS,
            PRE_SALE_INFO_EU_STANDARD,
            GENERAL_TERMS,
            PRIVACY_POLICY
        }
    }
}
