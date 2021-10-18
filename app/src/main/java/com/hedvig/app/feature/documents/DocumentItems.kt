package com.hedvig.app.feature.documents

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringRes
import com.hedvig.android.owldroid.fragment.InsuranceTermFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import kotlinx.parcelize.Parcelize

sealed class DocumentItems {

    data class Header(@StringRes val stringRes: Int) : DocumentItems()

    @Parcelize
    data class Document(
        private val title: String? = null,
        @StringRes private val titleRes: Int? = null,
        private val subtitle: String? = null,
        @StringRes private val subTitleRes: Int? = null,
        val uri: Uri,
        val type: Type = Type.GENERAL_TERMS,
    ) : DocumentItems(), Parcelable {
        enum class Type {
            TERMS_AND_CONDITIONS,
            PRE_SALE_INFO_EU_STANDARD,
            GENERAL_TERMS,
            PRIVACY_POLICY
        }

        fun getTitle(context: Context) = title ?: titleRes?.let(context::getString)
        fun getSubTitle(context: Context) = subtitle ?: subTitleRes?.let(context::getString)

        companion object {
            fun from(insuranceTerm: InsuranceTermFragment) = Document(
                title = insuranceTerm.displayName,
                subtitle = null,
                uri = Uri.parse(insuranceTerm.url),
                type = Type.GENERAL_TERMS
            )
        }
    }
}
