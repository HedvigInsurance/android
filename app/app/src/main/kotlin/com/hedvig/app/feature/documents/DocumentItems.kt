package com.hedvig.app.feature.documents

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import giraffe.fragment.InsuranceTermFragment
import kotlinx.parcelize.Parcelize
import octopus.CrossSalesQuery

sealed class DocumentItems {

  data class Header(@StringRes val stringRes: Int) : DocumentItems()

  @Parcelize
  data class Document(
    private val title: String? = null,
    @StringRes private val titleRes: Int? = null,
    private val subtitle: String? = null,
    @StringRes private val subTitleRes: Int? = null,
    val uriString: String,
  ) : DocumentItems(), Parcelable {

    fun getTitle(context: Context): String? = title ?: titleRes?.let(context::getString)
    fun getSubTitle(context: Context): String? = subtitle ?: subTitleRes?.let(context::getString)

    companion object {
      fun from(insuranceTerm: InsuranceTermFragment) = Document(
        title = insuranceTerm.displayName,
        subtitle = null,
        uriString = insuranceTerm.url,
      )

      fun from(insuranceTerm: CrossSalesQuery.Data.CurrentMember.CrossSell.ProductVariant.Document) = Document(
        title = insuranceTerm.displayName,
        subtitle = null,
        uriString = insuranceTerm.url,
      )
    }
  }

  data class CancelInsuranceButton(
    val insuranceId: String,
    val insuranceDisplayName: String,
  ) : DocumentItems()
}
