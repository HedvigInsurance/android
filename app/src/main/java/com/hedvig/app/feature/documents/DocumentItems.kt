package com.hedvig.app.feature.documents

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes

sealed class DocumentItems {

    data class Header(@StringRes val stringRes: Int) : DocumentItems()

    data class Document(
        private val title: String? = null,
        @StringRes private val titleRes: Int? = null,
        private val subtitle: String? = null,
        @StringRes private val subTitleRes: Int? = null,
        val uri: Uri,
        val type: Type = Type.GENERAL_TERMS
    ) : DocumentItems() {
        enum class Type {
            TERMS_AND_CONDITIONS,
            PRE_SALE_INFO_EU_STANDARD,
            GENERAL_TERMS,
            PRIVACY_POLICY
        }

        fun getTitle(context: Context) = title ?: titleRes?.let(context::getString)
        fun getSubTitle(context: Context) = title ?: titleRes?.let(context::getString)
    }
}
